#include "config_instance.h"

int ConfigInstance::init(std::string appkey)
{
    m_appkey = appkey;
    return 0;
}

void ConfigInstance::set_appkey(std::string appkey)
{
    m_appkey = appkey;
}

void ConfigInstance::set_env(std::string env)
{
    m_env = env;
}

void ConfigInstance::set_path(std::string path)
{
    m_path = path;
}

std::string ConfigInstance::get(std::string key)
{
    return ConfigProcessor::getInstance() -> get(key, m_appkey);
}

int ConfigInstance::set(std::string key, std::string value)
{
    return ConfigProcessor::getInstance() -> set(key, value, m_appkey);
}
