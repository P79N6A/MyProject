// =====================================================================================
//
//       Filename:  file_config_client.h
//
//    Description:
//
//        Version:  1.0
//        Created:  2015-08-07
//       Revision:  file config SDK in CPlusPlus
//
//
// =====================================================================================

#ifndef __FILE_CONFIG_CLIENT_H__
#define __FILE_CONFIG_CLIENT_H__
#include "file_config_listener.h"
#include <boost/shared_ptr.hpp>
#include <map>


class FileConfigClient {
    public:

        FileConfigClient();
        int init(std::string appkey);
        int addListener(std::string filename,
                    boost::shared_ptr<FileChangeListener> listener);
        std::string getFile(std::string filename);
        std::string getPath();

        std::string getAppkey();
        void setAppkey(std::string appkey);

    private:
        std::string mAppkey;
        bool mIsInited;

        static pthread_rwlock_t rwlock;
        std::map<std::string, boost::shared_ptr<FileChangeListener> >*
            mListenerMap;

};
#endif
