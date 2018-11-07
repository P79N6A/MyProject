// =====================================================================================
//
//       Filename:  config_instance.h
//
//    Description:
//
//        Version:  1.0
//        Created:  2015-07-16
//       Revision:  none
//
//
// =====================================================================================

#ifndef __SG_AGNET_CONFIG_INSTANCE_H__
#define __SG_AGNET_CONFIG_INSTANCE_H__

#include "sg_agent_config_processor.h"
#include <string>

class ConfigFileInstance
{
public:
    int init(std::string appkey);
    std::string get(std::string filename);
    std::string get(std::string filename, std::string filepath);

    void set_appkey(std::string);
    void set_env(std::string);
    void set_path(std::string);


private:
    std::string m_appkey;
    std::string m_env;
    std::string m_path;
};

#endif


