//
// Created by hawk on 2017/9/6.
//

#ifndef PROJECT_CONFIG_ZK_H_H
#define PROJECT_CONFIG_ZK_H_H

#include "mcc_sdk.h"

namespace mcc_sdk {

extern const std::string kStrDiskZKDir;
extern muduo::MutexLock zk_mutex;

struct ZKCfgInfo {
    int64_t version;
    GlobalConfigCallback callback_func;
    boost::unordered_map<std::string, std::vector < ConfigCallback> >
    map_callback_func;
    std::map<std::string, std::string> map_key_value;

    void SetGolbalCfgCallback(const GlobalConfigCallback &cfg_cb) {
        callback_func = cfg_cb;
    }

    void AddCfgCallback(const std::string &key,
                          const ConfigCallback &zk_cfg_cb) {
        map_callback_func[key].push_back(zk_cfg_cb);
    }

    bool UpdateCfg(const std::string &str_appkey,
                     const std::string &zk_file);

    void UpdateDiskFile(const std::string &str_appkey,
                        const std::string &new_zk_file) const;
};

}
#endif //PROJECT_CONFIG_ZK_H_H
