#include "sg_agent_config_processor.h"
#include "SGAgentErr.h"
#include "config_helper.h"
#include "sg_agent_def.h"
#include "inc_comm.h"
#include "config_def.h"
#include "log4cplus.h"

ConfigProcessor* ConfigProcessor::m_processor = NULL;
std::map<std::string, ConfigInstance*>* ConfigProcessor::m_instance_map = NULL;

pthread_rwlock_t  ConfigProcessor::rwlock = PTHREAD_RWLOCK_INITIALIZER;

ConfigProcessor::ConfigProcessor()
{
    pthread_rwlock_wrlock(&rwlock);
    m_instance_map = new std::map<std::string, ConfigInstance*>();
    pthread_rwlock_unlock(&rwlock);
}

ConfigProcessor::~ConfigProcessor()
{
    pthread_rwlock_wrlock(&rwlock);
    SAFE_DELETE(m_instance_map);
    pthread_rwlock_unlock(&rwlock);
}

std::string ConfigProcessor::get(std::string key, std::string appkey,
                std::string env , std::string path)
{
    ConfigInstance* instance = NULL;
    std::string ins_key = _gen_key(appkey, env, path);
    instance = _get_instance(ins_key);
    if (NULL == instance)
    {
        LOG_INFO("instance is null in ConfigProcessor");
        return "";
    }

    std::string res = "";
    int i = 0;
    do
    {
        res = instance -> get(key);
    } while (++i <= RETRY_TIMES && res.empty());
    return res;
}

int ConfigProcessor::set(std::string key, std::string value,
                std::string appkey, std::string env,
                std::string path)
{
    ConfigInstance* instance = NULL;
    std::string ins_key = _gen_key(appkey, env, path);
    instance = _get_instance(ins_key);
    if (NULL == instance)
    {
        return -2;
    }
    int ret = 0;
    int i = 0;
    do
    {
        ret = instance -> set(key, value);
    } while (++i <= RETRY_TIMES && 0 != ret);
    return ret;
}

int ConfigProcessor::add_app(std::string appkey, std::string env,
            std::string path)
{
    int ret = 0;
    ConfigInstance* instance;
    std::string ins_key = _gen_key(appkey, env, path);
    instance = _get_instance(ins_key);
    // 已经存在节点， 直接返回
    if (NULL != instance)
    {
        return 0;
    }

    instance = new ConfigInstance();
    int retry = 0;
    do {
        ret = instance -> init(appkey, env, path);
    } while (0 != ret && ++retry <= RETRY_TIMES);

    if (ret != 0)
    {
        SAFE_DELETE(instance);
        return ret;
    }

    ret = _insert_instance(ins_key, instance);
    LOG_DEBUG("insert into instance in ConfigInstance, ret = " << ret);

    return ret;
}

int ConfigProcessor::addListener(std::string key, boost::shared_ptr<ConfigChangeListener> listener,
            std::string appkey) {
    int ret = 0;
    ConfigInstance* instance = NULL;
    std::string ins_key = _gen_key(appkey);
    instance = _get_instance(ins_key);
    if (NULL == instance) {
        LOG_ERROR("can't get instance, key = " << ins_key);
        return -1;
    }

    int i = 0;
    do {
        ret = instance -> addListener(key, listener);
    } while (++i <= RETRY_TIMES && ret != 0);

    LOG_DEBUG("addListener appkey = " << appkey
            << ", key = " << key);

    return ret;
}

int ConfigProcessor::addListener(boost::shared_ptr<GlobalConfigChangeListener> listener,
            std::string appkey) {
    int ret = 0;
    ConfigInstance* instance = NULL;
    std::string ins_key = _gen_key(appkey);
    instance = _get_instance(ins_key);
    if (NULL == instance) {
        LOG_ERROR("can't get instance, key = " << ins_key);
        return -1;
    }

    int i = 0;
    do {
        ret = instance -> addGlobalListener(listener);
    } while (++i <= RETRY_TIMES && ret != 0);

    LOG_DEBUG("addBlobalListener appkey = " << appkey);

    return ret;
}

ConfigInstance* ConfigProcessor::_get_instance(std::string key)
{
    ConfigInstance* instance = NULL;
    std::map<std::string, ConfigInstance*>::iterator iter;
    iter = m_instance_map -> find(key);
    if (m_instance_map -> end() !=iter)
    {
        // 防止多线程不安全， iter置空的情况
        pthread_rwlock_rdlock(&rwlock);
        //instance = m_instance_map -> at(appkey);
        instance = iter -> second;
        pthread_rwlock_unlock(&rwlock);
    }
    else
    {
        LOG_WARN("not get instance from ConfigProcessor");
        instance = NULL;
    }
    return instance;
}

int ConfigProcessor::_insert_instance(std::string key, ConfigInstance* instance)
{
    std::map<std::string, ConfigInstance*>::iterator iter;

    iter = m_instance_map -> find(key);
    if (m_instance_map -> end() !=iter)
    { // 已经存在实例， 直接返回
        return 0;
    }

    //如果map中不存在，则insert pair
    pthread_rwlock_wrlock(&rwlock);
    m_instance_map -> insert(std::pair<std::string, ConfigInstance*>(key, instance));
    pthread_rwlock_unlock(&rwlock);

	return 0;
}

std::string ConfigProcessor::_gen_key(std::string key, std::string env,
            std::string path)
{
    return key + env + path;
}

ConfigProcessor* ConfigProcessor::getInstance()
{
    if (NULL == m_processor)
    {
        //初始化日志库
        log4cplus::PropertyConfigurator::doConfigure(LOG4CPLUS_TEXT(LOGCONF));

        m_processor = new ConfigProcessor();
    }
    return m_processor;
}

