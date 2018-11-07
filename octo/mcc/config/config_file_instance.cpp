#include "config_instance.h"

int ConfigFileInstance::init(std::string appkey)
{
    m_appkey = appkey;
    return 0;
}

void ConfigFileInstance::set_appkey(std::string appkey)
{
    m_appkey = appkey;
}

void ConfigFileInstance::set_env(std::string env)
{
    m_env = env;
}

void ConfigFileInstance::set_path(std::string path)
{
    m_path = path;
}

std::string ConfigFileInstance::get(std::string key)
{
    return ConfigProcessor::getInstance() -> get(key, m_appkey);
}

int ConfigFileInstance::set(std::string key, std::string value)
{
    return ConfigProcessor::getInstance() -> set(key, value, m_appkey);
}
