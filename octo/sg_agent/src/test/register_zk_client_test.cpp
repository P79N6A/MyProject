#include <gtest/gtest.h>
#include "sg_agent/regist_client.h"
#include "util/global_def.h"
#include "mns/mns_impl.h"
#include "sg_agent/mns.h"

extern GlobalVar *g_global_var;
extern MNS *g_mns;
using namespace sg_agent;
class RegisteZkClientTest : public testing::Test {

 public:
  void SetUp() {
    //m_client = RegistClient::getInstance()->GetRegisteZkClient();
    node.__set_appkey("com.sankuai.octo.tmy");
    node.__set_protocol("thrift");
    node.__set_envir(g_global_var->gEnv);
    node.__set_version("sg_agent_test");
    node.__set_fweight(10.0);
    node.__set_weight(10);
    node.__set_status(2);
    mns_impl = static_cast<MnsImpl *>(g_mns->GetMnsPlugin());

  }
 public:
  RegisteZkClient *m_client;
  SGService node;
  MnsImpl *mns_impl;
};

TEST_F(RegisteZkClientTest, zkNodeNotExist) {
		int port = 8987;
		std::string cell = "test";
		node.__set_ip("192.0.0.0");
		node.__set_port(port);
	  EXPECT_EQ(SUCCESS, RegistClient::getInstance()->registeService(node, RegistCmd::REGIST, UptCmd::ADD));
}

/*


TEST_F(RegisteZkClientTest, zkNodeNotExist) {
  std::string origin_ip = node.ip;
  int origin_port = node.port;
  node.__set_ip("10.0.0.0");
  node.__set_port(80);

  int ret = m_client->RegisterServiceNodeToZk(node, RegistCmd::UNREGIST, UptCmd::ADD);
  EXPECT_EQ(ERR_NODE_NOTFIND, ret);

  ret = m_client->RegisterServiceNodeToZk(node, RegistCmd::REGIST, UptCmd::DELETE);
  EXPECT_EQ(ERR_NODE_NOTFIND, ret);

  node.__set_ip(origin_ip);
  node.__set_port(origin_port);
}

TEST_F(RegisteZkClientTest, cellRegister) {
  int port = 8987;
  std::string cell = "test";
  node.__set_ip(g_global_var->gIp);
  node.__set_port(port);
  node.__set_appkey("com.sankuai.octo.tmy");
  EXPECT_EQ(SUCCESS, m_client->RegisterService(node, RegistCmd::REGIST, UptCmd::ADD));
  // sleep 5s
  sleep(5);
  node.__set_cell(cell);
  EXPECT_EQ(SUCCESS, m_client->RegisterService(node, RegistCmd::REGIST, UptCmd::ADD));
  sleep(5);
  ProtocolRequest requst;
  requst.__set_remoteAppkey("com.sankuai.octo.tmy");
  requst.__set_protocol("thrift");

  std::vector<SGService> srvlist;
  mns_impl->GetSrvList(srvlist, requst, true, false, false);
  EXPECT_FALSE(srvlist.empty());
  bool is_exist = false;
  for (std::vector<SGService>::const_iterator iter = srvlist.begin(); srvlist.end() != iter; ++iter) {
    if (g_global_var->gIp == iter->ip && port == iter->port) {
      is_exist = (cell == iter->cell);
    }
  }
  EXPECT_TRUE(is_exist);
};

TEST_F(RegisteZkClientTest, hotel_white_register) {
  SGService service;
  if (PPE == g_global_var->gAppenv || TEST == g_global_var->gAppenv) {
    service.__set_appkey("com.sankuai.inf.mnsc");
    service.__set_appkey("10.4.245.3");
    EXPECT_TRUE(RegistClient::getInstance()->IsHotelAllowRegister(service));

    service.__set_appkey("10.21.164.0");
    EXPECT_TRUE(RegistClient::getInstance()->IsHotelAllowRegister(service));

    service.__set_appkey("com.sankuai.travel.sop");
    service.__set_ip("10.4.245.3");
    EXPECT_TRUE(RegistClient::getInstance()->IsHotelAllowRegister(service));

    service.__set_ip("172.18.174.95");
    EXPECT_FALSE(RegistClient::getInstance()->IsHotelAllowRegister(service));
  } else {
    service.__set_appkey("com.sankuai.inf.mnsc");
    service.__set_appkey("10.4.245.3");
    EXPECT_TRUE(RegistClient::getInstance()->IsHotelAllowRegister(service));

    service.__set_appkey("10.21.164.0");
    EXPECT_TRUE(RegistClient::getInstance()->IsHotelAllowRegister(service));

    service.__set_appkey("com.sankuai.travel.sop");
    service.__set_ip("10.4.245.3");
    EXPECT_TRUE(RegistClient::getInstance()->IsHotelAllowRegister(service));

    service.__set_ip("172.18.174.95");
    EXPECT_TRUE(RegistClient::getInstance()->IsHotelAllowRegister(service));
  }

}*/
