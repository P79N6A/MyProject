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

#ifndef __SG_AGNET_FILECONFIG_PROCESSOR_H__
#define __SG_AGNET_FILECONFIG_PROCESSOR_H__

#include <boost/optional/optional.hpp>
#include "sg_agent_fileconfig_instance.h"
#include <string>
#include <map>


class FileConfigProcessor
{
public:
    FileConfigProcessor();
    ~FileConfigProcessor();

    /**
     * env&path非必需条件
     * 默认env取决本地SG_Agent环境
     * path默认为"/"
     */
    int add_app(std::string appkey);

    std::string get(std::string filename, std::string appkey);

    int addListener(std::string filename,
                boost::shared_ptr<FileChangeListener> listener,
                std::string appkey);

    static FileConfigProcessor* getInstance();
private:

    /**
     * appkey -> config instance
     */
    static std::map<std::string, FileConfigInstance*>* m_instance_map;

    bool m_inited;

    static pthread_rwlock_t rwlock;
    static FileConfigProcessor* m_processor;

    // ret = -2标示没有对应appkey的instance
    FileConfigInstance*_get_instance(std::string key);
    int _insert_instance(std::string key, FileConfigInstance* instance);

    std::string _gen_key(std::string filename);
};
#endif
