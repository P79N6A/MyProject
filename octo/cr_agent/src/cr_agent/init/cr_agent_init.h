//
// Created by smartlife on 2017/8/7.
//

#ifndef CRANE_AGENT_CR_AGENT_INIT_H
#define CRANE_AGENT_CR_AGENT_INIT_H
#include "../../comm/log4cplus.h"
#include "cr_agent_def.h"
#include "../../comm/tinyxml2.h"
#include "../../util/cr_common.h"

class CrAgentInit {

 public:
  CrAgentInit(){};
  ~CrAgentInit(){};

 public:
  static int CheckHealthy(void);
  static int InitEnv();
 private:

	static int m_EnvHealth;

};
#endif //CRANE_AGENT_CR_AGENT_INIT_H
