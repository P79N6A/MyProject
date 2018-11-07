#include "host_manager.h"

#include <dlfcn.h>
#include <stddef.h>
#include <stdio.h>
#include <cstdlib>
#include <stdlib.h>
#include <signal.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/wait.h>
#include <dirent.h>
#include <iostream>
#include <fstream>
#include <string>
#include <cstring>
#include <map>
#include <algorithm>
#include <boost/make_shared.hpp>
#include <sys/poll.h>
#include <glog/logging.h>
#include <muduo/net/EventLoop.h>
#include <rapidjson/document.h>
#include <rapidjson/writer.h>
#include <rapidjson/stringbuffer.h>
#include <transport/TSocket.h>    
#include <transport/TBufferTransports.h>    
#include <protocol/TBinaryProtocol.h>
#include <muduo/base/Timestamp.h>
#include <boost/algorithm/string.hpp>
#include "util/process.h"
#include "controlServerManager.h"

#include "util/sg_agent.h"
#include "util/perf.h"
#include "util/http_client.h"
#include "util/net_util.h"
#include "util/config_util.h"
#include "util/md5_util.h"
#include "util/ipc_message.h"
#include "util/json_util.h"
#include "Core.h"


using namespace std;
using namespace tinyxml2;
using namespace muduo;
using namespace muduo::net;
using namespace rapidjson;
using namespace ::apache::thrift;    
using namespace ::apache::thrift::protocol;    
using namespace ::apache::thrift::transport;    

extern int g_argc;
extern char** g_argv;

#define SAFE_DELETE(p) { if(p) { delete (p); (p)=NULL; } }
#define SAFE_FREE(p) { if(p) { free(p); (p)=NULL; } }
#define SAFE_DELETE_ARRAY(p) { if(p) { delete[] (p); (p)=NULL; } }
#define SAFE_RELEASE(p) { if(p) { (p)->Release(); (p)=NULL; } }

/*注意事项
 1.初期在整体版本升级的时候，是打包所有的小版一块儿升级，在大版本高于当前的版本的时候，
   controlServer不允许升级，小版本升级避免整体版本升级带来的回滚
   在大版本在controlServer确定之后才允许升级
 2.在后续的整体升级的时候，不打包小版本的升级，只升级自己
 3.1和2要有明确的时间划分
*/
namespace cplugin {

extern const char* g_cplugin_version;

static const double kLogGCInterval = 24 * 60 * 60.0; // 1min 
static const double kPerfTimerInterval = 3.0; // 3secs
static const double kPluginCheckTimerInterval = 5.0; // 5secs
static const double kZombieKillerInterval =  5.0; // 5secs
static const double kUpdateCpluginInfoTimerInterval =  60.0; // 1min
static const double kCheckVersionTimerInterval =  30 * 60.0; // 1min
static const double kGetControlServerTimerInterval = 10.0;
static const double kRandomRunInLoopInterval = 60.0;
static const double kReportPluginHealthInterval = 60.0;
static const double kReportPluginMoniterInterval = 60.0;
static const double kRetryActionInterval = 60.0;


static const int32_t SAVE_CHILD_LOG_NUMBER = 5;
static const int32_t POLL_WAIT_TIMEOUT =    20000;  //20ms

const  std::string DOWNLOAD_HOST_ONLINE_  = "cplugin.inf.vip.sankuai.com" ;
const  std::string DOWNLOAD_HOST_OFFLINE_ = "cplugin.inf.test.sankuai.com";
const std::string  CONTROL_SERVER_APPKEY = "com.sankuai.inf.octo.cpluginserver";
const std::string  CONTROL_CPLUGIN_APPKEY = "com.sankuai.octo.inf.cplugin";
const std::string  CPLUGIN_LOG_DIR        = "/var/sankuai/logs/cplugin";
const std::string  CPLUGIN_CONFIG_XML        = "/opt/meituan/apps/cplugin/cplugin.xml";
const std::string  CPLUGIN_CHILD_LOG_CONFIG =   "/opt/meituan/apps/cplugin/log4cplus.conf";
const std::string  SELF_PLUGIN_NAME = "cplugin_idc";
const std::string  CPLUGIN_PLUGIN_NAME = "cplugin";
const int          CPLUGIN_SG_HTTP_PORT = 5267;


HostManager::HostManager() 
    : timer_loop_(NULL),
      log_gc_cb_(boost::bind(&HostManager::LogGCHandler, this)),
      perf_timer_cb_(boost::bind(&HostManager::PerfTimerHandler, this)),
      plugin_check_timer_cb_(boost::bind(&HostManager::PluginCheckTimerHandler, this)),
      zombie_killer_cb_(boost::bind(&HostManager::ZombieKillerHandler, this)),
      get_control_list_cb_(boost::bind(&HostManager::GetControlServerList, this)),
      check_version_cb_(boost::bind(&HostManager::CheckVersionTimerHandler, this)),
      update_cp_info_timer_cb_(boost::bind(&HostManager::UpdateCpluginInfoTimerHandler, this)),
      report_plugin_health_cb_(boost::bind(&HostManager::ReportPluginHealth, this)),
      retry_action_cb_(boost::bind(&HostManager::RetryActionimerHandler, this)),
      moniter_action_cb_(boost::bind(&HostManager::RegularMoniterPlugin, this)),
      is_need_exec_(true),
      is_stoped_(false),
      index_for_control_server_(0),
      last_get_sential_time_(muduo::Timestamp::now()),
      index_for_sential_server_(0),
      pCpluginClientHandler(NULL),
      shortConnnection(false),
      agentDebugMode(false),
      config_file_(CONFIG_FOR_NORMAL),
      downloader_(NULL),
      controlManager_(NULL)
      {
        timer_loop_ = timer_thread_.startLoop();
        downloader_ = new Downloader();
        controlManager_ = new ControlManager(this);

        muduo::Timestamp timeStamp= muduo::Timestamp::now();
        cpugin_starttime_ = timeStamp.toString();
      }


bool  HostManager::InitCpluginConfig(){

  if(idcinfo_.online_){
    downloader_host_ = DOWNLOAD_HOST_ONLINE_;
  }else{
    downloader_host_ = DOWNLOAD_HOST_OFFLINE_;
  }

  downloader_->Init( downloader_host_.c_str(),  "80");

  XMLDocument doc;
  XMLError ret = doc.LoadFile("cplugin.xml");
  if (XML_SUCCESS != ret) {
    LOG(ERROR) << "Load cplugin xml error: " << ret;
    return false;
  }

  XMLElement* root = doc.RootElement();
  if (!root) {
    LOG(ERROR) << "Load cplugin xml error.";
    return false;
  }

  XMLElement* short_conn = NULL;
  short_conn = root->FirstChildElement("ShortConn");

  if (NULL == short_conn) {
    LOG(ERROR) << "Don't have ShortConn in config.";
  }else{
    string conn = short_conn->GetText();
    boost::trim(conn);
    if(conn == "1"){
      shortConnnection = true;
    }else{
      shortConnnection = false;
    }
  }

  XMLElement* debug_mode = NULL;
  debug_mode = root->FirstChildElement("AgentDebugMode");

  if (NULL == debug_mode) {
    LOG(ERROR) << "Don't have AgentDebugMode in config.";
  }else{
    string debug = debug_mode->GetText();
    boost::trim(debug);
    if(debug == "1"){
      agentDebugMode = true;
    }else{
      agentDebugMode = false;
    }
  }

  LOG(ERROR) << " AgentDebugMode . " << agentDebugMode;

  XMLElement* switch_server = NULL;
  switch_server = root->FirstChildElement("SwitchForServer");

  if (NULL == switch_server) {
    LOG(ERROR) << "Don't have SwitchForServer in config.";
  }else{
    string switch_s = switch_server->GetText();
    boost::trim(switch_s);
    if(switch_s == "1"){
      is_need_exec_ = true;
    }else{
      is_need_exec_ = false;
    }
  }


  XMLElement* update_sentinel_time = NULL;
  update_sentinel_time = root->FirstChildElement("SentinelUpdateTime");

  if (NULL == update_sentinel_time) {
    LOG(ERROR) << "Don't have SentinelUpdateTime in config.";
  }else{
    string updateSentinelTime = update_sentinel_time->GetText();
    boost::trim(updateSentinelTime);
    int32_t  temp  = atoi(updateSentinelTime.c_str());
    if(temp > 10*60 || temp < 24*60*60){
      configNode_.updateSentinelTime_ = temp;
    }
  }

  LOG(INFO) << "SentinelUpdateTime : " << configNode_.updateSentinelTime_;

  XMLElement* operator_time_out = NULL;
  operator_time_out = root->FirstChildElement("OperatorTimeOut");

  if (NULL == operator_time_out) {
    LOG(ERROR) << "Don't have OperatorTimeOut in config.";
  }else{
    string operatorTimeOut = operator_time_out->GetText();
    boost::trim(operatorTimeOut);
    int32_t  temp  = atoi(operatorTimeOut.c_str());
    configNode_.operatorTimeout_ = temp;
  }

  LOG(INFO) << "OperatorTimeOut : " << configNode_.operatorTimeout_;

  XMLElement* downloadFailedIntervalTime = NULL;
  downloadFailedIntervalTime = root->FirstChildElement("DownloadFailedIntervalTime");

  if (NULL == downloadFailedIntervalTime) {
    LOG(ERROR) << "Don't have DownloadFailedIntervalTime in config.";
  }else{
    string downloadFailedIntervalTimeStr = downloadFailedIntervalTime->GetText();
    boost::trim(downloadFailedIntervalTimeStr);
    int32_t  temp  = atoi(downloadFailedIntervalTimeStr.c_str());
    if(temp >= 60 || temp <= 24*60*60){
      configNode_.downloadFailedIntervalTime_ = temp;
    }
  }


  XMLElement* downloadFailedKeepTimes = NULL;
  downloadFailedKeepTimes = root->FirstChildElement("DownloadFailedKeepTimes");
  if (NULL == downloadFailedKeepTimes) {
    LOG(ERROR) << "Don't have downloadFailedKeepTimes in config.";
  }else{
    string downloadFailedKeepTimesStr = downloadFailedKeepTimes->GetText();
    boost::trim(downloadFailedKeepTimesStr);
    int32_t  temp  = atoi(downloadFailedKeepTimesStr.c_str());
    if(temp > 1 || temp < 100){
      configNode_.downloadFailedKeepTimes_ = temp;
    }
  }


  LOG(ERROR) << "DownloadFailedIntervalTime_ " << configNode_.downloadFailedIntervalTime_
          << "\r\nDownloadFailedKeepTimes "  << configNode_.downloadFailedKeepTimes_;

  string hostname;
  // Get hostname
  XMLElement* host = NULL;
  if(idcinfo_.online_){
    host = root->FirstChildElement("HostOnLine");
  }else{
    host = root->FirstChildElement("HostOffLine");
  }

  if (NULL == host) {
    LOG(ERROR) << "Don't have Downloader in config use. " << downloader_host_;
    return false;
  }

  downloader_host_ = host->GetText();
  boost::trim(downloader_host_);
  LOG(INFO) << "Downloader host: " << downloader_host_;

  downloader_->Init( downloader_host_.c_str(),  "80");

  return true;
}

bool HostManager::isNeedExec(){
     return is_need_exec_;
}


bool HostManager::Init() {
  LOG(INFO) << "HostManager::Init.  CPlugin version  "  << g_cplugin_version;


  bool appenv_flag = false;
  if(FillIdcInfo()){
      appenv_flag = true;
      LOG(INFO) << "HostManager::FillIdcInfo SUCCESS.";

      if(!InitCpluginConfig()){
         LOG(INFO) << "HostManager::InitCpluginConfig failed  use default.";
      }else{
         LOG(INFO) << "HostManager::config SUCCESS.";
      }

  }else{
      LOG(ERROR) << "HostManager::FillIdcInfo failed.";
  }



  OnCleanPreAndStart();

  timer_loop_->runEvery(kLogGCInterval, log_gc_cb_);
  timer_loop_->runEvery(kPerfTimerInterval, perf_timer_cb_);
  timer_loop_->runEvery(kZombieKillerInterval, zombie_killer_cb_);
  timer_loop_->runEvery(kPluginCheckTimerInterval, plugin_check_timer_cb_);

  if(!appenv_flag){
    do{
      if(FillIdcInfo()){
        LOG(INFO) << "HostManager::FillIdcInfo SUCCESS.";

        if(!InitCpluginConfig()){
          LOG(INFO) << "HostManager::InitCpluginConfig failed.";
        }else{
          LOG(INFO) << "HostManager::config failed use default.";
        }

        break;
      }

      LOG(ERROR) << "HostManager::FillIdcInfo failed wait 3s.";
      sleep(3);
    }while(1);
  }

  //和中控有交互，交互的控制放在函数内部
  timer_loop_->runEvery(kRetryActionInterval, retry_action_cb_);
  timer_loop_->runEvery(kUpdateCpluginInfoTimerInterval, update_cp_info_timer_cb_);
  timer_loop_->runEvery(kCheckVersionTimerInterval, check_version_cb_);
  timer_loop_->runEvery(kGetControlServerTimerInterval, get_control_list_cb_);
  timer_loop_->runEvery(kReportPluginHealthInterval, report_plugin_health_cb_);
  timer_loop_->runEvery(kReportPluginMoniterInterval, moniter_action_cb_);

  ops_.Init();

  return true;
}

    bool HostManager::FillIdcInfo(){
        if(g_cplugin_sgagent.InitCpluginSgagent() != 0){
          LOG(ERROR) << "InitCpluginSgagent failed";
          return false;
        }

        idcinfo_.env_ = g_cplugin_sgagent.str_env_;
        idcinfo_.octo_env_ = g_cplugin_sgagent.str_octo_env_;
        idcinfo_.host_name_ = g_cplugin_sgagent.str_hostname_;
        idcinfo_.local_ip_ = g_cplugin_sgagent.str_local_ip_;
        idcinfo_.host_ = g_cplugin_sgagent.str_host_;
        idcinfo_.os_version_ = GetCentOSVersion();
        idcinfo_.online_ = g_cplugin_sgagent.is_online_;
        idcinfo_.str_sentinel_host_url_ = g_cplugin_sgagent.str_sentinel_host_url_;

        LOG(INFO) << "\nHostManager::FillIdcInfo"
                  << "\nidcinfo_.env_:" << idcinfo_.env_
                  << "\nidcinfo_.octo_env_:" << idcinfo_.octo_env_
                  << "\nidcinfo_.host_name_:" << idcinfo_.host_name_
                  << "\nidcinfo_.local_ip_:" << idcinfo_.local_ip_
                  << "\nidcinfo_.host_:" << idcinfo_.host_
                  << "\nidcinfo_.os_version_:" << idcinfo_.os_version_
                  << "\nidcinfo_.str_sentinel_host_url_:" << idcinfo_.str_sentinel_host_url_
                  << "\nidcinfo_.online_:" << idcinfo_.online_;



      if(        idcinfo_.env_.empty() ||
            idcinfo_.local_ip_.empty() ||
           idcinfo_.host_name_.empty() ){
        LOG(ERROR) << "some necessary condition not content";
        return false;
      }

      return true;
    }

    boost::shared_ptr< SyncResponseBase > HostManager::RunInBackendHandlerSync(const boost::shared_ptr< SyncRequestBase >& request){

      SyncOperatorContextPtr context(
              new TaskContext<boost::shared_ptr<SyncRequestBase>, boost::shared_ptr<SyncResponseBase> >(request));

      size_t pending_tasks_size = timer_loop_->queueSize();
      LOG(INFO) << "task queue size: " << pending_tasks_size;
      if (pending_tasks_size < kMaxPendingTasks) {
        timer_loop_->runInLoop(boost::bind(&HostManager::BackendSyncHandler, this, context));
      } else {
        LOG(ERROR) << "backend thread overload, task queue size: "
                   << pending_tasks_size
                   << " throw request   : ";
        return boost::shared_ptr< SyncResponseBase >(new SyncMonitorResponse_t(request->type_, std::map<std::string, std::string>()));
      }

      //wait 5S
      context->WaitResult(configNode_.operatorTimeout_);
      if (NULL == context->get_response()) {
        LOG(ERROR) << "don't get response in time" ;
        return boost::shared_ptr< SyncResponseBase >(new SyncMonitorResponse_t(request->type_, std::map<std::string, std::string>()));
      }

      return *(context->get_response());

    }

    int32_t HostManager::RunInBackendHandler(const RequestParams_t& params_t){

      //if(!isNeedExec()){
      //    LOG(ERROR) << "backend isNeedExec false ";
      //    return CPLUGIN_ERR_CPLUGIN_PLUGIN_NO_SUPPORT;
      //}

      OperatorContextPtr context(
              new TaskContext<RequestParams_t, int32_t>(params_t));

      size_t pending_tasks_size = timer_loop_->queueSize();
      LOG(INFO) << "task queue size: " << pending_tasks_size;
      if (pending_tasks_size < kMaxPendingTasks) {
        timer_loop_->runInLoop(boost::bind(&HostManager::BackendHandler, this, context));
      } else {
        LOG(ERROR) << "backend thread overload, task queue size: "
                   << pending_tasks_size
                   << " throw request   : " << params_t.ToString();
        return CPLUGIN_ERR_CPLUGIN_PLUGIN_BUSY;
      }

      /*
      context->WaitResult(kOperatorTimeOut);
      if (NULL == context->get_response()) {
        LOG(ERROR) << "don't get response in time, key = " <<  context->get_request()->plugin_name;
        return CPLUGIN_ERR_CPLUGIN_PLUGIN_TIMEOUT;
      }

      return *(context->get_response());
       */
      return CPLUGIN_ERR_OK;
    }

    void HostManager::BackendSyncHandler(SyncOperatorContextPtr context){

    switch ((*(context->get_request()))->type_){
       case CPLUGIN_OPERATION_TYPE_MONITOR:
        {
          SyncMonitorParams_t* request = static_cast<SyncMonitorParams_t*>((*(context->get_request())).get());
          OnRegularMoniterPlugin(request->ags);

          SyncResponseBase* base = new SyncMonitorResponse_t((*(context->get_request()))->type_, strPluginMonitorInfo_);
          boost::shared_ptr< SyncResponseBase >  basePtr(base);
          context->set_response(basePtr);
          break;
         }

       default:
       {
           break;
       }
     }
    }

    void HostManager::BackendHandler(OperatorContextPtr context){

      LOG(INFO) << "\nBackendHandler "
                << " \n plugin_name: " << context->get_request()->plugin_name
                << " \n version: " << context->get_request()->plugin_version
                << " \n type: " << context->get_request()->type
                << " \n plugin_id: " << context->get_request()->plugin_id
                << " \n task_id: " << context->get_request()->task_id;

      int32_t  ret;
      std::string ret_msg;
      switch (context->get_request()->type){
        case CPLUGIN_OPERATION_TYPE_KEEPALIVE: {
          LOG(INFO) << "handle keepalive";
          ReportPluginHealth();
          break;
        }
        case CPLUGIN_OPERATION_TYPE_REMOVE: {

          ret = BackRemoveHandle(context->get_request()->plugin_name);
          if( CPLUGIN_ERR_OK != ret){
            LOG(ERROR) << "BackendHandler CPLUGIN_OPERATION_TYPE_REMOVE ERROR  " << context->get_request()->plugin_name;
          }
          break;
        }
        case CPLUGIN_OPERATION_TYPE_START:{

          ret = BackStartHandle(context->get_request()->plugin_name);
          if( CPLUGIN_ERR_OK != ret ){
            LOG(ERROR) << "BackendHandler CPLUGIN_OPERATION_TYPE_START ERROR  " << context->get_request()->plugin_name;
          }
          break;
        }
        case CPLUGIN_OPERATION_TYPE_STOP:{
          ret = BackStopHandle(context->get_request()->plugin_name);
          if(CPLUGIN_ERR_OK != ret){
            LOG(ERROR) << "BackendHandler CPLUGIN_OPERATION_TYPE_STOP ERROR  " << context->get_request()->plugin_name;
          }
          break;
        }
        case CPLUGIN_OPERATION_TYPE_RESTART:{
          ret = BackReStratHandle(context->get_request()->plugin_name);
          if(CPLUGIN_ERR_OK != ret){
            LOG(ERROR) << "BackendHandler CPLUGIN_OPERATION_TYPE_RESTART ERROR  " << context->get_request()->plugin_name;
          }
          break;
        }
        case CPLUGIN_OPERATION_TYPE_UPGREAD:{
          ret = BackUpgreadHandle(context->get_request()->plugin_name, context->get_request()->plugin_version);
          if(CPLUGIN_ERR_OK != ret){
            LOG(ERROR) << "BackendHandler CPLUGIN_OPERATION_TYPE_UPGREAD ERROR " << context->get_request()->plugin_name;
          }
          break;
        }
        case CPLUGIN_OPERATION_TYPE_ROLLBACK:{
          ret = BackRollBackHandle(context->get_request()->plugin_name, context->get_request()->plugin_version);
          if(CPLUGIN_ERR_OK != ret){
            LOG(ERROR) << "BackendHandler CPLUGIN_OPERATION_TYPE_ROLLBACK ERROR  " << context->get_request()->plugin_name;
          }
          break;
        }
        case CPLUGIN_OPERATION_TYPE_STARTNEW:{
          ret = BackStartNewHandle(context->get_request()->plugin_name, context->get_request()->plugin_version);
          if(CPLUGIN_ERR_OK != ret){
            RetryManager_.AddRetryNode(context->get_request()->plugin_name, context->get_request()->plugin_version);
            LOG(ERROR) << "BackendHandler CPLUGIN_OPERATION_TYPE_STARTNEW ERROR   " << context->get_request()->plugin_name;
          }
          break;
        }
        case CPLUGIN_OPERATION_TYPE_UPDATEFILE:{
          ret = BackUpdateFileHandle(context->get_request()->plugin_name, context->get_request()->plugin_version);
          if(CPLUGIN_ERR_OK != ret){
            LOG(ERROR) << "BackendHandler CPLUGIN_OPERATION_TYPE_UPDATEFILE  ERROR   " << context->get_request()->plugin_name;
          }
          break;
        }
        case CPLUGIN_OPERATION_TYPE_STOPALL:{
          ret = BackStopAllHandle(context->get_request()->plugin_name, context->get_request()->plugin_version);
          if(CPLUGIN_ERR_OK != ret){
            LOG(ERROR) << "BackendHandler CPLUGIN_OPERATION_TYPE_STOPALL  ERROR   " << context->get_request()->plugin_name;
          }

          return ;
        }
        default:
          ret = CPLUGIN_ERR_UNKNOW;
          LOG(ERROR) << "BackendHandler default " << context->get_request()->plugin_name
                     <<  " plugin_id: " << context->get_request()->plugin_id
                     <<  " task_id: " << context->get_request()->task_id;
          break;
      }

      ret_msg = UtilGetERRString(ret);
      ReportRequestResult(context->get_request()->plugin_id,
                          context->get_request()->task_id,
                          ret_msg);
      context->set_response(ret);
    }

    int32_t HostManager::Remove(const std::string& plugin_name, const int32_t plugin_id, const int32_t task_id){
      LOG(INFO) << "HostManager::Remove  : " << plugin_name;

      if(!UtilParamsCheck(plugin_name, "",  plugin_id,  task_id, false)){
        LOG(ERROR) << "UtilParamsCheck error  : "
                  << " name: " << plugin_name
                  << " plugin_id: " << plugin_id
                  << " task_id: " << task_id;
        return CPLUGIN_ERR_CPLUGIN_INVILAD_PARAMS;
      }

      RequestParams_t params_t(plugin_name, "",plugin_id, task_id , CPLUGIN_OPERATION_TYPE_REMOVE);

      return RunInBackendHandler(params_t);
    }

    int32_t HostManager::UpdateFile(const std::string& path, const std::string& content, const int32_t plugin_id, const int32_t task_id) {
      LOG(INFO) << "HostManager::UpdateFile  : " <<  path  <<  "   content :" << content;

      if(!UtilParamsCheck(path, content,  plugin_id,  task_id, false)){
        LOG(ERROR) << "UtilParamsCheck error  : "
                   << " path: " << path
                   << " plugin_id: " << plugin_id
                   << " task_id: " << task_id;
        return CPLUGIN_ERR_CPLUGIN_INVILAD_PARAMS;
      }

      RequestParams_t params_t(path, content, plugin_id, task_id , CPLUGIN_OPERATION_TYPE_UPDATEFILE);

      return RunInBackendHandler(params_t);
    }


    int32_t HostManager::KeepAlive() {
      LOG(INFO) << "HostManager::KeepAlive";
      RequestParams_t params_t("", "", 0, 0, CPLUGIN_OPERATION_TYPE_KEEPALIVE);
      RunInBackendHandler(params_t);
      return 0;
    }

    int32_t HostManager::Start(const std::string& plugin_name, const int32_t plugin_id, const int32_t task_id) {

      LOG(INFO) << "HostManager::Start  : " << plugin_name;

      if(!UtilParamsCheck(plugin_name, "",  plugin_id,  task_id, false)){
        LOG(ERROR) << "UtilParamsCheck error  : "
                  << " name: " << plugin_name
                  << " plugin_id: " << plugin_id
                  << " task_id: " << task_id;
        return CPLUGIN_ERR_CPLUGIN_INVILAD_PARAMS;
      }

      RequestParams_t params_t(plugin_name, "",plugin_id, task_id , CPLUGIN_OPERATION_TYPE_START);

      return RunInBackendHandler(params_t);
    }

    int32_t HostManager::ReStart(const std::string& plugin_name, const int32_t plugin_id, const int32_t task_id) {
      LOG(INFO) << "HostManager::ReStart  : " << plugin_name;

      if(!UtilParamsCheck(plugin_name, "",  plugin_id,  task_id, false)){
        LOG(ERROR) << "UtilParamsCheck error  : "
                   << " name: " << plugin_name
                   << " plugin_id: " << plugin_id
                   << " task_id: " << task_id;

        return CPLUGIN_ERR_CPLUGIN_INVILAD_PARAMS;
      }

      RequestParams_t params_t(plugin_name, "",plugin_id, task_id, CPLUGIN_OPERATION_TYPE_RESTART);
      return RunInBackendHandler(params_t);
    }

    int32_t HostManager::Stop(const std::string& plugin_name, const int32_t plugin_id, const int32_t task_id){
      LOG(INFO) << "HostManager::Stop : " << plugin_name;

      if(!UtilParamsCheck(plugin_name, "",  plugin_id,  task_id, false)){
        LOG(ERROR) << "UtilParamsCheck error  : "
                   << " name: " << plugin_name
                   << " plugin_id: " << plugin_id
                   << " task_id: " << task_id;
        return CPLUGIN_ERR_CPLUGIN_INVILAD_PARAMS;
      }

      RequestParams_t params_t(plugin_name, "",plugin_id, task_id, CPLUGIN_OPERATION_TYPE_STOP);
      return RunInBackendHandler(params_t);
    }

    int32_t HostManager::Upgrade(const std::string& plugin_name, const std::string& plugin_version,
                                 const int32_t plugin_id, const int32_t task_id){
      LOG(INFO) << "HostManager::Upgrade : " << plugin_name;

      if(!UtilParamsCheck(plugin_name, plugin_version,  plugin_id,  task_id, true)){
        LOG(ERROR) << "UtilParamsCheck error  : "
                   << " name: " << plugin_name
                   << " version: " << plugin_version
                   << " plugin_id: " << plugin_id
                   << " task_id: " << task_id;
        return CPLUGIN_ERR_CPLUGIN_INVILAD_PARAMS;
      }

      RequestParams_t params_t(plugin_name, plugin_version, plugin_id, task_id, CPLUGIN_OPERATION_TYPE_UPGREAD);
      return RunInBackendHandler(params_t);
    }

    int32_t HostManager::RollBack(const std::string& plugin_name, const std::string& plugin_version,
                                  const int32_t plugin_id, const int32_t task_id){
      LOG(INFO) << "HostManager::RollBack : " << plugin_name;

      if(!UtilParamsCheck(plugin_name, plugin_version,  plugin_id,  task_id, true)){
        LOG(ERROR) << "UtilParamsCheck error  : "
                   << " name: " << plugin_name
                   << " version: " << plugin_version
                   << " plugin_id: " << plugin_id
                   << " task_id: " << task_id;
        return CPLUGIN_ERR_CPLUGIN_INVILAD_PARAMS;
      }

      RequestParams_t params_t(plugin_name, plugin_version,plugin_id, task_id, CPLUGIN_OPERATION_TYPE_ROLLBACK);
      return RunInBackendHandler(params_t);
    }

    int32_t HostManager::StartNew(const std::string& plugin_name, const std::string& plugin_version,
                                  const int32_t plugin_id, const int32_t task_id){
        LOG(INFO) << "HostManager::StartNew : " << plugin_name;

        if(!UtilParamsCheck(plugin_name, plugin_version,  plugin_id,  task_id, true)){
        LOG(ERROR) << "UtilParamsCheck error  : "
                   << " name: " << plugin_name
                   << " version: " << plugin_version
                   << " plugin_id: " << plugin_id
                   << " task_id: " << task_id;
        return CPLUGIN_ERR_CPLUGIN_INVILAD_PARAMS;
       }

        RequestParams_t params_t(plugin_name, plugin_version,plugin_id, task_id, CPLUGIN_OPERATION_TYPE_STARTNEW);
        return RunInBackendHandler(params_t);
    }

    void HostManager::GetMonitorInfos(std::map<std::string, std::string> & _return, const std::vector<std::string> & agents){

      SyncRequestBase *base = new SyncMonitorParams_t(CPLUGIN_OPERATION_TYPE_MONITOR, agents);
      boost::shared_ptr< SyncRequestBase > params_t(base);

      _return =  (*(static_cast<SyncMonitorResponse_t*>(RunInBackendHandlerSync(params_t).get()))).info;
      return;
    }

    int32_t HostManager::notifyPluginAction(const std::vector<PluginAction> & plugin_list){
      LOG(INFO) << "HostManager::notifyPluginAction ;";

      std::vector<PluginAction>::const_iterator it =  plugin_list.begin();
      for(; it != plugin_list.end(); it++){
        LOG(INFO) << "HostManager::notifyPluginAction "
                  << "\r\n   Name: " << it->name
                  << "\r\n   version: " << it->md5
                  << "\r\n   user version: " << it->version
                  << "\r\n   plugin_id: " << it->plugin_id
                  << "\r\n   task_id: " << it->task_id
                  << "\r\n   op: " << it->op;

        switch(it->op){

          case Operation::INSTALL :{
            RequestParams_t params_t(it->name, it->md5, it->plugin_id, it->task_id, CPLUGIN_OPERATION_TYPE_STARTNEW);
            RunInBackendHandler(params_t);
            break;
          }
          case Operation::START :{
            RequestParams_t params_t(it->name, it->md5, it->plugin_id, it->task_id, CPLUGIN_OPERATION_TYPE_START);
            RunInBackendHandler(params_t);
            break;
          }
          case Operation::STOP :{
            RequestParams_t params_t(it->name, it->md5, it->plugin_id, it->task_id, CPLUGIN_OPERATION_TYPE_STOP);
            RunInBackendHandler(params_t);
            break;
          }
          case Operation::RESTART :{
            RequestParams_t params_t(it->name, it->md5, it->plugin_id, it->task_id, CPLUGIN_OPERATION_TYPE_RESTART);
            RunInBackendHandler(params_t);
            break;
          }
          case Operation::UPGRADE :{
            RequestParams_t params_t(it->name, it->md5, it->plugin_id, it->task_id, CPLUGIN_OPERATION_TYPE_UPGREAD);
            RunInBackendHandler(params_t);
            break;
          }

          case Operation::ROLLBACK :{
            RequestParams_t params_t(it->name, it->md5, it->plugin_id, it->task_id, CPLUGIN_OPERATION_TYPE_ROLLBACK);
            RunInBackendHandler(params_t);
            break;
          }

          case Operation::REMOVE :{
            RequestParams_t params_t(it->name, it->md5, it->plugin_id, it->task_id, CPLUGIN_OPERATION_TYPE_REMOVE);
            RunInBackendHandler(params_t);
            break;
          }
          default:{
            LOG(ERROR) << "HostManager::notifyPluginAction  ERROR"
                      << "\r\n   Name: " << it->name
                      << "\r\n   version: " << it->md5
                      << "\r\n   user version: " << it->version
                      << "\r\n   plugin_id: " << it->plugin_id
                      << "\r\n   task_id: " << it->task_id
                      << "\r\n   op: " << it->op;

            break;
          }
        }

      }

      return CPLUGIN_ERR_OK;
    }

    void HostManager::GetPluginInfos(std::map<std::string, TInfos> & _return) {
      boost::mutex::scoped_lock(pluginMap_mutex_);
      _return = strPluginMapInfo_;

    }

    bool HostManager::UtilParamsCheck(const string& plugin, const string& version, const int32_t plugin_id, const int32_t task_id, bool isversion){

      if(isversion && version.empty()){
        return false;
      }

      if(plugin.empty() || plugin_id < 0 || task_id < 0){
        return false;
      }

      return true;
    }

    void HostManager::UtilSaveConfig(const string& plugin, const string& version){

      XMLDocument doc;

      XMLError ret = doc.LoadFile(config_file_.c_str());
      if (XML_SUCCESS != ret) {
        LOG(ERROR) << "Load xml error: " << ret;
        return ;
      }

      XMLElement* root = doc.RootElement();
      if (!root) {
        return ;
      }

      for( XMLElement*  item = root->FirstChildElement( "Plugin" );
           item;
           item = item->NextSiblingElement( "Plugin" ) ) {
        string plugin_name(item->FirstChildElement("Name")->GetText());
        boost::trim(plugin_name);
        if (plugin_name == plugin) {
          string plugin_version(item->FirstChildElement("Version")->GetText());
          boost::trim(plugin_version);
          item->FirstChildElement("Version")->SetText(version.c_str());
          LOG(INFO) << "UtilSaveConfig: " << plugin_version << " --->" << version;

          XMLError result=doc.SaveFile(config_file_.c_str());
          if(XML_SUCCESS == result){
            LOG(INFO) << plugin << "  save  " <<  version << " result " << result;
          }else{
            LOG(ERROR) << plugin << "  save  " <<  version << " result " << result;
          }

          return;
        }
      }

      root->InsertEndChild(doc.NewElement("Plugin"));
      XMLElement* Target = root->LastChildElement("Plugin");

      XMLElement* Name = doc.NewElement("Name");
      Name->InsertFirstChild(doc.NewText(plugin.c_str()));
      Target->InsertEndChild(Name);

      XMLElement* ver = doc.NewElement("Version");
      ver->InsertFirstChild(doc.NewText(version.c_str()));
      Target->InsertEndChild(ver);

      XMLError result =doc.SaveFile(config_file_.c_str());

      if(XML_SUCCESS == result){
        LOG(INFO) << plugin << "  save  " <<  version << " result " << result;
      }else{
        LOG(ERROR) << plugin << "  save  " <<  version << " result " << result;
      }
    }

    string HostManager::UtilGetERRString(const int32_t ERR){
      string str_err;
      switch(ERR){
        case CPLUGIN_ERR_OK:{
          str_err = "";
          break;
        }
        case CPLUGIN_ERR_DOWNLOADING_ERROR:{
          str_err = "CPLUGIN_ERR_DOWNLOADING_ERROR";
          break;
        }
        case CPLUGIN_ERR_INVALID_UPDATING:{
          str_err = "CPLUGIN_ERR_INVALID_UPDATING";
          break;
        }
        case CPLUGIN_ERR_RUNNING_ERROR:{
          str_err = "CPLUGIN_ERR_RUNNING_ERROR";
          break;
        }
        case CPLUGIN_ERR_SOCKET_ERROR:{
          str_err = "CPLUGIN_ERR_SOCKET_ERROR";
          break;
        }
        case CPLUGIN_ERR_KILLING_ERROR:{
          str_err = "CPLUGIN_ERR_KILLING_ERROR";
          break;
        }
        case CPLUGIN_ERR_INVALID_PID:{
          str_err = "CPLUGIN_ERR_INVALID_PID";
          break;
        }
        case CPLUGIN_ERR_SOCKET_TIMEOUT:{
          str_err = "CPLUGIN_ERR_SOCKET_TIMEOUT";
          break;
        }
        case CPLUGIN_ERR_CPLUGIN_EMPTY_CONFIG:{
          str_err = "CPLUGIN_ERR_CPLUGIN_EMPTY_CONFIG";
          break;
        }
        case CPLUGIN_ERR_CPLUGIN_ERROR_CONFIG:{
          str_err = "CPLUGIN_ERR_CPLUGIN_ERROR_CONFIG";
          break;
        }
        case CPLUGIN_ERR_CPLUGIN_PLUGIN_ALREADY_START:{
          str_err = "CPLUGIN_ERR_CPLUGIN_PLUGIN_ALREADY_START";
          break;
        }
        case CPLUGIN_ERR_CPLUGIN_PLUGIN_ALREADY_VERSION:{
          str_err = "CPLUGIN_ERR_CPLUGIN_PLUGIN_ALREADY_VERSION";
          break;
        }
        case CPLUGIN_ERR_CPLUGIN_PLUGIN_NOT_START:{
          str_err = "CPLUGIN_ERR_CPLUGIN_PLUGIN_NOT_START";
          break;
        }
        case CPLUGIN_ERR_CPLUGIN_PLUGIN_BUSY:{
          str_err = "CPLUGIN_ERR_CPLUGIN_PLUGIN_BUSY";
          break;
        }
        case CPLUGIN_ERR_CPLUGIN_PLUGIN_TIMEOUT:{
          str_err = "CPLUGIN_ERR_CPLUGIN_PLUGIN_TIMEOUT";
          break;
        }
        case CPLUGIN_ERR_CPLUGIN_PLUGIN_CONFIG_ERR:{
          str_err = "CPLUGIN_ERR_CPLUGIN_PLUGIN_CONFIG_ERR";
          break;
        }
        case CPLUGIN_ERR_CPLUGIN_PLUGIN_NO_SUPPORT:{
          str_err = "CPLUGIN_ERR_CPLUGIN_PLUGIN_NO_SUPPORT";
          break;
        }
        case CPLUGIN_ERR_CPLUGIN_PLUGIN_INTERNAL:{
          str_err = "CPLUGIN_ERR_CPLUGIN_PLUGIN_INTERNAL";
          break;
        }
        case CPLUGIN_ERR_CPLUGIN_INVILAD_PARAMS:{
          str_err = "CPLUGIN_ERR_CPLUGIN_INVILAD_PARAMS";
          break;
        }
        case CPLUGIN_ERR_IDC_FILE_FALIED:{
          str_err = "CPLUGIN_ERR_IDC_FILE_FALIED";
          break;
        }
        case CPLUGIN_ERR_UNKNOW:{
          str_err = "CPLUGIN_ERR_UNKNOW";
          break;
        }
        default:{
          str_err = "CPLUGIN_ERR_UNKNOW_OTHER";
          break;
        }
      }

      return str_err;
    }

    void HostManager::UtilDeleteConfig(const string& plugin){

      XMLDocument doc;
      XMLError ret = doc.LoadFile(config_file_.c_str());
      if (XML_SUCCESS != ret) {
        LOG(ERROR) << "Load xml error: " << ret;
        return ;
      }

      XMLElement* root = doc.RootElement();
      if (!root) {
        return ;
      }

      for( XMLElement*  item = root->FirstChildElement( "Plugin" );
           item;
           item = item->NextSiblingElement( "Plugin" ) ) {
        string plugin_name(item->FirstChildElement("Name")->GetText());
        boost::trim(plugin_name);
        if (plugin_name == plugin) {

          root->DeleteChild(item);
          LOG(INFO) << "UtilDeleteConfig: " << plugin_name;
          break;
        }
      }


      int result=doc.SaveFile(config_file_.c_str());
      LOG(INFO) << plugin << "  UtilDeleteConfig  " << result;
    }

    void HostManager::UtilInsertConfig(const string& plugin, const string& version){

      XMLDocument doc;
      XMLError ret = doc.LoadFile(config_file_.c_str());
      if (XML_SUCCESS != ret) {
        LOG(ERROR) << "Load xml error: " << ret;
        return ;
      }

      XMLElement* root = doc.RootElement();
      if (!root) {
        return ;
      }

      for( XMLElement*  item = root->FirstChildElement( "Plugin" );
           item;
           item = item->NextSiblingElement( "Plugin" ) ) {
        string plugin_name(item->FirstChildElement("Name")->GetText());
        boost::trim(plugin_name);
        if (plugin_name == plugin) {
          string plugin_version(item->FirstChildElement("Version")->GetText());
          boost::trim(plugin_version);
          item->FirstChildElement("Version")->SetText(version.c_str());
          LOG(INFO) << "UtilInsertConfig: " << plugin_version << " --->" << version;
          break;
        }
      }

      int result=doc.SaveFile(config_file_.c_str());
      LOG(INFO) << plugin << "  save  " <<  version << " result " << result;
    }

void HostManager::OnCleanPreAndStart()
{
  LOG(INFO) << "HostManager::OnCleanPreAndStart";

  fstream config_file;
  config_file.open(config_file_.c_str(), ios::in);
  if (!config_file) {
    LOG(ERROR) << "Haven't download config";
    return;
  }
  config_file.close();

  XMLDocument doc;
  XMLError ret = doc.LoadFile(config_file_.c_str());
  if (XML_SUCCESS != ret) {
    LOG(ERROR) << "Load xml error: " << ret;
    return ;
  }

  XMLElement* root = doc.RootElement();
  if (!root) {
    return ;
  }

  for( XMLElement*  item = root->FirstChildElement( "Plugin" );
       item;
       item = item->NextSiblingElement( "Plugin" ) )
  {
    string  plugin_name(item->FirstChildElement("Name")->GetText());
    string  plugin_version(item->FirstChildElement("Version")->GetText());

    boost::trim(plugin_name);
    boost::trim(plugin_version);

    CPluginNode node ;
    node.plugin_pid_ = 0;
    node.push_fd_  = -1;
    node.now_version_ = plugin_version;
    node.statu_       = PLUGIN_STATU_ORIGIN;
    node.is_ok_ = false;

    node.last_time_download_failed = muduo::Timestamp::now();
    node.last_download_failed_times = 0;
    node.last_download_failed_inernal = configNode_.downloadFailedIntervalTime_ ;

    node.download_ = true;

    std::string config_file = plugin_name + "_config.xml";

    int32_t  retry_times = 3;
    do{

      if (UtilLoadConfig(config_file, node.preload_infos_, node.host_info_, node.plugin_infos_, node.config_infos_)){
        strPluginMap_.insert(make_pair(plugin_name, node));
        break;
      }

      LOG(ERROR) << "HostManager::Start Config Error failed  " <<  config_file;

      //down config;
      retry_times--;

      if(OnDownLoadAgentConfig(plugin_name, plugin_version) != CPLUGIN_ERR_OK){
        LOG(ERROR) << "OnCleanPreAndStart  download error  " <<  plugin_name << "   " << plugin_version;
        sleep(1);
      }
    }while(retry_times > 0);
  }

  OnShutDownPreAgent();

  StartAll();
}

void HostManager::OnShutDownPreAgent()
{
    StrPluginMap::iterator it = strPluginMap_.begin();
    for(; it != strPluginMap_.end(); it++){

      string agent_name = it->first;
      agent_name = cplugin::trim(agent_name);

      if(!agent_name.empty()){
        GetPidByNameAndKill(agent_name.c_str());
      }
    }


  LOG(INFO) << "OnShutDownPreAgent.";
  const string sgagent_cmd = " svc -d /service/sg_agent";
  const string sgagent_worker_cmd = " svc -d /service/sg_agent_worker";

  getErrorForSystemStatus(system(sgagent_cmd.c_str()));
  getErrorForSystemStatus(system(sgagent_worker_cmd.c_str()));
}

void HostManager::StartAll()
{
  StrPluginMap::iterator it = strPluginMap_.begin();
  for(; it != strPluginMap_.end(); it++){
    int32_t ret = OnStart(it->first, it->second);
    if(ret != CPLUGIN_ERR_OK){
      LOG(ERROR) << "HostManager::StartAll  error :" << it->first
                 << "  version:" << it->second.now_version_
                 << "  err :" << ret;
    }
  }
}

int32_t HostManager::OnStart(const std::string& plugin_name, CPluginNode& node)
{
  node.statu_ = PLUGIN_STATU_RUNING;
  LOG(INFO) << "HostManager::OnStart " << plugin_name;

   if(node.plugin_pid_ != 0) {
     LOG(INFO) << "A process is running, you cannot start the same process twice.";
     return CPLUGIN_ERR_OK;
   }

   bool check_result =  CheckFileIntegrityAdapter(plugin_name,  node.now_version_, node);
   if(!check_result){
      LOG(ERROR) << plugin_name << "  CheckFileIntegrity failed " << check_result ;
      return CPLUGIN_ERR_FILE_INTEGRITY;
   }

  int fds[2];
  int ret = socketpair(AF_UNIX, SOCK_DGRAM, 0, fds);
  if (ret < 0) {
    node.is_ok_ = false;
    LOG(ERROR) << "Socket pair error: " << strerror(errno);
    return CPLUGIN_ERR_SOCKET_ERROR;
  }

  pid_t pid = fork();
  if (0 == pid) {
    // Child process
    //初始化日志库
      CloseParentFd(fds[1]);

      log4cplus::PropertyConfigurator::doConfigure(LOG4CPLUS_TEXT(
            CPLUGIN_CHILD_LOG_CONFIG.c_str()));

    LOG_X_INFO("Start child process, pid: " << getpid());

    HostProcess host_process(fds[1], plugin_name, node.now_version_);
    host_process.Run(g_argc, g_argv);
  } else {
    // Parent process
    close(fds[1]); // Close child fd
    node.plugin_pid_ = pid;
    node.push_fd_ = fds[0];
  }

  int command = START;
  ssize_t n = WriteNBytes(node.push_fd_, &command, sizeof(command));
  if (n < 0) {

    node.is_ok_ = false;
    LOG(ERROR) << "write error.";
    return CPLUGIN_ERR_SOCKET_TIMEOUT;
  }

  LOG(INFO) << "Reading fd is " << node.push_fd_;
  int32_t result = -1;

  // wait 10 seconds at most
  struct pollfd pool_fd;

  pool_fd.fd = node.push_fd_;
  pool_fd.events = POLLIN;
  int val = poll(&pool_fd, 1, POLL_WAIT_TIMEOUT);
  switch (val) {
    case -1:
      node.is_ok_ = false;
      LOG(ERROR) << "read timeout.";
      return CPLUGIN_ERR_SOCKET_TIMEOUT;
    case 0:
      node.is_ok_ = false;
      LOG(ERROR) << "read timeout.";
      return CPLUGIN_ERR_SOCKET_TIMEOUT;
    default:
      n = ReadNBytes(node.push_fd_, &result, sizeof(result));
      break;
  }

  if (n <= 0) {
    node.is_ok_ = false;
    LOG(ERROR) << "read error.";
    return CPLUGIN_ERR_SOCKET_TIMEOUT;
  }

  if (result != 0){
    node.is_ok_ = false;
    LOG(ERROR) << "Start result  error" << result;
    return CPLUGIN_ERR_CPLUGIN_PLUGIN_INTERNAL;
  }

  LOG(INFO) << "Start plugin success  plugin: " << plugin_name
            << "\r\n  version: " << node.now_version_;

  node.is_ok_ = true;
  return CPLUGIN_ERR_OK;
}

    int32_t HostManager::OnHotUpgrade(const std::string& plugin_name, CPluginNode& node)
    {
      if(node.plugin_pid_ == 0) {
        LOG(INFO) << "A process is not running.";
        return CPLUGIN_ERR_CPLUGIN_PLUGIN_NOT_START;
      }

      int command = UPGRADE;
      ssize_t n = WriteNBytes(node.push_fd_, &command, sizeof(command));
      if (n < 0) {
        LOG(ERROR) << "write error.";
        return CPLUGIN_ERR_SOCKET_ERROR;
      }

      LOG(INFO) << "Reading fd is " << node.push_fd_;
      int32_t result = -1;

      // wait 10 seconds at most
      struct pollfd pool_fd;

      pool_fd.fd = node.push_fd_;
      pool_fd.events = POLLIN;
      int val = poll(&pool_fd, 1, POLL_WAIT_TIMEOUT);
      switch (val) {
        case -1:
              LOG(ERROR) << "read timeout.";
              return CPLUGIN_ERR_SOCKET_TIMEOUT;
        case 0:
          node.is_ok_ = false;
              LOG(ERROR) << "read timeout.";
              return CPLUGIN_ERR_SOCKET_TIMEOUT;
        default:
          n = ReadNBytes(node.push_fd_, &result, sizeof(result));
              break;
      }

      if (n <= 0) {
        LOG(ERROR) << "read error.";
        return CPLUGIN_ERR_SOCKET_ERROR;
      }

      LOG(INFO) << "OnHotUpgrade result " << result;
      if (result != 0){
        node.is_ok_ = false;
        LOG(ERROR) << "OnHotUpgrade result  error" << result;
        return CPLUGIN_ERR_INVALID_UPDATING;
      }

      return CPLUGIN_ERR_OK;
    }

int32_t HostManager::OnStopEx(int push_fd){
  int command = STOP;
  ssize_t n = WriteNBytes( push_fd, &command, sizeof(command));
  if (n < 0) {
    return CPLUGIN_ERR_SOCKET_TIMEOUT;
  }

  LOG(INFO) << "Reading fd is " << push_fd;
  int32_t result = -1;

  // wait 10 seconds at most
  struct pollfd pool_fd;

  pool_fd.fd = push_fd;
  pool_fd.events = POLLIN;
  int val = poll(&pool_fd, 1, POLL_WAIT_TIMEOUT);
  switch (val) {
    case -1:
          LOG(ERROR) << "read timeout.";
          return CPLUGIN_ERR_SOCKET_TIMEOUT;
    case 0:
          LOG(ERROR) << "read timeout.";
          return CPLUGIN_ERR_SOCKET_TIMEOUT;
    default:
      n = ReadNBytes(push_fd, &result, sizeof(result));
          break;
  }

  if (n <= 0) {
    LOG(ERROR) << "read error.";
    return CPLUGIN_ERR_SOCKET_TIMEOUT;
  }

  LOG(INFO) << "Stop result " << result;
  if (result != 0) {
    LOG(ERROR) << "Stop result  error" << result;
    return CPLUGIN_ERR_SOCKET_TIMEOUT;
  }

  return CPLUGIN_ERR_OK;
}


int32_t HostManager::OnStop(const std::string& plugin_name, CPluginNode& node)
{
    //TODO Distinguish between the stop from cplugin server  and the cplugin stop
    if(node.statu_ != PLUGIN_STATU_STOPED){
      if (node.plugin_pid_) {

        //STOP
        if(OnStopEx(node.push_fd_) != CPLUGIN_ERR_OK){

          LOG(INFO) << "Send kill signal to process " << node.plugin_pid_;
          int ret = kill(node.plugin_pid_, SIGKILL);
          if (0 != ret) {
            LOG(ERROR) << "Kill process failed, pid: " << node.plugin_pid_
                       << ", errno: " << errno;
            return CPLUGIN_ERR_KILLING_ERROR;
          }
        }

        pid_t pid = waitpid(node.plugin_pid_, NULL, 0);
        if (pid < 0) {
          LOG(ERROR) << "Waitpid error " << errno;
        } else {
          LOG(INFO) << "Get rid of zombie process "<< pid;
        }

        LOG(INFO) << "Reset plugin pid: " << node.plugin_pid_ << " to 0.";
        close(node.push_fd_);
        LOG(INFO) << "Close fd " << node.push_fd_;
        node.push_fd_ = -1;
        node.plugin_pid_ = 0;
        node.is_ok_ = false;
        node.statu_ = PLUGIN_STATU_STOPED;

      } else {
        LOG(INFO) << "The process has been killed or don't existed.";
        //close(node.push_fd_);
        node.push_fd_ = -1;
        node.plugin_pid_ = 0;
        node.is_ok_ = false;
        node.statu_ = PLUGIN_STATU_STOPED;
      }
    }
    return CPLUGIN_ERR_OK;
}

int32_t HostManager::StopAll() {
  if(is_stoped_){
     return CPLUGIN_ERR_OK;
  }

  RequestParams_t params_t(CPLUGIN_PLUGIN_NAME, g_cplugin_version, 1, 1, CPLUGIN_OPERATION_TYPE_STOPALL);

  if( RunInBackendHandler(params_t) == CPLUGIN_ERR_OK){
    is_stoped_ = true;
  }else{
    LOG(ERROR) << "Stop All Faile " ;
    return CPLUGIN_ERR_STOP_FAILED;
  }

  return CPLUGIN_ERR_OK;
}

  void HostManager::ResetPlugin(int pid) {

    LOG(ERROR) << "ResetPlugin begin " << pid;
    boost::mutex::scoped_lock(pluginMap_mutex_);
    LOG(ERROR) << "ResetPlugin lock " << pid;
    StrPluginMap::iterator it = strPluginMap_.begin();
    for(; it != strPluginMap_.end(); it++){
      if(it->second.plugin_pid_ == pid){
        LOG(ERROR) << "Reset plugin pid: " << it->second.plugin_pid_ << " to 0.";
        close(it->second.push_fd_);
        LOG(ERROR) << "Close fd " << it->second.push_fd_;
        it->second.push_fd_ = -1;
        it->second.plugin_pid_ = 0;
        it->second.is_ok_ = false;
      }
    }
    LOG(ERROR) << "ResetPlugin end " << pid;
  }

  bool HostManager::UtilEndWith(const char* str, const char* end) {
      bool result = false;

      if (str != NULL && end != NULL) {
        size_t l1 = strlen(str);
        size_t l2 = strlen(end);
        if (l1 >= l2) {
          if (strcmp(str + l1 - l2, end) == 0) {
            result = true;
          }
        }
      }
      return result;
    }


    void HostManager::LogGCHandler() {
      LOG(INFO) << "HostManager::LogGCHandler.";
      const string log_dir = CPLUGIN_LOG_DIR;

      DIR* dp;
      struct dirent* dirp;

      vector<string> parent_pid_logs;
      vector<string> remove_needed_logs;

      if ((dp = opendir(log_dir.c_str())) == NULL) {
        LOG(ERROR) << "Open log dir error, errno is " << errno;
        return;
      }

      int parent_pid = getpid();
      char parent_pid_str[16] = { 0 };

      sprintf(parent_pid_str, "%d", parent_pid);
      string parent_pid_file_suffix = ".";
      parent_pid_file_suffix.append(parent_pid_str);

      LOG(INFO) << parent_pid_file_suffix;

      while ((dirp = readdir(dp)) != NULL) {

        string d_name(dirp->d_name);

        if ((strcmp(dirp->d_name, ".") == 0) ||
            (strcmp(dirp->d_name, "..") == 0) ||
            (strcmp(dirp->d_name, "cplugin.INFO") == 0) ||
            (d_name.find("cplugin.child") != string::npos)
            ) {
          continue;
        } else if (UtilEndWith(dirp->d_name, parent_pid_file_suffix.c_str())) {
          parent_pid_logs.push_back(dirp->d_name);
        } else {
          remove_needed_logs.push_back(dirp->d_name);
        }
      }

      std::sort(parent_pid_logs.begin(), parent_pid_logs.end());



      if(parent_pid_logs.size() > SAVE_CHILD_LOG_NUMBER){
        remove_needed_logs.insert(remove_needed_logs.end(),
                                  parent_pid_logs.begin(),
                                  parent_pid_logs.begin() + parent_pid_logs.size() - SAVE_CHILD_LOG_NUMBER);
      }

      vector<string>::iterator it;
      for (it = remove_needed_logs.begin(); it != remove_needed_logs.end(); ++it) {
        string full_path;
        full_path.append(log_dir);
        full_path.append("/");
        full_path.append(*it);
        if (remove(full_path.c_str()) != 0) {
          LOG(ERROR) << "Cannot remove file: " << full_path
                    << ", errno is " << errno;
        }
      }
    }


    void HostManager::PerfTimerHandler() {
      Perf::Instance()->KeepUpdate();
    }

    void HostManager::PluginCheckTimerHandler() {

      boost::mutex::scoped_lock(pluginMap_mutex_);
      LOG(INFO) << "PluginCheckTimerHandler.";
      vector< Controller::PluginHealth > PluginHealthVec;

      StrPluginMap::iterator it = strPluginMap_.begin();
      for(; it != strPluginMap_.end(); it++){
        LOG(INFO) << "PluginCheckTimerHandler." << it->first << " status: " << it->second.statu_  << "  pid: " << it->second.plugin_pid_ <<
                "  isok:" << it->second.is_ok_;
        if((it->second.statu_ == PLUGIN_STATU_RUNING) && (!it->second.is_ok_) ){
          LOG(ERROR) << "PluginCheckTimerHandler." << it->first << "  " << it->second.statu_ ;

          Controller::PluginHealth plugin;
          plugin.__set_name( it->first );
          plugin.__set_status( Controller::HealthEnum::Dead );
          PluginHealthVec.push_back(plugin);

          if ( OnStop(it->first, it->second)  != CPLUGIN_ERR_OK){
             LOG(ERROR) << "PluginCheckTimerHandler  OnStop error : " << it->first;
             continue;
          }

          int32_t ret = OnStart(it->first, it->second);
          if(ret != CPLUGIN_ERR_OK){
            LOG(ERROR) << "PluginCheckTimerHandler  error :" << it->first
                       << "  version:" << it->second.now_version_
                       << "  err :" << ret;
          }
        }
      }

      if(PluginHealthVec.size() > 0)
      OnReportPluginHealth(PluginHealthVec);
    }


    void HostManager::ZombieKillerHandler() {
      while(true){
        pid_t pid = -1;
        pid = waitpid(-1, NULL, WNOHANG);
        if (0 == pid) {
           break;
          //LOG(INFO) << "No process need to be killed.";
        } else if (pid > 0) {
          LOG(ERROR) << "Process " << pid << " has been killed.";
          ResetPlugin(pid);
        } else {
          break;
          LOG(ERROR) << "Waitpid() error, errno is " << errno;
        }
      }
    }


    void HostManager::UpdateCpluginInfoTimerHandler() {

      if(!isNeedExec()){
        LOG(ERROR) << "UpdateCpluginInfoTimerHandler  isNeedExec false ";
        return;
      }

      string ip_addr;
      string host_name;
      Controller::Location loca;
      Controller::CPluginInfo inf;

      SysInfo sys_info = Perf::Instance()->GetSysInfo();

      ip_addr.assign(idcinfo_.local_ip_);
      host_name.assign(idcinfo_.host_name_);
      inf.__set_cpu(static_cast<int>(sys_info.cur_process_cpu_rate));
      inf.__set_mem(static_cast<int>(sys_info.mem_ocy));
      inf.__set_ver(g_cplugin_version);
      inf.__set_startTime(cpugin_starttime_);

      muduo::Timestamp timeStamp= muduo::Timestamp::now();
      inf.__set_timestamp(timeStamp.toString());

      LOG(INFO) << "UpdateCpluginInfoTimerHandler  "
                << "\r\n cpu : " << inf.cpu
                << "\r\n mem : " << inf.mem
                << "\r\n ver : " << g_cplugin_version
                << "\r\n start : " << cpugin_starttime_;

      if(shortConnnection){
        LOG(INFO) << "UpdateCpluginInfoTimerHandler  shortConnnection ";
        CpluginClientHandler* pCollector = OnGetOneControlServerCollector();
        if(!pCollector)
        {
          LOG(ERROR) << ("ERR OnGetOneControlServerCollector failed, getOneCollector fail." );
          return ;
        }

        Controller::ControllerServiceClient *control_client = static_cast<Controller::ControllerServiceClient*>(pCollector->getClient());
        if (!control_client) {
          LOG(ERROR) << ("static_cast control_client failed!");
          return ;
        }

        try {
          LOG(INFO)  << "service reportCpluginInfo  ";
          //loca params is drop.
          control_client->reportCpluginInfo( ip_addr, host_name, loca, inf);
        } catch (TException &tx) {
          LOG(ERROR)  << "service reportCpluginInfo error " << tx.what();
          //异常关闭连接
          pCollector -> closeConnection();
          //释放内存
          SAFE_DELETE(pCollector);

          return;
        }

        //使用完成，关闭连接
        pCollector->closeConnection();
        //释放内存
        SAFE_DELETE(pCollector);
      }else{
        LOG(INFO) << "UpdateCpluginInfoTimerHandler  longConnnection ";
        if(!pCpluginClientHandler) {
          LOG(ERROR) << ("ERR OnGetOneControlServerCollector failed, getOneCollector fail." );
          return ;
        }

        Controller::ControllerServiceClient *control_client = static_cast<Controller::ControllerServiceClient*>(pCpluginClientHandler->getClient());
        if(!control_client){
          LOG(ERROR)  << "get empty control Server  ";
          return ;
        }

        try {
          LOG(INFO)  << "service reportCpluginInfo  ";
          control_client->reportCpluginInfo( ip_addr, host_name, loca, inf);
        } catch (TException &tx) {
          LOG(ERROR)  << "service reportCpluginInfo error " << tx.what();
          //异常关闭连接

          pCpluginClientHandler -> closeConnection();
          SAFE_DELETE(pCpluginClientHandler);

          controlManager_->updateForFail();
          return;
        }

      }
}

    bool HostManager::UtilCompareFileMD5(const string& fileA, const string& fileB){

      if ((access(fileB.c_str(), 0)) == -1) {
        LOG(INFO)  << "UtilCompareFileMD5  " << fileB << "  not exist";
        return false;
      }

      if ((access(fileA.c_str(), 0)) == -1) {
        LOG(INFO)  << "UtilCompareFileMD5  " << fileA << "  not exist";
        return false;
      }


      MD5 md5FileA;
      if( md5FileA.FromFile(fileA) != 0){
        LOG(ERROR)  << "OnCheckFileIntegrity  " << fileA << "  gen md5 error";
        return false;
      }

      MD5 md5FileB;
      if( md5FileB.FromFile(fileB) != 0){
        LOG(ERROR)  << "OnCheckFileIntegrity  " << fileB << "  gen md5 error";
        return false;
      }

     return (md5FileB.md5() == md5FileA.md5());
    }

    bool HostManager::OnCheckFileIntegrity(const string& file,const  string& md5){

      if(agentDebugMode){
          return true;
      }

      if ((access(file.c_str(), 0)) == -1) {
        LOG(INFO)  << "OnCheckFileIntegrity  " << file << "  not exist";
        return false;
      }


        MD5 md5File;
        if( md5File.FromFile(file) != 0){
          LOG(ERROR)  << "OnCheckFileIntegrity  " << file << "  gen md5 error";
          return false;
        }

        if(md5File.md5() != md5){
          LOG(ERROR)  << "OnCheckFileIntegrity  " << file << "  mad 5 not match";
          return false;
        }

      return true;
    }

    int32_t  HostManager::UtilCreateDir(const string& name){
      string prefix;
      string lib_name;
      string mkdir_cmd;

      if(UtilPartPath(name, prefix, lib_name) != CPLUGIN_ERR_OK){
        LOG(ERROR) << "UtilPartPath error " << name;
        return CPLUGIN_ERR_PATH_ERROR;
      }

      mkdir_cmd = "mkdir -p  -m 770 " + prefix ;
      if ((access(prefix.c_str(), 0)) == -1) {
        getErrorForSystemStatus(system(mkdir_cmd.c_str()));
      }

      if ((access(prefix.c_str(), 0)) == -1){
        LOG(ERROR) << "mkdir_cmd Error:" << mkdir_cmd;
        return CPLUGIN_ERR_PATH_ERROR;
      }

      return CPLUGIN_ERR_OK;
    }

    void HostManager::UtilCPFile(const string& src, const string& dst){
      string cp_cmd = "cp  " + src + "  " + dst ;

      if(UtilCreateDir(dst) != CPLUGIN_ERR_OK){
        LOG(ERROR) << "UtilCreateDir error " << dst;
        return;
      }

      LOG(INFO) << cp_cmd ;

      getErrorForSystemStatus(system(cp_cmd.c_str()));

      return ;
    }

    bool HostManager::CheckFileIntegrityAdapter(const string& agent, const string& version, CPluginNode& node){

      bool needDownload = node.download_;
      bool result = CheckFileIntegrity(agent, version, needDownload);


      if(result){
         node.last_download_failed_inernal = configNode_.downloadFailedIntervalTime_;
         return true;
      }

      //node.last_time_download_failed = muduo::Timestamp::now();
      //node.last_download_failed_times = 0;

      if(needDownload){

        if(++node.last_download_failed_times > configNode_.downloadFailedKeepTimes_){
          LOG(ERROR) << "change download status from :  " <<  (needDownload? "True":"False")  << " to False"
                  << " last_download_failed_times "   <<  node.last_download_failed_times
                  << " downloadFailedKeepTimes_ "   << configNode_.downloadFailedKeepTimes_;
          node.download_ = false;
        }
      }else{
        if(muduo::timeDifference(muduo::Timestamp::now(), node.last_time_download_failed) > node.last_download_failed_inernal){

            LOG(ERROR) << "change download status from :  " <<  (needDownload? "True":"False")  << " to True"
                       << " now "   <<  muduo::Timestamp::now().microSecondsSinceEpoch()
                       << " last "  << node.last_time_download_failed.microSecondsSinceEpoch()
                       << " diff "  << node.last_download_failed_inernal;

            node.download_ = true;

            node.last_download_failed_times = 0;
            node.last_time_download_failed = muduo::Timestamp::now();
            if(node.last_download_failed_inernal <= 24*60*60){
               node.last_download_failed_inernal *= 2;
            }
        }

      }

      return false;
    }

    bool HostManager::CheckFileIntegrity(const string& agent, const string& version, bool download, const string& preix){

      std::vector<PreloadInfo> preload_infos_;
      HostInfo host_info_;
      std::vector<PluginInfo> plugin_infos_;
      std::vector<ConfigInfo> config_infos_;


      std::string config_file = preix + agent + "_config.xml";


      if (!UtilLoadConfig(config_file, preload_infos_, host_info_, plugin_infos_, config_infos_)){
        LOG(ERROR) << "CheckFileIntegrity Config Error failed  " <<  config_file;

        if(download && OnDownLoadAgentConfig(agent, version, preix) != CPLUGIN_ERR_OK){
          LOG(ERROR) << "CheckFileIntegrity  download error  " <<  agent << "   " << version;
        }
        
        return false;
      }

      bool flag = true;
      std::vector<PreloadInfo>::iterator it_pre = preload_infos_.begin();
      for(; it_pre != preload_infos_.end(); it_pre++){

        if(!OnCheckFileIntegrity( preix + it_pre->library_name, it_pre->hash)){
          LOG(ERROR) << "CheckFileIntegrity  " <<   preix + it_pre->library_name << "  not Integrity";

          if(preix != configNode_.tmp_dir ){
             if(OnCheckFileIntegrity( configNode_.tmp_dir + it_pre->library_name, it_pre->hash)){
               UtilCPFile(configNode_.tmp_dir + it_pre->library_name, it_pre->library_name);
               LOG(INFO) << "tmp dir: " <<configNode_.tmp_dir + it_pre->library_name <<  " FileIntegrity";
               continue;
             }
          }

          if(UtilCreateDir(preix + it_pre->library_name) != CPLUGIN_ERR_OK){
            LOG(ERROR)  << "OnDownloadDSOFromServer  " << preix + it_pre->library_name << "  UtilCreateDir failed";
            flag = false;
            continue;
          }

          if(download){
            string dso;
            int errcode = downloader_->GetDSO(it_pre->library_name, version,idcinfo_.os_version_ ,&dso);
            if (errcode == 0) {
              ofstream preload_file;
              LOG(INFO) << "save so: " <<preix + it_pre->library_name << endl;
              preload_file.open((preix + it_pre->library_name).c_str());
              if(preload_file.is_open()){
                preload_file <<dso;
                preload_file.close();
              }else{
                LOG(ERROR)  << "Open file  " << preix + it_pre->library_name << "  failed";
              }

            } else {
              LOG(ERROR) << "error happens when download " << preix + it_pre->library_name
                         << " errcode: " << errcode;
            }
          }

          flag = false;
        }
      }

      std::vector<PluginInfo>::iterator it_plugin = plugin_infos_.begin();
      for(; it_plugin != plugin_infos_.end(); it_plugin++){

        if(!OnCheckFileIntegrity(preix + it_plugin->library_name, it_plugin->hash)){
          LOG(ERROR) << "CheckFileIntegrity  " <<  preix + it_plugin->library_name << "  not Integrity";


          if(preix != configNode_.tmp_dir ){
            if(OnCheckFileIntegrity( configNode_.tmp_dir + it_plugin->library_name, it_plugin->hash)){
              UtilCPFile(configNode_.tmp_dir + it_plugin->library_name, it_plugin->library_name);
              LOG(INFO) << "tmp dir: " <<configNode_.tmp_dir + it_plugin->library_name <<  " FileIntegrity";
              continue;
            }
          }

          if(UtilCreateDir(preix + it_plugin->library_name) != CPLUGIN_ERR_OK){
            LOG(ERROR)  << "OnDownloadDSOFromServer  " << preix + it_plugin->library_name << "  UtilCreateDir failed";
            flag = false;
            continue;
          }

          if(download){
            string dso;
            int errcode = downloader_->GetDSO(it_plugin->library_name, version,idcinfo_.os_version_ ,&dso);
            if (errcode == 0) {
              ofstream preload_file;
              LOG(INFO) << "save so: " << preix + it_plugin->library_name << endl;
              preload_file.open((preix + it_plugin->library_name).c_str());
              if(preload_file.is_open()){
                preload_file <<dso;
                preload_file.close();
              }else{
                LOG(ERROR)  << "Open file  " << preix + it_plugin->library_name << "  failed";
              }

            } else {
              LOG(ERROR) << "error happens when download " << preix + it_plugin->library_name
                         << " errcode: " << errcode;
            }
          }

          flag = false;
        }

      }

      std::vector<ConfigInfo>::iterator it_config = config_infos_.begin();
      for(; it_config != config_infos_.end(); it_config++){
        if(!OnCheckFileIntegrity(preix + it_config->config_name, it_config->hash)){
          LOG(ERROR) << "CheckFileIntegrity  " <<  it_config->config_name << "  not Integrity";


          if(preix != configNode_.tmp_dir ){
            if(OnCheckFileIntegrity( configNode_.tmp_dir + it_config->config_name, it_config->hash)){
              UtilCPFile(configNode_.tmp_dir + it_config->config_name, it_config->config_name);
              LOG(INFO) << "tmp dir: " <<configNode_.tmp_dir + it_config->config_name <<  " FileIntegrity";
              continue;
            }
          }


          if(UtilCreateDir(preix + it_config->config_name) != CPLUGIN_ERR_OK){
            LOG(ERROR)  << "OnDownloadDSOFromServer  " << preix + it_config->config_name << "  UtilCreateDir failed";
            flag = false;
            continue;
          }

          if(download){
            string dso;
            int errcode = downloader_->GetDSO(it_config->config_name, version,idcinfo_.os_version_ ,&dso);
            if (errcode == 0) {
              ofstream preload_file;
              LOG(INFO) << "save so: " << preix + it_config->config_name << endl;
              preload_file.open((preix + it_config->config_name).c_str());
              if(preload_file.is_open()){
                preload_file <<dso;
                preload_file.close();
              }else{
                LOG(ERROR)  << "Open file  " << preix + it_config->config_name << "  failed";
              }

            } else {
              LOG(ERROR) << "error happens when download " << preix + it_config->config_name
                         << " errcode: " << errcode;
            }
          }

          flag = false;
        }
      }


    if(!OnCheckFileIntegrity(preix + host_info_.library_name, host_info_.hash)){
      LOG(ERROR) << "CheckFileIntegrity  " <<  preix + host_info_.library_name << "  not Integrity";


      if((preix != configNode_.tmp_dir)  && (OnCheckFileIntegrity( configNode_.tmp_dir + host_info_.library_name, host_info_.hash))){

          UtilCPFile(configNode_.tmp_dir + host_info_.library_name, host_info_.library_name);
          LOG(INFO) << "tmp dir: " <<configNode_.tmp_dir + host_info_.library_name <<  " FileIntegrity";

      }else{
        if(UtilCreateDir(preix + host_info_.library_name) != CPLUGIN_ERR_OK){
          LOG(ERROR)  << "OnDownloadDSOFromServer  " << preix + host_info_.library_name << "  UtilCreateDir failed";
          flag = false;
          return flag;
        }

        if(download){
          string dso;
          int errcode = downloader_->GetDSO(host_info_.library_name, version,idcinfo_.os_version_ ,&dso);
          if (errcode == 0) {
            ofstream preload_file;
            LOG(INFO) << "save so: " << preix + host_info_.library_name << endl;
            preload_file.open((preix + host_info_.library_name).c_str());
            if(preload_file.is_open()){
              preload_file <<dso;
              preload_file.close();
            }else{
              LOG(ERROR)  << "Open file  " << preix + host_info_.library_name << "  failed";
            }

          } else {
            LOG(ERROR) << "error happens when download " << preix + host_info_.library_name
                       << " errcode: " << errcode;
          }
        }

        flag = false;
      }
    }

      if((false == flag) && download){
        if(OnDownLoadAgentConfig(agent, version, preix) != CPLUGIN_ERR_OK){
          LOG(ERROR) << "download agent config download" <<  agent << "   " << version;
        }
      }

      return flag;
    }

    bool HostManager::UtilCPResource(const string& agent, const string& preix){

      std::vector<PreloadInfo> preload_infos_;
      HostInfo host_info_;
      std::vector<PluginInfo> plugin_infos_;
      std::vector<ConfigInfo> config_infos_;


      std::string config_file = preix + agent + "_config.xml";


      if (!UtilLoadConfig(config_file, preload_infos_, host_info_, plugin_infos_, config_infos_)){
        LOG(ERROR) << "UtilCPResource Config Error failed  " <<  config_file;
        return false;
      }

      std::vector<PreloadInfo>::iterator it_pre = preload_infos_.begin();
      for(; it_pre != preload_infos_.end(); it_pre++){
        if(!UtilCompareFileMD5(preix + it_pre->library_name, it_pre->library_name)){
            UtilCPFile(preix + it_pre->library_name, it_pre->library_name);
        }
      }

      std::vector<PluginInfo>::iterator it_plugin = plugin_infos_.begin();
      for(; it_plugin != plugin_infos_.end(); it_plugin++){
        if(!UtilCompareFileMD5(preix + it_plugin->library_name, it_plugin->library_name)){
          UtilCPFile(preix + it_plugin->library_name, it_plugin->library_name);
        }
      }

      std::vector<ConfigInfo>::iterator it_config = config_infos_.begin();
      for(; it_config != config_infos_.end(); it_config++){
        if(!UtilCompareFileMD5(preix + it_config->config_name, it_config->config_name)){
          UtilCPFile(preix + it_config->config_name, it_config->config_name);
        }
      }


      if(!UtilCompareFileMD5(preix + host_info_.library_name, host_info_.library_name)){
         UtilCPFile(preix + host_info_.library_name, host_info_.library_name);
      }

      UtilCPFile(config_file, "./" + agent + "_config.xml");
      return true;
    }



    void HostManager::RetryActionimerHandler(){
      LOG(INFO) << "RetryActionimerHandler   begin" ;
      StrRetryNodeMap retryMap = RetryManager_.GetRetryNode();
      StrRetryNodeMap::iterator it = retryMap.begin();
      for(; it != retryMap.end(); it++){
        LOG(INFO) << "RetryActionimerHandler   " <<  it->first << "  " << it->second->getVersion();
        int32_t  ret = BackStartNewHandle(it->first, it->second->getVersion());
        if(CPLUGIN_ERR_OK != ret){
          it->second->UpdateTime();
          LOG(ERROR) << "RetryActionimerHandler   " <<  it->first << "  " << UtilGetERRString(ret);
          continue;
        }

        LOG(INFO) << "RetryActionimerHandler   SUCCESS" << it->first << "  " << it->second->getVersion();
        RetryManager_.DeleteRetryNode(it->first);
      }
      LOG(INFO) << "RetryActionimerHandler   end" ;
    }

    void HostManager::ReportPluginHealth(){

      vector< Controller::PluginHealth > PluginHealthVec;
      boost::mutex::scoped_lock(pluginMap_mutex_);
      LOG(INFO) << "ReportPluginHealth.";
      StrPluginMap::iterator it = strPluginMap_.begin();
      for(; it != strPluginMap_.end(); it++){

        Controller::PluginHealth plugin;
        plugin.__set_name( it->first );

        if(!CheckFileIntegrityAdapter(it->first, it->second.now_version_, it->second)){
            plugin.__set_status(Controller::HealthEnum::InComplete);
            PluginHealthVec.push_back(plugin);
            continue;
        }

        if(it->second.is_ok_){
          plugin.__set_status( Controller::HealthEnum::Alive );
          PluginHealthVec.push_back(plugin);
          continue;
        }

        if(it->second.statu_ == PLUGIN_STATU_RUNING){
          plugin.__set_status( Controller::HealthEnum::Dead );
          PluginHealthVec.push_back(plugin);
          continue;
        }

        plugin.__set_status( Controller::HealthEnum::Stop );
        PluginHealthVec.push_back(plugin);
      }


      OnReportPluginHealth(PluginHealthVec);
    }


    void HostManager::OnReportPluginHealth(const vector< Controller::PluginHealth >& healthVec){

      if(!isNeedExec()){
        LOG(ERROR) << "OnReportPluginHealth  isNeedExec false ";
        return;
      }

      if(shortConnnection) {
        LOG(INFO) << "OnReportPluginHealth  shortConnnection ";
        CpluginClientHandler *pCollector = OnGetOneControlServerCollector();
        if (!pCollector) {
          LOG(ERROR) << ("ERR OnGetOneControlServerCollector failed, getOneCollector fail.");
          return;
        }

        Controller::ControllerServiceClient *control_client = static_cast<Controller::ControllerServiceClient *>(pCollector->getClient());
        if (!control_client) {
          LOG(ERROR) << ("static_cast control_client failed!");
          return;
        }

        try {
          LOG(INFO) << "service OnReportPluginHealth  ";
          control_client->reportHealth(idcinfo_.local_ip_, healthVec);
        } catch (TException &tx) {
          LOG(ERROR) << "service OnReportPluginHealth error " << tx.what();
          //异常关闭连接
          pCollector->closeConnection();
          //释放内存
          SAFE_DELETE(pCollector);

          return;
        }

        //使用完成，关闭连接
        pCollector->closeConnection();
        //释放内存
        SAFE_DELETE(pCollector);
      }else{

        LOG(INFO) << "OnReportPluginHealth  longConnnection ";
        if(!pCpluginClientHandler) {
          LOG(ERROR) << ("ERR OnGetOneControlServerCollector failed, getOneCollector fail." );
          return ;
        }

        Controller::ControllerServiceClient *control_client = static_cast<Controller::ControllerServiceClient*>(pCpluginClientHandler->getClient());
        if(!control_client){
          LOG(ERROR)  << "get empty control Server  ";
          return ;
        }

        try {
          LOG(INFO) << "service OnReportPluginHealth  ";
          control_client->reportHealth(idcinfo_.local_ip_, healthVec);
        } catch (TException &tx) {
          LOG(ERROR)  << "service OnReportPluginHealth error " << tx.what();
          //异常关闭连接

          pCpluginClientHandler -> closeConnection();
          SAFE_DELETE(pCpluginClientHandler);

          controlManager_->updateForFail();
          return;
        }

      }
    }

    CpluginClientHandler* HostManager::OnGetOneControlServerCollector()
    {

      if(vec_control_server_sgservice_.empty() )
      {
        LOG(ERROR) << "empty control Server list: " ;
        return NULL;
      }


      if(vec_control_server_sgservice_.size() - 1 <= index_for_control_server_ )
        index_for_control_server_ = 0;
      else{
        index_for_control_server_++;
      }

      do
      {
        CpluginClientHandler* pCollector = new CpluginClientHandler();
        if(pCollector)
        {
          int ret = pCollector->init(vec_control_server_sgservice_[index_for_control_server_].ip,
                                     vec_control_server_sgservice_[index_for_control_server_].port,
                                     CONTROL_SERVER);
          if((ret == 0) && pCollector->m_transport->isOpen())
          {
            return pCollector;
          } else {
            LOG(ERROR) << "OnGetOneControlServerCollector init failed! index = " << index_for_control_server_
                       << ", ip = " << vec_control_server_sgservice_[index_for_control_server_].ip
                       << ", port = " << vec_control_server_sgservice_[index_for_control_server_].port ;
            SAFE_DELETE(pCollector);
          }
        }

      } while(0);


      LOG(ERROR) << ("_getOneCollector fail!");
      return NULL;
    }

    void HostManager::updateControlServerList(vector<cplugin_sgagent::SGService>& vec){
      vec_control_server_sgservice_ = vec;
    }

    void HostManager::InitControlServerLongConn( const std::string& ip, const int32_t& port){

        LOG(INFO)  << "InitControlServerLongConn   IP: " <<  ip << "   port: " << port;
        if(pCpluginClientHandler){
            //删除原有的连接
            LOG(ERROR)  << "delete origin conn"  ;
            pCpluginClientHandler->closeConnection();
            SAFE_DELETE(pCpluginClientHandler);
            pCpluginClientHandler = NULL;
        }

        if(ip.empty() || port == 0){
          LOG(ERROR)  << "ip empty and port zero"  ;
          return;
        }

        do
        {
            CpluginClientHandler* pCollector = new CpluginClientHandler();
            if(pCollector)
            {
                int ret = pCollector->init(ip, port, CONTROL_SERVER);
                if((ret == 0) && pCollector->m_transport->isOpen())
                {
                    pCpluginClientHandler  = pCollector;
                    return;
                } else {
                    LOG(ERROR)  << "InitControlServerLongConn conn failed, ip = " << ip
                               << ", port = " << port ;
                    SAFE_DELETE(pCollector);
                    controlManager_->updateForFail();

                }
            }

        } while(0);


        LOG(ERROR) << ("InitControlServerLongConn fail!");
        return ;
    }

    void HostManager::CheckVersionTimerHandler() {

      if(!isNeedExec()){
        LOG(ERROR) << "CheckVersionTimerHandler  isNeedExec false ";
        return;
      }

      //LOG(INFO) << "HostManager::PostVersion";
      //GetRunningInfo();

      RegularCheckPlugin();
}

    std::string HostManager::OnGetOneURLCollector() {

      if (vec_sential_sgservice_.size() == 0 ||
              muduo::timeDifference(muduo::Timestamp::now(), last_get_sential_time_) > configNode_.updateSentinelTime_) {

        last_get_sential_time_ = muduo::Timestamp::now();

        string url;
        url.assign(
                idcinfo_.str_sentinel_host_url_ +
                "/api/servicelist?appkey=com.sankuai.inf.sg_sentinel&env="
                + idcinfo_.env_ +
                "&host=" + idcinfo_.host_ + "&hostname="
                + idcinfo_.host_name_ + "&ip="
                + idcinfo_.local_ip_);

        CHttpClient client;
        string response;
        long status;
        if (0 != client.Get(url, &response, &status)) {
          LOG(ERROR) << "Post metric failed.";
        }
        LOG(INFO) << response;

        vector<cplugin_sgagent::SGService> vec_sgservice;
        int8_t ret = CpluginSgagent::ParseSentineSgagentList(response, &vec_sgservice, "com.sankuai.inf.sg_sentinel");
        if(0 == ret){
          vec_sential_sgservice_.clear();
          LOG(INFO)  << "service getsvrlist size " << vec_sgservice.size();
          vector <cplugin_sgagent::SGService>::iterator it = vec_sgservice.begin();
          for(; it != vec_sgservice.end(); it++){
            if((*it).status == 2){
              vec_sential_sgservice_.push_back(*it);
            }
            LOG(INFO)  << CpluginSgagent::SGService2String(*it);
          }
        }
     }

      int size = vec_sential_sgservice_.size();
      if(size <= 0 ){
        LOG(ERROR) << "_getOneCollector fail!" << " empty sential sgagent";
        return "";
      }

      //round robind
      //从头到位的轮询
      if(index_for_sential_server_ < size - 2){
        index_for_sential_server_++;
      }else{
        index_for_sential_server_ = 0;
      }


      return FormatGetServerListUrl(vec_sential_sgservice_[index_for_sential_server_].ip,
                                    CPLUGIN_SG_HTTP_PORT);
    }

std::string HostManager::FormatGetServerListUrl(const std::string& ip, const int& port){
    std::string url;
    url.assign(ip + ":" + boost::lexical_cast<std::string>(port));
    url.append(
            "/api/servicelist?appkey=" + CONTROL_SERVER_APPKEY
            + "&env=" + idcinfo_.env_
            + "&host=" + idcinfo_.host_
            + "&hostname=" + idcinfo_.host_name_
            + "&ip=" + idcinfo_.local_ip_
            + "&protocol=thrift");

    return url;
}


 void HostManager::GetControlServerList(){
   if(!isNeedExec()){
     LOG(ERROR) << "GetControlServerList  isNeedExec false ";
     return;
   }

     //url need to do
     vector <cplugin_sgagent::SGService> vec_sgservice;

     std::string url = FormatGetServerListUrl("127.0.0.1", CPLUGIN_SG_HTTP_PORT);
     std::string response;
     long status;
     CHttpClient httpClient;
     int ret = httpClient.Get(url, &response, &status);
     if(CURLE_OK != ret){
         LOG(ERROR) << "GetControlServerList  httpClient false  from local";

         url = OnGetOneURLCollector();
         if(url.empty()){
             LOG(ERROR) << "GetControlServerList  httpClient false  from mnsc";
             return;
         }

         ret = httpClient.Get(url, &response, &status);
         if(CURLE_OK != ret){
             LOG(ERROR) << "GetControlServerList  httpClient false  from sential  " << url;
             return;
         }
     }

     int8_t ret_code = CpluginSgagent::ParseSentineSgagentList(response, &vec_sgservice, CONTROL_SERVER_APPKEY);
     if(0 == ret_code){
         controlManager_->update(vec_sgservice);
     }

   //LOG(INFO)  << "HostManager::GetControlServerList size:" << vec_control_server_sgservice_.size();
  return;
 }

void HostManager::GetRunningInfo() {

  LOG(INFO) << "HostManager::GetRunningInfo begin.";
  boost::mutex::scoped_lock(pluginMap_mutex_);
  strPluginMapInfo_.clear();
  StrPluginMap::iterator it = strPluginMap_.begin();
  for(; it != strPluginMap_.end(); it++)
  {

    LOG(INFO) << "HostManager::GetRunningInfo.";
    if (it->second.statu_ != PLUGIN_STATU_RUNING && !it->second.is_ok_) {
      LOG(INFO) << "Host manager is no running.";
      continue;
    }

    int err_code = CPLUGIN_ERR_UNKNOW;
    do {
      LOG(INFO) << "Send GetRunning command.";
      int command = GET_RUNNING_INFO;
      ssize_t n = WriteNBytes(it->second.push_fd_, &command, sizeof(command));
      if (n < 0) {
        LOG(ERROR) << "write error.";
        break;
      }

      char buffer[65535]  = {0};
      n = 0;
      struct pollfd pool_fd;

      pool_fd.fd = it->second.push_fd_;
      pool_fd.events = POLLIN;
      int val = poll(&pool_fd, 1, POLL_WAIT_TIMEOUT);
      switch (val) {
        case -1:
        case 0:
              LOG(ERROR) << "read timeout.";
              break;
        default:
          n = read(it->second.push_fd_, buffer, sizeof(buffer));
              break;
      }

      if (n <= 0) {
        LOG(ERROR) << "read error.";
        break;
      }

      TInfos infos;
      boost::shared_ptr<TMemoryBuffer> membuffer(new TMemoryBuffer());
      boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(membuffer));
      membuffer->resetBuffer(reinterpret_cast<uint8_t*>(buffer), n);
      infos.read(protocol.get());

      strPluginMapInfo_.insert(make_pair(it->first, infos));
      err_code = CPLUGIN_ERR_OK;
    } while (0);
  }

  LOG(INFO) << "HostManager::GetRunningInfo end.";
}

    std::string  HostManager::OnGetMoniterInfo(const std::string& agent, const CPluginNode& node) {

     LOG(INFO) << "HostManager::OnRegularMoniterPlugin.";
     if (node.statu_ != PLUGIN_STATU_RUNING || !node.is_ok_) {
       LOG(ERROR) << "Host manager is no running.";
       return Json_Util::getMoniterStr(MONITER_STATUS_STOP, "AGENT_STOP");
     }

     do {
       LOG(INFO) << "Send moniter command.";
       int command = MONITOR;
       ssize_t n = WriteNBytes(node.push_fd_, &command, sizeof(command));
       if (n < 0) {
         LOG(ERROR) << "write error.";
         return Json_Util::getMoniterStr(MONITER_STATUS_TIMEWOUT, "AGENT_TIMEOUT");
       }

       char buffer[65535] = {0};
       n = 0;
       struct pollfd pool_fd;

       pool_fd.fd = node.push_fd_;
       pool_fd.events = POLLIN;
       int val = poll(&pool_fd, 1, POLL_WAIT_TIMEOUT);
       switch (val) {
         case -1:
         case 0:
           LOG(ERROR) << "read timeout.";
               break;
         default:
           n = read(node.push_fd_, buffer, sizeof(buffer));
               break;
       }

       if (n <= 0) {
         LOG(ERROR) << "read error.";
         return Json_Util::getMoniterStr(MONITER_STATUS_INTERNAL_ERROR, "AGENT_INTERNAL_ERROR");
       }

       string temp(buffer, n);


       if ("error" == temp) {
         LOG(ERROR) << "internal error.";
         return Json_Util::getMoniterStr(MONITER_STATUS_INTERNAL_ERROR, "AGENT_INTERNAL_ERROR");
       }

       return temp;

     }while(0);
   }

   void HostManager::OnRegularMoniterPlugin(const std::vector< std::string >& agentVec){
     LOG(INFO) << "HostManager::OnRegularMoniterPlugin begin.";
     boost::mutex::scoped_lock(pluginMap_mutex_);
     strPluginMonitorInfo_.clear();


     if(!agentVec.empty()){
       vector< std::string >::const_iterator it_vec = agentVec.begin();
       for(; it_vec != agentVec.end(); it_vec++){

         StrPluginMap::iterator it = strPluginMap_.find(*it_vec);
         if(it != strPluginMap_.end()) {
           strPluginMonitorInfo_.insert(make_pair(it->first, OnGetMoniterInfo(it->first, it->second)));
         }else{
           strPluginMonitorInfo_.insert(make_pair(*it_vec, Json_Util::getMoniterStr(MONITER_STATUS_NOT_FOUND, "AGENT_NOT_FOUND")));
         }
       }
     }else {
       StrPluginMap::iterator it = strPluginMap_.begin();
       for (; it != strPluginMap_.end(); it++) {
         strPluginMonitorInfo_.insert(make_pair(it->first, OnGetMoniterInfo(it->first, it->second)));
       }
     }
     LOG(INFO) << "HostManager::OnRegularMoniterPlugin end";
   }

    void HostManager::RegularMoniterPlugin() {

      std::vector< std::string > agentVec;
      OnRegularMoniterPlugin(agentVec);
      LOG(INFO) << "HostManager::RegularMoniterPlugin  send.";

      if(shortConnnection){
        LOG(INFO) << "RegularMoniterPlugin  shortConnnection ";
        CpluginClientHandler* pCollector = OnGetOneControlServerCollector();
        if(!pCollector)
        {
          LOG(ERROR) << ("ERR OnGetOneControlServerCollector failed, getOneCollector fail." );
          return ;
        }

        Controller::ControllerServiceClient *control_client = static_cast<Controller::ControllerServiceClient*>(pCollector->getClient());
        if (!control_client) {
          LOG(ERROR) << ("static_cast control_client failed!");
          return ;
        }

        Controller::MoniterResponse  _return;
        try {
          LOG(INFO)  << "service RegularMoniterPlugin  ";

          Controller::MoniterRequest request;
          request.__set_ip_addr(idcinfo_.local_ip_);
          request.__set_agent_info(strPluginMonitorInfo_);

          control_client->reportMoniterInfo(_return, request );

          if(_return.ret != CPLUGIN_ERR_OK){
            LOG(INFO) << "HostManager::RegularMoniterPlugin error: "  << _return.ret;
          }

        } catch (TException &tx) {
          LOG(ERROR)  << "service RegularMoniterPlugin error " << tx.what();
          //异常关闭连接
          pCollector -> closeConnection();
          //释放内存
          SAFE_DELETE(pCollector);

          return;
        }

        //使用完成，关闭连接
        pCollector->closeConnection();
        //释放内存
        SAFE_DELETE(pCollector);
      }else{
        LOG(INFO) << "RegularMoniterPlugin  longConnnection ";
        if(!pCpluginClientHandler) {
          LOG(ERROR) << ("ERR OnGetOneControlServerCollector failed, getOneCollector fail." );
          return ;
        }

        Controller::ControllerServiceClient *control_client = static_cast<Controller::ControllerServiceClient*>(pCpluginClientHandler->getClient());
        if(!control_client){
          LOG(ERROR)  << "get empty control Server  ";
          return ;
        }

        Controller::MoniterResponse  _return;
        try {
          LOG(INFO)  << "service RegularMoniterPlugin  ";

          Controller::MoniterRequest request;
          request.__set_ip_addr(idcinfo_.local_ip_);
          request.__set_agent_info(strPluginMonitorInfo_);

          control_client->reportMoniterInfo(_return, request);

          if(_return.ret != CPLUGIN_ERR_OK){
            LOG(INFO) << "HostManager::RegularMoniterPlugin error: "  << _return.ret;
          }

        } catch (TException &tx) {
          LOG(ERROR)  << "service RegularMoniterPlugin error " << tx.what();
          //异常关闭连接

          pCpluginClientHandler -> closeConnection();
          SAFE_DELETE(pCpluginClientHandler);

          controlManager_->updateForFail();
          return;
        }

      }

      LOG(INFO) << "HostManager::RegularMoniterPlugin  send end.";
    }


    void HostManager::RegularCheckPlugin() {
      LOG(INFO) << "HostManager::RegularCheckPlugin begin.";

      std::vector< Controller::Plugin> request;
      //std::map<std::string, TInfos>::iterator it = strPluginMapInfo_.begin();
      StrPluginMap::iterator it =  strPluginMap_.begin();
      for(; it != strPluginMap_.end(); it++){
        Controller::Plugin plugin;
        plugin.__set_version(it->second.now_version_) ;
        plugin.__set_name(it->first);
        plugin.__set_md5(it->second.now_version_);
        request.push_back(plugin);
      }

      if(shortConnnection){
      LOG(INFO) << "RegularCheckPlugin  shortConnnection ";
      CpluginClientHandler* pCollector = OnGetOneControlServerCollector();
      if(!pCollector)
      {
        LOG(ERROR) << ("ERR OnGetOneControlServerCollector failed, getOneCollector fail." );
        return ;
      }

      Controller::ControllerServiceClient *control_client = static_cast<Controller::ControllerServiceClient*>(pCollector->getClient());
      if (!control_client) {
        LOG(ERROR) << ("static_cast control_client failed!");
        return ;
      }

      std::vector<Controller::Plugin>  _return;
      try {
        LOG(INFO)  << "service reportCpluginInfo  ";

        int32_t  ret = control_client->regularCheckPlugin( idcinfo_.local_ip_, request);

        if(ret != CPLUGIN_ERR_OK){
          LOG(INFO) << "HostManager::RegularCheckPlugin error: "  << ret;
        }

      } catch (TException &tx) {
        LOG(ERROR)  << "service reportCpluginInfo error " << tx.what();
        //异常关闭连接
        pCollector -> closeConnection();
        //释放内存
        SAFE_DELETE(pCollector);

        return;
      }

      //使用完成，关闭连接
      pCollector->closeConnection();
      //释放内存
      SAFE_DELETE(pCollector);
      }else{
        LOG(INFO) << "RegularCheckPlugin  longConnnection ";
        if(!pCpluginClientHandler) {
          LOG(ERROR) << ("ERR OnGetOneControlServerCollector failed, getOneCollector fail." );
          return ;
        }

        Controller::ControllerServiceClient *control_client = static_cast<Controller::ControllerServiceClient*>(pCpluginClientHandler->getClient());
        if(!control_client){
          LOG(ERROR)  << "get empty control Server  ";
          return ;
        }

        try {
          LOG(INFO)  << "service RegularCheckPlugin  ";
          int32_t  ret = control_client->regularCheckPlugin( idcinfo_.local_ip_, request);

          if(ret != CPLUGIN_ERR_OK){
            LOG(INFO) << "HostManager::RegularCheckPlugin error: "  << ret;
          }

        } catch (TException &tx) {
          LOG(ERROR)  << "service RegularCheckPlugin error " << tx.what();
          //异常关闭连接

          pCpluginClientHandler -> closeConnection();
          SAFE_DELETE(pCpluginClientHandler);

          controlManager_->updateForFail();
          return;
        }

      }

      /*
      if(_return.size() > 0){
        LOG(INFO)  << "return update some plugin " ;
        std::vector<Controller::Plugin>::iterator it = _return.begin();
        for(; it != _return.end(); it++){

          srand((int)time(0));
          double after_time =(double)(rand() % int(kRandomRunInLoopInterval));

          LOG(INFO)  << "plugin: " <<it->name
                     << " version: " <<  it->md5
                     << " after time to update: " << after_time;

          timer_loop_->runAfter(after_time, boost::bind(&HostManager::BackUpgreadHandle, this, it->name, it->md5));
        }
      }
     */
      LOG(INFO) << "HostManager::RegularCheckPlugin end.";
      return ;
    }

    void HostManager::ReportRequestResult( const int32_t plugin_id, const int32_t task_id, const string& ret_msg ) {
      LOG(INFO) << "HostManager::ReportRequestResult begin.";

      if(shortConnnection){
        LOG(INFO) << "ReportRequestResult  shortConnnection ";
        CpluginClientHandler* pCollector = OnGetOneControlServerCollector();
        if(!pCollector)
        {
          LOG(ERROR) << ("ERR OnGetOneControlServerCollector failed, getOneCollector fail." );
          return ;
        }

        Controller::ControllerServiceClient *control_client = static_cast<Controller::ControllerServiceClient*>(pCollector->getClient());
        if (!control_client) {
          LOG(ERROR) << ("static_cast control_client failed!");
          return ;
        }

        try {
          int32_t ret = control_client->reportVersion(idcinfo_.local_ip_, plugin_id, task_id, ret_msg);
          LOG(INFO)  << "service ReportRequestResult  :" << ret;
        } catch (TException &tx) {
          LOG(ERROR)  << "service reportCpluginInfo error " << tx.what();
          //异常关闭连接
          pCollector -> closeConnection();
          //释放内存
          SAFE_DELETE(pCollector);

          return;
        }

        //使用完成，关闭连接
        pCollector->closeConnection();
        //释放内存
        SAFE_DELETE(pCollector);

        LOG(INFO) << "HostManager::ReportRequestResult end.";
        return ;
      }else{
        LOG(INFO) << "ReportRequestResult  longConnnection ";
        if(!pCpluginClientHandler) {
          LOG(ERROR) << ("ERR OnGetOneControlServerCollector failed, getOneCollector fail." );
          return ;
        }

        Controller::ControllerServiceClient *control_client = static_cast<Controller::ControllerServiceClient*>(pCpluginClientHandler->getClient());
        if(!control_client){
          LOG(ERROR)  << "get empty control Server  ";
          return ;
        }

        try {
          int32_t ret = control_client->reportVersion(idcinfo_.local_ip_, plugin_id, task_id, ret_msg);
          LOG(INFO)  << "service ReportRequestResult  :" << ret;
        } catch (TException &tx) {
          LOG(ERROR)  << "service ReportRequestResult error " << tx.what();
          //异常关闭连接

          pCpluginClientHandler -> closeConnection();
          SAFE_DELETE(pCpluginClientHandler);

          controlManager_->updateForFail();
          return;
        }
      }
    }

    int32_t HostManager::UtilPartPath(const string& path,  string& prefix, string& agent,  string& file) {
      std::size_t  pos = path.find_last_of("/");
      if(pos == std::string::npos){
        return CPLUGIN_ERR_UNKNOW;
      }else{
        prefix = path.substr(0, pos );
        file =   path.substr(pos + 1);
      }

      pos = prefix.find_last_of("/");
      if(pos == std::string::npos){
        return CPLUGIN_ERR_UNKNOW;
      }else{
        agent =   prefix.substr(pos + 1);
        prefix = prefix.substr(0, pos );
      }

      return CPLUGIN_ERR_OK;
    }

int32_t HostManager::UtilPartPath(const string& file_name,  string& prefix,  string& lib_name) {
        std::size_t  pos = file_name.find_last_of("/");
        if(pos == std::string::npos){
            return CPLUGIN_ERR_UNKNOW;
        }else{
            prefix = file_name.substr(0, pos + 1);
            lib_name = file_name.substr(pos + 1);
            return CPLUGIN_ERR_OK;
        }
}


int32_t HostManager::OnDownLoadAgentConfig(const string& plugin_name, const string& version , const string& prefix){
      string config;
      int errcode = downloader_->GetConfig(&config, version, plugin_name, idcinfo_.os_version_);
      if (errcode != 0) {
        LOG(ERROR) << "download xml error: " << errcode;
        return CPLUGIN_ERR_DOWNLOADING_ERROR;
      }

      string file_name = prefix + plugin_name +"_config.xml";
      ofstream config_file;
      LOG(INFO) << "save file: " << file_name << endl;
      config_file.open(file_name.c_str(), ios::out );
      config_file << config ;
      config_file.close();

      return CPLUGIN_ERR_OK;
}

int32_t HostManager::OnDownloadDSOFromServer(const string &download_lib_name,
                                             const string &tmp_lib_name,
                                             const string &version,
                                             const string &md5){
  int errcode;

  if ((access(tmp_lib_name.c_str(), 0)) != -1) {

    MD5 md5File;
    if( md5File.FromFile(tmp_lib_name) != 0){
      LOG(ERROR)  << "OnDownloadDSOFromServer  " << tmp_lib_name << "  gen md5 error";
      return CPLUGIN_ERR_DOWNLOADING_ERROR;
    }

    if(md5File.md5() == md5){
      LOG(INFO) << "file " << tmp_lib_name << " existed and md5 is equal";
      return CPLUGIN_ERR_OK;
    }
  }

  if(UtilCreateDir(tmp_lib_name) != CPLUGIN_ERR_OK){
    LOG(ERROR)  << "OnDownloadDSOFromServer  " << tmp_lib_name << "  UtilCreateDir failed";
    return CPLUGIN_ERR_PATH_ERROR;
  }

  LOG(INFO) << "download so: " << download_lib_name << endl;
  string dso;
  errcode = downloader_->GetDSO(download_lib_name, version,idcinfo_.os_version_ ,&dso);
  if (errcode == 0) {
    ofstream preload_file;
    LOG(INFO) << "save so: " << tmp_lib_name << endl;
    preload_file.open(tmp_lib_name.c_str());
    preload_file <<dso;
    preload_file.close();
  } else {
    LOG(ERROR) << "error happens when download " << download_lib_name
               << " errcode: " << errcode;
    return CPLUGIN_ERR_DOWNLOADING_ERROR;
  }

  return CPLUGIN_ERR_OK;
}


int32_t HostManager::OnDownloadFromServer(const std::string& version,const std::string& plugin_name,
                                          std::vector<PreloadInfo>& preload_infos,
                                        HostInfo& host_info, std::vector<PluginInfo>& plugin_infos,
                                          std::vector<ConfigInfo>& config_infos,
                                           std::string& file_name) {
  string config;
  int errcode = downloader_->GetConfig(&config, version, plugin_name, idcinfo_.os_version_);
  if (errcode != 0) {
    LOG(ERROR) << "download xml error: " << errcode;
    return CPLUGIN_ERR_DOWNLOADING_ERROR;
  }

  XMLDocument doc;
  int ret = doc.Parse(config.c_str());
  if (0 != ret) {
    LOG(ERROR) << "Load xml error: " << ret;
    return CPLUGIN_ERR_UNKNOW;
  }

  XMLElement* root = doc.RootElement();
  if (!root) {
    return CPLUGIN_ERR_UNKNOW;
  }

  // Download preload so
  XMLElement* preload_host = root->FirstChildElement("Preload");
  if (preload_host && !ParsePreload(preload_host, &preload_infos)) {
    return CPLUGIN_ERR_UNKNOW;
  }

  vector<PreloadInfo>::const_iterator it;
  for (it = preload_infos.begin(); it != preload_infos.end(); ++it) {
    const string& preload_lib_name =   (*it).library_name;
    const string tmp_lib_name = configNode_.tmp_dir  + (*it).library_name;
    string  preload_lib_name_md5 =     (*it).hash;

    if(OnDownloadDSOFromServer(preload_lib_name, tmp_lib_name, version, preload_lib_name_md5) == CPLUGIN_ERR_OK){
       continue;
    }else{
      LOG(ERROR) << "download dso error: " << preload_lib_name;
      return CPLUGIN_ERR_DOWNLOADING_ERROR;
    }
  }

  // Download host so 
  XMLElement* host_root = root->FirstChildElement("Host");
  if (host_root && !ParseHost(host_root, &host_info)) {
    return CPLUGIN_ERR_UNKNOW;
  }
  
  const string& host_lib_name = host_info.library_name;
  const string tmp_host_name = configNode_.tmp_dir  + host_info.library_name;
  string  host_lib_name_md5 =     host_info.hash;

  if(OnDownloadDSOFromServer( host_lib_name, tmp_host_name, version, host_lib_name_md5) != CPLUGIN_ERR_OK){
    LOG(ERROR) << "download dso error: " << host_lib_name;
    return CPLUGIN_ERR_DOWNLOADING_ERROR;
  }

  // Download plugin so 
  XMLElement* plugin_root = root->FirstChildElement("Plugin");
  if (plugin_root && !ParsePlugin(plugin_root, &plugin_infos)) {
    return CPLUGIN_ERR_UNKNOW;
  }

  vector<PluginInfo>::const_iterator plugin_it;
  for (plugin_it = plugin_infos.begin(); plugin_it != plugin_infos.end(); ++plugin_it) {
    const string& plugin_lib_name = (*plugin_it).library_name;
    const string tmp_plugin_name = configNode_.tmp_dir  + (*plugin_it).library_name;

    string  plugin_lib_name_md5 =     (*plugin_it).hash;

    if(OnDownloadDSOFromServer( plugin_lib_name, tmp_plugin_name, version, plugin_lib_name_md5) == CPLUGIN_ERR_OK){
      continue;
    }else{
      LOG(ERROR) << "download dso error: " << plugin_lib_name;
      return CPLUGIN_ERR_DOWNLOADING_ERROR;
    }
  }

  // Download config
  XMLElement* config_root = root->FirstChildElement("Config");
  if (config_root && !ParseConfig(config_root, &config_infos)) {
    return CPLUGIN_ERR_UNKNOW;
  }

  vector<ConfigInfo>::const_iterator config_it;
  for (config_it = config_infos.begin(); config_it != config_infos.end(); ++config_it) {
    const string& config_name = (*config_it).config_name;
    const string tmp_config_name = configNode_.tmp_dir  + (*config_it).config_name;
    string  config_name_md5 =     (*config_it).hash;

    if(OnDownloadDSOFromServer(config_name, tmp_config_name, version, config_name_md5) == CPLUGIN_ERR_OK){
      continue;
    }else{
      LOG(ERROR) << "download dso error: " << config_name;
      return CPLUGIN_ERR_DOWNLOADING_ERROR;
    }
  }

  file_name = configNode_.tmp_dir  + "/" + plugin_name +"_config.xml";
  ofstream config_file;
  LOG(INFO) << "save file: " << file_name << endl;
  config_file.open(file_name.c_str(), ios::out );
  config_file << config ;
  config_file.close();

  return CPLUGIN_ERR_OK;
}


    bool HostManager::OnRemoveNode(const string& plugin){
        boost::mutex::scoped_lock(pluginMap_mutex_);

        StrPluginMap::iterator it = strPluginMap_.find(plugin);
        if(it == strPluginMap_.end())
        {
            LOG(ERROR) << plugin << "have no.";
            return false;
        }

        strPluginMap_.erase(it);
        return true;
    }

    bool HostManager::OnAddNode(const string& plugin,const CPluginNode& node){
        boost::mutex::scoped_lock(pluginMap_mutex_);

        StrPluginMap::iterator it = strPluginMap_.find(plugin);
        if(it != strPluginMap_.end())
        {
            LOG(ERROR) << plugin << "already have .";
            return false;
        }

        strPluginMap_.insert(make_pair(plugin, node));
        return true;
    }

    bool HostManager::OnModifyNode(const string& plugin,const CPluginNode& node){
        boost::mutex::scoped_lock(pluginMap_mutex_);

        StrPluginMap::iterator it = strPluginMap_.find(plugin);
        if(it == strPluginMap_.end())
        {
            LOG(ERROR) << plugin << " have no.";
            return false;
        }

        strPluginMap_[plugin] =  node;
        return true;
    }

    bool HostManager::OnGetNode(const string& plugin, CPluginNode& node){
        boost::mutex::scoped_lock(pluginMap_mutex_);

        StrPluginMap::iterator it = strPluginMap_.find(plugin);
        if(it == strPluginMap_.end())
        {
            LOG(ERROR) << plugin << " have no.";
            return false;
        }

        node = strPluginMap_[plugin];
        return true;

    }


    int32_t HostManager::BackRemoveHandle(std::string plugin_name) {
      LOG(INFO) << "HostManager::BackRemoveHandle";

      CPluginNode node;
      bool ret =  OnGetNode(plugin_name, node);
      if(!ret){
        LOG(ERROR) << plugin_name << "have no run.";
        return CPLUGIN_ERR_CPLUGIN_PLUGIN_NOT_START;
      }

      if( OnStop(plugin_name, node)  !=  CPLUGIN_ERR_OK ){
        LOG(ERROR) << "BackRemoveHandle OnStop : " << plugin_name << " failed.";
        return CPLUGIN_ERR_KILLING_ERROR;
      }

      OnRemoveNode(plugin_name);
      UtilDeleteConfig(plugin_name);
      return CPLUGIN_ERR_OK;
    }

    int32_t HostManager::BackStartHandle(std::string plugin_name) {
      LOG(INFO) << "HostManager::BackStartHandle";

      CPluginNode node;
      bool ret =  OnGetNode(plugin_name, node);
      if(!ret){
        LOG(ERROR) << plugin_name << " have no in map";
        return CPLUGIN_ERR_CPLUGIN_PLUGIN_NOT_START;
      }

      if(node.statu_ == PLUGIN_STATU_STOPED){
        int32_t ret = OnStart(plugin_name, node);
        if(ret != CPLUGIN_ERR_OK){
          LOG(ERROR) << "HostManager::BackStartHandle  error :" << plugin_name
                     << "  version:" << node.now_version_
                     << "  err :" << ret;
          return ret;
        }
        OnModifyNode(plugin_name, node);
      }

      return CPLUGIN_ERR_OK;
    }

    int32_t HostManager::BackStopHandle(std::string plugin_name){
      LOG(INFO) << "HostManager::BackStopHandle";

      CPluginNode node;
      bool ret =  OnGetNode(plugin_name, node);
      if(!ret){
        LOG(ERROR) << plugin_name << "have no run.";
        return CPLUGIN_ERR_CPLUGIN_PLUGIN_NOT_START;
      }

      if( OnStop(plugin_name, node)  !=  CPLUGIN_ERR_OK ){
        LOG(ERROR) << "BackStopHandle OnStop : " << plugin_name << " failed.";
        return CPLUGIN_ERR_KILLING_ERROR;
      }

      OnModifyNode(plugin_name, node);
      return CPLUGIN_ERR_OK;
    }

    int32_t HostManager::BackReStratHandle(std::string plugin_name){
      LOG(INFO) << "HostManager::BackReStratHandle";

      CPluginNode node;
      bool ret =  OnGetNode(plugin_name, node);
      if(!ret){
        LOG(ERROR) << plugin_name << "have no in map .";
        return CPLUGIN_ERR_CPLUGIN_PLUGIN_NOT_START;
      }

      if( OnStop(plugin_name, node)  !=  CPLUGIN_ERR_OK ){
        LOG(ERROR) << "BackReStratHandle OnStop : " << plugin_name << " failed.";
        return CPLUGIN_ERR_KILLING_ERROR;
      }

      int32_t ret_t = OnStart(plugin_name, node);

      OnModifyNode(plugin_name, node);

      if(ret_t != CPLUGIN_ERR_OK){
        LOG(ERROR) << "HostManager::BackReStratHandle  error :" << plugin_name
                   << "  version:" << node.now_version_
                   << "  err :" << ret_t;
        return ret_t;
      }

      return CPLUGIN_ERR_OK;
    }

    int32_t HostManager::UpdateIdc(const std::string& plugin_name, const std::string& plugin_version) {
      string config;
      int errcode = downloader_->GetConfig(&config, plugin_version, plugin_name, idcinfo_.os_version_, "idc.xml");
      if (errcode != 0) {
        LOG(ERROR) << "download idc xml error: " << errcode;
        return CPLUGIN_ERR_DOWNLOADING_ERROR;
      }

      ofstream out_stream;
      try {
        out_stream.open(CpluginSgagent::kStrIDCRegionInfoFileFullPath.c_str(), ios::trunc);
      } catch (const ofstream::failure &e) {
        LOG(ERROR) << "Exception in opening local idc config file.";
        return CPLUGIN_ERR_IDC_FILE_FALIED;
      }

      if (out_stream.is_open()) {
        out_stream << config;
      }

      if (out_stream.bad()) {
        LOG(ERROR) << "Write local idc config file failed.";
        out_stream.close();
        return CPLUGIN_ERR_IDC_FILE_FALIED;
      }
      out_stream.close();
      //update self idc info
      if (!FillIdcInfo()) {
        return CPLUGIN_ERR_IDC_FILE_FALIED;
      }

      if(!UtilUpateAgentConfig("sg_agent", CpluginSgagent::kStrIDCRegionInfoFileFullPath)){
        LOG(ERROR) << "Write UtilUpateAgentConfig failed.";
      }

        InitCpluginConfig();

      return CPLUGIN_ERR_OK;
    }


    int32_t HostManager::UpdateCPlugin(const std::string& plugin_name, const std::string& plugin_version) {
      string config;
      int errcode = downloader_->GetConfig(&config, plugin_version, plugin_name, idcinfo_.os_version_, "cplugin.xml");
      if (errcode != 0) {
        LOG(ERROR) << "download cplugin xml error: " << errcode;
        return CPLUGIN_ERR_DOWNLOADING_ERROR;
      }

      ofstream out_stream;
      try {
        out_stream.open(CPLUGIN_CONFIG_XML.c_str(), ios::trunc);
      } catch (const ofstream::failure &e) {
        LOG(ERROR) << "Exception in opening local config file.";
        return CPLUGIN_ERR_CPLUGIN_ERROR_CONFIG;
      }

      if (out_stream.is_open()) {
        out_stream << config;
      }

      if (out_stream.bad()) {
        LOG(ERROR) << "Write local  config file failed.";
        out_stream.close();
        return CPLUGIN_ERR_CPLUGIN_ERROR_CONFIG;
      }

      out_stream.close();

        InitCpluginConfig();

      return CPLUGIN_ERR_OK;
    }

    int32_t HostManager::BackUpgreadHandle(std::string plugin_name, std::string plugin_version){
      LOG(INFO) << "HostManager::BackUpgreadHandle" ;
      if (SELF_PLUGIN_NAME == plugin_name) {
        return UpdateIdc(plugin_name, plugin_version);
      }

      if (CPLUGIN_PLUGIN_NAME == plugin_name) {
        return UpdateCPlugin(plugin_name, plugin_version);
      }

      CPluginNode node;
      bool result =  OnGetNode(plugin_name, node);
      if(!result){
        LOG(ERROR) << plugin_name << "have no run.";
        return CPLUGIN_ERR_CPLUGIN_PLUGIN_NOT_START;
      }

      if(node.now_version_ == plugin_version)
      {
        LOG(ERROR) << plugin_name << " already this version. " <<  plugin_version;
        return CPLUGIN_ERR_OK;
      }

      vector<PreloadInfo> t_preload_infos;
      vector<PluginInfo> t_plugin_infos;
      vector<ConfigInfo> t_config_infos;
      HostInfo t_host_info;
      std::string file_name;
      int ret = OnDownloadFromServer(plugin_version, plugin_name, t_preload_infos,
                                     t_host_info, t_plugin_infos, t_config_infos, file_name);

      if(CPLUGIN_ERR_OK != ret){
        return CPLUGIN_ERR_DOWNLOADING_ERROR;
      }

      if(!CheckFileIntegrity(plugin_name, plugin_version, false, configNode_.tmp_dir)){
        LOG(ERROR) << "HostManager::BackUpgreadHandle CheckFileIntegrity failed  " << ret;
        return CPLUGIN_ERR_DISK_FULL;
      }

      UPDATE_TYPE update_type = OnCheckUpdateMethod( node, t_preload_infos, t_host_info, t_plugin_infos, t_config_infos);
      if(update_type == UPDATE_TYPE_PLUGIN){

        //cp resource
        if(!UtilCPResource(plugin_name, configNode_.tmp_dir)){
          LOG(ERROR) << "HostManager::BackUpgreadHandle UtilCPResource failed  " << ret;
          return CPLUGIN_ERR_DISK_FULL;
        }

        int32_t ret = OnHotUpgrade(plugin_name, node);

        node.host_info_ = t_host_info;
        node.preload_infos_ = t_preload_infos;
        node.plugin_infos_ = t_plugin_infos;
        node.config_infos_ = t_config_infos;

        node.now_version_ = plugin_version;


        if(ret != CPLUGIN_ERR_OK){
          LOG(ERROR) << "BackUpgreadHandle hot update error  do all update"  << ret;


          if( OnStop(plugin_name, node)  !=  CPLUGIN_ERR_OK ){
            LOG(ERROR) << "BackUpgreadHandle OnStop : " << plugin_name << " failed.";
            return CPLUGIN_ERR_KILLING_ERROR;
          }

          int32_t ret_t = OnStart(plugin_name, node);

          OnModifyNode(plugin_name, node);

          if(ret_t != CPLUGIN_ERR_OK){
            LOG(ERROR) << "HostManager::BackUpgreadHandle  error :" << plugin_name
                       << "  version:" << node.now_version_
                       << "  err :" << ret_t;
            return ret_t;
          }
        }

      }else if(update_type == UPDATE_TYPE_ALL){
        LOG(INFO) << "HostManager::BackUpgreadHandle UPDATE_TYPE_ALL update type" ;

        node.host_info_ = t_host_info;
        node.preload_infos_ = t_preload_infos;
        node.plugin_infos_ = t_plugin_infos;
        node.config_infos_ = t_config_infos;

        node.now_version_ = plugin_version;

        if( OnStop(plugin_name, node)  !=  CPLUGIN_ERR_OK ){
          LOG(ERROR) << "BackUpgreadHandle OnStop : " << plugin_name << " failed.";
          return CPLUGIN_ERR_KILLING_ERROR;
        }

        //cp resource
        if(!UtilCPResource(plugin_name, configNode_.tmp_dir)){
          LOG(ERROR) << "HostManager::BackUpgreadHandle UtilCPResource failed  " << ret;
          return CPLUGIN_ERR_DISK_FULL;
        }

        int32_t ret_t = OnStart(plugin_name, node);

        OnModifyNode(plugin_name, node);

        if(ret_t != CPLUGIN_ERR_OK){
          LOG(ERROR) << "HostManager::BackUpgreadHandle  error :" << plugin_name
                     << "  version:" << node.now_version_
                     << "  err :" << ret_t;
          return ret_t;
        }

      }else if(update_type == UPDATE_TYPE_CONFIG){
        LOG(INFO) << "HostManager::BackUpgreadHandle UPDATE_TYPE_CONFIG update type, todo nothing" ;

        //cp resource
        if(!UtilCPResource(plugin_name, configNode_.tmp_dir)){
          LOG(ERROR) << "HostManager::BackUpgreadHandle UtilCPResource failed  " << ret;
          return CPLUGIN_ERR_DISK_FULL;
        }

        node.host_info_ = t_host_info;
        node.preload_infos_ = t_preload_infos;
        node.plugin_infos_ = t_plugin_infos;
        node.config_infos_ = t_config_infos;
        node.now_version_ = plugin_version;
        OnModifyNode(plugin_name, node);
      }else{
        LOG(ERROR) << "HostManager::BackUpgreadHandle UNKNOW update type" ;
        return CPLUGIN_ERR_UNKNOW;
      }

      UtilInsertConfig( plugin_name, plugin_version);
      return CPLUGIN_ERR_OK;
    }

    int32_t HostManager::BackRollBackHandle(std::string plugin_name, std::string plugin_version){
      LOG(INFO) << "HostManager::BackRollBackHandle" ;
      return BackUpgreadHandle(plugin_name, plugin_version);
    }

    int32_t HostManager::BackStartNewHandle(std::string plugin_name, std::string plugin_version){
      LOG(INFO) << "HostManager::BackStartNewHandle";

      CPluginNode node;
      bool result =  OnGetNode(plugin_name, node);
      if(result){
        LOG(ERROR) << plugin_name << "have already run.";
        return CPLUGIN_ERR_OK;
      }

      vector<PreloadInfo> t_preload_infos;
      vector<PluginInfo> t_plugin_infos;
      vector<ConfigInfo> t_config_infos;
      HostInfo t_host_info;
      std::string config_file;
      int ret = OnDownloadFromServer(plugin_version, plugin_name, node.preload_infos_,
                                     node.host_info_, node.plugin_infos_, node.config_infos_, config_file);

      if(CPLUGIN_ERR_OK != ret){
        LOG(ERROR) << "HostManager::StartNew OnDownloadFromServer failed  " << ret;
        return CPLUGIN_ERR_DOWNLOADING_ERROR;
      }

      if(!CheckFileIntegrity(plugin_name, plugin_version, false, configNode_.tmp_dir)){
        LOG(ERROR) << "HostManager::StartNew CheckFileIntegrity failed  " << ret;
        return CPLUGIN_ERR_DISK_FULL;
      }
      //cp resource
      if(!UtilCPResource(plugin_name, configNode_.tmp_dir)){
        LOG(ERROR) << "HostManager::StartNew UtilCPResource failed  " << ret;
        return CPLUGIN_ERR_DISK_FULL;
      }

      node.plugin_pid_ = 0;
      node.push_fd_  = -1;
      node.now_version_ = plugin_version;
      node.statu_       = PLUGIN_STATU_ORIGIN;
      node.is_ok_ = false;
      node.last_time_download_failed = muduo::Timestamp::now();
      node.last_download_failed_times = 0;
      node.last_download_failed_inernal = configNode_.downloadFailedIntervalTime_ ;

      UtilSaveConfig( plugin_name, plugin_version);

      int32_t ret_t = OnStart(plugin_name, node);

      if(ret_t != CPLUGIN_ERR_OK){
        LOG(ERROR) << "HostManager::StartNew  error :" << plugin_name
                   << "  version:" << node.now_version_
                   << "  err :" << ret_t;
      }

      if(!OnAddNode(plugin_name, node)) {
        LOG(ERROR) << "HostManager::StartNew have alreadly  ";

        if( OnStop(plugin_name, node)  !=  CPLUGIN_ERR_OK ){
          LOG(ERROR) << "BackStartNewHandle OnStop : " << plugin_name << " failed.";
          return CPLUGIN_ERR_KILLING_ERROR;
        }

      }

      return ret_t;
    }

    int32_t HostManager::BackUpdateFileHandle(std::string path, std::string content){
      LOG(INFO) << "HostManager::BackUpdateFileHandle";

      string prefix;
      string agent;
      string file;

      if(UtilPartPath(path, prefix, agent, file) != CPLUGIN_ERR_OK){
        LOG(ERROR) << "UtilPartPath error " << file;
        return CPLUGIN_ERR_PATH_ERROR;
      }

      CPluginNode node;
      bool result =  OnGetNode(agent, node);
      if(!result){
        //mem have no this agent, just log.
        LOG(ERROR) << agent << "have no run.";
      }

      ofstream config_file;
      LOG(INFO) << "save config: " << path << endl;
      config_file.open(path.c_str());
      if(config_file.is_open()){
        config_file <<content;
        config_file.close();

        if(agent != CPLUGIN_PLUGIN_NAME)
        if(!UtilUpateAgentConfig(agent, path)){
          LOG(ERROR) << "Write UtilUpateAgentConfig failed." << agent << "  "  << path << "  " << content ;
          return  CPLUGIN_ERR_CPLUGIN_PLUGIN_CONFIG_ERR;
        }
      }

      return CPLUGIN_ERR_OK;
    }

    int32_t HostManager::BackStopAllHandle(std::string agent, std::string version){
      LOG(INFO) << "HostManager::BackStopAllHandle " <<  agent << "  " <<  version;

      StrPluginMap temp;
      temp.swap(strPluginMap_);

      StrPluginMap::iterator it = temp.begin();
      for(; it != temp.end(); it++) {
        LOG(INFO) << "StopAll Stop " << it->first << "  begin";
        if (OnStop(it->first, it->second) != CPLUGIN_ERR_OK) {
          LOG(ERROR) << "StopAll Stop " << it->first << "  failed";
        }
        LOG(INFO) << "StopAll Stop " << it->first << "  END";
      }

      return CPLUGIN_ERR_OK;
    }

    UPDATE_TYPE  HostManager::OnCheckUpdateMethod(CPluginNode& node,
                                                  std::vector<PreloadInfo>& preload_infos,
                                                  HostInfo& host_info,
                                                  std::vector<PluginInfo> plugin_infos,
                                                  std::vector<ConfigInfo>& config_infos){

      //now update all
      //return UPDATE_TYPE_ALL;

      if(host_info.hash != node.host_info_.hash){
          LOG(INFO) << "host hash not equal UPDATE_TYPE_ALL";
          return  UPDATE_TYPE_ALL;
      }

      do{

         int32_t new_size = preload_infos.size();
         int32_t old_size = node.preload_infos_.size();
         if( new_size != old_size){
           LOG(INFO) << "proload size not equal UPDATE_TYPE_ALL";
           return UPDATE_TYPE_ALL;
         }

          std::vector<PreloadInfo>::iterator it = preload_infos.begin();
          for(; it != preload_infos.end(); it++){
            bool flag = false;
              std::vector<PreloadInfo>::iterator it_sub = node.preload_infos_.begin();
              for(; it_sub != node.preload_infos_.end(); it_sub++){
                  if(it->name == it_sub->name  ){

                    if(it->library_name != it_sub->library_name || it->hash != it_sub->hash) {
                      LOG(INFO) << "config  UPDATE_TYPE_ALL hash not match"
                                << "\r\n  new name: " << it->name  << " old name: " << it_sub->name
                                << "\r\n  new lib name: " << it->library_name  << " old lib name: " << it_sub->library_name
                                << "\r\n  new hash: " << it->hash  << " old hash: " << it_sub->hash;
                      return UPDATE_TYPE_ALL;
                    }

                    flag = true;
                    break;
                  }
              }

            if(flag == false){
               LOG(INFO) << "proload  UPDATE_TYPE_ALL";
               return UPDATE_TYPE_ALL;
            }
          }
      }  while(0);

      do{
        int32_t new_size = plugin_infos.size();
        int32_t old_size = node.plugin_infos_.size();
        if( new_size != old_size){
          LOG(INFO) << "plugin size not equal UPDATE_TYPE_ALL";
          return UPDATE_TYPE_ALL;
        }

        std::vector<PluginInfo>::iterator it = plugin_infos.begin();
        for(; it != plugin_infos.end(); it++){
          bool flag = false;
          std::vector<PluginInfo>::iterator it_sub = node.plugin_infos_.begin();
          for(; it_sub != node.plugin_infos_.end(); it_sub++){
            if(it->name == it_sub->name  ){

              if((it->library_name != it_sub->library_name || it->hash != it_sub->hash) &&
                      (it->hot_update == UPDATE_TYPE_ALL || it_sub->hot_update == UPDATE_TYPE_ALL)) {
                LOG(INFO) << "config  UPDATE_TYPE_ALL hash not match"
                          << "\r\n  new name: " << it->name  << " old name: " << it_sub->name
                          << "\r\n  new lib name: " << it->library_name  << " old lib name: " << it_sub->library_name
                          << "\r\n  new hash: " << it->hash  << " old hash: " << it_sub->hash
                          << "\r\n  new hotupdate: " << it->hot_update  << " old hotupdate: " << it_sub->hot_update;
                return UPDATE_TYPE_ALL;
              }

              flag = true;
              break;
            }
          }

          if(flag == false){
            LOG(INFO) << "lib  UPDATE_TYPE_ALL";
            return UPDATE_TYPE_ALL;
          }
        }
      }  while(0);

        do{
          int32_t new_size = config_infos.size();
          int32_t old_size = node.config_infos_.size();
          if( new_size != old_size){
            LOG(INFO) << "config size not equal UPDATE_TYPE_ALL";
            return UPDATE_TYPE_ALL;
          }

            std::vector<ConfigInfo>::iterator it = config_infos.begin();
            for(; it != config_infos.end(); it++){
                bool flag = false;
                std::vector<ConfigInfo>::iterator it_sub = node.config_infos_.begin();
                for(; it_sub != node.config_infos_.end(); it_sub++){
                    if(it->config_name == it_sub->config_name  ){

                      if(it->link_name != it_sub->link_name ||
                              it->hot_update == UPDATE_TYPE_ALL ||
                              it_sub->hot_update == UPDATE_TYPE_ALL )

                        //if(it->link_name != it_sub->link_name || it->hash != it_sub->hash)
                        {
                            LOG(INFO) << "config  UPDATE_TYPE_ALL hash not match"
                                    << "\r\n  new name: " << it->config_name  << " old name: " << it_sub->config_name
                                    << "\r\n  new link name: " << it->link_name  << " old link name: " << it_sub->link_name
                                    << "\r\n  new hash: " << it->hash  << " old hash: " << it_sub->hash
                                    << "\r\n  new hotupdate: " << it->hot_update  << " old hotupdate: " << it_sub->hot_update;
                            return UPDATE_TYPE_ALL;
                        }


                      flag = true;
                      break;
                    }
                }

              if(flag == false){
                LOG(INFO) << "config  UPDATE_TYPE_ALL";
                return UPDATE_TYPE_ALL;
              }
            }
        }  while(0);


        LOG(INFO) << "UPDATE_TYPE_CONFIG";
        return UPDATE_TYPE_CONFIG;
    }


    bool HostManager::UtilLoadConfig(std::string& config_file,
                                 std::vector<PreloadInfo>& preload_infos,
                                 HostInfo& host_info,
                                 std::vector<PluginInfo>& plugin_infos,
                                 std::vector<ConfigInfo>& config_infos) {
      std::string config_file_ = config_file;

      XMLDocument doc;
      LOG(ERROR) << "Load xml : " << config_file_;
      XMLError ret = doc.LoadFile(config_file_.c_str());
      if (XML_SUCCESS != ret) {
        LOG(ERROR) << "Load xml error: " << ret;
        return false;
      }

      XMLElement* root = doc.RootElement();
      if (!root) {
        return false;
      }

      XMLElement* preload_host = root->FirstChildElement("Preload");
      if (preload_host && !ParsePreload(preload_host, &preload_infos)) {
        return false;
      }

      XMLElement* host_root = root->FirstChildElement("Host");
      if (host_root && !ParseHost(host_root, &host_info)) {
        return false;
      }

      XMLElement* plugin_root = root->FirstChildElement("Plugin");
      if (plugin_root && !ParsePlugin(plugin_root, &plugin_infos)) {
        return false;
      }

      XMLElement* config_root = root->FirstChildElement("Config");
      if (config_root && !ParseConfig(config_root, &config_infos)) {
        return false;
      }

      return true;
    }


    bool HostManager::UtilUpateAgentConfig(const string& agent, const string& file_name){

      XMLDocument doc;
      string config_file_name = agent + "_config.xml";
      XMLError ret = doc.LoadFile(config_file_name.c_str());
      if (XML_SUCCESS != ret) {
        LOG(ERROR) << "Load xml error: " << ret;
        return false;
      }

      XMLElement* root = doc.RootElement();
      if (!root) {
        return false;
      }



      // Download config
      XMLElement* config_root = root->FirstChildElement("Config");
      if (!config_root){
        return false;
      }

      XMLElement* file = config_root->FirstChildElement("File");
      if (!file){
        return false;
      }

      bool flag = false;
      do {
        string  attr_name = file->Attribute("ConfigName");
        if(attr_name == file_name){

          MD5 md5File;
          if( md5File.FromFile(attr_name) != 0){
            LOG(ERROR)  << "UtilUpateAgentConfig    gen md5 error";
            return false;
          }

          LOG(INFO) << attr_name << "  md5  " << md5File.md5();

          file->SetAttribute("Hash", md5File.md5().c_str());
          int result=doc.SaveFile(config_file_name.c_str());
          LOG(INFO) << "  save agent_config.xml result " << result;

          flag = true;
          break;
        }

        file = file->NextSiblingElement();
      } while (file);

      if(!flag){
        LOG(ERROR)  << "UtilUpateAgentConfig    update md5 error";
        return false;
      }

      return true;
    }

} // namespace cplugin 
