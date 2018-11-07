
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
#include <map>
#include <boost/property_tree/ptree.hpp>
#include <boost/optional/optional.hpp>
#include <boost/property_tree/json_parser.hpp>
#include "sg_agent/count_request.h"
#define private public
#define protected public
#include "util/sg_agent_def.h"
#include "sg_agent/sg_agent_conf.h"
#include "sg_agent/config_client.h"
#undef private
#undef protected


using boost::property_tree::ptree;
using namespace sg_agent;

const int NUM = 12;

std::string keys [12]= {"com.meituan.service.user", "com.sankuai.cos.mtconfig",
    "com.sankuai.hotel.dealing.beta", "com.sankuai.hotel.dealing.dev",
    "com.sankuai.inf.data.statistic", "com.sankuai.inf.logCollector",
    "com.sankuai.inf.msgp", "com.sankuai.inf.sg_agent",
    "com.sankuai.pay.account", "com.sankuai.pay.config",
    "com.sankuai.waimai.openmessagecenter", "com.sankuai.waimai.rabbitmqplugin"};

class ConfigUnitTest: public testing::Test
{
    public:
      virtual void SetUp()
      {

        mtConfigClient = new MtConfigCollector();
        mtConfigClient->init();
      }
    public:
      MtConfigCollector *mtConfigClient;
      proc_conf_param_t param;
      proc_conf_param_t param_cell;
      proc_conf_param_t param_swimlane;
      proc_conf_param_t param_cell_and_swimlane;
      proc_conf_param_t param_no_flags;
};

/*
TEST_F(ConfigUnitTest, LoadMccCfg) {
  EXPECT_EQ(0, mtConfigClient->LoadMccCfg());
}



TEST_F(ConfigUnitTest, GetMccClusterAppkey) {
  std::cout << "normal test : " << std::endl;

  std::string req_appkey = "com.sankuai.pay.test";
  std::string mcc_appkey = "";
  EXPECT_EQ(0, mtConfigClient->GetMccClusterAppkey(req_appkey, mcc_appkey));

  EXPECT_STREQ(mcc_appkey.c_str(), "com.sankuai.inf.octo.mccpay");

  req_appkey = "com.sankuai.hotel.test";
  mcc_appkey = "";
  EXPECT_EQ(0, mtConfigClient->GetMccClusterAppkey(req_appkey, mcc_appkey));

  EXPECT_STREQ(mcc_appkey.c_str(), "com.sankuai.inf.octo.mcchotel");

  req_appkey = "com.sankuai.trip.test";
  mcc_appkey = "";
  EXPECT_EQ(0, mtConfigClient->GetMccClusterAppkey(req_appkey, mcc_appkey));

  EXPECT_STREQ(mcc_appkey.c_str(), "com.sankuai.inf.octo.mcchotel");

  req_appkey = "com.sankuai.travel.test";
  mcc_appkey = "";
  EXPECT_EQ(0, mtConfigClient->GetMccClusterAppkey(req_appkey, mcc_appkey));

  EXPECT_STREQ(mcc_appkey.c_str(), "com.sankuai.inf.octo.mcchotel");

  req_appkey = "com.sankuai.waimai.test";
  mcc_appkey = "";
  EXPECT_EQ(0, mtConfigClient->GetMccClusterAppkey(req_appkey, mcc_appkey));

  EXPECT_STREQ(mcc_appkey.c_str(), "com.sankuai.inf.octo.mccwaimai");

  req_appkey = "com.sankuai.banma.test";
  mcc_appkey = "";
  EXPECT_EQ(0, mtConfigClient->GetMccClusterAppkey(req_appkey, mcc_appkey));

  EXPECT_STREQ(mcc_appkey.c_str(), "com.sankuai.inf.octo.mccbanma");

  std::cout << "abnormal test：" << std::endl;
  req_appkey = "com.sankuai.abnormal.test";
  mcc_appkey = "";
  EXPECT_EQ(-1, mtConfigClient->GetMccClusterAppkey(req_appkey, mcc_appkey));

  req_appkey = "com.sankuai.banma";
  mcc_appkey = "";
  EXPECT_EQ(-1, mtConfigClient->GetMccClusterAppkey(req_appkey, mcc_appkey));

}


TEST_F(ConfigUnitTest, syncRelationToMtconfig) {
  EXPECT_EQ(-1,mtConfigClient->syncRelationToMtconfig(param));
}


TEST_F(ConfigUnitTest, _getServiceList) {
  std::string req_appkey = "";
  //EXPECT_EQ(0,mtConfigClient->_getServiceList(req_appkey));

  std::string cmd = "iptables -A INPUT -p tcp --dport 5266 -j DROP";
  int status = std::system(cmd.c_str());
  cmd = "iptables -A OUTPUT -p tcp --dport 5266 -j DROP";
  status = std::system(cmd.c_str());
  req_appkey = "com.sankuai.cos.mtconfig";
  //EXPECT_EQ(-201012,mtConfigClient->_getServiceList(req_appkey));//获取服务列表失败
  cmd = "iptables -A INPUT -p tcp --dport 5266 -j ACCEPT";
  status = std::system(cmd.c_str());
  cmd = "iptables -A OUTPUT -p tcp --dport 5266 -j ACCEPT";
  status = std::system(cmd.c_str());
  req_appkey = "com.sankuai.cos.mtconfig";
  //EXPECT_EQ(-300004,mtConfigClient->_getServiceList(req_appkey));//serlist为空

}


TEST_F(ConfigUnitTest, _getOneCollector) {
  int err = 0;
  std::string req_appkey = "com.sankuai.travel.test";

 // mtConfigClient->_getOneCollector(err, req_appkey);
  EXPECT_EQ(0,err);

  req_appkey = "com.sankuai.octo.test";
 // mtConfigClient->_getOneCollector(err, req_appkey);
 // EXPECT_EQ(-201013,err);//serlist 为空

  req_appkey = "com.sankuai.octo.test";
  std::string cmd = "iptables -A INPUT -p tcp --dport 9001 -j DROP";
  int status = std::system(cmd.c_str());
  cmd = "iptables -A OUTPUT -p tcp --dport 9001 -j DROP";
  status = std::system(cmd.c_str());

  //mtConfigClient->_getOneCollector(err, req_appkey);

  cmd = "iptables -A INPUT -p tcp --dport 9001 -j ACCEPT";
  status = std::system(cmd.c_str());
  cmd = "iptables -A OUTPUT -p tcp --dport 9001 -j ACCEPT";
  status = std::system(cmd.c_str());
  //EXPECT_EQ(-201014,err);//连接建立失败

}



TEST_F(ConfigUnitTest, GetConfigFromServer) {
  EXPECT_EQ(MTCONFIG_OK, ConfigClient::getInstance()->GetConfigFromServer(&param));
  param.appkey = "com.sankuai.inf.mcc_test";
  std::string result = param.conf;
  EXPECT_FALSE(result.empty());
  std::cout << "get result: " << result << std::endl;

  try {
      std::stringstream ss(result);
      ptree data;
      read_json(ss, data);

      std::string octo = data.get<std::string>("octo");
      EXPECT_STREQ(octo.c_str(), "222");

      std::string xzj_test = data.get<std::string>("xzj_test");
      EXPECT_STREQ(xzj_test.c_str(), "bbb");

      std::string bbb = data.get<std::string>("bbb");
      EXPECT_STREQ(bbb.c_str(), "bbb");

      std::string test = data.get<std::string>("test");
      EXPECT_STREQ(test.c_str(), "test1");
  } catch (...) {
    EXPECT_EQ(0, 0);
    return;
  }
}
*/

TEST_F(ConfigUnitTest, ModifySetConfig) {
    param.appkey = "com.sankuai.octo.tmy";
    param.env = "test";
    param.path = "/";
    param.swimlane = "set_test";
    EXPECT_EQ(-201401, ConfigClient::getInstance()->SetConfig(param));
    std::string res = "";
    EXPECT_EQ(0, ConfigClient::getInstance()->GetConfig(res, param));
    std::cout << res << std::endl;
}

TEST_F(ConfigUnitTest, CountGetConfigReq) {
  param.__set_appkey("com.sankuai.octo.yangjie");
  param.__set_env("test");
  param.__set_path("/");
  boost::unordered_map<std::string ,int> count;
  CountRequest::GetInstance()->GetReqData(count);
  for ( int i = 0; i < 10 ; ++i) {
    EXPECT_EQ(200,mtConfigClient->getConfig(param));
  }

  CountRequest::GetInstance()->GetReqData(count);
  EXPECT_EQ(10, count.at("allconfig"));
  EXPECT_EQ(10, count.at("config"));
//todo 失败的场景构造
}

/*
 * cell: cA  c=cA  swimlane: cA  c=cASwimlane
 * */
TEST_F(ConfigUnitTest, SetMccConfigTest) {
    std::string res = "";
    param_cell.__set_appkey("com.sankuai.octo.tmy");
    param_cell.__set_env("test");
    param_cell.__set_path("/");
    param_cell.__set_cell("cell01");
    param_cell.__set_key("c");
    param_cell.__set_token("45E40F1457F37F68AF2BAFE7880D4E0DC319A1CD");
    param_cell.__set_conf("{\"cell\":\"ccc\"}");
    EXPECT_EQ(0, ConfigClient::getInstance()->SetConfig(param_cell));
    EXPECT_EQ(0, ConfigClient::getInstance()->GetConfig(res, param_cell));
    std::cout << res << std::endl;
  #if 0
		sleep(20);
    std::cout <<std::endl;
    std::string res_cell = "";
    param_swimlane.appkey = "com.sankuai.octo.tmy";
    param_swimlane.env = "prod";
    param_swimlane.path = "/";
    param_swimlane.swimlane = "cA";
    param_swimlane.key = "c";
    ConfigClient::getInstance()->GetConfig(res_cell, param_swimlane);
    std::cout<<"the swimlane result is: "<<res_cell<<std::endl;
		sleep(20);
#endif

}
/*
 * cell: cA  c=cA  swimlane: cA  c=cASwimlane
 * */
TEST_F(ConfigUnitTest, GetMccConfigTest) {
    std::string res = "";
    param_cell.__set_appkey("com.sankuai.octo.tmy");
    param_cell.env = "prod";
    param_cell.path = "/";
    param_cell.cell = "cA";
    param_cell.key = "c";
		std::cout << ConfigClient::getInstance()->GetConfig(res, param_cell) << std::endl;;
    std::cout<<"the cell result is: "<<res<<std::endl;

		sleep(20);
    std::cout <<std::endl;
    std::string res_cell = "";
    param_swimlane.appkey = "com.sankuai.octo.tmy";
    param_swimlane.env = "prod";
    param_swimlane.path = "/";
    param_swimlane.swimlane = "cA";
    param_swimlane.key = "c";
    ConfigClient::getInstance()->GetConfig(res_cell, param_swimlane);
    std::cout<<"the swimlane result is: "<<res_cell<<std::endl;
		sleep(20);

    std::cout <<std::endl;
    std::string res_cell_and_swimlane = "";
    param_cell_and_swimlane.appkey = "com.sankuai.octo.tmy";
    param_cell_and_swimlane.env = "prod";
    param_cell_and_swimlane.path = "/";
    param_cell_and_swimlane.cell = "cA";
    param_cell_and_swimlane.swimlane = "cA";
    param_cell_and_swimlane.key = "c";
    ConfigClient::getInstance()->GetConfig(res_cell_and_swimlane, param_cell_and_swimlane);
    std::cout<<"the cell and swimlane result is: "<<res_cell_and_swimlane<<std::endl;
		sleep(20);
    std::string res_no_flags = "";

    param_no_flags.appkey = "com.sankuai.octo.tmy";
    param_no_flags.env = "prod";
    param_no_flags.path = "/";
    param_no_flags.key = "c";
    ConfigClient::getInstance()->GetConfig(res_no_flags, param_no_flags);
    std::cout<<"the no flags result is: "<<res_no_flags<<std::endl;
		sleep(70);
		
  }

