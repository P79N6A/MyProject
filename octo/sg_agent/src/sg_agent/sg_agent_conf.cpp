#include "sg_agent_conf.h"

#include "comm/inc_comm.h"
#include "util/sg_agent_def.h"
#include "util/appkey_path_operation.h"
#include "zk_tools.h"
#include "util/switch_operate.h"
#include "util/sg_agent_env.h"
#include "operation_common_types.h"
#include "util/global_def.h"

extern GlobalVar *g_global_var;

namespace sg_agent {

int LoadConfig() {
  tinyxml2::XMLDocument sg_agent_mutable;
  tinyxml2::XMLError sg_agent_mutable_load = sg_agent_mutable.LoadFile(SG_AGENT_MUTABLE_CONF.c_str());
  if (tinyxml2::XML_SUCCESS != sg_agent_mutable_load) {
    LOG_FATAL("can't load " << sg_agent::SG_AGENT_MUTABLE_CONF);
    return ERR_NOSGAGENTMUTABLECONF;
  }

  tinyxml2::XMLElement *agentMutableConf = sg_agent_mutable.FirstChildElement("SGAgentMutableConf");
  if (NULL == agentMutableConf) {
    LOG_FATAL("can't find SGAgentMutableConf in " << sg_agent::SG_AGENT_MUTABLE_CONF);
    return ERR_NOSGAGENTMUTABLECONF;
  }
  tinyxml2::XMLElement *agentFun = agentMutableConf->FirstChildElement("SGAgentFun");
  if (NULL == agentFun) {
    LOG_FATAL("can't find SGAgentFun in " << sg_agent::SG_AGENT_MUTABLE_CONF);
    return ERR_NOSGAGENTMUTABLECONF;
  }

  //读取mafka开关初始化配置
  int key = Switch::SwitchMafka;
  int ret = sg_agent::SGAgentSwitch::initSwitch(key, g_global_var->isOpenMafka);
  if (0 != ret) {
    LOG_ERROR("init mafka switch fail! key = " << key << ", value = "
                                               << g_global_var->isOpenMafka
                                               << ", ret = " << ret);
    return ret;
  }
  LOG_INFO("init mafka switch ok! key = " << key << ", value = "
                                          << g_global_var->isOpenMafka);


  //读取自动环境切换配置，是否关闭环境切换功能
  const char *strOpenSwitchEnv = agentFun->FirstChildElement("OpenSwitchEnv")->GetText();
  g_global_var->gOpenSwitchEnv = (NULL != strstr(strOpenSwitchEnv, "open")) ? 1 : 0;
  LOG_INFO("get OpenSwitchEnv = " << g_global_var->gOpenSwitchEnv);

  const char *strAutoStage = agentFun->FirstChildElement("OpenAutoSwitchStage")->GetText();
  g_global_var->gOpenAutoSwitchStage = (NULL != strstr(strAutoStage, "open")) ? 1 : 0;
  LOG_INFO("get gOpenAutoSwitchStage: " << g_global_var->gOpenAutoSwitchStage);

  const char *strAutoTest = agentFun->FirstChildElement("OpenAutoSwitchTest")->GetText();
  g_global_var->gOpenAutoSwitchTest = (NULL != strstr(strAutoTest, "open")) ? 1 : 0;
  LOG_INFO("get gOpenAutoSwitchTest: " << g_global_var->gOpenAutoSwitchTest);

  //读取动态自动归组开关
  const char *strOpenAutoRoute =
      agentFun->FirstChildElement("OpenAutoRoute")->GetText();
  g_global_var->gOpenAutoRoute = (strstr(strOpenAutoRoute, "close") != NULL) ? 0 : 1;
  LOG_INFO("get OpenAutoRoute = " << g_global_var->gOpenAutoRoute);

  const char *clientPort =
      agentMutableConf->FirstChildElement("ClientPort")->GetText();
  g_global_var->gPort = atoi(clientPort);

  //区分线下云主机
  tinyxml2::XMLElement *new_cloud =
      agentMutableConf->FirstChildElement("NewCloud");
  g_global_var->isNewCloud = (NULL != new_cloud);

  //获取logCollector的appkey
  const char *logCollectorAppkey =
      agentMutableConf->FirstChildElement("RemoteLogAppkey")->GetText();
  g_global_var->gLogCollectorAppkey = logCollectorAppkey;
  if (g_global_var->gLogCollectorAppkey.empty()) {
    LOG_ERROR("can't find logCollector Appkey in " << sg_agent::SG_AGENT_MUTABLE_CONF);
    return ERR_NOSGAGENTMUTABLECONF;
  }
  LOG_INFO("remoteLogCollector Appkey is: " << g_global_var->gLogCollectorAppkey);


  //读取降级配置，是否关闭getLocalConfig功能
  const char *strOpenConfig =
      sg_agent_mutable.FirstChildElement("SGAgentMutableConf")
          ->FirstChildElement("SGAgentFun")
          ->FirstChildElement("OpenConfig")->GetText();
  g_global_var->gOpenConfig = (NULL != strstr(strOpenConfig, "open")) ? 1 : 0;
  LOG_INFO("get gOpenConfig: " << g_global_var->gOpenConfig);

  //读取降级配置，是否关闭getConfig功能
  const char *strOpenMtConfig =
      sg_agent_mutable.FirstChildElement("SGAgentMutableConf")
          ->FirstChildElement("SGAgentFun")
          ->FirstChildElement("OpenMtConfig")->GetText();
  g_global_var->gOpenMtConfig = (NULL != strstr(strOpenMtConfig, "open")) ? 1 : 0;
  LOG_INFO("get gOpenMtConfig: " << g_global_var->gOpenMtConfig);

  //读取降级配置，是否关闭通用日志上报功能--add by henxin
  const char *strOpenCommonLog =
      sg_agent_mutable.FirstChildElement("SGAgentMutableConf")
          ->FirstChildElement("SGAgentFun")
          ->FirstChildElement("OpenCommonLog")->GetText();
  g_global_var->gOpenCommonLog = (NULL != strstr(strOpenCommonLog, "open")) ? 1 : 0;
  LOG_INFO("get gOpenCommonLog : " << g_global_var->gOpenCommonLog);

  //读取降级配置，是否关闭quota功能
  const char *strOpenQuota =
      sg_agent_mutable.FirstChildElement("SGAgentMutableConf")
          ->FirstChildElement("SGAgentFun")
          ->FirstChildElement("OpenQuota")->GetText();
  g_global_var->gOpenQuota = (NULL != strstr(strOpenQuota, "open")) ? 1 : 0;
  LOG_INFO("get gQuota: " << g_global_var->gOpenQuota);

  //读取降级配置，是否关闭模块调用上报功能
  const char *strOpenModule =
      sg_agent_mutable.FirstChildElement("SGAgentMutableConf")
          ->FirstChildElement("SGAgentFun")
          ->FirstChildElement("OpenModuleInvoke")->GetText();
  g_global_var->gOpenModuleInvoke = (NULL != strstr(strOpenModule, "open")) ? 1 : 0;
  LOG_INFO("get gOpenModuleInvoke: " << g_global_var->gOpenModuleInvoke);

  //读取降级配置，是否关闭自检调用上报功能
  const char *strSelfCheck =
      sg_agent_mutable.FirstChildElement("SGAgentMutableConf")
          ->FirstChildElement("SGAgentFun")
          ->FirstChildElement("OpenSelfCheck")->GetText();
  g_global_var->gOpenSelfCheck = (NULL != strstr(strSelfCheck, "open")) ? 1 : 0;
  LOG_INFO("get gOpenSelfCheck: " << g_global_var->gOpenSelfCheck);

  //读取降级配置，是否关闭文件配置功能
  const char *strFileConfig =
      sg_agent_mutable.FirstChildElement("SGAgentMutableConf")
          ->FirstChildElement("SGAgentFun")
          ->FirstChildElement("OpenFileConfig")->GetText();
  g_global_var->gOpenFileConfig = (NULL != strstr(strFileConfig, "open")) ? 1 : 0;
  LOG_INFO("get gOpenFileConfig: " << g_global_var->gOpenFileConfig);

  const char *strHlb =
      sg_agent_mutable.FirstChildElement("SGAgentMutableConf")
          ->FirstChildElement("SGAgentFun")
          ->FirstChildElement("OpenHlb")->GetText();
  g_global_var->gOpenHlb = (NULL != strstr(strHlb, "open")) ? 1 : 0;
  LOG_INFO("get gOpenHlb: " << g_global_var->gOpenHlb);

  //读取降级配置，是否关闭服务访问控制功能
  const char *strAuth =
      sg_agent_mutable.FirstChildElement("SGAgentMutableConf")
          ->FirstChildElement("SGAgentFun")
          ->FirstChildElement("OpenAuth")->GetText();
  g_global_var->gOpenAuth = (NULL != strstr(strAuth, "open")) ? 1 : 0;
  LOG_INFO("get gOpenAuth: " << g_global_var->gOpenAuth);

  //读取环境切换配置，是否关闭环境切换功能
  const char *strSwitchEnv =
      sg_agent_mutable.FirstChildElement("SGAgentMutableConf")
          ->FirstChildElement("SGAgentFun")
          ->FirstChildElement("OpenSwitchEnv")->GetText();
  g_global_var->gOpenSwitchEnv = (NULL != strstr(strSwitchEnv, "open")) ? 1 : 0;
  LOG_INFO("get gOpenSwitchEnv: " << g_global_var->gOpenSwitchEnv);

  //读取unifiedProto切换配置，是否关闭环境切换功能
  tinyxml2::XMLElement *proto_lcappkey =
      agentFun->FirstChildElement("OpenUnifiedProtoChange4LocalAppkey");
  if (NULL != proto_lcappkey) {
    const char *strUnifiedProto4LocalAppkey =
        proto_lcappkey->GetText();
    g_global_var->gOpenUnifiedProtoChange4LocalAppkey = (NULL != strstr(strUnifiedProto4LocalAppkey, "open")) ? 1 : 0;
  }
  LOG_INFO("current gOpenUnifiedProtoChange4LocalAppkey: "
               << g_global_var->gOpenUnifiedProtoChange4LocalAppkey);

  //读取unifiedProto切换配置，是否关闭环境切换功能
  tinyxml2::XMLElement *proto_mtthrift =
      agentFun->FirstChildElement("OpenUnifiedProtoChange4MTthrift");
  if (NULL != proto_mtthrift) {
    const char *strUnifiedProto4MTthrift =
        proto_mtthrift->GetText();
    g_global_var->gOpenUnifiedProtoChange4MTthrift = (NULL != strstr(strUnifiedProto4MTthrift, "open")) ? 1 : 0;
  }
  LOG_INFO("current gOpenUnifiedProtoChange4MTthrift: "
               << g_global_var->gOpenUnifiedProtoChange4MTthrift);



  // 初始化ZK
  ret = ZkTools::Init(sg_agent_mutable, "/mns/sankuai/" + g_global_var->gEnvStr);
  if (SUCCESS != ret) {
    LOG_FATAL("failed to init Zk, ret = " << ret);
    return ret;
  }

  //读取sg_agent版本信息和appkey信息
  const char *sgagent_appkey =
      sg_agent_mutable.FirstChildElement("SGAgentMutableConf")
          ->FirstChildElement("SGAgentAppKey")->GetText();
  LOG_INFO("current sgagent appkey: " << sgagent_appkey);

  const char *sgagent_version =
      sg_agent_mutable.FirstChildElement("SGAgentMutableConf")
          ->FirstChildElement("SGAgentVersion")->GetText();
  g_global_var->gVersion = std::string(sgagent_version);
  LOG_INFO("current sgagent version: " << g_global_var->gVersion);
  g_global_var->gSgagentAppkey = sgagent_appkey;

  return ret;
}

} // namespace sg_agent

