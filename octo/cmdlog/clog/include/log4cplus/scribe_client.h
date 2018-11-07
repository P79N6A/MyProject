#ifndef __SCRIBE_CLIENT_H__
#define __SCRIBE_CLIENT_H__

#include <string>
#include <vector>
#include <stdint.h>

#include <transport/TSocket.h>
#include <transport/TBufferTransports.h>
#include <protocol/TBinaryProtocol.h>

#include "../gen-cpp/ScribeLog.h"

using namespace ::apache::thrift;
using namespace ::apache::thrift::protocol;
using namespace ::apache::thrift::transport;

namespace cmdlog { 
  namespace scribe {
    void log_error(const char *message);
    struct ScribeClientConfig {

        ScribeClientConfig()
        {
            host = "127.0.0.1";
            port = 4252;
            category = "default";
            timeout_ms = 200;
            local_logdir = "/opt/logs/logs/default";
            send_interval_sec = 1;
        }

        std::string category;
        std::string host;
        std::string local_logdir;
        unsigned int port;
        unsigned int timeout_ms;
        unsigned int send_interval_sec;
    };

    class ScribeClient {

        public:

            enum LogSeverity {
                LOG_DEBUG   =   0,
                LOG_INFO    =   1,
                LOG_WARNING =   2,
                LOG_ERROR   =   3,
                LOG_FATAL   =   4,
            };

            ScribeClient(const ScribeClientConfig& conf);
            virtual ~ScribeClient();

            bool send(const std::string& msg);
            bool syncsend(const std::string& msg);
        private:

            friend void* log_thread(void* arg);

            void doRealSend(std::vector<LogEntry>*);
            void doSend();

            bool isOpen();

        private:

            boost::shared_ptr<TFramedTransport> m_transport;
            boost::shared_ptr<ScribeLogClient> m_client;

            ScribeClientConfig m_conf;
            const std::string m_hostname, m_hostip;

            pthread_t m_pid;
            pthread_mutex_t m_wr_lock;
            std::vector<LogEntry> m_logbufA, m_logbufB, *m_producer, *m_consumer;
    };
    
    typedef boost::shared_ptr<ScribeClient> ScribeClientPtr;
    }//namespace scribe
}// namespace cmdlog 

#endif
