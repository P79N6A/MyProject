#include <gtest/gtest.h>
#define private public
#define protected public
#include "sg_agent/mnscache_client.h"
#include "sg_agent/count_request.h"
#undef private
#undef protected
#include "mns/mns_zk_client.h"
#include "mns/mns_zk_tools.h"
#include "sg_agent/mns.h"
#include "mns/mns_impl.h"
#include "sg_agent/mtconfig_server_client.h"
std::string gEnvStr;
extern MNS *g_mns;

class ServiceZkClientTest : public testing::Test {
 public:
  void SetUp() {
    gEnvStr = "prod";
    zkPath = "/mns/sankuai/prod";
    conf_mutable.LoadFile(SG_AGENT_MUTABLE_CONF.c_str());
    localAppkey = "unittest_key";
    remoteAppkey = "com.sankuai.mns.regiontest";
    version = "";
    mtConfigClient = new MtConfigCollector();
    mns_impl = static_cast<MnsImpl*>(g_mns->GetMnsPlugin());
		zk_client_ = mns_impl->GetZkClient();
  }
 public:
  tinyxml2::XMLDocument conf_mutable;
  std::string zkPath;
  ServiceZkClient *zk_client_;
  MnsImpl *mns_impl;
	std::string localAppkey;
  std::string remoteAppkey;
  std::string version;
  MtConfigCollector *mtConfigClient;
};



TEST_F(ServiceZkClientTest, appkeyOrServiceNameNotExists) {
	ProtocolResponse reps;
	ProtocolRequest no_appkey_req;
	no_appkey_req.__set_protocol("thrift");	
	no_appkey_req.__set_remoteAppkey("notappkeynull");
	EXPECT_EQ(ERR_NODE_NOTFIND, g_mns->GetMnsPlugin()->GetSrvList(reps.servicelist, no_appkey_req, false, true,true));
	EXPECT_EQ(ERR_NODE_NOTFIND, g_mns->GetMnsPlugin()->GetSrvList(reps.servicelist, no_appkey_req, true, false, false));
	EXPECT_EQ(ERR_NODE_NOTFIND, g_mns->GetMnsPlugin()->GetSrvList(reps.servicelist, no_appkey_req, true, false, true));

	ProtocolRequest no_servicename_req;
	no_servicename_req.__set_protocol("thrift");
	no_servicename_req.__set_serviceName("notservicenamenull");
	EXPECT_EQ(ERR_NODE_NOTFIND, g_mns->GetMnsPlugin()->GetSrvList(reps.servicelist, no_servicename_req, false, true, true));
	EXPECT_EQ(ERR_NODE_NOTFIND, g_mns->GetMnsPlugin()->GetSrvList(reps.servicelist, no_servicename_req, true, false, false));
	EXPECT_EQ(ERR_NODE_NOTFIND, g_mns->GetMnsPlugin()->GetSrvList(reps.servicelist, no_servicename_req, true, false, true));

}

TEST_F(ServiceZkClientTest, getAppkeyByServiceName_thrift) {
  std::string serviceName = "world";
  std::string protocol = "thrift";
  std::set<std::string> appkeys;
  EXPECT_EQ(0, zk_client_->GetAppkeyByServiceName(appkeys, localAppkey, serviceName, version, protocol));
  EXPECT_EQ(1, appkeys.size());
}

TEST_F(ServiceZkClientTest, getAppkeyByServiceName_pigeon) {
  std::string serviceName = "hello";
  std::string protocol = "pigeon";
  std::set<std::string> appkeys;
  EXPECT_EQ(0, zk_client_->GetAppkeyByServiceName(appkeys, localAppkey, serviceName, version, protocol));//节点不存在
  EXPECT_EQ(1, appkeys.size());
}

TEST_F(ServiceZkClientTest, serviceNameNotExited) {
  std::string serviceName = "null";
  std::string protocol = "thrift";
  std::set<std::string> appkeys;
  EXPECT_EQ(ERR_NODE_NOTFIND, zk_client_->GetAppkeyByServiceName(appkeys, localAppkey, serviceName, version, protocol));
  EXPECT_EQ(0, appkeys.size());
}

TEST_F(ServiceZkClientTest, serviceList_thrift) {
  std::string protocol = "thrift";
  std::vector<SGService> serviceList;
  EXPECT_EQ(0, zk_client_->GetSrvListByProtocol(serviceList, localAppkey, remoteAppkey, version, protocol));
  EXPECT_EQ(2, serviceList.size());
}

TEST_F(ServiceZkClientTest, serviceList_http) {
  std::string protocol = "http";
  std::vector<SGService> serviceList;
  EXPECT_EQ(0, zk_client_->GetSrvListByProtocol(serviceList, localAppkey, remoteAppkey, version, protocol));
  EXPECT_EQ(1, serviceList.size());
}

TEST_F(ServiceZkClientTest, serviceList_cellar) {
  std::string protocol = "cellar";
  std::vector<SGService> serviceList;
  EXPECT_EQ(0, zk_client_->GetSrvListByProtocol(serviceList, localAppkey, remoteAppkey, version, protocol));
  EXPECT_EQ(3, serviceList.size());
}

TEST_F(ServiceZkClientTest, routeList_thrift) {
  std::string protocol = "thrift";
  std::vector<CRouteData> routeList;
  EXPECT_EQ(0, zk_client_->getRouteListByProtocol(routeList, localAppkey, remoteAppkey, version, protocol));
  EXPECT_EQ(7, routeList.size());
}

TEST_F(ServiceZkClientTest, routeList_http) {
  std::string protocol = "http";
  std::vector<CRouteData> routeList;
  EXPECT_EQ(0, zk_client_->getRouteListByProtocol(routeList, localAppkey, remoteAppkey, version, protocol));
  EXPECT_EQ(1, routeList.size());
}

TEST_F(ServiceZkClientTest, routeList_cellar) {
  std::string protocol = "cellar";
  std::vector<CRouteData> routeList;
  EXPECT_EQ(0, zk_client_->getRouteListByProtocol(routeList, localAppkey, remoteAppkey, version, protocol));
  EXPECT_EQ(1, routeList.size());
}

TEST_F(ServiceZkClientTest, FilterCell) {
	ProtocolRequest req;
	req.__set_remoteAppkey("com.sankuai.octo.yangjie");
  req.__set_protocol("thrift");
	std::vector<SGService> serviceList;
	EXPECT_EQ(0, mns_impl->GetSrvList(serviceList, req, false, true, true));
  EXPECT_EQ(8, serviceList.size());
}

TEST_F(ServiceZkClientTest, GetAppkeyDesc) {
  AppkeyDescResponse dec_res;
  std::string appkey = "com.sankuai.octo.tmy";
  EXPECT_EQ(0, zk_client_->GetAppkeyDesc(dec_res, appkey));
  EXPECT_STREQ("thrift", dec_res.desc.category.c_str());
  EXPECT_STREQ("inf", dec_res.desc.owt.c_str());
  EXPECT_STREQ("octo", dec_res.desc.pdl.c_str());
  EXPECT_EQ(1, dec_res.desc.business);

  appkey = "";
  EXPECT_EQ(-1, zk_client_->GetAppkeyDesc(dec_res, appkey));

  appkey = "unknow";
  EXPECT_EQ(-101, zk_client_->GetAppkeyDesc(dec_res, appkey));
}

TEST_F(ServiceZkClientTest , getMNSCache) {
  std::vector<SGService> serviceList;
  std::string appkey = "com.sankuai.octo.tmy";
  std::string version = "";
  std::string env = "test";
  std::string protocol = "thrift";
	boost::unordered_map<std::string, int> count;
  CountRequest::GetInstance()->GetReqData(count);
  for (int i = 0;i < 10; ++i) {
    EXPECT_EQ(200,MnsCacheCollector::getInstance()->getMNSCache(serviceList, appkey,version,env,protocol));
  }
  CountRequest::GetInstance()->GetReqData(count);
  EXPECT_EQ(10, count.at("allmnsc"));
  EXPECT_EQ(10, count.at("mnsc"));
  //todo 增加异常情况
}
