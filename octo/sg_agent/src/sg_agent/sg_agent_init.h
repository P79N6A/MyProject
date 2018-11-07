//
// Created by Lhmily on 07/05/2017.
//

#ifndef SG_AGENT_SG_AGENT_INIT_H
#define SG_AGENT_SG_AGENT_INIT_H
#include <muduo/net/EventLoop.h>
#include <muduo/net/EventLoopThread.h>
#include <muduo/net/EventLoopThreadPool.h>
#include <muduo/base/CountDownLatch.h>

namespace sg_agent {
class SgAgentInit {
 public:
  SgAgentInit();
  bool CheckHealthy(void);

  muduo::net::EventLoop *sg_agent_sub_loop_;
  muduo::CountDownLatch healthy_countdownlatch;

 private:

  muduo::net::EventLoop *healthy_loop_;
  muduo::net::EventLoopThread healthy_thread_;

  muduo::net::EventLoopThread sg_agent_sub_thread_;

  int16_t healthy_interval_secs;

  std::string FetchElemValByTinyXML(const tinyxml2::XMLElement
                                    *&p_xml_elem,
                                    const std::string &str_key) throw
  (exception);
  int HandleSgagentMutableFile(void); //ZK init inside
  const tinyxml2::XMLElement *
  FetchElemByTinyXML(const tinyxml2::XMLElement *&p_xml_elem,
                     const std::string &str_key) throw
  (exception);
  int8_t FetchLocalAddrInfo(void);
  void HandleCheckHealthyFailed(void);
  int8_t LoadMafkaSwitchInfo(void);
};
}

#endif //SG_AGENT_SG_AGENT_INIT_H
