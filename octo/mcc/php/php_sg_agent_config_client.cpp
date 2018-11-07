#include "php_sg_agent_config_client.h"
#include "sg_agent_config_client.h"
#include "sgagent_common_types.h"
#include "msgparam.h"
#include <iostream>
#include "config_helper.h"


int sg_agent_config_setConfig(std::string appkey, std::string env,
            std::string path, std::string data)
{
    std::cout << "appkey = " << appkey
        << "; env = " << env
        << "; path = " << path << std::endl;

    proc_conf_param_t c_conf = genSetParamObj(appkey, env, path, data);
    int ret = 0;
    boost::shared_ptr<sg_agent_config_client>
        sg_agent_client(new sg_agent_config_client);
    ret = sg_agent_client -> init();
    if (0 != ret)
    {
        return ret;
    }

    ret = sg_agent_client -> setConfig(c_conf);
    sg_agent_client -> destroy();
    return ret;
}

std::string sg_agent_config_getConfig(std::string appkey,
            std::string env, std::string path)
{
    std::cout << "appkey = " << appkey
        << "; env = " << env
        << "; path = " << path << std::endl;

    proc_conf_param_t c_node = genGetParamObj(appkey, env, path);
    int ret = 0;
    boost::shared_ptr<sg_agent_config_client>
        sg_agent_client(new sg_agent_config_client);
    ret = sg_agent_client -> init();
    if (0 != ret)
    {
        std::string errJson
            = "{\"ret\":-202101,\"msg\":\"failed to connect\"}";
        return errJson;
    }

    std::string sRet = "";
    ret = sg_agent_client -> getConfig(sRet, c_node);
    sg_agent_client -> destroy();
    return sRet;
}

int config_init(std::string appkey, std::string env,
            std::string path)
{
    std::cout << "appkey = " << appkey
        << "; env = " << env
        << "; path = " << path << std::endl;

    //ConfigInstance* processor = ConfigInstance::getInstance();
    processor = new ConfigInstance();
    int ret = processor -> init(appkey, env, path);
    return ret;
}

std::string config_get(std::string key)
{
    std::cout << "key = " << key << std::endl;
    if (NULL == processor) {
        std::cout << "processor is null" << std::endl;
    }

    //std::string ret = ConfigInstance::getInstance()
    //    -> get(key);
    std::string ret = processor -> get(key);
    return ret;
}

int config_setConfig(std::string key, std::string value)
{
    std::cout << "key = " << key
        << "; value = " << value << std::endl;
    if (NULL == processor) {
        std::cout << "processor is null" << std::endl;
    }

    //int ret = ConfigInstance::getInstance()
    //    -> set(key, value);
    int ret = processor -> set(key, value);
    return ret;
}
