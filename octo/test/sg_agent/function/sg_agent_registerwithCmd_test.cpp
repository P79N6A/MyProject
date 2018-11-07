#include <limits.h>
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include <time.h>
#include "interface_base.h"

class SgAgentRegisterWithCmd: public InterfaceBase
{
    protected:
        virtual void SetUp()
        {
            start_time_ = time(NULL);
            sg_agent_handler_.init(appkey_s, ip_, port_);

            oservice.appkey = "com.sankuai.mns.registerwithCmd";
            cout << "appkey = " << oservice.appkey << endl;
            oservice.ip = ip_;
            oservice.port = 5266;
            oservice.version = "test";
            oservice.weight = 10;
            oservice.role = 0;
            oservice.envir = 2;
            oservice.fweight = 10.0;
            oservice.lastUpdateTime = start_time_;
            oservice.status = 2;
            oservice.heartbeatSupport = 3;
        }

        virtual void TearDown()
        {
        }

        SGAgentHandler sg_agent_handler_;
        SGService oservice;
};
TEST_F(SgAgentRegisterWithCmd, http_servicename_RegistAndCheck)
{
    SGService oTmp = oservice;
    oTmp.appkey = "com.sankuai.inf.test1";
    oTmp.protocol = "thrift";
    ServiceDetail sd;
    sd.unifiedProto = true;
    const std::string http_servicename = "http://test.dianping.com/weixinService/weiXinMessageService_1.0.0";
    oTmp.serviceInfo[http_servicename] = sd;
    EXPECT_EQ(0, sg_agent_handler_.registeServicewithCmd(1, oTmp));

    // check
    ProtocolRequest req;
    req.protocol = "thrift";
    req.remoteAppkey = "";
    req.serviceName = http_servicename;

    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);

    sg_agent_handler_.client_->getServiceListByProtocol(response, req);
    EXPECT_EQ(0, response.errcode);
    bool is_exist = false;
    for(std::vector<SGService>::iterator iter = response.servicelist.begin(); response.servicelist.end() != iter; ++iter){
      if(oTmp.ip == iter->ip && oTmp.port == iter->port){
        is_exist = true;
	break;
      }
    }

    EXPECT_TRUE(is_exist);
    for (std::vector<SGService>::iterator iter = response.servicelist.begin();
            iter != response.servicelist.end(); ++iter) {
        std::cout << iter -> appkey << " " << iter -> ip << ":" << iter -> port
            << "; heartbeatSupport: " << iter -> heartbeatSupport << std::endl;
    }
};

TEST_F(SgAgentRegisterWithCmd, HearbeatSupportCheck)
{
    //注册某一节点
    EXPECT_EQ(0, sg_agent_handler_.registeServicewithCmd(1, oservice));
    //注销
    EXPECT_EQ(0, sg_agent_handler_.unRegisterService(oservice));

    usleep(5000);

    ProtocolRequest request;
    request.protocol = "thrift";
    request.remoteAppkey = "com.sankuai.mns.registerwithCmd";

    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);

    sg_agent_handler_.client_->getServiceListByProtocol(response, request);
    EXPECT_EQ(0, response.errcode);

    vector<SGService>::iterator iter = response.servicelist.begin();
    while (response.servicelist.end() != iter) {
        EXPECT_EQ(3, iter -> heartbeatSupport);
        ++iter;
    }
};

TEST_F(SgAgentRegisterWithCmd, WrongAppkey)
{
    SGService oTmp = oservice;
    oTmp.appkey = "com.sankuai.helowrongappkey";
    EXPECT_EQ(0, sg_agent_handler_.registeServicewithCmd(1, oTmp));

    usleep(5000);

    ProtocolRequest request;
    request.protocol = "thrift"; request.remoteAppkey = "com.sankuai.octo.tmy";

    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);

    sg_agent_handler_.client_->getServiceListByProtocol(response, request);
    EXPECT_EQ(0, response.errcode);
    EXPECT_EQ(13, response.servicelist.size());
};

TEST_F(SgAgentRegisterWithCmd, new_RegistPigeanNode)
{
    SGService oTmp = oservice;
    oTmp.appkey = "com.sankuai.octo.hellopigeon";
    oTmp.protocol = "pigeon";
    ServiceDetail sd;
    sd.unifiedProto = true;
    oTmp.serviceInfo["hello"] = sd;
    oTmp.serviceInfo["code"] = sd;
    oTmp.serviceInfo["world"] = sd;
    EXPECT_EQ(0, sg_agent_handler_.registerService(oTmp));
};

TEST_F(SgAgentRegisterWithCmd, RegistServiceNameReset)
{
    SGService oTmp = oservice;
    oTmp.appkey = "com.sankuai.octo.hellopigeon";
    oTmp.protocol = "thrift";
    ServiceDetail sd;
    sd.unifiedProto = true;
    oTmp.serviceInfo["hello"] = sd;
    sd.unifiedProto = true;
    oTmp.serviceInfo["world"] = sd;
    EXPECT_EQ(0, sg_agent_handler_.registeServicewithCmd(0, oTmp));

    SGService Tmp = oservice;
    oTmp.appkey = "com.sankuai.octo.servicenamecheck01";
    Tmp.protocol = "thrift";
    Tmp.ip = "10.4.244.111";
    ServiceDetail sdt;
    sdt.unifiedProto = true;
    Tmp.serviceInfo["hello"] = sdt;
    EXPECT_EQ(0, sg_agent_handler_.registeServicewithCmd(0, Tmp));
};

TEST_F(SgAgentRegisterWithCmd, RegistServiceNameReset02)
{
    SGService oTmp = oservice;
    oTmp.appkey = "com.sankuai.octo.servicenamecheck01";
    oTmp.protocol = "thrift";
    ServiceDetail sd;
    sd.unifiedProto = false;
    oTmp.serviceInfo["hello"] = sd;
    EXPECT_EQ(0, sg_agent_handler_.registeServicewithCmd(0, oTmp));
};

TEST_F(SgAgentRegisterWithCmd, RegistServiceNameAdd)
{
    SGService oTmp = oservice;
    oTmp.appkey = "com.sankuai.octo.hellopigeon";
    oTmp.protocol = "thrift";
    ServiceDetail sd;
    sd.unifiedProto = true;
    oTmp.serviceInfo["code"] = sd;
    EXPECT_EQ(0, sg_agent_handler_.registeServicewithCmd(1, oTmp));

    sleep(10);

    // check
    std::string protocol = "thrift";
    ProtocolRequest req;

    req.protocol = protocol;
    req.remoteAppkey = "";
    req.serviceName = "code";
    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);

    sg_agent_handler_.client_->getServiceListByProtocol(response, req);
    EXPECT_EQ(0, response.errcode);
    bool is_exist = false;
    for(std::vector<SGService>::iterator iter = response.servicelist.begin(); response.servicelist.end() != iter; ++iter){
      is_exist = false;
      if(ip_ == iter->ip && port_ == iter->port){
        is_exist = true;
	break;
      }
    }
    EXPECT_TRUE(is_exist);
    for (std::vector<SGService>::iterator iter = response.servicelist.begin();
            iter != response.servicelist.end(); ++iter) {
        std::cout << iter -> appkey << " " << iter -> ip << ":" << iter -> port
            << "; heartbeatSupport: " << iter -> heartbeatSupport << std::endl;
    }
};

TEST_F(SgAgentRegisterWithCmd, RegistServiceNameAdd02)
{
    SGService oTmp = oservice;
    oTmp.appkey = "com.sankuai.octo.hellopigeon";
    oTmp.protocol = "thrift";
    ServiceDetail sd;
    oTmp.serviceInfo["world"] = sd;
    EXPECT_EQ(0, sg_agent_handler_.registeServicewithCmd(1, oTmp));
};

TEST_F(SgAgentRegisterWithCmd, RegistServiceNameDelete)
{
    SGService oTmp = oservice;
    oTmp.appkey = "com.sankuai.octo.hellopigeon";
    oTmp.protocol = "thrift";
    ServiceDetail sd;
    sd.unifiedProto = true;
    oTmp.serviceInfo["code"] = sd;
    EXPECT_EQ(0, sg_agent_handler_.registeServicewithCmd(2, oTmp));
};
