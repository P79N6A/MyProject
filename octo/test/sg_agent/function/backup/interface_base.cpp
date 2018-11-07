#include "interface_base.h"
#include "tinyxml2.h"

SGAgentHandler TestInterfaceBase::sg_agent_handler_;
ZkClientOperation TestInterfaceBase::zk_operator_;

void TestInterfaceBase::SetUpTestCase()
{

        //加载配置项
        tinyxml2::XMLDocument conf;
        conf.LoadFile("configure.xml");
        const char* agentport = conf.FirstChildElement("AgentPort")->GetText();
        port_ = atoi(agentport);
        ip_ = conf.FirstChildElement("AgentHost")->GetText();
        appkey_ = conf.FirstChildElement("Appkey")->GetText();
        
        zkserver_ = conf.FirstChildElement("ZkServer")->GetText();
}

void TestInterfaceBase::TearDownCase()
{
        sg_agent_handler_.deinit();
        zk_operator_.deinit();
}

void TestInterfaceBase::InitHandler()
{
    sg_agent_handler_.init(appkey_, ip_, port_);
    zk_operator_.init(appkey_, zkserver_);
}

