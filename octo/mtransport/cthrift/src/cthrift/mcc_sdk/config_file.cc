//
// Created by Xiang Zhang on 2017/9/6.
//

#include <boost/filesystem.hpp>

#include "config_file.h"

using namespace mcc_sdk;

void FileCfgInfo::UpdateDiskFile(const std::string &str_appkey,
                                 const ConfigFile &new_cfg_file) const {
    const std::string
            str_file_full_path
            (kStrDiskFileDir + "/" + str_appkey + "/" + new_cfg_file.filename);
    CLOG_STR_DEBUG("file full path " << str_file_full_path);

    boost::filesystem::path full_path(
            str_file_full_path,
            boost::filesystem::native);

    if (CTHRIFT_UNLIKELY(
            !(boost::filesystem::exists(full_path.parent_path())) && !(boost::filesystem::create_directories(full_path.parent_path()
            )))) {
        CLOG_STR_ERROR("dir path " << str_file_full_path << " NOT exist, and "
                "create failed");
    } else {
        muduo::MutexLockGuard lock(file_mutex);
        ofstream output_stream;
        try {
            output_stream.open(str_file_full_path.c_str(), ios::trunc);  //TODO try?
        } catch (const ofstream::failure &e) {
            CLOG_STR_ERROR("Exception opening config file: " << str_file_full_path);
            return;
        }
        if (CTHRIFT_LIKELY(output_stream.is_open())) {
            output_stream << new_cfg_file.md5 << " "
                          << new_cfg_file.filecontent; //TODO write format
            //disk file
            // format
        }
        if (CTHRIFT_UNLIKELY(output_stream.bad())) {
            CLOG_STR_ERROR("Writing config file failed: " << str_file_full_path);
        }
        output_stream.close();
    }
}

void FileCfgInfo::UpdateFileCfg(const std::string &str_appkey,
                                const ConfigFile &new_cfg_file) {
    if (CTHRIFT_UNLIKELY(new_cfg_file.md5 != (this->cfg_file).md5)) {
        CLOG_STR_INFO("appkey: " << str_appkey << " filename: "
                 << new_cfg_file.filename
                 << " need update");
    } else {
        CLOG_STR_INFO("appkey: " << str_appkey << " filename: "
                  << new_cfg_file.filename
                  << " NO need update");
        return;
    }

    this->cfg_file = new_cfg_file;
    std::vector<FileConfigCallback>::const_iterator it = (this->vec_callback_func).begin();
    while (it != (this->vec_callback_func).end()) {
        CLOG_STR_DEBUG("invoke callback func for appkey: " << str_appkey << " "
                "filename: " << cfg_file.filename);

        (*it)(str_appkey, new_cfg_file.filename, new_cfg_file.filecontent);

        CLOG_STR_DEBUG("invoke callback func done");
        ++it;
    }

    UpdateDiskFile(str_appkey, new_cfg_file);
}
