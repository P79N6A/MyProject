#include "sg_agent_config_instance.h"
#include <string>
#ifndef __SG_AGENT_CONFIG_H
#define __SG_AGENT_CONFIG_H
extern "C"
{
    static ConfigInstance* processor;

    int sg_agent_config_setConfig(std::string, std::string, std::string, std::string);

    std::string sg_agent_config_getConfig(std::string, std::string, std::string);

    int config_init(std::string, std::string, std::string);
    std::string config_get(std::string);
    int config_set(std::string, std::string);
}

#endif
