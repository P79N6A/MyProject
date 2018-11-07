// =====================================================================================
//
//       Filename:  ConfigHelper.h
//
//    Description:
//
//        Version:  1.0
//        Created:  2015-07-09
//       Revision:  none
//
//
// =====================================================================================
#include <string>
#include <boost/property_tree/ptree.hpp>
#include <boost/property_tree/json_parser.hpp>
#include <boost/foreach.hpp>

#include "SGAgent.h"
#include "SGAgentErr.h"
#include "msgparam.h"
#include "json/json.h"
#include "log4cplus.h"

using namespace boost::property_tree;

#ifndef __CONFIG_HELPER_H
#define __CONFIG_HELPER_H
static proc_conf_param_t genSetParamObj(std::string appkey,
            std::string env, std::string path,
            std::string data)
{
    proc_conf_param_t c_conf;
    c_conf.__set_appkey(appkey);
    c_conf.__set_env(env);
    c_conf.__set_path(path);
    c_conf.__set_conf(data);

    c_conf.__set_cmd(MQ_SET_CONF);
    return c_conf;
}

static proc_conf_param_t genGetParamObj(std::string appkey,
            std::string env, std::string path)
{
    proc_conf_param_t c_node;

    c_node.__set_appkey(appkey);
    c_node.__set_env(env);
    c_node.__set_path(path);

    c_node.__set_cmd(MQ_GET_CONF);

    return c_node;
}

static int _json2map(const Json::Value item, boost::shared_ptr<std::map<std::string, std::string> > kv)
{
    // 遍历数组
    if (0 >= item.size()) {
        LOG_WARN("json item is empty");
        return -1;
    }

    Json::Value::Members mem = item.getMemberNames();
    for (Json::Value::Members::iterator iter = mem.begin(); iter != mem.end();
            ++iter)
    {
        std::string ky = *iter;
        std::string val = item[*iter].asString();
        LOG_DEBUG("update key = << " << ky << ", value = " << val);

        std::map<std::string, std::string>::iterator iter;
        iter = kv -> find(ky);
        if (kv -> end() !=iter)
        {
            //如果map中已经存在，则只替换val
            iter -> second = val;
        }
        else
        {
            //如果map中不存在，则insert pair
            kv -> insert(std::pair<std::string, std::string>(ky, val));
        }

        iter = kv -> find(ky);
        if (kv -> end() !=iter)
        {
            // 防止多线程不安全， iter置空的情况
            val = kv -> at(ky);
        }
    }

    return 0;
}

static Json::Value _map2jsoncpp(boost::shared_ptr<std::map<std::string, std::string> > kv)
{
    Json::Value item;
    std::map<std::string, std::string>::iterator iter;

    for (iter = kv -> begin(); iter != kv -> end(); ++iter)
    {
        item[iter -> first] = iter -> second;
    }

    return item;
}

static int genFileParam(file_param_t& param, std::string appkey, const std::vector<std::string>* filenames,
    const std::vector<std::string>* md5s = NULL)
{
    if (NULL == filenames)
    {
        return -1;
    }
    if (NULL != md5s && filenames -> size() != md5s -> size())
    {
        return -1;
    }

    param.__set_appkey(appkey);

    std::vector<ConfigFile> configFiles;
    int size = filenames -> size();
    for (int i = 0; i < size; ++i)
    {
        ConfigFile conf;
        conf.__set_filename(filenames -> at(i));
        if (NULL != md5s)
        {
            conf.__set_md5(md5s -> at(i));
        }
        configFiles.push_back(conf);
    }
    param.__set_configFiles(configFiles);
    return 0;
}

static int _insert2map(std::string key, std::string value, std::map<std::string, std::string>* kv)
{
    std::map<std::string, std::string>::iterator iter;
    iter = kv -> find(key);
    if (kv -> end() !=iter)
    {
        //如果map中已经存在，则只替换val
        iter -> second = value;
    }
    else
    {
        //如果map中不存在，则insert pair
        kv -> insert(std::pair<std::string, std::string>(key, value));
    }

    iter = kv -> find(key);
    if (kv -> end() !=iter)
    {
        // 防止多线程不安全， iter置空的情况
        value = kv -> at(key);
    }
}

#endif
