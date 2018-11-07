//
// Created by hawk on 2017/9/14.
//
#include <iostream>
#include <cstdio>

#include <muduo/base/Logging.h>

#include <rapidjson/document.h>
#include <rapidjson/filereadstream.h>

#include "ops_fetcher.h"

namespace Controller {
    using namespace std;
    using namespace rapidjson;

    const string ops_fetcher::uri_host_base_ = "https://ops.sankuai.com/api/stree/tag/host?fields=ip_lan,idc";
    const string ops_fetcher::uri_business_base_ = "https://ops.sankuai.com/api/v0.2/";
    const string ops_fetcher::ops_auth_ = "Authorization: Bearer 6e0f033b45a278d2a6cad32940de88c9b4bd5725";
    const string ops_fetcher::core_srv_name_ = "核心服务";
    const char* ops_fetcher::cluster_path_ = "/opt/meituan/apps/cplugin_server/cluster.json";

    ops_fetcher::ops_fetcher() : p_curl_(NULL), headers_(NULL){
    }

    bool ops_fetcher::initCurl() {
        p_curl_ = curl_easy_init();
        headers_ = curl_slist_append(headers_, ops_auth_.c_str());
        //set auth token in http header
        curl_easy_setopt(p_curl_, CURLOPT_HTTPHEADER, headers_);
        return NULL != p_curl_;
    }

    int8_t ops_fetcher::getPdl(const string &name, vector<string> &ret) {
        string uri = uri_business_base_ + name;
        LOG_DEBUG << "get departure info by uri: " << uri;

        if (httpRequest(uri) || jsonParsePdl(name, ret)) {
            return -1;
        }
        return 0;
    }

    int8_t ops_fetcher::getHostRank(const string &hostname, srv_rank &ret) {
        string uri = uri_business_base_ + "hosts/" + hostname + "/srvs";
        if (httpRequestRank(uri) || jsonParseRank("srvs", ret)) {
            return -1;
        }
        return 0;
    }

    int8_t ops_fetcher::getIpList(const string &corp,
                                  const string &owt,
                                  const string &pdl,
                                  const string &cluster,
                                  map<string, vector<ip_host> >&ret) {
        string uri = uri_host_base_ + "&corp=" + corp + "&owt="+ owt + (pdl.empty() ? "" : "&pdl="+pdl)
                     + (cluster.empty() ? "" : "&cluster="+cluster);
        if (httpRequest(uri) || jsonParseIP("data", ret)) {
            return -1;
        }
        return 0;
    }

    ops_fetcher::~ops_fetcher() {
        curl_slist_free_all(headers_);
        curl_easy_cleanup(p_curl_);
    }

    int8_t ops_fetcher::httpRequest(const string& uri) {
        curl_easy_setopt(p_curl_, CURLOPT_URL, uri.c_str());

        //clear http buf.
        if (!http_buf_.empty()) {
            string().swap(http_buf_);
        }

        curl_easy_setopt(p_curl_, CURLOPT_WRITEFUNCTION, Controller::write_data);
        curl_easy_setopt(p_curl_, CURLOPT_WRITEDATA, &http_buf_);

        CURLcode res = curl_easy_perform(p_curl_);
        if (CURLE_OK != res) {
            LOG_ERROR << "curl_easy_perform() failed: " << curl_easy_strerror(res);
            return -1;
        }
        return 0;
    }

    int8_t ops_fetcher::httpRequestRank(const string& uri) {
        curl_easy_setopt(p_curl_, CURLOPT_URL, uri.c_str());

        //clear http buf.
        if (!http_rank_buf_.empty()) {
            string().swap(http_rank_buf_);
        }

        curl_easy_setopt(p_curl_, CURLOPT_WRITEFUNCTION, Controller::write_data);
        curl_easy_setopt(p_curl_, CURLOPT_WRITEDATA, &http_rank_buf_);

        CURLcode res = curl_easy_perform(p_curl_);
        if (CURLE_OK != res) {
            LOG_ERROR << "curl_easy_perform() failed: " << curl_easy_strerror(res);
            return -1;
        }
        return 0;
    }

    int8_t ops_fetcher::jsonParsePdl(const string &name, vector<string> &result) {
        Document d;
        d.Parse(http_buf_.c_str());
        if (!d.IsObject()) {
            LOG_ERROR << " can not parse a valid json object";
            return -1;
        }
        const char *name_p = name.c_str();
        if (d.MemberEnd() == d.FindMember(name_p)) {
            LOG_ERROR << name << " is not a valid param in json string";
            return -1;
        }

        Value &doc = d[name_p];
        if (!doc.IsArray() || doc.Empty()) {
            LOG_DEBUG <<"key " << name << " is not contain a array.";
            return -1;
        }

        for (size_t i = 0; i < doc.Size(); ++i) {
            Value &v = doc[i];
            if (v.HasMember("key")) {
                result.push_back(v["key"].GetString());
            }
        }
        return 0;
    }

    int8_t ops_fetcher::jsonParseIP(const string &name, map<string, vector<ip_host> > &result) {
        Document d;
        d.Parse(http_buf_.c_str());
        if (!d.IsObject()) {
            LOG_ERROR << " can not parse a valid json object";
            return -1;
        }

        const char *name_p = name.c_str();
        if (d.MemberEnd() == d.FindMember(name_p)) {
            LOG_ERROR << name << " is not a valid param in json string";
            return -1;
        }

        Value &doc = d[name_p];
        if (!doc.IsArray() || doc.Empty()) {
            LOG_DEBUG <<"key " << name << " is not contain a array.";
            return -1;
        }
        srv_rank tmp_rank = NON_CORE_SRV;
        for (size_t i = 0; i < doc.Size(); ++i) {
            Value &v = doc[i];
            if (v.HasMember("idc") && v.HasMember("ip_lan") && v.HasMember("name")) {
                if (0 == getHostRank(v["name"].GetString(), tmp_rank)) {
                    result[v["idc"].GetString()].push_back(ip_host(v["ip_lan"].GetString(), v["name"].GetString(), tmp_rank));
                } else {
                    result[v["idc"].GetString()].push_back(ip_host(v["ip_lan"].GetString(), v["name"].GetString()));
                }
            }
        }
        return 0;
    }

    int8_t ops_fetcher::jsonParseRank(const string &name, srv_rank &result) {
        Document d;
        d.Parse(http_rank_buf_.c_str());
        if (!d.IsObject()) {
            LOG_ERROR << " can not parse a valid json object";
            return -1;
        }

        const char *name_p = name.c_str();
        if (d.MemberEnd() == d.FindMember(name_p)) {
            LOG_ERROR << name << " is not a valid param in json string";
            return -1;
        }

        Value &doc = d[name_p];
        if (!doc.IsArray() || doc.Empty()) {
            LOG_DEBUG <<"key " << name << " is not contain a array.";
            return -1;
        }

        result = NON_CORE_SRV;
        if (doc[0].HasMember("rank") && core_srv_name_ == doc[0]["rank"].GetString()) {
            result = CORE_SRV;
        }
        return 0;
    }
    //通过本地的json配置文件载入环境变量；未来通过mcc进行获取。
    int8_t ops_fetcher::getCluster(vector<string> &env_list, const char* key) {
        FILE *fp = fopen(cluster_path_, "r");
        if (NULL == fp) {
            LOG_ERROR << "Open env config file failed";
            return -1;
        }

        char read_buffer[256];
        FileReadStream is(fp, read_buffer, sizeof(read_buffer));

        Document d;
        d.ParseStream(is);
        Value &doc = d[key];
        if (!doc.IsArray()) {
            LOG_DEBUG << "can not get env array in this PC";
	    fclose(fp);
            return -1;
        }

        for (Value::ConstValueIterator itr = doc.Begin(); itr != doc.End(); ++itr) {
            env_list.push_back(itr->GetString());
        }

        fclose(fp);
        return 0;
    }
}
