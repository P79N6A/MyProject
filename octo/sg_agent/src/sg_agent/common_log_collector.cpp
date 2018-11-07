// =====================================================================================
//
//      Filename:  log_collector.cpp
//
//      Description: Send log info to server.
//
//      Version:  1.0
//      Created:  2015-04-18
//      Revision:  none
//
// =====================================================================================

#include "common_log_collector.h"
#include <dlfcn.h>
#include <boost/bind.hpp>
#include <remote/falcon/falcon_collector.h>
#include "remote/mafka/mafka_client.h"
#include "util/falcon_mgr.h"

#include "mns/mns_iface.h"
#include "util/global_def.h"
#include "util/sgagent_stat.h"
#include "sgcommon_invoker.h"

extern void *g_handle;
extern GlobalVar *g_global_var;

using namespace muduo::net;

namespace sg_agent {

static const size_t kMaxPendingTasks = 1000;

CommonLogCollector *CommonLogCollector::mCommonLogCollector = NULL;

CommonLogCollector *CommonLogCollector::getInstance() {
  if (NULL == mCommonLogCollector) {
    mCommonLogCollector = new CommonLogCollector();
  }
  return mCommonLogCollector;
}

void CommonLogCollector::Destroy() {
  SAFE_DELETE(mCommonLogCollector);
}

CommonLogCollector::CommonLogCollector()
    : loop_(NULL),
      loop_thread_(NULL),
      retry_(DEFAULT_SENDCOMMONLOG_RETRY) {
}

CommonLogCollector::~CommonLogCollector() {
  EventLoopThreadProxyDestroyer(loop_thread_);
}

int CommonLogCollector::init(int retry) {
  retry_ = retry;
  loop_thread_ = EventLoopThreadProxyMaker();
  if (NULL == loop_thread_) {
    LOG_ERROR("fail to init loop_thread_");
    return -1;
  }
  // 启动worker处理线程
  loop_ = loop_thread_->startLoop();
  return 0;
}

int CommonLogCollector::sendCommonLogs(const CommonLog &oCommonLog) {
  //拷贝用户数据
  CommonLog tmp;
  tmp.cmd = oCommonLog.cmd;
  tmp.content = oCommonLog.content;
  tmp.extend = oCommonLog.extend;

  LOG_DEBUG("send CommonLog To workr handler. cmd = " << oCommonLog.cmd);
  FalconCollector::Count("sg_agent.commonlog.count", "");

  size_t pending_tasks_size = loop_->queueSize();
  FalconMgr::SetCommonLogQueueSize(pending_tasks_size);

  boost::shared_ptr<CommonLogHandler> task(new CommonLogHandler(this, tmp));
  if (pending_tasks_size < kMaxPendingTasks) {
    loop_->runInLoop(task);
    FalconCollector::SetRate("sg_agent.commonlog.drop.rate", "", false);
  } else {
    FalconCollector::SetRate("sg_agent.commonlog.drop.rate", "", true);
    return FAILURE;
  }

  return 0;
}
//根据gOpenCommonLog字段确认
void CommonLogCollector::CommonLogBackendHandler(CommonLog oCommonLog) {

  if((7 != oCommonLog.cmd) ||(1 == g_global_var->gCommonToLogCollector && 7 == oCommonLog.cmd)){
    int ret = logCollector_.sendCommonLogs(oCommonLog);
    if (ret != 0) {
      LOG_WARN("worker handler send CommonLog To LogCollector. ret = " << ret << ", cmd = " << oCommonLog.cmd);
      FalconCollector::SetRate("sg_agent.logcollector.success.rate", "", false);
    } else {
      FalconCollector::SetRate("sg_agent.logcollector.success.rate", "", true);
    }
  }

  if (oCommonLog.cmd == 7 && g_global_var->isOpenMafka) {
    MafkaClient::getInstance()->SendAsync(oCommonLog.content.c_str(), (int) oCommonLog.content.length());
  }

}

int CommonLogCollector::sendModuleInvokeInfo(const SGModuleInvokeInfo &oInfo) {
  //拷贝用户数据
  SGModuleInvokeInfo tmp;
  tmp.traceId = oInfo.traceId;
  tmp.spanId = oInfo.spanId;
  tmp.spanName = oInfo.spanName;
  tmp.localAppKey = oInfo.localAppKey;
  tmp.localHost = oInfo.localHost;
  tmp.localPort = oInfo.localPort;
  tmp.remoteAppKey = oInfo.remoteAppKey;
  tmp.remoteHost = oInfo.remoteHost;
  tmp.remotePort = oInfo.remotePort;
  tmp.start = oInfo.start;
  tmp.cost = oInfo.cost;
  tmp.type = oInfo.type;
  tmp.status = oInfo.status;
  tmp.count = oInfo.count;
  tmp.debug = oInfo.debug;
  tmp.extend = oInfo.extend;

  size_t pending_tasks_size = loop_->queueSize();
  FalconMgr::SetModuleInvokerQueueSize(pending_tasks_size);

  boost::shared_ptr<ModuleInvokerHandler> task(new ModuleInvokerHandler(this, tmp));
  if (pending_tasks_size < kMaxPendingTasks) {
    loop_->runInLoop(task);
  } else {
    LOG_WARN("sendModuleInvokeInfo backend thread overload, task queue size: "
                 << pending_tasks_size);
    return -1;
  }

  return 0;
}

void CommonLogCollector::ModuleInvokerBackendHandler(SGModuleInvokeInfo oInfo) {
  int ret = logCollector_.sendModuleInvokeInfo(oInfo);
  if (ret != 0) {
    LOG_WARN("worker handler send ModuleInvoker To LogCollector. ret = " << ret
                                                                         << ", traceId = " << oInfo.traceId
                                                                         << ", localAppKey = " << oInfo.localAppKey
                                                                         << ", remoteAppKey = " << oInfo.remoteAppKey);
  }

  //TODO:
  LOG_DEBUG("worker handler send ModuleInvoker To LogCollector. ret = " << ret
                                                                        << ", traceId = " << oInfo.traceId);
}

} // namespace
