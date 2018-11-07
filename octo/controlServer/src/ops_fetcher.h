//
// Created by Xiang Zhang on 2017/9/14.
//

#ifndef CONTROLSERVER_OPS_FETCHER_H
#define CONTROLSERVER_OPS_FETCHER_H

#include <string>
#include <vector>
#include <map>

#include <curl/curl.h>

#include "common.h"

namespace Controller {
class ops_fetcher {
public:
    ops_fetcher();

    bool initCurl();

    int8_t getIpList(const std::string& corp,
                     const std::string& owt,
                     const std::string& pdl,
                     const std::string& cluster,
                     std::map<std::string, std::vector<ip_host> > &ret);

    int8_t getPdl(const std::string& name, std::vector<std::string> &ret);
    int8_t getHostRank(const std::string& hostname, srv_rank &ret);

    int8_t getCluster(std::vector<std::string> &env_list, const char* key);

    //for test
    std::string &getHttpbuf() {
        return http_buf_;
    }


    ~ops_fetcher();
private:
    CURL *p_curl_;
    struct curl_slist *headers_;

    std::string http_buf_;
    std::string http_rank_buf_;

    static const std::string uri_host_base_;
    static const std::string uri_business_base_;
    static const std::string ops_auth_;
    static const std::string core_srv_name_;
    static const char* cluster_path_;

private:
    int8_t httpRequest(const std::string& uri);
    int8_t httpRequestRank(const std::string& uri);

    int8_t jsonParsePdl(const std::string& name,
                        std::vector<std::string>& result);

    int8_t jsonParseIP(const std::string &name,
                       std::map<std::string, std::vector<ip_host> > &result);

    int8_t jsonParseRank(const std::string &name, srv_rank &result);
};
}


#endif //CONTROLSERVER_OPS_FETCHER_H
