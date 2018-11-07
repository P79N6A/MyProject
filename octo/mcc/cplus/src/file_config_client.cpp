// =====================================================================================
//
//       Filename:  file_config_client.cpp
//
//    Description:
//
//        Version:  1.0
//        Created:  2015-08-07
//       Revision:  file config SDK in CPlusPlus
//
//
// =====================================================================================

#include "file_config_client.h"
#include "sg_agent_fileconfig_processor.h"

const static std::string CONF_PATH = "/opt/meituan/apps/mcc/";

pthread_rwlock_t  FileConfigClient::rwlock = PTHREAD_RWLOCK_INITIALIZER;

FileConfigClient::FileConfigClient():mIsInited(false) {
    mListenerMap = new std::map<std::string, boost::shared_ptr<FileChangeListener> >();
}

int FileConfigClient::init(std::string appkey) {
    mAppkey = appkey;

    if (NULL == mListenerMap) {
        mListenerMap = new std::map<std::string, boost::shared_ptr<FileChangeListener> >();
    }
    int ret = FileConfigProcessor::getInstance() -> add_app(appkey);
    if (0 == ret) {
        mIsInited = true;
    }
    return ret;
}

int FileConfigClient::addListener(
            std::string filename,
            boost::shared_ptr<FileChangeListener> listener) {
    if (!mIsInited || mAppkey.empty()) {
        return -1;
    }
    return FileConfigProcessor::getInstance()
        -> addListener(filename, listener, mAppkey);
}

std::string FileConfigClient::getFile(std::string filename) {
    if (!mIsInited || mAppkey.empty()) {
        return "";
    }
    std::string tmp_res = FileConfigProcessor::getInstance() -> get(filename, mAppkey);
    return tmp_res;
}

std::string FileConfigClient::getPath() {
    return CONF_PATH + mAppkey + "/";
}

std::string FileConfigClient::getAppkey() {
    return mAppkey;
}

void FileConfigClient::setAppkey(std::string appkey) {
    mAppkey = appkey;
}

