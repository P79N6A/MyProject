#include <limits.h>
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include "interface_base.h"

class SgAgentProtocolFunc: public InterfaceBase
{
    protected:
        virtual void SetUp()
        {
            start_time_ = time(NULL);
            sg_agent_handler_.init(appkey_s, ip_, port_);
            oservice.appkey = appkey_s;
            oservice.ip = ip_;
            oservice.port = 5266;
            oservice.weight = 10;
            oservice.status = 2;
            oservice.role = 0;
            oservice.envir = 2;
            oservice.fweight = 0.10;
            request.localAppkey = "cailei_test";
		  	request.remoteAppkey = appkey_s;
        }

        virtual void TearDown()
        {
            const time_t end_time = time(NULL);
//            EXPECT_TRUE(end_time - start_time_ <= 1) << "SgAgentProtocolFunc testcase took too long.";
        }

        SGAgentHandler sg_agent_handler_;
        SGService oservice;
        ProtocolRequest request;
};

TEST_F(SgAgentProtocolFunc, thrift_protocol)
{
    std::string protocol = "thrift";
    oservice.protocol = protocol;
	oservice.__set_swimlane("Hocker");
    EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));

    usleep(5000);

    request.protocol = protocol;

    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);
	sg_agent_handler_.client_->getServiceListByProtocol(response, request);
    EXPECT_EQ(0, response.errcode);
	std::cout << "size of servicelist without swimalne : "  << response.servicelist.size() << std::endl;

    //新接口，获取全部的serverlist
	servicelist = vector<SGService>();
	response.__set_servicelist(servicelist);
	sg_agent_handler_.client_->getOriginServiceList(response, request);
    EXPECT_EQ(0, response.errcode);
	std::cout << "size of origin servicelist : " << response.servicelist.size() << std::endl;
	//请求中增加泳道信息
	request.__set_swimlane("Hocker");
	servicelist = vector<SGService>();
	response.__set_servicelist(servicelist);
	sg_agent_handler_.client_->getServiceListByProtocol(response, request);
	EXPECT_EQ(0, response.errcode);
	std::cout << "size of servicelist without swimalne : "  << response.servicelist.size() << std::endl;
	servicelist = vector<SGService>();
	response.__set_servicelist(servicelist);
	sg_agent_handler_.client_->getOriginServiceList(response, request);
	EXPECT_EQ(0, response.errcode);
	std::cout << "size of origin servicelist : " << response.servicelist.size() << std::endl;

	request.__set_swimlane("killer");
	servicelist = vector<SGService>();
	response.__set_servicelist(servicelist);
	sg_agent_handler_.client_->getServiceListByProtocol(response, request);
	EXPECT_EQ(0, response.errcode);
	std::cout << "size of servicelist without swimalne : "  << response.servicelist.size() << std::endl;
	servicelist = vector<SGService>();
	response.__set_servicelist(servicelist);
	sg_agent_handler_.client_->getOriginServiceList(response, request);
	EXPECT_EQ(0, response.errcode);
	std::cout << "size of origin servicelist : " << response.servicelist.size() << std::endl;
};

TEST_F(SgAgentProtocolFunc, http_protocol)
{
    std::string protocol = "http";
    oservice.protocol = protocol;
	oservice.__set_swimlane("Hocker");
	EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));

    usleep(5000);

    request.protocol = protocol;
    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);

    sg_agent_handler_.client_->getServiceListByProtocol(response, request);
    EXPECT_EQ(0, response.errcode);
	std::cout << "size of servicelist without swimalne : "  << response.servicelist.size() << std::endl;

	//新接口，获取全部的serverlist
	servicelist = vector<SGService>();
	response.__set_servicelist(servicelist);
	sg_agent_handler_.client_->getOriginServiceList(response, request);
	EXPECT_EQ(0, response.errcode);
	std::cout << "size of origin servicelist : " << response.servicelist.size() << std::endl;

	//请求中增加泳道信息
	request.__set_swimlane("Hocker");
	servicelist = vector<SGService>();
	response.__set_servicelist(servicelist);
	sg_agent_handler_.client_->getServiceListByProtocol(response, request);
	EXPECT_EQ(0, response.errcode);
	std::cout << "size of servicelist without swimalne : "  << response.servicelist.size() << std::endl;
	servicelist = vector<SGService>();
	response.__set_servicelist(servicelist);
	sg_agent_handler_.client_->getOriginServiceList(response, request);
	EXPECT_EQ(0, response.errcode);
	std::cout << "size of origin servicelist : " << response.servicelist.size() << std::endl;

	request.__set_swimlane("killer");
	servicelist = vector<SGService>();
	response.__set_servicelist(servicelist);
	sg_agent_handler_.client_->getServiceListByProtocol(response, request);
	EXPECT_EQ(0, response.errcode);
	std::cout << "size of servicelist without swimalne : "  << response.servicelist.size() << std::endl;
	servicelist = vector<SGService>();
	response.__set_servicelist(servicelist);
	sg_agent_handler_.client_->getOriginServiceList(response, request);
	EXPECT_EQ(0, response.errcode);
	std::cout << "size of origin servicelist : " << response.servicelist.size() << std::endl;
};

TEST_F(SgAgentProtocolFunc, tair_protocol)
{
    std::string protocol = "tair";
    oservice.protocol = protocol;
    oservice.__set_swimlane("Hocker");
	EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));

    usleep(5000);

    request.protocol = protocol;
    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);

    sg_agent_handler_.client_->getServiceListByProtocol(response, request);
    EXPECT_EQ(0, response.errcode);
	std::cout << "size of servicelist without swimalne : "  << response.servicelist.size() << std::endl;

	//新接口，获取全部的serverlist
	servicelist = vector<SGService>();
	response.__set_servicelist(servicelist);
	sg_agent_handler_.client_->getOriginServiceList(response, request);
	EXPECT_EQ(0, response.errcode);
	std::cout << "size of origin servicelist : " << response.servicelist.size() << std::endl;

	//请求中增加泳道信息
	request.__set_swimlane("Hocker");
	servicelist = vector<SGService>();
	response.__set_servicelist(servicelist);
	sg_agent_handler_.client_->getServiceListByProtocol(response, request);
	EXPECT_EQ(0, response.errcode);
	std::cout << "size of servicelist without swimalne : "  << response.servicelist.size() << std::endl;
	servicelist = vector<SGService>();
	response.__set_servicelist(servicelist);
	sg_agent_handler_.client_->getOriginServiceList(response, request);
	EXPECT_EQ(0, response.errcode);
	std::cout << "size of origin servicelist : " << response.servicelist.size() << std::endl;

	request.__set_swimlane("killer");
	servicelist = vector<SGService>();
	response.__set_servicelist(servicelist);
	sg_agent_handler_.client_->getServiceListByProtocol(response, request);
	EXPECT_EQ(0, response.errcode);
	std::cout << "size of servicelist without swimalne : "  << response.servicelist.size() << std::endl;
	servicelist = vector<SGService>();
	response.__set_servicelist(servicelist);
	sg_agent_handler_.client_->getOriginServiceList(response, request);
	EXPECT_EQ(0, response.errcode);
	std::cout << "size of origin servicelist : " << response.servicelist.size() << std::endl;
};

TEST_F(SgAgentProtocolFunc, register_error)
{
    oservice.serverType = 2;
    EXPECT_EQ(-1, sg_agent_handler_.registerService(oservice));
};

TEST_F(SgAgentProtocolFunc, getServiceListByProtocol_error)
{
    request.protocol = "other";
    ProtocolResponse response;
    sg_agent_handler_.client_->getServiceListByProtocol(response, request);
    EXPECT_EQ(0, response.servicelist.size());
	std::cout << "size of servicelist without swimalne : "  << response.servicelist.size() << std::endl;

	//新接口，获取全部的serverlist
	ProtocolResponse all_response;
	sg_agent_handler_.client_->getOriginServiceList(all_response, request);
	EXPECT_EQ(0, all_response.servicelist.size());
	std::cout << "size of origin servicelist : " << response.servicelist.size() << std::endl;
};


TEST_F(SgAgentProtocolFunc, serviceName_thrift)
{
    std::string protocol = "thrift";
    ProtocolRequest req = request;

    req.protocol = protocol;
    req.remoteAppkey = "";
    req.serviceName = "world";
    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);

    sg_agent_handler_.client_->getServiceListByProtocol(response, req);
    EXPECT_EQ(0, response.errcode);
	std::cout << "servicelist filte swimlane:" << std::endl;
    for (std::vector<SGService>::iterator iter = response.servicelist.begin();
            iter != response.servicelist.end(); ++iter) {
        std::cout << iter -> appkey << " " << iter -> ip << ":" << iter -> port << std::endl;
    }

	//新接口，获取全部的serverlist
	servicelist = vector<SGService>();
	response.__set_servicelist(servicelist);
	sg_agent_handler_.client_->getOriginServiceList(response, req);
	EXPECT_EQ(0, response.errcode);
	std::cout << "origin servicelist:" << std::endl;
	for (std::vector<SGService>::iterator iter = response.servicelist.begin();
			iter != response.servicelist.end(); ++iter) {
		std::cout << iter -> appkey << " " << iter -> ip << ":" << iter -> port << std::endl;
	}
};

TEST_F(SgAgentProtocolFunc, serviceName_thrift_mutilAppkey)
{
    std::string protocol = "thrift";
    ProtocolRequest req = request;

    req.protocol = protocol;
    req.remoteAppkey = "";
    req.serviceName = "hello";
    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);

    sg_agent_handler_.client_->getServiceListByProtocol(response, req);
    EXPECT_EQ(0, response.errcode);
	std::cout << "servicelist filte swimlane:" << std::endl;
    for (std::vector<SGService>::iterator iter = response.servicelist.begin();
            iter != response.servicelist.end(); ++iter) {
        std::cout << iter -> appkey << " " << iter -> ip << ":" << iter -> port << std::endl;
    }
	servicelist = vector<SGService>();
	response.__set_servicelist(servicelist);

	sg_agent_handler_.client_->getOriginServiceList(response, req);
	EXPECT_EQ(0, response.errcode);
	std::cout << "origin servicelist:" << std::endl;
	for (std::vector<SGService>::iterator iter = response.servicelist.begin();
			iter != response.servicelist.end(); ++iter) {
		std::cout << iter -> appkey << " " << iter -> ip << ":" << iter -> port << std::endl;
	}
};

TEST_F(SgAgentProtocolFunc, serviceName_thrift_filteServiceName)
{
    std::string protocol = "thrift";
    ProtocolRequest req = request;

    req.protocol = protocol;
    req.remoteAppkey = "";
    req.serviceName = "world";
    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);

    sg_agent_handler_.client_->getServiceListByProtocol(response, req);
    EXPECT_EQ(0, response.errcode);
	std::cout << "servicelist filte swimlane :" << std::endl;
    for (std::vector<SGService>::iterator iter = response.servicelist.begin();
            iter != response.servicelist.end(); ++iter) {
        std::cout << iter -> appkey << " " << iter -> ip << ":" << iter -> port << std::endl;
    }
	servicelist = vector<SGService>();
	response.__set_servicelist(servicelist);

	sg_agent_handler_.client_->getOriginServiceList(response, req);
	EXPECT_EQ(0, response.errcode);
	std::cout << "origin servicelist :" << std::endl;
	for (std::vector<SGService>::iterator iter = response.servicelist.begin();
			iter != response.servicelist.end(); ++iter) {
		std::cout << iter -> appkey << " " << iter -> ip << ":" << iter -> port << std::endl;
	}
};

/*
//大服务列表
TEST_F(SgAgentProtocolFunc, thrift_protocol_biglist)
{
	std::string protocol = "thrift";
	oservice.protocol = protocol;
	oservice.ip = "10.4.245.3";
	oservice.__set_swimlane("Hocker");
	EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));

	usleep(5000);

	request.protocol = protocol;
	request.remoteAppkey = "com.sankuai.octo.tmy";

	ProtocolResponse response;
	vector<SGService> servicelist;
	response.__set_servicelist(servicelist);
	sg_agent_handler_.client_->getServiceListByProtocol(response, request);
	EXPECT_EQ(0, response.errcode);
	std::cout << "size of servicelist without swimalne : "  << response.servicelist.size() << std::endl;

	//新接口，获取全部的serverlist
	vector<SGService> all_servicelist;
	response.__set_servicelist(all_servicelist);
	sg_agent_handler_.client_->getOriginServiceList(response, request);
	EXPECT_EQ(0, response.errcode);
	std::cout << "size of origin servicelist : " << response.servicelist.size() << std::endl;

};*/
