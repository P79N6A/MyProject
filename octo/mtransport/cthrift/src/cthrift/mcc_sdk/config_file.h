//
// Created by Xiang Zhang on 2017/9/6.
//

#ifndef PROJECT_CONFIG_FILE_H
#define PROJECT_CONFIG_FILE_H

#include "mcc_sdk.h"

namespace mcc_sdk {
extern const std::string kStrDiskFileDir;
extern muduo::MutexLock file_mutex;
struct FileCfgInfo {
    ConfigFile cfg_file;
    std::vector <FileConfigCallback> vec_callback_func;


    void SetFileCfgCallback(const FileConfigCallback &file_cfg_cb) {
        vec_callback_func.push_back(file_cfg_cb);
    }

    void UpdateFileCfg(const std::string &str_appkey,
                       const ConfigFile &cfg_file); //disk file, cache, callback

    std::string ToString(void) const {
        return std::string("filename: " + cfg_file.filename + " md5: " + cfg_file.md5
                      + " filecontent: " + cfg_file.filecontent);
    }

    void UpdateDiskFile(const std::string &str_appkey,
                        const ConfigFile &new_cfg_file) const;
};
}
#endif //PROJECT_CONFIG_FILE_H
