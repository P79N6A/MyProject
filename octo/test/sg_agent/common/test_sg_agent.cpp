#include "test_sg_agent.h"

int SGAgentHandler::init(const string &remoteAppkey, const std::string &ip, int port)
{
    ip_ = ip;
    port_ = port;

    localAppkey_ = "com.sankuai.octo.testSgagent";
    remoteAppkey_ = remoteAppkey;

    socket_ = boost::shared_ptr<TSocket>(new TSocket(ip, port));
    transport_ = boost::shared_ptr<TFramedTransport>(new TFramedTransport(socket_));
    protocol_ = boost::shared_ptr<TBinaryProtocol>(new TBinaryProtocol(transport_));
    socket_->setConnTimeout(500);
    socket_->setSendTimeout(500);
    socket_->setRecvTimeout(500);

    checkConnection();

    client_ = new SGAgentClient(protocol_);
    return 0;
}

int SGAgentHandler::deinit()
{
    if(client_)
    {
        delete client_;
        client_ = NULL;
    }

    closeConnection();
}

int SGAgentHandler::getServiceList(vector<SGService> &sgserviceList)
{

    client_->getServiceList(sgserviceList,localAppkey_, remoteAppkey_);
    cout << "getServiceList = " << sgserviceList.size() << endl;
    return 0;
}

int SGAgentHandler::registerService(SGService &service)
{
    int ret = client_->registService(service);
    return ret;
}

int SGAgentHandler::registeServicewithCmd(int cmd, SGService &service)
{
    int ret = client_->registServicewithCmd(cmd, service);
    return ret;
}

int SGAgentHandler::unRegisterService(SGService &service)
{
    int ret = client_->unRegistService(service);
    return ret;
}

int SGAgentHandler::uploadModuleInvoke(SGModuleInvokeInfo &oInfo)
{
    int ret = client_->uploadModuleInvoke(oInfo);
    if(ret)
    {
        cout << "uploadModuleInvoke fail" << endl;
    }
    return ret;
}

int SGAgentHandler::uploadLog(SGLog &oLog)
{
    int ret = client_->uploadLog(oLog);
    if(ret)
    {
        cout << "uploadLog fail" << endl;
    }
    return ret;
}

int SGAgentHandler::checkConnection()
{
    if(!transport_->isOpen()) {
        try
        {
            transport_->open();
        }
        catch(...){
            cout << "connect to sg_agent fail which ip: " << ip_ << "port: " << port_ << endl;
            return -2;
        }
    }

    return 0;
}

int SGAgentHandler::closeConnection()
{
    try
    {
        cout << "begin close connection !" << endl;
        transport_->close();
    }
    catch(...) {
        return -1;
        cout << "ERROR, close connection fail!" << endl;
    }

    return 0;
}

void SGAgentHandler::PrintServiceList(const std::vector<SGService> &serviceList)
{
    for(int i = 0; i < serviceList.size(); ++i)
    {
        std::cout << "servicelist's " << i << ", SGService: " << std::endl;
        std::cout << "appkey = " << serviceList[i].appkey << std::endl;
        std::cout << "ip = " << serviceList[i].ip << std::endl;
        std::cout << "weight = " << serviceList[i].weight << std::endl;
        std::cout << "fweight = " << serviceList[i].fweight << std::endl;
        std::cout << "serverType = " << serviceList[i].serverType << std::endl;
        std::cout << "protocol = " << serviceList[i].protocol << std::endl;
        std::cout << "status = " << serviceList[i].status << std::endl;
        std::cout << "envir = " << serviceList[i].envir << std::endl;
        std::cout << "port = " << serviceList[i].port << std::endl;
        std::cout << "extend = " << serviceList[i].extend << std::endl;
        std::cout << "version = " << serviceList[i].version << std::endl;
    }
}

bool SGAgentHandler::isInServiceList(vector<SGService>& servicelist, const SGService &service)
{
    for(vector<SGService>::iterator itor = servicelist.begin(); itor != servicelist.end(); itor++)
    {
        if(itor->ip == service.ip && itor->port == service.port 
                && itor->extend == service.extend
                && itor->envir == service.envir
                && itor->status == service.status
                && itor->weight == service.weight
                && itor->role == service.role)
              //  && itor->appkey == service.appkey)
                //&& itor->protocol == service.protocol
        {
            return true;
            std::cout << "extend: " << itor->extend << std::endl;
        }
    }
    return false;
}
