#include "switch_operate.h"
#include "comm/log4cplus.h"
#include "sg_agent_def.h"
#include "global_def.h"

extern GlobalVar *g_global_var;

namespace sg_agent {

int SGAgentSwitch::initSwitch(const int key, bool &isOpen) {
  int ret = 0;
  if (_checkSwitchFile()) {
    ret = _readSwitchFile(key, isOpen);
    if (0 != ret) {
      LOG_ERROR("readSwitchFile fail, key = " << key << ", value = " << isOpen);
    }
  } else {
    ret = _readDefaultFile(key, isOpen);
    if (0 != ret) {
      LOG_ERROR(
          "readDefaultFile fail, key = " << key << ", value = " << isOpen);
      return ret;
    }

    ret = _createSwitchFile();
    if (0 != ret) {
      LOG_ERROR("createSwitch fail, key = " << key << ", value = " << isOpen);
      return ret;
    }

    ret = _writeSwitchFile(key, isOpen);
  }

  return ret;
}

int SGAgentSwitch::setSwitch(const int key, bool &isOpen) {
  int ret = 0;
  if (isOpen == g_global_var->isOpenMafka) {
    LOG_INFO("the swith is the same, switch = " << isOpen);
  } else {
    LOG_INFO("the swith need to change, current: " << g_global_var->isOpenMafka
                                                   << ", new: " << isOpen);
    if (_checkSwitchFile()) {
      ret = _writeSwitchFile(key, isOpen);
    } else {
      ret = _createSwitchFile();
      if (0 != ret) {
        LOG_ERROR("createSwitch fail, key = " << key << ", value = " << isOpen);
        return ret;
      }

      ret = _writeSwitchFile(key, isOpen);
    }
  }

  //保证写文件成功后，内存值和文件值一致
  if (0 == ret) {
    LOG_INFO("the gloable swith changed, current: " << g_global_var->isOpenMafka
                                                    << ", key: " << key);
    g_global_var->isOpenMafka = isOpen;
  }
  return ret;
}

/**
 * 初始化环境时，判断是否存在开关文件
 **/
bool SGAgentSwitch::_checkSwitchFile() {
  tinyxml2::XMLDocument switchXml;
  tinyxml2::XMLError operateRet
      = switchXml.LoadFile(AGENT_SWITCH_FILE.c_str());
  if (tinyxml2::XML_NO_ERROR != operateRet) {
    LOG_ERROR("agent_switch.xml is not existed");
    return false;
  } else {
    LOG_INFO("These is agent_switch.xml, ");
    return true;
  }
}

int SGAgentSwitch::_readDefaultFile(const int key, bool &isOpen) {
  LOG_INFO(
      "Read switch from sg_agent_mutable.xml: " << key << ", value" << isOpen);

  tinyxml2::XMLDocument mutableConf;
  tinyxml2::XMLError operateRet
      = mutableConf.LoadFile(SG_AGENT_MUTABLE_CONF.c_str());
  if (tinyxml2::XML_NO_ERROR != operateRet) {
    LOG_ERROR("sg_agent_mutable.xml is not existed");
    return -1;
  }

  tinyxml2::XMLElement *agentMutableConf =
      mutableConf.FirstChildElement("SGAgentMutableConf");
  if (NULL == agentMutableConf) {
    LOG_ERROR("can't find SGAgentMutableConf in sg_agent_mutable.xml");
    return -1;
  }

  tinyxml2::XMLElement *agentFun =
      agentMutableConf->FirstChildElement("SGAgentFun");
  if (NULL == agentFun) {
    LOG_ERROR("can't find SGAgentFun in sg_agent_mutable.xml of sg_agent.");
    return -1;
  }

  //TODO:
  tinyxml2::XMLElement *switchKey =
      agentFun->FirstChildElement("OpenMafka");
  if (NULL == switchKey) {
    LOG_ERROR("can't find OpenMafka in " << AGENT_SWITCH_FILE);
    return -1;
  }

  const char *openCache = switchKey->GetText();
  //必须判断NULL
  if (openCache) {
    if (strstr(openCache, "open") != NULL) {
      isOpen = true;
    } else {
      isOpen = false;
    }
    LOG_INFO("RUN current isOpen:" << isOpen);
  }

  return 0;
}

int SGAgentSwitch::_createSwitchFile() {
  tinyxml2::XMLDocument switchXml;
  tinyxml2::XMLNode *sgAgentSwitch = switchXml.NewElement("AgentSwitch");
  switchXml.InsertFirstChild(sgAgentSwitch);

  tinyxml2::XMLError eResult = switchXml.SaveFile(AGENT_SWITCH_FILE.c_str());
  if (tinyxml2::XML_NO_ERROR != eResult) {
    LOG_ERROR("Failed to save " << AGENT_SWITCH_FILE);
    return -1;
  } else {
    LOG_INFO("Succeed to save " << AGENT_SWITCH_FILE);
  }
  return 0;
}

int SGAgentSwitch::_writeSwitchFile(const int key, const bool isOpen) {
  LOG_INFO("write switch into agent_switch.xml, value: " << isOpen);
  tinyxml2::XMLDocument switchXml;
  tinyxml2::XMLError operateRet
      = switchXml.LoadFile(AGENT_SWITCH_FILE.c_str());

  tinyxml2::XMLNode *switchFun =
      switchXml.FirstChild();
  if (NULL == switchFun) {
    LOG_ERROR("can't find AgentSwitch in " << AGENT_SWITCH_FILE);
    return -1;
  }

  tinyxml2::XMLElement *switchKey =
      switchFun->FirstChildElement("OpenMafka");
  if (NULL == switchKey) {
    //不存在这个开关节点，则创建一个
    LOG_INFO("new switch key in " << AGENT_SWITCH_FILE);
    switchKey = switchXml.NewElement("OpenMafka");
    switchFun->InsertEndChild(switchKey);
  }

  switchKey->SetText(isOpen);

  tinyxml2::XMLError eResult = switchXml.SaveFile(AGENT_SWITCH_FILE.c_str());
  if (tinyxml2::XML_NO_ERROR != eResult) {
    LOG_ERROR("Failed to save " << AGENT_SWITCH_FILE);
    return -1;
  } else {
    LOG_INFO("Succeed to save " << AGENT_SWITCH_FILE);
  }
  return 0;
}

int SGAgentSwitch::_readSwitchFile(const int key, bool &isOpen) {
  LOG_INFO("Read switch from agent_switch.xml, value: " << isOpen);
  tinyxml2::XMLDocument switchXml;
  tinyxml2::XMLError operateRet
      = switchXml.LoadFile(AGENT_SWITCH_FILE.c_str());

  tinyxml2::XMLElement *switchFun =
      switchXml.FirstChildElement("AgentSwitch");
  if (NULL == switchFun) {
    LOG_ERROR("can't find AgentSwitch in " << AGENT_SWITCH_FILE);
    return -1;
  }

  tinyxml2::XMLElement *switchKey =
      switchFun->FirstChildElement("OpenMafka");
  if (NULL == switchKey) {
    LOG_ERROR("can't find OpenMafka in " << AGENT_SWITCH_FILE);
    return -1;
  }

  const char *openCache = switchKey->GetText();
  if (openCache) {
    isOpen = atoi(openCache);
  }

  return 0;
}

}//namespace
