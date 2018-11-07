#ifndef GLOBAL_DEF_H_
#define GLOBAL_DEF_H_

#include <string>
#include <comm/inc_comm.h>
#include "SGAgentErr.h"
#include <boost/lexical_cast.hpp>
#include <boost/unordered_map.hpp>
#include <muduo/base/CountDownLatch.h>

typedef enum {
  PROD, STAGING, DEV, PPE, TEST
} Appenv;

typedef struct GlobalVar {
 public:
  GlobalVar();
  int InitEnv(const std::string &env_str, const std::string &deployenv);
  bool CheckHealthyIPort(std::string *p_str_err) const;

  int gOpen;
  int gClose;

  int gEnv;
  std::string gEnvStr;
  Appenv gAppenv;
  bool gIsOnline;

  int gOpenConfig;
  int gOpenMtConfig;
  int gOpenLog;
  int gOpenCommonLog;
  int gCommonToLogCollector;
  int gOpenModuleInvoke;
  int gOpenQuota;
  int gOpenSelfCheck;
  int gOpenFileConfig;
  int gOpenHlb;
  int gOpenAuth;
  int gOpenSwitchEnv;
  int gOpenAutoSwitchStage;
  int gOpenAutoSwitchTest;
  int gOpenProperties;
  int gOpenAutoRoute;
  int gOpenUnifiedProtoChange4LocalAppkey;
  int gOpenUnifiedProtoChange4MTthrift;

  bool isOpenMNSCache;
  bool isOpenMafka;
  bool isNewCloud;

  int gPort;
  std::string gIp;
  std::string gMask;
  std::string gVersion;
  std::string gLogCollectorAppkey;
  std::string gMnscAppkey;
  std::string gSgagentAppkey;
  std::string gAppkeysFile;
} GlobalVar;
#endif // GLOBAL_DEF_H_
