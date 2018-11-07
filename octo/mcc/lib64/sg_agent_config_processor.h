// =====================================================================================
//
//       Filename:  sg_agent_config_processor.h
//
//    Description:
//
//        Version:  1.0
//        Created:  2015-07-09
//       Revision:  none
//
//
// =====================================================================================

#ifndef __SG_AGNET_CONFIG_PROCESSOR_H__
#define __SG_AGNET_CONFIG_PROCESSOR_H__

#include "sg_agent_config_instance.h"
#include <string>
#include <map>


class ConfigProcessor
{
public:
    ConfigProcessor();
    ~ConfigProcessor();

    /**
     * env&path非必需条件
     * 默认env取决本地SG_Agent环境
     * path默认为"/"
     */
    int add_app(std::string appkey, std::string env = "",
                std::string path = "/");

    std::string get(std::string key, std::string appkey,
                std::string env = "", std::string path = "/");
    int set(std::string key, std::string value,
                std::string appkey, std::string env = "",
                std::string path = "/");

    static ConfigProcessor* getInstance();
private:

    /**
     * appkey -> config instance
     */
    static std::map<std::string, ConfigInstance*>* m_instance_map;

    bool m_inited;

    static pthread_rwlock_t rwlock;
    static ConfigProcessor* m_processor;
    static std::string TAG;

    // ret = -2标示没有对应appkey的instance
    ConfigInstance*_get_instance(std::string key);
    int _insert_instance(std::string key, ConfigInstance* instance);

    std::string _gen_key(std::string key, std::string env = "", std::string path = "/");
};
#endif
