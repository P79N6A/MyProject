#include "test_worker.h"

int SGWorkerHandler::init(const std::string &ip, int port)
{
    ip_ = ip;
    port_ = port;

    socket_ = boost::shared_ptr<TSocket>(new TSocket(ip, port));
    transport_ = boost::shared_ptr<TFramedTransport>(new TFramedTransport(socket_));
    protocol_ = boost::shared_ptr<TBinaryProtocol>(new TBinaryProtocol(transport_));
    socket_->setConnTimeout(500);
    socket_->setSendTimeout(500);
    socket_->setRecvTimeout(500);

    checkConnection();

    client_ = new SGAgentWorkerClient(protocol_);
    return 0;
}

int SGWorkerHandler::deinit()
{
    if(client_)
    {
        delete client_;
        client_ = NULL;
    }

    return closeConnection();
}


int SGWorkerHandler::checkConnection()
{
    if(!transport_->isOpen()) {
        try
        {
            transport_->open();
        }
        catch(...){
            cout << "connect to sg_agent_worker fail which ip: " << ip_ << "port: " << port_ << endl;
            return -2;
        }
    }

    return 0;
}

int SGWorkerHandler::closeConnection()
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
