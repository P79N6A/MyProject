#include <regex.h>
#include "SGAgentHandler.h"
#include "common_log_collector.h"
#include "util/sgagent_stat.h"
#include "regist_client.h"
#include "config_client.h"
#include "file_config_client.h"
#include "quota_client.h"
#include "auth_client.h"
#include "util/sg_agent_env.h"
#include "util/sgagent_shutdown.h"
#include "util/falcon_mgr.h"
#include "operation_common_types.h"
#include "util/switch_operate.h"
#include "util/global_def.h"
#include "mns.h"

extern MNS *g_mns;
extern GlobalVar *g_global_var;

namespace sg_agent {

SGAgentHandler::SGAgentHandler() {
}

int SGAgentHandler::Init() {
  int ret = 0;
  LOG_INFO("RUN Start to init mns processor");

  FalconMgr::Init();

  /**
   * init
   */
  ret = FileConfigClient::getInstance()->init();
  if (0 != ret) {
    LOG_FATAL("failed to init FileConfigClient, errorCode = " << ret);
    return ret;
  }
  ret = RegistClient::getInstance()->init();
  if (0 != ret) {
    LOG_FATAL("failed to init SGAgentHandler, errorCode = " << ret);
    return ret;
  }

  //add quota
  ret = QuotaClient::getInstance()->init();
  if (0 != ret) {
    LOG_FATAL("failed to init SGAgentHandler, errorCode = " << ret);
    return ret;
  }

  //auth
  ret = AuthClient::getInstance()->init();
  if (0 != ret) {
    LOG_FATAL("failed to init SGAgentHandler, errorCode = " << ret);
    return ret;
  }

  ret = ConfigClient::getInstance()->Init();
  if (0 != ret) {
    LOG_FATAL("ERR init ConfigClient failed. ret = " << ret);
    return ret;
  }

  ret = CommonLogCollector::getInstance()->init();
  if (0 != ret) {
    LOG_ERROR("failed to init SGAgentHandler, errorCode = " << ret);
    return ret;
  }

  //注册sg_agent自己
  SGService oservice;
  oservice.appkey = g_global_var->gSgagentAppkey;
  oservice.version = g_global_var->gVersion;
  oservice.ip = g_global_var->gIp;
  oservice.port = g_global_var->gPort;
  oservice.weight = 10;
  oservice.status = fb_status::ALIVE;
  oservice.role = 0;
  oservice.envir = g_global_var->gEnv;
  oservice.lastUpdateTime = time(0);
  oservice.extend = "";
  oservice.protocol = "thrift";

  ret = registService(oservice);
  if (unlikely(SUCCESS != ret)) {
    LOG_ERROR("failed to regist sg_agent to mns, ret = "
                  << ret);
  }
  LOG_INFO("succeed to regist self to mns ret:" << ret);

  return 0;
}

void SGAgentHandler::getServiceListByProtocol(ProtocolResponse &_return, const ProtocolRequest &req) {
  // default not enable swimlane2.0
  bool enable_swimlane2 = false;
  if (req.__isset.enableSwimlane2) {
    enable_swimlane2 = req.enableSwimlane2;
  }
  _return.errcode = g_mns->GetMnsPlugin()->GetSrvList(_return.servicelist, req, false, !enable_swimlane2, !enable_swimlane2);
  LOG_INFO("getServiceListByProtocol size = " << _return.servicelist.size() <<
                                              " errorCode = " << _return.errcode);
}

void SGAgentHandler::getOriginServiceList(ProtocolResponse &_return, const ProtocolRequest &req) {
  _return.errcode = g_mns->GetMnsPlugin()->GetSrvList(_return.servicelist, req, true, false, false);
  LOG_INFO("getOriginServiceList size = " << _return.servicelist.size() <<
                                          " errorCode = " << _return.errcode);
}

void SGAgentHandler::getServiceListWithZKFweight(ProtocolResponse &_return, const ProtocolRequest &req) {

  _return.errcode = g_mns->GetMnsPlugin()->GetSrvList(_return.servicelist, req, true, true, true);
  LOG_INFO("getServiceListWithZKFweight size = " << _return.servicelist.size() <<
                                                 " errorCode = " << _return.errcode);
}


void SGAgentHandler::getDegradeActions(std::vector<DegradeAction> &_return,
                                       const std::string &localAppkey,
                                       const std::string &remoteAppkey) {
  //如果需要降级，则直接返回
  if (g_global_var->gOpenQuota == 0) {
    LOG_WARN("gOpenQuota is 0, you need to open it");
    return;
  }

  if (unlikely(remoteAppkey.empty())) {
    LOG_WARN("remoteAppkey is empty! localAppkey = " << localAppkey);
    return;
  }

  char buf[50] = {0};
  snprintf(buf, sizeof(buf), "getDegradeActions");

  QuotaClient::getInstance()
      ->getActions(_return, localAppkey, remoteAppkey);

  LOG_DEBUG("getDegradeActions input"
                << ", localAppkey: " << localAppkey
                << ", remoteAppkey : " << remoteAppkey
                << ", return size : " << _return.size());
  return;
}

void SGAgentHandler::getServiceList(std::vector<SGService> &_return,
                                    const std::string &localAppkey,
                                    const std::string &remoteAppkey) {
  if (unlikely(remoteAppkey.empty())) {
    LOG_WARN("remoteAppkey is empty! localAppkey = " << localAppkey);
    return;
  }

  ProtocolRequest req;
  req.__set_protocol("thrift");
  req.__set_remoteAppkey(remoteAppkey);
  req.__set_localAppkey(localAppkey);
  g_mns->GetMnsPlugin()->GetSrvList(_return, req, false, true, true);
}

void SGAgentHandler::getHttpServiceList(std::vector<SGService> &_return,
                                        const std::string &localAppkey,
                                        const std::string &remoteAppkey) {
  //如果需要降级，则直接返回
  if (g_global_var->gOpenHlb == 0) {
    LOG_WARN("gOpenHlb is 0, you need to open it");
    return;
  }

  if (unlikely(remoteAppkey.empty())) {
    LOG_WARN("remoteAppkey is empty! localAppkey = " << localAppkey);
    return;
  }

  ProtocolRequest req;
  req.__set_localAppkey(localAppkey);
  req.__set_remoteAppkey(remoteAppkey);
  req.__set_protocol("http");

  g_mns->GetMnsPlugin()->GetSrvList(_return, req, false, true, true);
}

void SGAgentHandler::getAppKeyListByBusinessLine(std::vector<std::string> &_return,
                                                 const int32_t businessLineCode) {
  return;
}

int32_t SGAgentHandler::registService(const SGService &oService) {
  return registServicewithCmd(UptCmd::ADD, oService);
}

int32_t SGAgentHandler::registServicewithCmd(const int32_t uptCmd, const SGService &oService) {

  START_TIME
  char buf[50] = {0};
  snprintf(buf, sizeof(buf), "registService");

  //int ret = 0;
  int ret = RegistClient::getInstance()->registeService(oService, g_global_var->isNewCloud, uptCmd);

  LOG_INFO("registService appkey = " << oService.appkey
                                     << "; protocol = " << oService.protocol
                                     << "; ret = " << ret);

  static InvokeStat registerServiceStat;
  int count = 0;
  if (InvokeStat::GetInvokeStatInfo(registerServiceStat, count)) {
    LOG_STAT("registerServiceStat request count is:" << count);
    END_TIME("registService")
  }
  return ret;
}

int32_t SGAgentHandler::unRegistService(const SGService &oService) {
  START_TIME
  //cmtrace 上报
  char buf[50] = {0};
  snprintf(buf, sizeof(buf), "registService");
  int ret = RegistClient::getInstance()->unRegisteService(oService, g_global_var->isNewCloud);

  LOG_INFO("unRegistService appkey = " << oService.appkey
                                       << "; ret = " << ret);

  static InvokeStat unRegisterServiceStat;
  int count = 0;
  if (InvokeStat::GetInvokeStatInfo(unRegisterServiceStat, count)) {
    LOG_STAT("unRegisterServiceStat request count is:" << count);
    END_TIME("unRegistService")
  }
  return ret;
}

int SGAgentHandler::updateConfig(const ::ConfigUpdateRequest &request) {
  //如果需要降级，则直接返回
  if (g_global_var->gOpenMtConfig == 0) {
    LOG_WARN("you need to open updateconfig function");
    return ERR_CONFIG_OPENFUNC_NOT;
  }

  START_TIME
  int ret = 0;
  ret = ConfigClient::getInstance()->UpdateConfig(request);
  LOG_DEBUG("updateConfig size = " << request.nodes.size()
                                   << "; ret = " << ret);
  for (int i = 0; i < request.nodes.size(); ++i) {
    LOG_DEBUG("updateConfig appkey << " << request.nodes[i].appkey
                                        << "; env = " << request.nodes[i].env
                                        << "; path = "
                                        << request.nodes[i].path);
  }

  static InvokeStat updateConfigStat;
  int count = 0;
  if (InvokeStat::GetInvokeStatInfo(updateConfigStat, count)) {
    LOG_STAT("updateConfigStat request count is:" << count);
    END_TIME("updateConfig")
  }
  return ret;
}

void SGAgentHandler::getConfig(std::string &_return,
                               const ::proc_conf_param_t &node) {
  //如果需要降级，则直接返回
  if (g_global_var->gOpenMtConfig == 0) {
    LOG_ERROR("you need to open getconfig function");
    _return = "";
    return;
  }

  START_TIME
  int ret = ConfigClient::getInstance()->GetConfig(_return, node);
  if (0 != ret) { // SDK有定期轮训，防止日志打印过多问题, 只打印错误日志
    LOG_ERROR("getConfig appkey = " << node.appkey
                                    << "; env = " << node.env
                                    << "; path = " << node.path
                                    << "; conf = " << node.conf
                                    << ": swimlane = " << node.swimlane
                                    << "; ret = " << _return);
  }

  static InvokeStat getConfigStat;
  int count = 0;
  if (InvokeStat::GetInvokeStatInfo(getConfigStat, count)) {
    LOG_STAT("getConfigStat request count is:" << count);
    END_TIME("getConfig")
  }
}

int32_t SGAgentHandler::setConfig(const proc_conf_param_t &conf) {
  //如果需要降级，则直接返回
  if (g_global_var->gOpenMtConfig == 0) {
    LOG_WARN("you need to open setconfig function");
    return ERR_CONFIG_OPENFUNC_NOT;
  }

  START_TIME
  int ret = 0;
  LOG_INFO("setConfig appkey = " << conf.appkey
                                 << "; env = " << conf.env
                                 << "; path = " << conf.path
                                 << "; conf = " << conf.conf);

  ret = ConfigClient::getInstance()->SetConfig(conf);
  if (SUCCESS != ret) {
    LOG_ERROR("not get succeed return from setConfig, ret = " << ret);
  }

  static InvokeStat setConfigStat;
  int count = 0;
  if (InvokeStat::GetInvokeStatInfo(setConfigStat, count)) {
    LOG_STAT("setConfigStat request count is:" << count);
    END_TIME("setConfig")
  }
  return ret;
}

void SGAgentHandler::getLocalConfig(
    std::string &_return,
    const std::string &localAppkey,
    const std::string &ip) {
  LOG_WARN("getLocalConfig function is closed, localAppkey = "
               << localAppkey
               << "; ip = " << ip);
  _return = "";
}

int32_t SGAgentHandler::uploadLog(const SGLog &oLog) {
  return 0;
}

int32_t SGAgentHandler::uploadCommonLog(const CommonLog &oLog) {
  //如果需要降级，则直接返回
  if (g_global_var->gOpenCommonLog == 0) {
    LOG_WARN("you need to open commonLog function");
    return 0;
  }

  START_TIME
  CommonLogCollector::getInstance()->sendCommonLogs(oLog);
  static InvokeStat uploadCommonLogStat;
  int count = 0;
  if (InvokeStat::GetInvokeStatInfo(uploadCommonLogStat, count)) {
    LOG_STAT("uploadCommonLogStat request count is:" << count);
    END_TIME("sendCommonLogs")
  }
  return 0;
}

int32_t SGAgentHandler::uploadModuleInvoke(const SGModuleInvokeInfo &oInfo) {
  //如果需要降级，则直接返回
  if (g_global_var->gOpenModuleInvoke == 0) {
    LOG_WARN("you need to open ModuleInvoke function");
    return 0;
  }

  START_TIME
  CommonLogCollector::getInstance()->sendModuleInvokeInfo(oInfo);
  static InvokeStat uploadModuleStat;
  int count = 0;
  if (InvokeStat::GetInvokeStatInfo(uploadModuleStat, count)) {
    LOG_STAT("uploadModuleStat request count is:" << count);
    END_TIME("sendModuleInvoke")
  }
  return 0;
}

void SGAgentHandler::getZabbixInfo(ZabbixSelfCheck &_return) {
  //如果需要降级，则直接返回
  if (g_global_var->gOpenSelfCheck == 0) {
    LOG_WARN("you need to open selfcheck function");
    return;
  }

  char buf[50] = {0};
  snprintf(buf, sizeof(buf), "getZabbixInfo");

  std::map<int, long> mqs;

  FalconMgr::GetQueueSizeRes(&mqs);

  _return.msgQueueBytes = mqs;
  _return.bufferKeyNum = 20;
  _return.missBuffNum =
      0; //TODO:ProtocolServiceClient::getInstance()->getMissCount();

  _return.zkConnections = 2;
  _return.mtConfigConnections = 4;
  _return.logCollectorConnections = 4;

  FalconMgr::GetBufferSizeRes(&_return.bufferSize);
  FalconMgr::GetServiceListReqRes(&_return.reqStat);
}

void
SGAgentHandler::getService(SGService &_return,
                           const std::string &localAppkey,
                           const std::string &remoteAppkey,
                           const int32_t strategy) {
  return;
}

void SGAgentHandler::getName(std::string &_return) {
  _return = HANDLERNAME;
  return;
}

void SGAgentHandler::getVersion(std::string &_return) {
  _return = g_global_var->gVersion;
  return;
}

int32_t SGAgentHandler::getEnv() {
  return g_global_var->gEnv;
}

fb_status::type SGAgentHandler::getStatus() {
  return fb_status::ALIVE;
}

void SGAgentHandler::getStatusDetails(std::string &_return) {
  _return = "sg_agent is alive";
  return;
}

void SGAgentHandler::getFileConfig(::file_param_t &_return,
                                   const ::file_param_t &file) {
  //如果需要降级，则直接返回
  if (0 == g_global_var->gOpenFileConfig) {
    LOG_WARN("you need to OpenFileConfig function");
    return;
  }

  //调用cmtrace上报
  char buf[50] = {0};
  snprintf(buf, sizeof(buf), "getFileConfig");

  int serverStatus =
      FileConfigClient::getInstance()->getFileConfig(_return, file);
      _return.err = serverStatus;
}

int32_t SGAgentHandler::notifyFileConfigIssued(const ::file_param_t &files) {
  //如果需要降级，则直接返回
  if (0 == g_global_var->gOpenFileConfig) {
    LOG_WARN("you need to OpenFileConfig function");
    return 0;
  }
  //调用cmtrace上报
  char buf[50] = {0};
  snprintf(buf, sizeof(buf), "notifyFileConfigIssued");

  int serverStatus = FileConfigClient::getInstance()->notifyIssued(files);

  return serverStatus;
}

int32_t SGAgentHandler::notifyFileConfigWork(const ::file_param_t &files) {
  //如果需要降级，则直接返回
  if (0 == g_global_var->gOpenFileConfig) {
    LOG_WARN("you need to OpenFileConfig function");
    return 0;
  }
  char buf[50] = {0};
  snprintf(buf, sizeof(buf), "notifyFileConfigWork");

  int serverStatus = FileConfigClient::getInstance()->notifyWork(files);

  return serverStatus;
}

void SGAgentHandler::getAuthorizedConsumers(std::string &_return,
                                            const std::string &targetAppkey) {
  //如果需要降级，则直接返回
  if (0 == g_global_var->gOpenAuth) {
    LOG_WARN("you need to OpenAuth function");
    return;
  }
  char buf[50] = {0};
  snprintf(buf, sizeof(buf), "getAuthorizedConsumers");

  AuthClient::getInstance()->getAuthorized(AUTH_CONSUMER,
                                           _return,
                                           targetAppkey);

}

void SGAgentHandler::getAuthorizedProviders(std::string &_return,
                                            const std::string &targetAppkey) {
  //如果需要降级，则直接返回
  if (0 == g_global_var->gOpenAuth) {
    LOG_WARN("you need to OpenAuth function");
    return;
  }
  char buf[50] = {0};
  snprintf(buf, sizeof(buf), "getAuthorizedProviders");

  AuthClient::getInstance()->getAuthorized(AUTH_PROVIDER,
                                           _return,
                                           targetAppkey);

}

bool SGAgentHandler::switchEnv(const std::string &env,
                               const std::string &verifyCode) {
  if (0 == g_global_var->gOpenSwitchEnv) {
    return false;
  }
  return sg_agent::SGAgentEnv::switchEnv(env, g_global_var->gEnv, verifyCode);
}

void SGAgentHandler::getHttpPropertiesByBusinessLine(
    std::map<std::string, HttpProperties> &_return,
    const int32_t bizCode) {
  return;
}

void SGAgentHandler::getHttpPropertiesByAppkey(std::map<std::string,
                                                        HttpProperties> &_return,
                                               const std::string &appkey) {
}

void SGAgentHandler::getCounters(std::map<std::string, int64_t> &_return) {
}

int64_t SGAgentHandler::getCounter(const std::string &key) {
  return 0;
}

void
SGAgentHandler::setOption(const std::string &key, const std::string &value) {
  return;
}

void SGAgentHandler::getOption(std::string &_return, const std::string &key) {
  return;
}

void SGAgentHandler::getOptions(std::map<std::string, std::string> &_return) {
  return;
}

void SGAgentHandler::getCpuProfile(std::string &_return,
                                   const int32_t profileDurationInSec) {
  return;
}

int64_t SGAgentHandler::aliveSince() {
  return 0;
}

void SGAgentHandler::reinitialize() {
  return;
}

bool SGAgentHandler::shutdown(const std::string &verifyCode) {
  LOG_INFO("sg_agent shutdown.");
  return sg_agent::SGAgentShutdown::shutdownSelf(verifyCode);
}

void SGAgentHandler::setRemoteSwitch(SwitchResponse &_return,
                                     const SwitchRequest &req) {
  const int key = req.key;
  bool value = req.value;
  LOG_INFO("switch Request: key = " << key
                                    << ", value = " << value
                                    << ", verifyCode = " << req.verifyCode
                                    << ", switchName = " << req.switchName);

  if ("agent.octo.sankuai.com" != req.verifyCode) {
    std::string msg = "the switch verifyCode is error.";
    LOG_ERROR(msg);
    _return.errcode = -1;
    _return.__set_msg(msg);
    return;
  }

  //TODO:目前只针对mafka开关生效
  if (Switch::SwitchMafka != key) {
    std::string msg = "Other Switch is not available.";
    LOG_ERROR(msg << ", key = " << key);
    _return.errcode = -2;
    _return.__set_msg(msg);
    return;
  }

  int ret = sg_agent::SGAgentSwitch::setSwitch(key, value);
  if (0 != ret) {
    LOG_ERROR("set switch fail! key = " << key
                                        << ", value = " << value
                                        << ", verifyCode = " << req.verifyCode
                                        << ", switchName = " << req.switchName
                                        << ", ret = " << ret);
  }
  _return.errcode = ret;
}
void SGAgentHandler::getAppkeyDesc(AppkeyDescResponse &_return, const std::string &appkey){

  _return.errCode = g_mns->GetMnsPlugin()->GetAppkeyDesc(_return, appkey);
  LOG_INFO("getAppkeyDesc appkey = " << appkey
                                     <<",ret code = " << _return.errCode);
}

} // namespace
