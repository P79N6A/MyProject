// =====================================================================================
//
//       Filename:  config_client.cpp
//
//    Description:
//
//        Version:  1.0
//        Created:  2016-01-15
//       Revision:  kv config SDK in CPlusPlus
//
//
// =====================================================================================

#include "kv_config_client.h"
#include "sg_agent_config_processor.h"
#include "log4cplus.h"

pthread_rwlock_t  KVConfigClient::rwlock = PTHREAD_RWLOCK_INITIALIZER;

KVConfigClient::KVConfigClient():mIsInited(false) {
    // todo
    // mListenerMap = new std::map<std::string, boost::shared_ptr<ConfigChangeListener> >();
}

int KVConfigClient::Init(std::string appkey) {
    mAppkey = appkey;

    // todo
    //if (NULL == mListenerMap) {
    //    mListenerMap = new std::map<std::string, boost::shared_ptr<ConfigChangeListener> >();
    //}
    int ret = ConfigProcessor::getInstance() -> add_app(appkey);
    if (0 == ret) {
        mIsInited = true;
    }
    return ret;
}

// todo
//int KVConfigClient::AddListener(
//            std::string key,
//            boost::shared_ptr<ConfigChangeListener> listener) {
//    int ret = _check(key);
//    if (0 != ret) {
//        LOG_ERROR("failed to check in addListener, ret = " << ret);
//        return ret;
//    }
//
//    return ConfigProcessor::getInstance()
//        -> addListener(key, listener, mAppkey);
//}

int KVConfigClient::AddGlobalListener(
            boost::shared_ptr<GlobalConfigChangeListener> listener) {
    int ret = _check();
    if (0 != ret) {
        LOG_ERROR("failed to check in addListener, ret = " << ret);
        return ret;
    }

    return ConfigProcessor::getInstance()
        -> addListener(listener, mAppkey);
}

std::string KVConfigClient::GetValue(std::string key) {
    int ret = _check(key);
    if (0 != ret) {
        LOG_ERROR("failed to check in GetValue, ret = " << ret);
        return "";
    }

    std::string value = ConfigProcessor::getInstance() -> get(key, mAppkey);
    return value;
}

std::string KVConfigClient::GetAppkey() {
    return mAppkey;
}

int KVConfigClient::_check(std::string key) {
    if (0 != _check()) {
        return -1;
    }
    else if (key.empty()) {
        LOG_ERROR("key is empty");
        return -1;
    }
    return 0;
}

int KVConfigClient::_check() {
    if (!mIsInited) {
        LOG_ERROR("KVConfigClient need init first");
        return -1;
    }
    else if (mAppkey.empty()) {
        LOG_ERROR("appkey is empty");
        return -1;
    }
    return 0;
}
