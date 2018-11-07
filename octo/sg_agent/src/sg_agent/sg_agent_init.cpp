//
// Created by Lhmily on 07/05/2017.
//

#include <boost/algorithm/string/trim.hpp>
#include "comm/inc_comm.h"
#include "boost/bind.hpp"
#include "sg_agent_init.h"
#include "util/sg_agent_def.h"
#include "util/sg_agent_env.h"
#include "util/switch_operate.h"
#include "util/global_def.h"
#include "zk_tools.h"
#include "operation_common_types.h"
#include "regist_client.h"
extern GlobalVar *g_global_var;

namespace sg_agent {

//not init sg_agent_sub_loop_.
SgAgentInit::SgAgentInit() : healthy_countdownlatch(1), sg_agent_sub_loop_(NULL) {
  healthy_interval_secs = kHealthyCheckMaxInterval;
  g_global_var = new GlobalVar();
  healthy_loop_ = healthy_thread_.startLoop();
  sg_agent_sub_loop_ = sg_agent_sub_thread_.startLoop();
}

const tinyxml2::XMLElement *
SgAgentInit::FetchElemByTinyXML(const tinyxml2::XMLElement *&p_xml_elem,
                                const std::string &str_key) throw(exception) {
  const tinyxml2::XMLElement *p_xml_elem_key = p_xml_elem->FirstChildElement(str_key.c_str());
  if (!p_xml_elem_key) {
    LOG_ERROR("Not find " << str_key);
    //boost::throw_exception("Not find" + str_key);
    throw std::logic_error("Not find" + str_key);
  }

  return p_xml_elem_key;
}

std::string
SgAgentInit::FetchElemValByTinyXML(const tinyxml2::XMLElement
                                   *&p_xml_elem,
                                   const std::string &str_key)throw(exception) {
  const tinyxml2::XMLElement *p_xml_elem_key =
      FetchElemByTinyXML(p_xml_elem, str_key); //already check NULL inside
  LOG_DEBUG("Fetch " << str_key << " value: " << p_xml_elem_key->GetText());

  return std::string(p_xml_elem_key->GetText());
}

int SgAgentInit::HandleSgagentMutableFile(void) {
  tinyxml2::XMLDocument sg_agent_mutable;
  const tinyxml2::XMLError sg_agent_mutable_xml_ret =
      sg_agent_mutable.LoadFile(sg_agent::SG_AGENT_MUTABLE_CONF.c_str());
  if (unlikely(tinyxml2::XML_SUCCESS != sg_agent_mutable_xml_ret)) {
    LOG_ERROR("fail to load " << sg_agent::SG_AGENT_MUTABLE_CONF << ", ret = "
                              << sg_agent_mutable_xml_ret);
    return FAILURE;
  }

  // 初始化ZK
  int iZKInitRes = ZkTools::Init(sg_agent_mutable, "/mns/sankuai/" +
      g_global_var->gEnvStr);
  if (SUCCESS != iZKInitRes) {
    LOG_ERROR("failed to init Zk, ret = " << iZKInitRes);
    return FAILURE;
  }

  const tinyxml2::XMLElement *agentMutableConf =
      sg_agent_mutable.FirstChildElement("SGAgentMutableConf");
  if (unlikely(NULL == agentMutableConf)) {
    LOG_ERROR(
        "can't find SGAgentMutableConf in " << sg_agent::SG_AGENT_MUTABLE_CONF);
    return FAILURE;
  }

  g_global_var->isNewCloud = (NULL != agentMutableConf->FirstChildElement("NewCloud"));  //check if offline cloud host
  LOG_INFO("get isNewCloud = " << g_global_var->isNewCloud);

  try {
    //handle SGAgentMutableConf first class
    std::string str_tmp(FetchElemValByTinyXML(agentMutableConf, "ClientPort"));
    g_global_var->gPort = atoi(str_tmp.c_str());
    LOG_INFO("get gPort = " << str_tmp);

    //fetch appkey of logCollector
    g_global_var->gLogCollectorAppkey.assign(FetchElemValByTinyXML(agentMutableConf, "RemoteLogAppkey"));

    LOG_INFO("get gLogCollectorAppkey = " << g_global_var->gLogCollectorAppkey);
    if (unlikely((g_global_var->gLogCollectorAppkey).empty())) {    //TODO necessary?
      LOG_ERROR("can't find logCollector Appkey in "
                    << sg_agent::SG_AGENT_MUTABLE_CONF);
      return FAILURE;
    }
		g_global_var->gMnscAppkey.assign(FetchElemValByTinyXML(agentMutableConf, "MNSCacheAppkey"));                                                                                                      
		if (unlikely((g_global_var->gMnscAppkey).empty())) {    
			LOG_ERROR("can't find MNSCacheAppkey in "
					<< sg_agent::SG_AGENT_MUTABLE_CONF);
			return FAILURE;
		}

    //读取sg_agent版本信息和appkey信息
    (g_global_var->gSgagentAppkey).assign(FetchElemValByTinyXML(agentMutableConf,
                                                                "SGAgentAppKey"));
    LOG_INFO("current sgagent appkey: " << g_global_var->gSgagentAppkey);

    (g_global_var->gVersion).assign(FetchElemValByTinyXML(agentMutableConf,
                                                          "SGAgentVersion"));
    LOG_INFO("current sgagent version: " << g_global_var->gVersion);

    // init hotel register white lists
    const tinyxml2::XMLElement *hotel_appkeys = agentMutableConf->FirstChildElement("HotelTravelAppkeys");
    if (NULL != hotel_appkeys) {
      // HotelTravelAppkeys 只为了防止酒旅的appkey的误注册问题，应兼容配置文件中没有该配置
      const tinyxml2::XMLElement *appkey_item = hotel_appkeys->FirstChildElement("Item");
      std::set<std::string> hotel_white_appkeys;
      LOG_INFO("HotelTravelAppkeys:")
      while (NULL != appkey_item) {
        std::string hotel_appkey_str(appkey_item->GetText());
        boost::trim(hotel_appkey_str);
        if (!hotel_appkey_str.empty()) {
          // ignore the empty appkey.
          hotel_white_appkeys.insert(hotel_appkey_str);
          LOG_INFO("\t\t" << hotel_appkey_str);
        }

        appkey_item = appkey_item->NextSiblingElement("Item");
      }
      const tinyxml2::XMLElement *hotel_idcs = agentMutableConf->FirstChildElement("HotelTravelRegisterIDC");
      if (NULL != hotel_idcs) {
        const tinyxml2::XMLElement *idc_item = hotel_idcs->FirstChildElement("Item");
        std::vector<boost::shared_ptr<IDC> > idcs;
        LOG_INFO("HotelTravelRegisterIDC");
        while (NULL != idc_item) {
          const tinyxml2::XMLElement *ip_ptr = idc_item->FirstChildElement("IP");
          const tinyxml2::XMLElement *mask_ptr = idc_item->FirstChildElement("MASK");
          if (NULL != ip_ptr && NULL != mask_ptr) {
            std::string ip_str(ip_ptr->GetText());
            std::string mask_str(mask_ptr->GetText());
            boost::trim(ip_str);
            boost::trim(mask_str);
            if ((!ip_str.empty()) && (!mask_str.empty())) {
              boost::shared_ptr<IDC> idc(new IDC());
              idc->set_ip(ip_str);
              idc->set_mask(mask_str);
              LOG_INFO("\t\tIP = " << ip_str << ", MASK = " << mask_str);
              idcs.push_back(idc);
            } else {
              LOG_WARN("\t\tIP or MASK is empty");
            }
          } else {
            LOG_WARN("\t\tIP or MASK miss.");
          }
          idc_item = idc_item->NextSiblingElement("Item");
        }

        // init register white list
        if (!hotel_white_appkeys.empty() && !idcs.empty()) {
          RegistClient::getInstance()->SetHotelRegisterWhiteList(hotel_white_appkeys, idcs);
        }
      }

    }

    //handle SGAgentFun sub tree
    const tinyxml2::XMLElement
        *agentFun = FetchElemByTinyXML(agentMutableConf, "SGAgentFun");

    g_global_var->gOpenSwitchEnv =
        (strstr(FetchElemValByTinyXML(agentFun, "OpenSwitchEnv").c_str(), "open")) ? 1 : 0;  //TODO compare nocase
    LOG_INFO("get OpenSwitchEnv = " << g_global_var->gOpenSwitchEnv);

    g_global_var->gOpenAutoSwitchStage =
        (strstr(FetchElemValByTinyXML(agentFun, "OpenAutoSwitchStage").c_str(),
                "open")) ? 1 : 0;
    LOG_INFO(
        "get gOpenAutoSwitchStage = " << g_global_var->gOpenAutoSwitchStage);

    g_global_var->gOpenAutoSwitchTest =
        (strstr(FetchElemValByTinyXML(agentFun, "OpenAutoSwitchTest").c_str(),
                "open")) ? 1 : 0;
    LOG_INFO("get gOpenAutoSwitchTest = " << g_global_var->gOpenAutoSwitchTest);

    g_global_var->gOpenAutoRoute =
        (strstr(FetchElemValByTinyXML(agentFun, "OpenAutoRoute").c_str(),
                "close")) ? 0 : 1;
    LOG_INFO("get gOpenAutoRoute = " << g_global_var->gOpenAutoRoute);

    //读取降级配置，是否关闭getLocalConfig功能
    g_global_var->gOpenConfig =
        (strstr(FetchElemValByTinyXML(agentFun, "OpenConfig").c_str(), "open"))
        ? 1 : 0;
    LOG_INFO("get gOpenConfig = " << g_global_var->gOpenConfig);

    //读取降级配置，是否关闭getConfig功能
    g_global_var->gOpenMtConfig =
        (strstr(FetchElemValByTinyXML(agentFun, "OpenMtConfig").c_str(),
                "open")) ? 1 : 0;
    LOG_INFO("get gOpenMtConfig = " << g_global_var->gOpenMtConfig);

    //读取降级配置，是否关闭通用日志上报功能
    g_global_var->gOpenCommonLog =
        (strstr(FetchElemValByTinyXML(agentFun, "OpenCommonLog").c_str(),
                "open")) ? 1 : 0;
    LOG_INFO("get gOpenCommonLog = " << g_global_var->gOpenCommonLog);

    //CommonLogSwitch开关，是否发送数据到commonlog
    g_global_var->gCommonToLogCollector =
        (strstr(FetchElemValByTinyXML(agentFun, "CommonlogToLogColletor").c_str(),
                "open")) ? 1 : 0;
    LOG_INFO("get CommonlogToLogColletor = " << g_global_var->gCommonToLogCollector);

    //读取降级配置，是否关闭quota功能
    g_global_var->gOpenQuota =
        (strstr(FetchElemValByTinyXML(agentFun, "OpenQuota").c_str(), "open"))
        ? 1 : 0;
    LOG_INFO("get gOpenQuota = " << g_global_var->gOpenQuota);

    //读取降级配置，是否关闭模块调用上报功能
    g_global_var->gOpenModuleInvoke =
        (strstr(FetchElemValByTinyXML(agentFun, "OpenModuleInvoke").c_str(),
                "open")) ? 1 : 0;
    LOG_INFO("get gOpenModuleInvoke = " << g_global_var->gOpenModuleInvoke);

    //读取降级配置，是否关闭自检调用上报功能
    g_global_var->gOpenSelfCheck =
        (strstr(FetchElemValByTinyXML(agentFun, "OpenSelfCheck").c_str(),
                "open")) ? 1 : 0;
    LOG_INFO("get gOpenSelfCheck = " << g_global_var->gOpenSelfCheck);

    //读取降级配置，是否关闭文件配置功能
    g_global_var->gOpenFileConfig =
        (strstr(FetchElemValByTinyXML(agentFun, "OpenFileConfig").c_str(),
                "open")) ? 1 : 0;
    LOG_INFO("get gOpenFileConfig = " << g_global_var->gOpenFileConfig);

    g_global_var->gOpenHlb =
        (strstr(FetchElemValByTinyXML(agentFun, "OpenHlb").c_str(), "open")) ? 1
                                                                             : 0;
    LOG_INFO("get gOpenHlb = " << g_global_var->gOpenHlb);

    //读取降级配置，是否关闭服务访问控制功能
    g_global_var->gOpenAuth =
        (strstr(FetchElemValByTinyXML(agentFun, "OpenAuth").c_str(), "open"))
        ? 1 : 0;
    LOG_INFO("get gOpenAuth = " << g_global_var->gOpenAuth);

    //读取unifiedProto切换配置，是否关闭环境切换功能
    g_global_var->gOpenUnifiedProtoChange4LocalAppkey =
        (strstr(FetchElemValByTinyXML(agentFun,
                                      "OpenUnifiedProtoChange4LocalAppkey").c_str(),
                "open")) ? 1 : 0;
    LOG_INFO("get gOpenUnifiedProtoChange4LocalAppkey = "
                 << g_global_var->gOpenUnifiedProtoChange4LocalAppkey);

    //读取unifiedProto切换配置，是否关闭环境切换功能
    g_global_var->gOpenUnifiedProtoChange4MTthrift =
        (strstr(FetchElemValByTinyXML(agentFun,
                                      "OpenUnifiedProtoChange4MTthrift").c_str(),
                "open")) ? 1 : 0;
    LOG_INFO("get gOpenUnifiedProtoChange4MTthrift = "
                 << g_global_var->gOpenUnifiedProtoChange4MTthrift);
  } catch (boost::exception &e) {
    return FAILURE;  //already log inside
  }

  return SUCCESS;
}

int8_t SgAgentInit::FetchLocalAddrInfo(void) {
  char ip[INET_ADDRSTRLEN] = {0};
  char mask[INET_ADDRSTRLEN] = {0};

  int iRet = getIntranet(ip, mask); // mask default = 255.255.0.0
  if (unlikely(SUCCESS != iRet)) {
    LOG_ERROR("failed to get IP, Mask  by getIntranet, ret = " << iRet);

    // 使用host方式获取IP
    iRet = getHost(g_global_var->gIp);
    if (unlikely(SUCCESS != iRet)) {
      LOG_ERROR("failed to get IP by getHost, ret = " << iRet);
      return -1;
    }

    LOG_INFO("get local IP = " << g_global_var->gIp);
  } else {
    g_global_var->gIp = std::string(ip);
    g_global_var->gMask = std::string(mask);
    LOG_INFO("get local IP = " << g_global_var->gIp
                               << ", Mask = " << g_global_var->gMask);
  }

  return SUCCESS;
}

int8_t SgAgentInit::LoadMafkaSwitchInfo(void) {  //TODO need optimize implement inside
  int ret = sg_agent::SGAgentSwitch::initSwitch(Switch::SwitchMafka,
                                                g_global_var->isOpenMafka);
  if (0 != ret) {
    LOG_ERROR(
        "init mafka switch fail! key = " << Switch::SwitchMafka << ", value = "
                                         << g_global_var->isOpenMafka
                                         << ", ret = " << ret);
    return -1;
  }
  LOG_INFO("init mafka switch ok! key = " << Switch::SwitchMafka << ", value = "
                                          << g_global_var->isOpenMafka);

  return 0;
}

void SgAgentInit::HandleCheckHealthyFailed(void) {
  //5 time backoff
  healthy_interval_secs =
      kHealthyCheckMaxInterval < healthy_interval_secs * 5 ?
      kHealthyCheckMaxInterval : healthy_interval_secs * 5;  //keep max
  // value low avoid overflow for int16_t

  LOG_ERROR("CheckHealthy failed, will check after " << healthy_interval_secs
                                                     << " secs");

  healthy_loop_->runAfter(healthy_interval_secs,
                          boost::bind(&SgAgentInit::CheckHealthy,
                                      this)); //keep check
}

//check by order, once one failed, stop and quit, since one may relay on
// last variable
bool SgAgentInit::CheckHealthy(void) {
  static bool b_appenv_ok = false;  //static for keep status, avoid duplicate work when retry
  if (!b_appenv_ok) {
    b_appenv_ok = (SUCCESS == SGAgentEnv::InitEnv());
    if (unlikely(!b_appenv_ok)) {
      LOG_ERROR("fail to init env");
      HandleCheckHealthyFailed();
      return false;
    }
  }

  static bool b_fetch_machine_info_ok = false;
  if (!b_fetch_machine_info_ok) {
    b_fetch_machine_info_ok = (SUCCESS == FetchLocalAddrInfo());
    if (unlikely(!b_fetch_machine_info_ok)) {
      LOG_ERROR("fail to FetchLocalAddrInfo");
      HandleCheckHealthyFailed();
      return false;
    }
  }

  //TODO need fetch idc handle logic here together
  static bool b_idc_xml_load_ok = false;
  if (!b_idc_xml_load_ok) {
    tinyxml2::XMLDocument idc_xml;
    int idc_xml_load_ret = idc_xml.LoadFile((sg_agent::kStrIDCFileFullPath).c_str());
    b_idc_xml_load_ok = (tinyxml2::XML_SUCCESS == idc_xml_load_ret);
    if (unlikely(!b_idc_xml_load_ok)) {
      LOG_ERROR(
          "fail to load " << sg_agent::kStrIDCFileFullPath << ", ret = "
                          << idc_xml_load_ret);
      HandleCheckHealthyFailed();
      return false;
    }
  }

  static bool b_sgagent_mutable_file_load_ok = false;
  if (!b_sgagent_mutable_file_load_ok) {
    b_sgagent_mutable_file_load_ok = (SUCCESS == HandleSgagentMutableFile());
    if (unlikely(!b_sgagent_mutable_file_load_ok)) {
      LOG_ERROR("fail to load " << sg_agent::SG_AGENT_MUTABLE_CONF);

      HandleCheckHealthyFailed();
      return false;
    }
  }

  static bool b_load_mafka_switch_info_ok = false;
  if (!b_load_mafka_switch_info_ok) {
    b_load_mafka_switch_info_ok = (SUCCESS == LoadMafkaSwitchInfo());
    if (unlikely(!b_load_mafka_switch_info_ok)) {
      LOG_ERROR("fail to FetchLocalAddrInfo");

      HandleCheckHealthyFailed();
      return false;
    }
  }

  LOG_INFO("CheckHealthy success");    //once success, NO more check, TODO
  if (unlikely(1 == healthy_countdownlatch.getCount())) {
    healthy_countdownlatch.countDown();
  }

  return true;

}

}

