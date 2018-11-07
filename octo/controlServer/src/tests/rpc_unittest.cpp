//
// Created by Xiang Zhang on 2017/9/11.
//
#include <string>
#include <vector>
#include <iostream>

#include <transport/TSocket.h>
#include <transport/TBufferTransports.h>
#include <protocol/TBinaryProtocol.h>

#include <gtest/gtest.h>

#include "controller_types.h"
#include "../ControlServer.h"

using namespace std;

using namespace ::apache::thrift;
using namespace ::apache::thrift::protocol;
using namespace ::apache::thrift::transport;

using namespace Controller;

using testing::Types;

struct Host {
    int port;
    string ip_addr;
    Host(int p, const string& str) :
            port(p), ip_addr(str) {

    }
};

/*int client_union(const Host& h) {
    Plugin p;
    p.name ="cr_agent";
    p.version = "1.1.2";
    p.md5 = "2ecda0df0da62392df0696a05c7d0359";
    p.name ="cplugin_idc";
    p.version = "12.08";
    p.md5 = "12.08";
    Department d;
    //d.owt = "meituan.inf";
    d.owt = "";
    Location l;
    //l.region = "shanghai";
    l.region = "";
    string env("");
    //string env("test");
    int ret = -1;
    try {
        boost::shared_ptr<TSocket> socket(new TSocket(h.ip_addr, h.port));
        boost::shared_ptr<TFramedTransport> transport(new TFramedTransport(socket));
        boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));
        ControllerServiceClient client(protocol);
        transport->open();

        ret = client.handlePluginUnion(Operation::UPGRADE, p, d, l, env);
        transport->close();
    } catch (TApplicationException& tx){
        std::cout << "ERROR: " <<  tx.what() << std::endl;
        return -1;
    } catch (TException& tx) {
        std::cout << "ERROR: " << tx.what() << std::endl;
        return -1;
    }
    return ret;
}

TEST(UnionTest, HandleZeroReturn) {
    Host p(5299, "10.4.229.149");
    EXPECT_EQ(0, client_union(p));
}*/

int client_list(const Host& h) {
    Plugin p;
    p.name ="sg_agent";
    p.version = "sg_agent_test";
    p.md5 = "sg_agent_test";
    int ret = -1;
    try {
        boost::shared_ptr<TSocket> socket(new TSocket(h.ip_addr, h.port));
        boost::shared_ptr<TFramedTransport> transport(new TFramedTransport(socket));
        boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));
        ControllerServiceClient client(protocol);
        transport->open();

	
	vector<string> ip_list;
	ip_list.push_back("10.21.171.169");
	ret = client.handlePluginList(Operation::UPGRADE, p, ip_list);
        //ret = client.handlePluginUnion(Operation::INSTALL, p, d, l, env);
        transport->close();
    } catch (TApplicationException& tx){
        std::cout << "ERROR: " <<  tx.what() << std::endl;
        return -1;
    } catch (TException& tx) {
        std::cout << "ERROR: " << tx.what() << std::endl;
        return -1;
    }
    return ret;
}

TEST(IpListTest, HandleZeroReturn) {
    Host p(5299, "10.21.136.222");
    EXPECT_EQ(0, client_list(p));
}
/*
int client_check(const Host& h) {
    Plugin p;
    p.name ="unitplugin_name";
    p.version = "12.08";
    p.md5 = "12.08";
    int ret = -1;
    try {
        boost::shared_ptr<TSocket> socket(new TSocket(h.ip_addr, h.port));
        boost::shared_ptr<TFramedTransport> transport(new TFramedTransport(socket));
        boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));
        ControllerServiceClient client(protocol);
        transport->open();

        vector<Plugin> p_list;
        p_list.push_back(p);
        ret = client.regularCheckPlugin("10.20.109.67", p_list);
        transport->close();
    } catch (TApplicationException& tx){
        std::cout << "ERROR: " <<  tx.what() << std::endl;
        return -1;
    } catch (TException& tx) {
        std::cout << "ERROR: " << tx.what() << std::endl;
        return -1;
    }
    return ret;
}

TEST(RegularCheckTest, HandleTrueReturn) {
    Host p(5299, "10.4.229.149");
    EXPECT_EQ(0, client_check(p));
}*/
