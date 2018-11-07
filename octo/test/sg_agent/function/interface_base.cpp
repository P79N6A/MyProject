#include "interface_base.h"

std::string InterfaceBase::ip_;
int InterfaceBase::port_;
std::string InterfaceBase::zkserver_;
std::string InterfaceBase::appkey_s;

void InterfaceBase::SetUpTestCase()
{
    //加载配置项
    tinyxml2::XMLDocument conf;
    conf.LoadFile("configure.xml");
    const char* agentport = conf.FirstChildElement("AgentPort")->GetText();
    port_ = atoi(agentport);
    ip_ = conf.FirstChildElement("AgentHost")->GetText();
    zkserver_ = conf.FirstChildElement("ZkServer")->GetText();
    appkey_s = conf.FirstChildElement("Appkey")->GetText();

}

