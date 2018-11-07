#ifndef __THRIFT_CLIENT_HANDLER_H__
#define __THRIFT_CLIENT_HANDLER_H__

#include <pthread.h>
#include <boost/make_shared.hpp>
//#include <octoidl/sgagent_service_types.h>
#include <protocol/TBinaryProtocol.h>
#include <transport/TBufferTransports.h>
#include <transport/TSocket.h>
#include <Thrift.h>
#include <protocol/TProtocol.h>
#include <transport/TTransport.h>
#include <boost/shared_ptr.hpp>
#include <boost/lexical_cast.hpp>


namespace cplugin{

enum ProcType
{
    SG_AEGNT,
    CONTROL_SERVER
};

class CpluginClientHandler
{
public:
    CpluginClientHandler();
    ~CpluginClientHandler();
    int init(const std::string& host, int port, ProcType proc_type);
    int checkConnection();
    int createConnection();
    int closeConnection();
    int checkHandler();
    void* getClient() {
        return m_client;
    }

    bool m_closed;
    std::string m_host;
    int m_port;
    void* m_client;
    boost::shared_ptr<apache::thrift::transport::TSocket> m_socket;
    boost::shared_ptr<apache::thrift::transport::TTransport> m_transport;
    boost::shared_ptr<apache::thrift::protocol::TProtocol> m_protocol;
    ProcType type;
};
};
#endif

