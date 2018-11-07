#ifndef MNS_SDK_H_
#define MNS_SDK_H_

#include "../cthrift_common.h"

namespace mns_sdk {

//获取服务列表回调函数，允许为空，另外注意MNS库的子线程在执行该函数，注意线程安全问题
typedef boost::function<void(const std::vector<SGService> &vec,
							 const std::string& str_svr_appkey)>
    SvrListCallback;

//获取服务列表的回调函数, 三个vector分别代表服务列表中新增的,需要删除的,
//内容需要改变的, 三者允许任意为空. 另外注意是MNS库的子线程在执行这个函数, 注意线程安全问题
typedef boost::function<void(const std::vector<SGService> &vec_add,
                             const std::vector<SGService> &vec_del,
                             const std::vector<SGService> &vec_chg,
							 const std::string& str_svr_appkey)>
    UpdateSvrListCallback;

//启用clog统一日志，初始化日志系统，程序进程调用一次即可


void InitMNS(void);//初始化mns_sdk客户端，仅初始化一次即可。

void InitMNS(const double &sec, const double &timeout = 0.5);//初始化mns_sdk客户端，并设置服务列表拉取间隔(s)，仅初始化一次即可。

//服务在一个进程中可以既调用StartSvr注册服务,又调用StartClient获取服务列表
//同步注册服务, 默认等待时间不超过50ms, 返回非0表示注册失败

//注册服务
int8_t StartSvr(const std::string &str_appkey,
                const int16_t &i16_port,
                const int32_t &i32_svr_type,   //0:thrift, 1:http, 2:other
                const std::string &str_proto_type = "thrift");

int8_t StartSvr(const std::string &str_appkey,
				const std::vector<std::string> &service_name_list,
				const int16_t &i16_port,
				const int32_t &i32_svr_type,   //0:thrift, 1:http, 2:other
				const std::string &str_proto_type); //"thrift", "http"
//thrift/http/cellar...


//返回服务列表，存入用户传入的res_svr_list中
int8_t getSvrList(const std::string &str_svr_appkey,
		  const std::string &str_cli_appkey,
		  const std::string &str_proto_type,
		  const std::string &str_service_name,
		  std::vector<SGService>* p_svr_list);

//获取所有类型服务节点，推荐，支持在一条业务线程中多次绑定不同的str_svr_appkey。
int8_t StartClient(const std::string &str_svr_appkey,
                   const std::string &str_cli_appkey,
                   const std::string &str_proto_type, //client支持的协议类型 thrift/http/cellar..., 定期来取的svrlist将按照这个类型进行过滤
                   const std::string &str_service_name, //IDL文件中的service名字,可按这个名字来过滤服务节点,可填空串返回全部服务节点
                   const SvrListCallback &cb); //异步用回调定期获取服务列表, 返回非0表示参数错误(str_svr_appkey为空等)

int8_t StartClient(const std::string &str_svr_appkey,
                   const std::string &str_cli_appkey,
                   const std::string &str_proto_type, //client支持的协议类型 thrift/http/cellar..., 定期来取的svrlist将按照这个类型进行过滤
                   const std::string &str_service_name, //IDL文件中的service名字,可按这个名字来过滤服务节点,可填空串返回全部服务节点
                   const UpdateSvrListCallback &cb); //异步用回调定期获取服务列表, 返回非0表示参数错误(str_svr_appkey为空等)

int8_t AddSvrListCallback(const std::string &str_svr_appkey,
                          const SvrListCallback &cb,
                          std::string *p_err_info);

int8_t AddUpdateSvrListCallback(const std::string &str_svr_appkey,
				const UpdateSvrListCallback &cb,
				std::string *p_err_info);

void DestroyMNS(void);//退出时调用

int8_t CheckIfSameRegionIDCWithLocalIP(const std::string &str_ip, 
                                       bool *p_b_is_same_region, 
				       bool *p_b_is_same_idc); //将输入IP与本机IP对比，判断是否属于同一region或同一IDC

int8_t CheckIfSameRegionIDCWithTwoIPs(const std::string &str_ip1,
                                      const std::string &str_ip2,
                                      bool *p_b_is_same_region,
                                      bool *p_b_is_same_idc); //将输入的两个IP对比，判断是否属于同一region或同一IDC

int8_t GetLocateInfoByIP(const std::string &str_ip,
                         cthrift::LocateInfo *p_locate_info); //获取一个IP的地理位置信息(地域，机房)


int8_t GetOctoEnv(std::string *p_str_env);  //实时获取最新的OCTO环境
int8_t GetEnv(std::string *p_str_env, bool *p_online);
}
#endif //MNS_SDK_H_

