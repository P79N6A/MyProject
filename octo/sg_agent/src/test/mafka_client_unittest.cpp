// =====================================================================================
//
//       Filename:  main.cpp
//
//    Description:  learn msg
//
//        Version:  1.0
//        Created:  2015-04-17 11时41分55秒
//       Revision:  none
//
// =====================================================================================
#include <gtest/gtest.h>
#include <iostream>
#include "Producer.h"
#include "Log.h"
#include "CompleteHandler.h"

class TestHandler : public mafka::CompletionHandler {
 public:
  virtual void OnComplete(int error_code, int partition, const char *buf, int len) {
    if (error_code) {
      std::cout << "send complete error_code = " << error_code << std::endl;
    }
    std::cout << "send complete error_code = " << error_code << std::endl;
  }
};

TestHandler handler;

class MafkaClient : public testing::Test {
};

TEST_F(MafkaClient, sendAsync) {
  mafka::SetupLogger("/tmp/mafka.log");
  mafka::ProducerPtr producer = mafka::NewProducer("com.sankuai.inf.sg_agent",
                                                   "octo.logCollector.commonlog",
                                                   "common",
																									 true);
  if (!producer) {
    std::cout << "ptr null" << std::endl;
    return;
  }
  const int MAX_COUNT = 5;
  for (int i = 0; i < MAX_COUNT; ++i) {
    std::string content("logCollector commonlog message:");
    int ret = producer->SendAsync(content.c_str(), (int) content.length(), handler);
    EXPECT_EQ(0, ret);
  }

  mafka::ProducerPtr producer_invoker = mafka::NewProducer("com.sankuai.inf.sg_agent",
                                                           "octo.logCollector.uploadInvoker",
                                                           "common",
																													 true);
  if (!producer_invoker) {
    std::cout << "ptr_invoker null" << std::endl;
    return;
  }
  for (int i = 0; i < MAX_COUNT; ++i) {
    std::string content("logCollector commonlog message:");
    int ret = producer_invoker->SendAsync(content.c_str(), (int) content.length(), handler);
    EXPECT_EQ(0, ret);
  }
}
