//
// Created by hawk on 2017/9/7.
//

#include <boost/filesystem.hpp>
#define BOOST_SPIRIT_THREADSAFE
#include <boost/property_tree/ptree.hpp>
#include <boost/property_tree/json_parser.hpp>
#include <boost/property_tree/ini_parser.hpp>
#include <boost/foreach.hpp>

#include "config_dynamic.h"

using namespace mcc_sdk;

void ZKCfgInfo::UpdateDiskFile(const std::string &str_appkey,
                               const std::string &new_zk_content) const {
    const std::string str_file_full_path
            (kStrDiskZKDir + "/" + str_appkey);
    CLOG_STR_DEBUG("file full path " << str_file_full_path);

    boost::filesystem::path full_path(str_file_full_path, boost::filesystem::native);

    if (CTHRIFT_UNLIKELY(
            !(boost::filesystem::exists(full_path.parent_path())) && !(boost::filesystem::create_directories(full_path.parent_path()
            )))) {
        CLOG_STR_ERROR("dir path " << str_file_full_path << " NOT exist, and "
                "create failed");
    } else {
        muduo::MutexLockGuard lock(zk_mutex);
        ofstream output_stream;
        try {
            output_stream.open(str_file_full_path.c_str(), ios::trunc);
        } catch (const ofstream::failure &e) {
            CLOG_STR_ERROR("Exception opening config file: " << str_file_full_path);
            return;
        }
        if (CTHRIFT_LIKELY(output_stream.is_open())) {
            output_stream << new_zk_content;
            //disk file
            // format
        }
        if (CTHRIFT_UNLIKELY(output_stream.bad())) {
            CLOG_STR_ERROR("Writing config file failed: " << str_file_full_path);
        }
        output_stream.close();
    }
}

bool ZKCfgInfo::UpdateCfg(const std::string &str_appkey,
                          const std::string &zk_content) {

    boost::property_tree::ptree pt;
    std::stringstream zk_ss(zk_content);
    try {
        boost::property_tree::read_json(zk_ss, pt);
    } catch (boost::property_tree::ptree_error &e) {
        CLOG_STR_ERROR("parser dynamic config failed, error: "
                  << e.what());
        return false;
    }

    int64_t new_version = pt.get<int64_t>("version", this->version);
    CLOG_STR_INFO("  new version " << new_version << " old version " << this->version);
    if (new_version > this->version) {
        CLOG_STR_INFO("appkey: " << str_appkey
                 << " dynamic config need update");
    } else {
        CLOG_STR_INFO("appkey: " << str_appkey
                  << " dynamic config NO need update");
        return false;
    }
    //chang ZKCfgInfo object version
    this->version = new_version;

    //iterate json conf
    std::string old_value = "";
	std::map<std::string, std::string> new_map;
    BOOST_FOREACH(boost::property_tree::ptree::value_type &v, pt.get_child("data")) {
        new_map[v.first] = v.second.data();
        if (this->map_key_value[v.first] == v.second.data()) {
            continue;
        }
        CLOG_STR_DEBUG("update map for appkey:key: " << str_appkey << ":"
                  << v.first << " data: " << v.second.data());
        old_value = this->map_key_value[v.first];

        //iterate callback fun
        CLOG_STR_DEBUG("invoke callback func for appkey:key: " << str_appkey << ":"
                  << v.first);
        for (std::vector<ConfigCallback>::const_iterator it =
                (this->map_callback_func[v.first]).begin();
             (this->map_callback_func[v.first]).end() != it; ++it) {

            (*it)(str_appkey, v.first, v.second.data(), old_value);
        }
        CLOG_STR_DEBUG("invoke key callback func done");
    }
    //change kv_map, using new map.
    (this->map_key_value).swap(new_map);

    if (!callback_func.empty()) {
        CLOG_STR_DEBUG("invoke zk global callback func for appkey: " << str_appkey);
        callback_func(str_appkey, this->version, this->map_key_value);
    }

    return true;
}
