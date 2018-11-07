//#include <boost/optional/optional.hpp>
#include "sg_agent_config_instance.h"
#include "SGAgentErr.h"
#include "sg_agent_config_client.h"
#include "config_helper.h"
#include "sg_agent_def.h"
#include "config_def.h"
#include "inc_comm.h"
#include "json/json.h"
#include "log4cplus.h"

const std::string ConfigInstance::TAG = "ConfigInstance";

//ConfigInstance::ConfigInstance():m_path("/"), m_inited(false)
ConfigInstance::ConfigInstance():m_path("/"), m_inited(false), m_version(""), is_first_get(true)
{
    kv = boost::shared_ptr<std::map<std::string, std::string> >(new std::map<std::string, std::string>());
    mData = "";
    mListenerMap = new std::map<std::string,
                 boost::shared_ptr<ConfigChangeListener> >();
}

ConfigInstance::~ConfigInstance()
{
}

int ConfigInstance::init(std::string appkey, std::string env, std::string path)
{
    int ret = 0;
    m_appkey = appkey;
    m_env = env;
    m_path = path;
    pthread_rwlock_init(&rwlock, NULL);

    if(pthread_create(&mSid, NULL, _updateBuffer, this) != 0)
    {
        ret = ERR_CREATEPTHREAD_FAILED;
    }

    m_inited = true;
    return ret;
}

std::string ConfigInstance::get(std::string key)
{
    // read from cache
    std::string value = "";

    std::string appkey = m_appkey;

    pthread_rwlock_rdlock(&rwlock);
    value = _get_from_kv(key);
    pthread_rwlock_unlock(&rwlock);
    if (!value.empty())
    {
        return value;
    }

    if (is_first_get) {
        int ret = _get_from_agent(m_appkey, m_env, m_path);
        if (0 == ret)
        {
            pthread_rwlock_rdlock(&rwlock);
            value = _get_from_kv(key);
            pthread_rwlock_unlock(&rwlock);
            return value;
        }
    }
    return "";
}

int ConfigInstance::set(std::string key, std::string value)
{
    pthread_rwlock_rdlock(&rwlock);
    Json::Value item = _map2jsoncpp(kv);
    pthread_rwlock_unlock(&rwlock);

    item[key] = value;
    std::string str_json = item.toStyledString();

    proc_conf_param_t node = genSetParamObj(m_appkey, m_env, m_path, str_json);
    try {
        return sg_agent_config_client::getInstance() -> setConfig(node);
    }
    catch (...)
    {
        return -1;
    }
}

int ConfigInstance::addListener(std::string key,
            boost::shared_ptr<ConfigChangeListener> listener)
{
    LOG_DEBUG("add listener = " << key.c_str());
    int ret = 0;
    std::map<std::string,
        boost::shared_ptr<ConfigChangeListener> >::iterator iter;
    iter = mListenerMap -> find(key);
    if (mListenerMap -> end() !=iter)
    {
        //如果map中已经存在，则只替换val
        iter -> second = listener;
    }
    else
    {
        //如果map中不存在，则insert pair
        mListenerMap -> insert(std::pair<std::string,
                    boost::shared_ptr<ConfigChangeListener> >(key, listener));
    }

    return ret;
}

int ConfigInstance::addGlobalListener(
            boost::shared_ptr<GlobalConfigChangeListener> listener)
{
    LOG_DEBUG("add glabal listener");
    mGlobalListener = listener;

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

std::string ConfigInstance::get_appkey()
{
    return m_appkey;
}

std::string ConfigInstance::get_env()
{
    return m_env;
}

std::string ConfigInstance::get_path()
{
    return m_path;
}

bool ConfigInstance::get_inited()
{
    return m_inited;
}

std::string ConfigInstance::_get_from_kv(std::string key)
{
    
    std::string value;
    std::map<std::string, std::string>::iterator iter;

    if (NULL == kv)
    {
        LOG_ERROR("ConfigInstance, kv is null");
        return "";
    }

    pthread_rwlock_rdlock(&rwlock);
    iter = kv -> find(key);
    if (kv -> end() !=iter)
    {
        // 防止多线程不安全， iter置空的情况
        value = kv -> at(key);
    }
    pthread_rwlock_unlock(&rwlock);
    return value;
}

int ConfigInstance::_get_from_agent(std::string appkey,
            std::string env, std::string path)
{
    std::string value = "";
    int ret = 0;

    std::string str_json = "";
    proc_conf_param_t node = genGetParamObj(appkey, env, path);
    try {
        sg_agent_config_client::getInstance() -> getConfig(str_json, node);
    }
    catch (...)
    {
        return -1;
    }

    if (str_json.empty())
    {
        LOG_INFO(": get null from agent, appkey = " << appkey);
        return -1;
    }
    
    //std::stringstream ss(str_json);

    //ptree pt;
    Json::Reader reader;
    Json::Value item;

    if (!reader.parse(str_json, item)) {
        std::cout << "failed to parse json: " << std::endl;
        return -1;
    }

    ret = item["ret"].asInt();
    if (ret != 0)
    {
        std::string msg = item["msg"].asString();
        if (!msg.empty())
        {
            LOG_ERROR("ret = " << ret << ", not has msg" << ret);
        }
        else
        {
            LOG_ERROR("ret = " << ret << ", msg = " << msg);
        }
        return ret;
    }

    std::string version = item["verson"].asString();
    if (!m_version.empty() && !version.empty() &&
                0 == m_version.compare(version))
    { // version相等， 不需要更新
        return 0;
    }
    m_version = version;


    Json::Value data = item["data"];
    if (data.isNull()) {
        LOG_ERROR("faild to get data from json, data is null");
        return -1;
    }
    Json::FastWriter fastWriter;
    std::string str_data = fastWriter.write(data);

    if (NULL != mGlobalListener && !mData.empty()
            && 0 != str_data.compare(mData)) {
        mGlobalListener -> OnEvent(mData, str_data);
    }

    pthread_rwlock_wrlock(&rwlock);
    ret = _json2map(data, kv);
    mData = str_data;
    pthread_rwlock_unlock(&rwlock);

    if (0 != ret)
    {
        LOG_ERROR("failed to do json2map, errno = " <<  ret);
        return ret;
    }
    is_first_get = false;
    return ret;
}

int ConfigInstance::_checkListener(
        boost::shared_ptr<std::map<std::string, std::string> > newkv) {
    // todo
    return 0;
}
void* ConfigInstance::_updateBuffer(void* ptr)
{
    ConfigInstance* proc_ptr = (ConfigInstance*) ptr;
    if (proc_ptr -> get_appkey().empty()
                || false == proc_ptr -> get_inited())
    {
        return NULL;
    }

    if (!sg_agent_config_client::getInstance() -> is_online())
    {
        proc_ptr -> m_update_time = UPDATE_TIME_OFFLIE;
    }
    else
    {
        proc_ptr -> m_update_time = UPDATE_TIME_ONLINE;
    }

    while(1)
    {
        try
        {
            proc_ptr -> _get_from_agent(
                        proc_ptr -> get_appkey(),
                        proc_ptr -> get_env(),
                        proc_ptr -> get_path());

            usleep(proc_ptr -> m_update_time);
        }
        catch(TException& e)
        {
            continue;
        }
    }

    LOG_ERROR("leave out from update scan");

    return 0;
}
