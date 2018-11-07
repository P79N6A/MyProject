#include "sg_agent_fileconfig_processor.h"
#include "SGAgentErr.h"
#include "config_helper.h"
#include "sg_agent_def.h"
#include "inc_comm.h"
#include "config_def.h"
#include "log4cplus.h"

FileConfigProcessor* FileConfigProcessor::m_processor = NULL;
std::map<std::string, FileConfigInstance*>* FileConfigProcessor::m_instance_map = NULL;

pthread_rwlock_t  FileConfigProcessor::rwlock = PTHREAD_RWLOCK_INITIALIZER;

FileConfigProcessor::FileConfigProcessor()
{
    pthread_rwlock_wrlock(&rwlock);
    m_instance_map = new std::map<std::string, FileConfigInstance*>();
    pthread_rwlock_unlock(&rwlock);
}

FileConfigProcessor::~FileConfigProcessor()
{
    pthread_rwlock_wrlock(&rwlock);
    SAFE_DELETE(m_instance_map);
    pthread_rwlock_unlock(&rwlock);
}

std::string FileConfigProcessor::get(std::string filename, std::string appkey)
{
    FileConfigInstance* instance = NULL;
    std::string ins_key = _gen_key(appkey);
    instance = _get_instance(ins_key);
    if (NULL == instance)
    {
        LOG_ERROR("Not get instance, appkey = " << appkey << ", you need add_app before");
        return "";
    }

    std::string res = "";
    int i = 0;
    do
    {
        res = instance -> get(filename);
    } while (++i <= RETRY_TIMES && res.empty());

    if (res.empty()) {
        LOG_DEBUG("get filecontent is empty");
    }
    return res;
}

int FileConfigProcessor::add_app(std::string appkey)
{
    int ret = 0;
    FileConfigInstance* instance;
    std::string ins_key = _gen_key(appkey);
    instance = _get_instance(ins_key);
    // 已经存在节点， 直接返回
    if (NULL != instance)
    {
        return 0;
    }

    instance = new FileConfigInstance();
    int retry = 0;
    do {
        ret = instance -> init(appkey);
    } while (0 != ret && ++retry <= RETRY_TIMES);

    if (ret != 0)
    {
        LOG_ERROR("failed to init app, appkey = " << appkey);
        SAFE_DELETE(instance);
        return ret;
    }

    ret = _insert_instance(ins_key, instance);

    if (ret != 0)
    {
        LOG_ERROR("failed to add app, appkey = " << appkey);
        SAFE_DELETE(instance);
        return ret;
    }

    return ret;
}

int FileConfigProcessor::addListener(std::string filename, boost::shared_ptr<FileChangeListener> listener,
            std::string appkey) {
    int ret = 0;
    FileConfigInstance* instance = NULL;
    std::string ins_key = _gen_key(appkey);
    instance = _get_instance(ins_key);
    if (NULL == instance) {
        LOG_ERROR("can't get instance, key = " << ins_key);
        return -1;
    }

    int i = 0;
    do {
        ret = instance -> addListener(filename, listener);
    } while (++i <= RETRY_TIMES && ret != 0);

    LOG_DEBUG("addListener appkey = " << appkey
            << ", filename = " << filename);

    return ret;
}

FileConfigInstance* FileConfigProcessor::_get_instance(std::string key) {
    FileConfigInstance* instance = NULL;
    std::map<std::string, FileConfigInstance*>::iterator iter;
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
        LOG_WARN("not get instance from map, key = " << key);
        instance = NULL;
    }
    return instance;
}

int FileConfigProcessor::_insert_instance(std::string key, FileConfigInstance* instance)
{
    std::map<std::string, FileConfigInstance*>::iterator iter;

    iter = m_instance_map -> find(key);
    if (m_instance_map -> end() !=iter)
    { // 已经存在实例， 直接返回
        LOG_DEBUG("appkey = " << key << " is in instance map, don't insert again");
        return 0;
    }

    //如果map中不存在，则insert pair
    pthread_rwlock_wrlock(&rwlock);
    m_instance_map -> insert(std::pair<std::string, FileConfigInstance*>(key, instance));
    pthread_rwlock_unlock(&rwlock);

	return 0;
}

std::string FileConfigProcessor::_gen_key(std::string filename)
{
    return filename;
}

FileConfigProcessor* FileConfigProcessor::getInstance()
{
    if (NULL == m_processor)
    {
        //初始化日志库
        log4cplus::PropertyConfigurator::doConfigure(LOG4CPLUS_TEXT(LOGCONF));

        m_processor = new FileConfigProcessor();
    }
    return m_processor;
}

