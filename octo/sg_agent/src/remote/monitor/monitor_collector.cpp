/**
 *@给cplugin收集监控信息
 *@
 */
#include "monitor_collector.h"

using namespace sg_agent;

const int PID_ITEM = 0;
const int VMRSS_ITEM = 1;
const int VERSION_ITEM = 2;
const int CPU_ITEM = 3;
const int FILECONFIG_ITEM = 4;
const int KVCONFIG_ITEM = 5;
const int COMMON_LOG_ITEM = 6;
const int ROUTE_LIST_ITEM = 7;
const int SERVICE_LIST_ITEM = 8;
const int MODULE_INVOKER_ITEM = 9;
const int REGISTER_ITEM = 10;
const int MCC_FCONFIG_ALLREQ_ITEM = 11;
const int MCC_FCONFIG_SUCCESSREQ_ITEM = 12;
const int MCC_CONFIG_ALLREQ_ITEM = 13;
const int MCC_CONFIG_SUCCESSREQ_ITEM = 14;
const int MNSC_ALLREQ_ITEM = 15;
const int MNSC_SUCCESSREQ_ITEM = 16;

const int MAX_MONITOER_ITEM = 16;


extern GlobalVar *g_global_var;
SgMonitorCollector *SgMonitorCollector::s_instance = NULL;
muduo::MutexLock SgMonitorCollector::s_cmutex;

SgMonitorCollector *SgMonitorCollector::GetInstance() {
  if (NULL == s_instance) {
    muduo::MutexLockGuard lock(s_cmutex);
    if (NULL == s_instance) {
      s_instance = new SgMonitorCollector();
    }
  }
  return s_instance;
}

float SgMonitorCollector::CalcuProcCpuUtil(const int& pid) {

  float proc_cpu_util = 0.0;
  unsigned long total_cpu_delta = GetTotalCpuTime() - s_cpuJiffValue.total_cpu_delta;
  unsigned long proc_cpu_delta = GetProcCpuTime(pid) - s_cpuJiffValue.proc_cpu_delta;
  LOG_INFO("proc_cpu_delta: " << proc_cpu_delta << "total_cpu_delta: " << total_cpu_delta);
  std::string test_cpu = "";
  if (0 != total_cpu_delta) {
    proc_cpu_util = (float) (100 * sysconf(_SC_NPROCESSORS_CONF) * proc_cpu_delta) / (float) total_cpu_delta;
    s_cpuJiffValue.total_cpu_delta = GetTotalCpuTime();
    s_cpuJiffValue.proc_cpu_delta = GetProcCpuTime(pid);
    LOG_INFO("the sg_agent proc cpu util: " << proc_cpu_util);
  } else {
    LOG_ERROR("it is first to calcu proc cpu util,value is empty");
  }

  LOG_INFO("the sg_agent proc cpu util: " << Round(proc_cpu_util, 2));
  return Round(proc_cpu_util, 2);
}
int SgMonitorCollector::DoInitMonitorInfo() {

  if (!m_has_init) {
    config_collector = new MtConfigCollector();
    m_metric_value[PID_ITEM] = "sg_agent.pid";
    m_metric_value[VMRSS_ITEM] = "sg_agent.vmRss";
    m_metric_value[VERSION_ITEM] = "sg_agent.version";
    m_metric_value[CPU_ITEM] = "sg_agent.cpu";
    m_metric_value[FILECONFIG_ITEM] = "sg_agent.fileConfigQueueLen";
    m_metric_value[KVCONFIG_ITEM] = "sg_agent.kvConfigQueueLen";
    m_metric_value[COMMON_LOG_ITEM] = "sg_agent.logQueueLen";
    m_metric_value[ROUTE_LIST_ITEM] = "sg_agent.routeListQueueLen";
    m_metric_value[SERVICE_LIST_ITEM] = "sg_agent.serviceListQueueLen";
    m_metric_value[MODULE_INVOKER_ITEM] = "sg_agent.invokerQueueLen";
    m_metric_value[REGISTER_ITEM] = "sg_agent.registerQueueLen";
    m_metric_value[MCC_FCONFIG_ALLREQ_ITEM] = "sg_agent.mccFileConfigAllReq";
    m_metric_value[MCC_FCONFIG_SUCCESSREQ_ITEM] = "sg_agent.mccFileConfigSuccessReq";
    m_metric_value[MCC_CONFIG_ALLREQ_ITEM] = "sg_agent.mccAllReq";
    m_metric_value[MCC_CONFIG_SUCCESSREQ_ITEM] = "sg_agent.mccSucessReq";
    m_metric_value[MNSC_ALLREQ_ITEM] = "sg_agent.mnscAllReq";
    m_metric_value[MNSC_SUCCESSREQ_ITEM] = "sg_agent.mnscSuccessReq";
    s_cpuJiffValue.proc_cpu_delta = GetProcCpuTime(getpid());
    s_cpuJiffValue.total_cpu_delta = GetTotalCpuTime();

    GetEndPoint(m_end_point);
    m_has_init = true;
  }
  return SUCCESS;
}
int SgMonitorCollector::GetCollectorMonitorInfo(std::string &mInfo) {

  DoInitMonitorInfo();

  char *out;
  cJSON *json = cJSON_CreateObject();
  if (!json) {
    LOG_ERROR("json is NULL, create json_object failed.");
    return FAILURE;
  }
  cJSON *all_info_json = cJSON_CreateArray();
  if (!all_info_json) {
    LOG_ERROR("all_srvlist_json is NULL, create json_object failed.");
    cJSON_AddNumberToObject(json, "ret", HTTP_INNER_ERROR);
    mInfo = cJSON_Print(json);
    cJSON_Delete(json);
    return FAILURE;
  }
  cJSON_AddNumberToObject(json, "ret", HTTP_RESPONSE_OK);
  cJSON_AddStringToObject(json, "retMsg", "success");

  CountRequest::GetInstance()->GetReqData(m_monitor_data);
  for (int iter = 0; iter <= MAX_MONITOER_ITEM; iter++) {
    CollectorInfo2Json(json, all_info_json, iter);
  }
  cJSON_AddItemToObject(json, "data", all_info_json);
  out = cJSON_Print(json);
  mInfo = out;
  SAFE_FREE(out);
  cJSON_Delete(json);
  LOG_INFO("CollectorInfo2Json success");

  return SUCCESS;
}
int64_t SgMonitorCollector::GetTimeStamp() {

  timeval cur_time;
  gettimeofday(&cur_time, NULL);

  return ((int64_t) cur_time.tv_sec);

}
void SgMonitorCollector::GetEndPoint(std::string &end_point) {

  char host_name[256] = {0};
  gethostname(host_name, sizeof(host_name));
  std::string host_name_str(host_name);
  LOG_INFO("end point" << host_name_str);
  end_point = (std::string::npos != host_name_str.find(".sankuai.com")
      || std::string::npos != host_name_str.find(".office.mos")) ? host_name_str.substr(0, host_name_str.find("."))
                                                                 : host_name_str;
  if (end_point.empty() || "unknown" == end_point) {
    LOG_WARN("fail to init monitor endpoint");
  }
}
int SgMonitorCollector::CollectorInfo2Json(cJSON *json, cJSON *json_arrary, int type) {

  if (NULL == json || NULL == json_arrary) {
    LOG_ERROR("the json object is null");
    return FAILURE;
  }
  std::string mon_info = "";
  cJSON *root;
  char *out;
  root = cJSON_CreateObject();
  if (NULL == root) {
    LOG_ERROR("the create json object is failed");
    return FAILURE;
  }
  cJSON_AddStringToObject(root, "endpoint", m_end_point.c_str());
  cJSON_AddStringToObject(root, "metric", m_metric_value.at(type).c_str());
  cJSON_AddNumberToObject(root, "timestamp", GetTimeStamp());
  cJSON_AddNumberToObject(root, "step", 60);
  SetValueByType(root, type);
  cJSON_AddStringToObject(root, "counterType", "GAUGE");
  cJSON_AddStringToObject(root, "tags", "sg_agent");
  out = cJSON_Print(root);
  mon_info = out;
  SAFE_FREE(out);
  cJSON_AddItemToArray(json_arrary, root);

  return SUCCESS;

}
void SgMonitorCollector::SetValueByType(cJSON *root, int type) {

  int ret = SUCCESS;
  pid_t sg_pid = getpid();
  switch (type) {
    case PID_ITEM: {
      cJSON_AddNumberToObject(root, "value", getpid());
      break;
    }
    case VMRSS_ITEM: {
      cJSON_AddNumberToObject(root, "value", GetProcMemUtil(sg_pid));
      break;
    }
    case VERSION_ITEM: {
      cJSON_AddStringToObject(root, "value", g_global_var->gVersion.c_str());
      break;
    }
    case CPU_ITEM: {
      cJSON_AddNumberToObject(root, "value", CalcuProcCpuUtil(sg_pid));
      break;
    }
    case FILECONFIG_ITEM: {
      cJSON_AddNumberToObject(root, "value", FalconMgr::GetFileConfigQueueSize());
      break;
    }
    case KVCONFIG_ITEM: {
      cJSON_AddNumberToObject(root, "value", FalconMgr::GetKvConfigQueueSize());
      break;
    }
    case COMMON_LOG_ITEM: {
      cJSON_AddNumberToObject(root, "value", FalconMgr::GetCommonLogQueueSize());
      break;
    }
    case ROUTE_LIST_ITEM: {
      cJSON_AddNumberToObject(root, "value", FalconMgr::GetRouteListQueueSize());
      break;
    }
    case SERVICE_LIST_ITEM: {
      cJSON_AddNumberToObject(root, "value", FalconMgr::GetServiceListQueueSize());
      break;
    }
    case MODULE_INVOKER_ITEM: {
      cJSON_AddNumberToObject(root, "value", FalconMgr::GetModuleInvokerQueueSize());
      break;
    }
    case REGISTER_ITEM: {
      cJSON_AddNumberToObject(root, "value", FalconMgr::GetRegisteQueueSize());
      break;
    }
    case MCC_FCONFIG_ALLREQ_ITEM: {
      cJSON_AddNumberToObject(root, "value", m_monitor_data.at("allfconfig"));
      break;
    }
    case MCC_FCONFIG_SUCCESSREQ_ITEM: {
      cJSON_AddNumberToObject(root, "value", m_monitor_data.at("fconfig"));
      break;
    }
    case MCC_CONFIG_ALLREQ_ITEM: {
      cJSON_AddNumberToObject(root, "value", m_monitor_data.at("allconfig"));
      break;
    }
    case MCC_CONFIG_SUCCESSREQ_ITEM: {
      cJSON_AddNumberToObject(root, "value", m_monitor_data.at("config"));
      break;
    }
    case MNSC_ALLREQ_ITEM: {
      cJSON_AddNumberToObject(root, "value", m_monitor_data.at("allmnsc"));
      break;
    }
    case MNSC_SUCCESSREQ_ITEM: {
      cJSON_AddNumberToObject(root, "value", m_monitor_data.at("mnsc"));
      break;
    }
    default: {
      LOG_INFO("unkown collector type");
    }
  }
}

