// =====================================================================================
//
//       Filename:  sg_agent_configfile_instance.h
//
//    Description:
//
//        Version:  1.0
//        Created:  2015-07-09
//       Revision:  none
//
//
// =====================================================================================

#ifndef __SG_AGNET_FILECONFIG_INSTANCE_H__
#define __SG_AGNET_FILECONFIG_INSTANCE_H__

#include <string>
#include <map>
#include <boost/shared_ptr.hpp>
#include "file_config_listener.h"
#include "config_common_types.h"

// JNI 的addListener比较特殊， 增加全局的listener
const std::string JNI_LISTENER = "JNI_LISTENER";

class FileConfigInstance
{
public:
    FileConfigInstance();
    ~FileConfigInstance();

    int init(std::string appkey);
    std::string get(std::string filename);
    int addListener(std::string filename, boost::shared_ptr<FileChangeListener> listener);

    void set_appkey(std::string);
    void set_env(std::string);
    void set_path(std::string);
    std::string get_appkey();
    std::string get_env();
    std::string get_path();
    std::map<std::string, ConfigFile>* get_kv();
    std::map<std::string, boost::shared_ptr<FileChangeListener> >*
        getListenerMap();

    pthread_rwlock_t getLock();

    bool get_inited();

private:
    const static std::string TAG; //  = "FileConfigInstance";

    std::string m_appkey;
    std::string m_env;
    std::string m_path;

    std::string m_version; // version of config

    bool m_inited;
    pthread_rwlock_t rwlock;
    pthread_t mSid;

    int m_update_time; // update scan time

    std::map<std::string, ConfigFile>* kv;

    // 存储Listener
    std::map<std::string,
        boost::shared_ptr<FileChangeListener> >*
        mListenerMap;

    std::string _get_from_kv(std::string key);
    int _get_from_agent(file_param_t& configFileRes, std::string,
                std::string, std::string md5 = "");
    static void* _updateBuffer(void*);

    static int _insert_kv(std::string key,
                file_param_t configFiles,
                FileConfigInstance* instance);

    static int _insert_map(std::string key, ConfigFile value,
            std::map<std::string, ConfigFile>* kv);

};
#endif
