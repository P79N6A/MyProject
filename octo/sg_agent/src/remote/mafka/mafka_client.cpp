#include "mafka_client.h"
#include "Log.h"
#include <cthrift/cthrift_sgagent.h>
#include "remote/falcon/falcon_collector.h"

extern GlobalVar *g_global_var;
namespace sg_agent {

const std::string castlename = "octo";
const std::string appkeyname = "com.sankuai.inf.sg_agent";

//根据环境进行topic区分
const std::string topic_prod_dev = "octo.logCollector.commonlog"; //prod和dev使用相同的topic
const std::string topic_staging = "staging.octo.logCollector.commonlog";
const std::string topic_test = "test.octo.logCollector.commonlog";
const std::string topic_ppe = "ppe.octo.logCollector.commonlog";

class MafkaResponseHandler : public mafka::CompletionHandler {
 public:
  virtual void OnComplete(int error_code, int partition, const char *buf, int len) {
    if (error_code) {
      ERROR("send complete error_code = ", error_code, "partition = ", partition);
      FalconCollector::SetRate("sg_agent.mafka.success.rate", "", false);
    } else {
      DEBUG("send conplete OK!!!, errcode :", error_code, "partition = ", partition);
      FalconCollector::SetRate("sg_agent.mafka.success.rate", "", true);
    }
  }
};

MafkaResponseHandler g_mafka_resp_handler;

MafkaClient *MafkaClient::mafkaClient_ = NULL;

MafkaClient *MafkaClient::getInstance() {
  if (NULL == mafkaClient_) {
    mafkaClient_ = new MafkaClient();
  }
  return mafkaClient_;
}
MafkaClient::MafkaClient() {
  cthrift::CthriftSgagent::SetIsOpenSentinel(false);
  cthrift::CthriftSgagent::SetIsOpenCat(false);
  //cthrift::CthriftSgagent::SetIsOpenMtrace(false);
  mafka::SetupLogger("/var/sankuai/logs/sg_agent/mafka.log");
  std::string topic ="";
  switch(g_global_var->gAppenv){
    case PROD:
    case DEV:{
      topic = topic_prod_dev;
      break;
    }
    case STAGING:{
      topic = topic_staging;
      break;
    }
    case PPE:{
      topic = topic_ppe;
      break;
    }
    case TEST:{
      topic = topic_test;
      break;
    }
    default:{
      topic = topic_prod_dev;
      break;
    }
  }

  producer_ = mafka::NewProducer(appkeyname,topic,castlename, true);
  if (producer_) {
    producer_->ForbidSerializable();
  }
  int retry_count = 0;
  while (!producer_ && retry_count < 3) {
    ERROR("mafka producer init failed! retry again..., retry = ", retry_count);
    usleep(50 * 1000);
    producer_ = mafka::NewProducer(appkeyname, topic, castlename, true);
    if (producer_) {
      producer_->ForbidSerializable();
    }
    ++retry_count;
  }
}

MafkaClient::~MafkaClient() {
}

int MafkaClient::SendAsync(const char *content, int length) {
  FalconCollector::Count("sg_agent.mafka.send.count", "");
  int ret = FAILURE;
  if (producer_) {
    ret = producer_->SendAsync(content, length, g_mafka_resp_handler);
    if (SUCCESS != ret) {
      ERROR("send mafka failed, ret = ", ret);
    }
  } else {
    WARN("Mafka client init fail! we need to check");
    ret = -2;
  }
  return ret;
}

} // namespace
