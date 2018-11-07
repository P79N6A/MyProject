#include "global_def.h"
#include "SGAgentErr.h"
GlobalVar::GlobalVar() {
  gOpen = 1;
  gClose = 0;

  gEnv = 0;
  gEnvStr = "";
  gAppenv = DEV; // default dev
  gIsOnline = false;

  gOpenConfig = 0;
  gOpenMtConfig = 0;
  gOpenLog = 0;
  gOpenCommonLog = 0;
  gCommonToLogCollector = 1; //缺省向logCollector发送数据
  gOpenModuleInvoke = 0;
  gOpenQuota = 0;
  gOpenSelfCheck = 0;
  gOpenFileConfig = 0;
  gOpenHlb = 0;
  gOpenAuth = 0;
  gOpenSwitchEnv = 0;
  gOpenAutoSwitchStage = 0;
  gOpenAutoSwitchTest = 0;
  gOpenProperties = 0;
  gOpenAutoRoute = 1;
  gOpenUnifiedProtoChange4LocalAppkey = 1;
  gOpenUnifiedProtoChange4MTthrift = 1;

  isOpenMNSCache = true;
  isOpenMafka = true;
  isNewCloud = false;

  gPort = 0;
  gIp = "";
  gMask = "255.255.0.0";
  gVersion = "";
  gLogCollectorAppkey = "";
  gMnscAppkey = "";
  gSgagentAppkey = "";
  gAppkeysFile = "/data/webapps/appkeys";
}

int GlobalVar::InitEnv(const std::string &env_str, const std::string &deployenv_str) {
  LOG_INFO("start to parse the host env, env = " << env_str << ", deployenv = " << deployenv_str);

  // 优先解释env字段，无法解释时，再解释deployenv。请勿优化以下if语句
  if ("prod" == env_str) {
    gAppenv = PROD;
  } else if ("staging" == env_str) {
    gAppenv = STAGING;
  } else if ("dev" == env_str) {
    gAppenv = DEV;
  } else if ("ppe" == env_str) {
    gAppenv = PPE;
  } else if ("test" == env_str) {
    gAppenv = TEST;
  } else if ("product" == deployenv_str || "prod" == deployenv_str) {
    gAppenv = PROD;
  } else if ("staging" == deployenv_str) {
    gAppenv = STAGING;
  } else if ("dev" == deployenv_str || "alpha" == deployenv_str) {
    gAppenv = DEV;
  } else if ("ppe" == deployenv_str || "prelease" == deployenv_str) {
    gAppenv = PPE;
  } else if ("qa" == deployenv_str || "test" == deployenv_str) {
    gAppenv = TEST;
  } else {
    LOG_ERROR("fail to parse the host env, invalid appenv.");
    return ERR_INVALID_ENV;
  }
  switch (gAppenv) {
    case PROD: gEnvStr = "prod";
      gEnv = 3;
      gIsOnline = true;
      LOG_INFO("success to init host env = prod (online)");
      break;
    case STAGING: gEnvStr = "stage";
      gEnv = 2;
      gIsOnline = true;
      LOG_INFO("success to init host env = staging (online)");
      break;
    case DEV: gEnvStr = "prod";
      gEnv = 3;
      gIsOnline = false;
      LOG_INFO("success to init host env = dev (offline)");
      break;
    case PPE: gEnvStr = "stage";
      gEnv = 2;
      gIsOnline = false;
      LOG_INFO("success to init host env = ppe (offline)");
      break;
    case TEST: gEnvStr = "test";
      gEnv = 1;
      gIsOnline = false;
      LOG_INFO("success to init host env = test (offline)");
      break;
    default: LOG_ERROR("fail to init host env.");
      return ERR_INVALID_ENV;
  }
  return SUCCESS;
}

bool GlobalVar::CheckHealthyIPort(std::string *p_str_err) const {
  bool b_gPort = (0 < gPort && 65535 > gPort);
  if (!b_gPort) {
    try {
      p_str_err->append("gPort " + boost::lexical_cast<std::string>(gPort) +
          "invalid;");
    } catch (boost::bad_lexical_cast &e) {
      p_str_err->append(
          "boost::bad_lexical_cast for gPort, reason: " + std::string(e.what()
          ) +
              ";");
    }
  }

  bool b_gIp = !(gIp.empty());
  if (!b_gIp) {
    p_str_err->append("; gIp empty");
  }

  return b_gPort && b_gIp;
}
