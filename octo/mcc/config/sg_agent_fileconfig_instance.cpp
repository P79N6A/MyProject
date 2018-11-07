#include "sg_agent_fileconfig_instance.h"
#include "SGAgentErr.h"
#include "config_helper.h"
#include "sg_agent_def.h"
#include "config_def.h"
#include "md5.h"
#include "sg_agent_config_client.h"
#include "inc_comm.h"
#include "log4cplus.h"

const std::string FileConfigInstance::TAG = "FileConfigInstance";

FileConfigInstance::FileConfigInstance():m_path("/"), m_inited(false), m_version("")
{
    kv = new std::map<std::string, ConfigFile>();
    mListenerMap = new std::map<std::string,
                 boost::shared_ptr<FileChangeListener> >();
}

FileConfigInstance::~FileConfigInstance()
{
    SAFE_DELETE(kv);

    SAFE_DELETE(mListenerMap);
}

int FileConfigInstance::init(std::string appkey)
{
    int ret = 0;
    m_appkey = appkey;
    pthread_rwlock_init(&rwlock, NULL);

    if(pthread_create(&mSid, NULL, _updateBuffer, this) != 0)
    {
        ret = ERR_CREATEPTHREAD_FAILED;
    }

    m_inited = true;
    return ret;
}

std::string FileConfigInstance::get(std::string filename)
{
    // read from cache
    std::string value = "";

    std::string appkey = m_appkey;
    value = _get_from_kv(filename);
    if (!value.empty())
    {
        return value;
    }

    file_param_t configFileRes;
    int ret = _get_from_agent(configFileRes, m_appkey, filename);
    if (0 == ret)
    {
        ret = _insert_kv(filename, configFileRes, this);

        value = _get_from_kv(filename);
        return value;
    }
    return "";
}

int FileConfigInstance::addListener(std::string filename,
            boost::shared_ptr<FileChangeListener> listener)
{
    LOG_DEBUG("add listener = " << filename.c_str());
    int ret = 0;
    std::map<std::string,
        boost::shared_ptr<FileChangeListener> >::iterator iter;
    iter = mListenerMap -> find(filename);
    if (mListenerMap -> end() !=iter)
    {
        //如果map中已经存在，则只替换val
        iter -> second = listener;
    }
    else
    {
        //如果map中不存在，则insert pair
        mListenerMap -> insert(std::pair<std::string,
                    boost::shared_ptr<FileChangeListener> >(filename, listener));
    }

    return ret;
}

void FileConfigInstance::set_appkey(std::string appkey)
{
    m_appkey = appkey;
}

void FileConfigInstance::set_env(std::string env)
{
    m_env = env;
}

void FileConfigInstance::set_path(std::string path)
{
    m_path = path;
}

std::string FileConfigInstance::get_appkey()
{
    return m_appkey;
}

std::string FileConfigInstance::get_env()
{
    return m_env;
}

std::string FileConfigInstance::get_path()
{
    return m_path;
}

std::map<std::string , ConfigFile>* FileConfigInstance::get_kv() {
    return kv;
}

std::map<std::string , boost::shared_ptr<FileChangeListener> >*
    FileConfigInstance::getListenerMap() {
    return mListenerMap;
}

pthread_rwlock_t FileConfigInstance::getLock()
{
    return rwlock;
}

bool FileConfigInstance::get_inited()
{
    return m_inited;
}

std::string FileConfigInstance::_get_from_kv(std::string key)
{
    std::string value;
    ConfigFile confFile;
    std::map<std::string, ConfigFile>::iterator iter;

    if (NULL == kv)
    {
        LOG_ERROR("kv is NULL");
        return "";
    }

    pthread_rwlock_rdlock(&rwlock);
    iter = kv -> find(key);
    if (kv -> end() !=iter)
    {
        confFile = kv -> at(key);
        value = confFile.filecontent;
    }
    pthread_rwlock_unlock(&rwlock);
    return value;
}

int FileConfigInstance::_get_from_agent(file_param_t& configFileRes,
            std::string appkey,
            std::string filename, std::string md5)
{
    std::string value = "";
    int ret = 0;

    file_param_t file_param;
    ConfigFile file;
    file.__set_filename(filename);
    if (!md5.empty())
    {
        file.__set_md5(md5);
    }
    std::vector<ConfigFile> files;
    files.push_back(file);
    file_param.__set_configFiles(files);
    file_param.__set_appkey(appkey);
    if (0 != ret)
    {
        LOG_ERROR("Failed to gen file_param");
        return -1;
    }

    try {
        sg_agent_config_client::getInstance() -> getFileConfig(configFileRes,
                    file_param);
    }
    catch (...)
    {
        return -1;
    }

    if (0 != configFileRes.err || 0 == configFileRes.configFiles.size())
    {
        LOG_ERROR("Failed to get result from agent, err = "
                << configFileRes.err);
        return -1;
    }

    return ret;
}

void* FileConfigInstance::_updateBuffer(void* ptr)
{
    FileConfigInstance* proc_ptr = (FileConfigInstance*) ptr;
    if (proc_ptr -> get_appkey().empty()
                || false == proc_ptr -> get_inited())
    {
        LOG_ERROR("appkey is empty or not init before");
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

    LOG_DEBUG("scan time = " <<  proc_ptr -> m_update_time);

    int msgLen = 0;
    int ret = 0;
    int count = 0;
    int spin = sg_agent::DEFAULT_SPIN_NUM;
    while(1)
    {
        try
        {
            std::map<std::string, ConfigFile>::iterator iter;
            for (iter = proc_ptr -> get_kv() -> begin();
                        iter != proc_ptr -> get_kv() -> end(); ++iter) {
                std::string filename = iter -> first;
                std::string md5 = iter -> second.md5;
                file_param_t file;
                ret = proc_ptr -> _get_from_agent(file,
                            proc_ptr -> get_appkey(),
                            filename, md5);
                if (0 == ret) {
                    ret = _insert_kv(filename, file, proc_ptr);
                }
            }

            usleep(proc_ptr -> m_update_time);
        }
        catch(TException& e)
        {
            continue;
        }
    }
    LOG_WARN("update is stop");

    return 0;
}

int FileConfigInstance::_insert_kv(std::string key, file_param_t configFileRes,
        FileConfigInstance* instance) {
    std::vector<ConfigFile>::iterator iter;
    for (iter = configFileRes.configFiles.begin();
                iter != configFileRes.configFiles.end(); ++iter) {
        ConfigFile file = *iter;
        std::string filename = iter -> filename;
        MD5 mdfive(file.filecontent);
        std::string md5 = mdfive.md5();
        LOG_DEBUG("scan err_code = " << iter -> err_code);
        LOG_DEBUG("scan listenerMap's size = " << instance -> getListenerMap() -> size());
        if (0 == iter -> err_code) {
            if (md5 != file.md5) {
                continue;
            }
            LOG_DEBUG("insert map content = " << file.filecontent.c_str());

            std::map<std::string,
                boost::shared_ptr<FileChangeListener> >::iterator iter;
            std::map<std::string,
                boost::shared_ptr<FileChangeListener> >* listenerMap
                    = instance -> getListenerMap();

            iter = listenerMap -> find(filename);
            if ((listenerMap -> end() !=iter) ||
                (listenerMap -> end()
                 != (iter = listenerMap -> find(JNI_LISTENER)))) {
                LOG_DEBUG("do Listener's OnEvent");
                boost::shared_ptr<FileChangeListener> listener =
                    iter -> second;

                std::string oldFile = instance -> _get_from_kv(filename);

                if (!oldFile.empty() && oldFile != file.filecontent) {
                    listener -> OnEvent(filename, oldFile, file.filecontent);
                }
            }
            else {
                LOG_DEBUG("no Listener");
            }

            pthread_rwlock_t lock = instance -> getLock();
            pthread_rwlock_wrlock(&lock);
            _insert_map(filename, file, instance -> get_kv());
            pthread_rwlock_unlock(&lock);
        }
    }
    return 0;
}

int FileConfigInstance::_insert_map(std::string key, ConfigFile value,
            std::map<std::string, ConfigFile>* kv)
{
    std::map<std::string, ConfigFile>::iterator iter;
    iter = kv -> find(key);
    if (kv -> end() !=iter)
    {
        //如果map中已经存在，则只替换val
        iter -> second = value;
    }
    else
    {
        //如果map中不存在，则insert pair
        kv -> insert(std::pair<std::string, ConfigFile>(key, value));
    }

    iter = kv -> find(key);
    if (kv -> end() !=iter)
    {
        // 防止多线程不安全， iter置空的情况
        value = kv -> at(key);
    }
}

