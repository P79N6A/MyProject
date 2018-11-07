#ifndef MCC_SDK_H_
#define MCC_SDK_H_

#include <map>

#include "../cthrift_common.h"

namespace mcc_sdk {
//开始时调用, 定期检查云端配置更新的间隔时间默认为500ms，可以自己设置; 返回值0:成功，-1:失败 检查err info字符串获取细节
int8_t InitMCCClient(std::string *p_str_err_info,
                     const std::string &cat_appkey,
                     const int32_t &i32_reqest_timeout_ms = 500,
                     const int32_t &i32_loop_interval_ms = 500);

int8_t InitMCCClient(std::string *p_str_err_info,
                     const int32_t &i32_reqest_timeout_ms = 500,
                     const int32_t &i32_loop_interval_ms = 500);

//接入clog统一日志,参考example,一个进程仅需要初始化一次

//<--------------------------------拉取静态配置文件接口--------------------------->
typedef boost::function<void(
const std::string
    &str_appkey,
const std::string
    &str_file_name,
const std::string
    &str_file_content
)>
FileConfigCallback;  //获取变动后的整个文件配置，允许为空，另外注意MCC库的子线程在执行该函数，注意线程安全问题


int8_t SetFileConfigCallbackFunc(const std::string &str_appkey, const std::string
&str_file_name, const FileConfigCallback &file_cfg_cb, std::string *p_str_err_info); //如针对同一<appkey,
// file_name>组合已设置过回调函数，则新旧回调会并存

int8_t GetFileCfg(const std::string &str_appkey, const std::string
&str_file_name, std::string *p_str_file_content, std::string *p_err_info);
//0:成功，-1:失败， 检查err info字符串获取细节


//<--------------------------------拉取动态配置文件------------------------------->
typedef boost::function<void(
        const std::string
        &str_appkey,
        const int64_t
        &i64_version,
        const std::map<std::string, std::string>
        &kv_map
)>
GlobalConfigCallback;  //动态配置文件变化时触发，允许为空，另外注意MCC库的子线程在执行该函数，注意线程安全问题

typedef boost::function<void(
        const std::string
        &str_appkey,
        const std::string
        &str_key,
        const std::string
        &str_new_value,
        const std::string
        &str_old_value
)>
ConfigCallback;  //动态配置文件中str_key对应的value变化时触发，允许为空，另外注意MCC库的子线程在执行该函数，注意线程安全问题

int8_t SetGlobalConfigCallbackFunc(const std::string &str_appkey,
const GlobalConfigCallback &cfg_cb, std::string *p_str_err_info); //如针对同一appkey多次设置回调函数，仅新回调有效

int8_t AddConfigCallbackFunc(const std::string &str_appkey, const std::string &key,
const ConfigCallback &cfg_cb, std::string *p_str_err_info); // 如针对同一<appkey, key>组合设置多条回调函数，所有回调都有效

int8_t SetCfg(const std::string &str_appkey,
              const std::string &str_key,
              const std::string &str_value,
              const std::string &token,
              std::string *p_err_info);

int8_t GetCfg(const std::string &str_appkey,
              const std::string &str_key,
              std::string *p_str_value,
              std::string *p_err_info);//返回配置文件中一个key(str_key)，对应的value内容。

int8_t GetGlobalCfg(const std::string &str_appkey,
                std::map<std::string, std::string> *map_conf,
                std::string *p_err_info);//返回整个配置文件内容，json串。

void DestroyMCCClient(void);//退出时调用
}  // namespace mcc_sdk
#endif  // MCC_SDK_H_
