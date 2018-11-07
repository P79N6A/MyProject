#ifndef HOST_MANAGER_H_
#define HOST_MANAGER_H_

#include <string>
#include <vector>
#include <map>
#include <boost/shared_ptr.hpp>
#include <boost/thread.hpp>
#include <boost/thread/mutex.hpp>
#include <muduo/net/EventLoop.h>
#include <muduo/net/EventLoopThread.h>

#include "thrift_client_handler.h"

//#include <octoidl/sgagent_common_types.h>
//#include <octoidl/sgagent_service_types.h>
//#include <octoidl/sgagent_worker_service_types.h>
#include "gen-cpp/cplugin_sgagent_common_types.h"
#include "controlServer/controlServer_types.h"
#include "controlServer/ControllerService.h"
#include "util/http_client.h"

#include "define/port.h"
#include "define/task_context.h"
#include "util/ops.h"
#include "util/tinyxml2.h"

#include "core_server.h"
#include "host_process.h"
#include <map>
#include "RetryManager.h"
#include "cplugin_def.h"


using namespace std;

namespace cplugin {

typedef std::map<std::string, CPluginNode> StrPluginMap;

class ControlManager;


typedef struct CPlugin_ConfigNode{
    CPlugin_ConfigNode():
    updateSentinelTime_(600),
    operatorTimeout_(5000000),
    downloadFailedKeepTimes_(5),
    downloadFailedIntervalTime_(60*60*2),
    tmp_dir("/opt/meituan/apps/cplugin/res/")
    {}
    int32_t updateSentinelTime_;
    int32_t operatorTimeout_;
    int32_t downloadFailedKeepTimes_;
    int32_t downloadFailedIntervalTime_;
    std::string tmp_dir;
}CPlugin_ConfigNode;

class HostManager {

 public:
    typedef boost::shared_ptr< TaskContext<RequestParams_t, int32_t> > OperatorContextPtr;
    typedef boost::shared_ptr< TaskContext< boost::shared_ptr< SyncRequestBase >, boost::shared_ptr< SyncResponseBase > > > SyncOperatorContextPtr;

    HostManager();
  bool Init();
  bool isNeedExec();
  bool InitCpluginConfig();

  void BackendHandler(OperatorContextPtr context);
  void BackendSyncHandler(SyncOperatorContextPtr context);

  int32_t KeepAlive();
  int32_t Start(const std::string& plugin_name, const int32_t plugin_id, const int32_t task_id) ;
  int32_t ReStart(const std::string& plugin_name, const int32_t plugin_id, const int32_t task_id) ;
  int32_t Stop(const std::string& plugin_name, const int32_t plugin_id, const int32_t task_id) ;
  int32_t Upgrade(const std::string& plugin_name, const std::string& plugin_version, const int32_t plugin_id, const int32_t task_id) ;
  int32_t RollBack(const std::string& plugin_name, const std::string& plugin_version, const int32_t plugin_id, const int32_t task_id) ;
  int32_t StartNew(const std::string& plugin_name, const std::string& plugin_version, const int32_t plugin_id, const int32_t task_id);
  int32_t Remove(const std::string& plugin_name, const int32_t plugin_id, const int32_t task_id);
  int32_t notifyPluginAction(const std::vector<PluginAction> & plugin_list);
  void GetPluginInfos(std::map<std::string, TInfos> & _return) ;
  void GetMonitorInfos(std::map<std::string, std::string> & _return, const std::vector<std::string> & agents);
  int32_t UpdateFile(const std::string& path, const std::string& content, const int32_t plugin_id, const int32_t task_id);

  bool is_stopping() { return is_stoped_; }
  bool is_shortConn(){ return shortConnnection;}
  void ResetPlugin(int pid);
  void StartAll();
  int32_t StopAll();

    void InitControlServerLongConn(const std::string& ip, const int32_t& port);
    void updateControlServerList(vector<cplugin_sgagent::SGService>& vec);
 private:
    int32_t UpdateCPlugin(const std::string& plugin_name, const std::string& plugin_version);
    int32_t UpdateIdc(const std::string& plugin_name, const std::string& plugin_version);
    int32_t RunInBackendHandler(const RequestParams_t& params_t);
    boost::shared_ptr< SyncResponseBase > RunInBackendHandlerSync(const boost::shared_ptr< SyncRequestBase >& request);
    int32_t BackStartHandle(std::string plugin_name);
    int32_t BackStopHandle(std::string plugin_name);
    int32_t BackReStratHandle(std::string plugin_name);
    int32_t BackUpgreadHandle(std::string plugin_name, std::string plugin_version);
    int32_t BackRollBackHandle(std::string plugin_name, std::string plugin_version);
    int32_t BackStartNewHandle(std::string plugin_name, std::string plugin_version);
    int32_t BackRemoveHandle(std::string plugin_name);
    int32_t BackUpdateFileHandle(std::string path, std::string content);
    int32_t BackStopAllHandle(std::string path, std::string content);

    void OnCleanPreAndStart();
    void OnShutDownPreAgent();
    int32_t OnStart(const std::string& plugin_name, CPluginNode& node);
    int32_t OnStop(const std::string& plugin_name, CPluginNode& node);
    int32_t OnStopEx(int push_fd);
    int32_t OnHotUpgrade(const std::string& plugin_name, CPluginNode& node);

  int32_t OnDownloadFromServer(const std::string& version,
                               const std::string& plugin_name,
                               std::vector<PreloadInfo>& preload_infos,
                               HostInfo& host_info,
                               std::vector<PluginInfo>& plugin_infos,
                               std::vector<ConfigInfo>& config_infos,
                               std::string& file_name);

  int32_t OnDownloadDSOFromServer(const string &download_lib_name,
                                  const string &tmp_lib_name,
                            const string &version,
                            const string &md5);

  int32_t OnDownLoadAgentConfig(const string& plugin_name, const string& version, const string& prefix="");

  UPDATE_TYPE  OnCheckUpdateMethod(CPluginNode& node,
                                   std::vector<PreloadInfo>& preload_infos,
                                   HostInfo& host_info,
                                   std::vector<PluginInfo> plugin_infos_,
                                   std::vector<ConfigInfo>& config_infos);

  void RegularCheckPlugin();
  void GetRunningInfo();

  bool FillIdcInfo();
  bool UtilUpateAgentConfig(const string& agent, const string& file_name);
  bool UtilLoadConfig(std::string& config_file,
                      std::vector<PreloadInfo>& preload_infos,
                      HostInfo& host_info,
                      std::vector<PluginInfo>& plugin_infos,
                      std::vector<ConfigInfo>& config_infos) ;

  bool UtilParamsCheck(const string& plugin, const string& version, const int32_t plugin_id,
                       const int32_t task_id, bool isversion);
  void UtilSaveConfig(const string& plugin, const string& version);
  void UtilInsertConfig(const string& plugin, const string& version) ;
  void UtilDeleteConfig(const string& plugin) ;
  std::string UtilGetERRString(const int32_t ERR) ;
  int32_t UtilPartPath(const string& file_name,  string& prefix,  string& lib_name);
  int32_t  UtilPartPath(const string& path,  string& prefix, string& agent,  string& file);
  int32_t  UtilCreateDir(const string& name);
  void  UtilCPFile(const string& src, const string& dst);
  bool UtilEndWith(const char* str, const char* end) ;
  bool UtilCPResource(const string& agent, const string& preix);
  bool UtilCompareFileMD5(const string& fileA, const string& fileB);

  std::string  OnGetOneURLCollector();
  CpluginClientHandler*  OnGetOneControlServerCollector();

  bool OnRemoveNode(const string& plugin);
  bool OnAddNode(const string& plugin,const CPluginNode& node);
  bool OnModifyNode(const string& plugin,const CPluginNode& node);
  bool OnGetNode(const string& plugin, CPluginNode& node);

  void LogGCHandler();
  void PerfTimerHandler();
  void PluginCheckTimerHandler();
  void ZombieKillerHandler();
  void UpdateCpluginInfoTimerHandler();
  void CheckVersionTimerHandler();
  void GetControlServerList();
  std::string FormatGetServerListUrl(const std::string& ip, const int& port);
  void ReportPluginHealth();
  void RetryActionimerHandler();
  void RegularMoniterPlugin();
  void ReportRequestResult(const int32_t plugin_id, const int32_t task_id, const std::string& ret_msg);

 bool CheckFileIntegrityAdapter(const string& agent, const string& version , CPluginNode& node);
 bool CheckFileIntegrity(const string& agent,const string& version, bool download, const string& preix = "");
 bool OnCheckFileIntegrity(const string& file,const  string& md5);
 void OnReportPluginHealth(const std::vector< Controller::PluginHealth >& healthVec);
 void OnRegularMoniterPlugin(const std::vector< std::string >& agentVec);
 std::string OnGetMoniterInfo(const std::string& agent, const CPluginNode& node);


  muduo::net::EventLoop* timer_loop_;
  muduo::net::TimerCallback log_gc_cb_;
  muduo::net::TimerCallback perf_timer_cb_;
  muduo::net::TimerCallback plugin_check_timer_cb_;
  muduo::net::TimerCallback zombie_killer_cb_;
  muduo::net::TimerCallback get_control_list_cb_;
  muduo::net::TimerCallback check_version_cb_;
  muduo::net::TimerCallback update_cp_info_timer_cb_;


  muduo::net::TimerCallback report_plugin_health_cb_;
  muduo::net::TimerCallback retry_action_cb_;
  muduo::net::TimerCallback moniter_action_cb_;
  muduo::net::EventLoopThread timer_thread_;

  bool is_need_exec_;
  bool is_stoped_;


  boost::mutex pluginMap_mutex_;
  StrPluginMap strPluginMap_;

  std::map<std::string, TInfos> strPluginMapInfo_;
  std::map<std::string, std::string> strPluginMonitorInfo_;

  vector<cplugin_sgagent::SGService> vec_control_server_sgservice_;
  int index_for_control_server_;

  vector<cplugin_sgagent::SGService> vec_sential_sgservice_;
  muduo::Timestamp last_get_sential_time_;
  int index_for_sential_server_;


  CpluginClientHandler* pCpluginClientHandler;
  bool  shortConnnection;
  bool  agentDebugMode;

  std::string config_file_;

  CPlugin_ConfigNode configNode_;

  Downloader *downloader_;
  std::string downloader_host_;
  std::string  cpugin_starttime_;
  IDCInfo idcinfo_;

  RetryManager  RetryManager_;
  ControlManager *controlManager_;

  Ops ops_;
};

} // namespace cplugin 

#endif // HOST_MANAGER_H_
