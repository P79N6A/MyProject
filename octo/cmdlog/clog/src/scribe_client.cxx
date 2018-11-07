#include <netdb.h>
#include <arpa/inet.h>
#include <sys/utsname.h>

#include <cstdio>
#include <sys/time.h>

#include <stdexcept>
#include <fstream>
#include <log4cplus/scribe_client.h>
#include <log4cplus/tblog.h>
using namespace std;
namespace cmdlog {
    namespace scribe {
        // YYYYMMDD
        static inline const string current_date(void)
        {
            size_t len;
            struct timeval tv;
            struct tm tp;
            char buf[128];

            gettimeofday(&tv, NULL);

            localtime_r(&tv.tv_sec, &tp);
            len = strftime(buf, 128, "%Y%m%d", &tp);
            buf[len] = '\0';

            return buf;
        }

        // YYYY-MM-DD hh:mm:ss.xxx
        static inline const string current_timestr(void)
        {
            size_t len;
            struct timeval tv;
            struct tm tp;
            char buf[128];

            gettimeofday(&tv, NULL);

            localtime_r(&tv.tv_sec, &tp);
            len = strftime(buf, 128, "%F %T", &tp);
            sprintf(buf + len, ",%03ld", tv.tv_usec);
            buf[len + 4] = '\0';

            return buf;
        }

        static inline void get_hostname(string& namestr)
        {
            struct utsname buf;

            if (uname(&buf) != 0)
                namestr = "(unknown)";
            else
                namestr = buf.nodename;
        }

        static inline const string& hostname()
        {
            static string namestr;

            if (namestr.empty()) {
                get_hostname(namestr);
                if (namestr.empty())
                    namestr = "(unknown)";
            }

            return namestr;
        }

        static inline void get_hostip_by_name(const string& namestr, string& ipstr)
        {
            struct hostent* ent;

            ent = gethostbyname(namestr.c_str());
            if (!ent)
                goto end;

            switch (ent->h_addrtype) {
                case AF_INET:
                case AF_INET6:
                    {
                        char str[128];
                        inet_ntop(ent->h_addrtype, ent->h_addr, str, sizeof(str));
                        ipstr = str;
                    }
                break;

                default:
                    goto end;
            }

            return;

        end:
            ipstr = "(unknown)";
        }

        static inline const string& hostip()
        {
            static string ipstr;

            if (ipstr.empty()) {
                get_hostip_by_name(hostname(), ipstr);
                if (ipstr.empty())
                    ipstr = "(unknown)";
            }

            return ipstr;
        }

        void log_error(const char *message)
        {
            ScribeClientConfig m_conf; 
            const string locallog = m_conf.local_logdir + "/" + m_conf.category + ".flume." + current_date();
            ofstream logfile(locallog.c_str(), ofstream::app);
            if (logfile.is_open()) {
                logfile << message;
                logfile.close();
            }
        }

        /* ------------------------------------------------------------------------- */

        #define RESERVED_ENTRY_NR 1024

        void* log_thread(void* arg)
        {
            ScribeClient* client = (ScribeClient*)arg;
            unsigned int send_interval_sec = client->m_conf.send_interval_sec;

            while (true) {
                sleep(send_interval_sec);
                client->doSend();
            }

            return NULL;
        }

        static const string severity_str[] = {
            "DEBUG", "INFO", "WARNING", "ERROR", "FATAL",
        };

        ScribeClient::ScribeClient(const ScribeClientConfig& conf)
        : m_hostname(hostname()), m_hostip(hostip())
        {
            m_conf = conf;

            m_logbufA.reserve(RESERVED_ENTRY_NR);
            m_logbufB.reserve(RESERVED_ENTRY_NR);
            m_producer = &m_logbufA;
            m_consumer = &m_logbufB;

            pthread_mutex_init(&m_wr_lock, NULL);

            boost::shared_ptr<TSocket> socket(new TSocket(m_conf.host, m_conf.port));
            if (!socket)
                throw std::runtime_error("scribe client: error creating socket");

            socket->setRecvTimeout(m_conf.timeout_ms);
            socket->setSendTimeout(m_conf.timeout_ms);

            m_transport = boost::shared_ptr<TFramedTransport>(new TFramedTransport(socket));
            if (!m_transport)
                throw std::runtime_error("scribe client: error creating transport");

            boost::shared_ptr<TBinaryProtocol> protocol(new TBinaryProtocol(m_transport));
            if (!protocol)
                throw std::runtime_error("scribe client: error creating protocol");

            m_client = boost::shared_ptr<ScribeLogClient>(new ScribeLogClient(protocol));
            if (!m_client)
                throw std::runtime_error("scribe client: error creating client");

            m_transport->open();
            if (!m_transport->isOpen())
                throw std::runtime_error("scribe client: error opening transport");

           // if (pthread_create(&m_pid, NULL, log_thread, this) != 0)
           //     throw std::runtime_error("scribe client: create update thread failed.");
        }

        ScribeClient::~ScribeClient()
        {
            //pthread_cancel(m_pid);
            //pthread_join(m_pid, NULL);

            pthread_mutex_destroy(&m_wr_lock);

            // clean up buffers
            if (isOpen()) {
                if (!m_logbufA.empty())
                    doRealSend(&m_logbufA);

                if (!m_logbufB.empty())
                    doRealSend(&m_logbufB);

                m_transport->close();
            }
        }

        
        bool ScribeClient::isOpen()
        {
            return m_transport->isOpen();
        }

        bool ScribeClient::send(const string& msg)
        {
            LogEntry entry;
            entry.category = m_conf.category;
            entry.message = msg + "\n"; 
            pthread_mutex_lock(&m_wr_lock);
            m_producer->push_back(entry);
            pthread_mutex_unlock(&m_wr_lock);

            return true;
        }


        bool ScribeClient::syncsend(const string& msg)
        {
            if (msg.empty()) {
                return false;
            }
            if (!m_transport->isOpen()) {
                m_transport->open();
                if (!m_transport->isOpen()){
                    TBSYS_LOG(ERROR,"cannot open m_transport: send %s failed",msg.c_str());
                    return false;
                }
            } 
            vector<LogEntry> log;
            LogEntry newlog;
            newlog.category = m_conf.category;
            newlog.message = msg;
            log.push_back(newlog);
            if (log.size() == 0) {
                return false;
            }
            int err = m_client->Log(log);
            if (err) {
                TBSYS_LOG(ERROR,"send mes error %s",msg.c_str());
                return false;
            }
            return true; 
        }

        void ScribeClient::doRealSend(vector<LogEntry>* entrylist)
        {
        again:
            try {
                int err = m_client->Log(*entrylist);
                if (err) {
                    if (isOpen())
                        goto failed;
                }

                return;
            } catch (apache::thrift::TException& e){
                //cmdlog::scribe::log_error(e.what());
                TBSYS_LOG(ERROR,"doRealSend error %s",e.what());
                try {
                    // re-connect and try again
                    m_transport->close();
                    m_transport->open();
                    if (m_transport->isOpen())
                        goto again;
                } catch (...) {
                TBSYS_LOG(ERROR,"doRealSend error close or open");
                }
            }

        failed:
                for (vector<LogEntry>::const_iterator x = entrylist->begin(); x != entrylist->end(); ++x) {
			TBSYS_LOG(ERROR,"failed to send log %s",(x->message).c_str());
			}
	/*
            const string locallog = m_conf.local_logdir + "/" + m_conf.category + ".flume." + current_date();
            ofstream logfile(locallog.c_str(), ofstream::app);
            if (logfile.is_open()) {
                for (vector<LogEntry>::const_iterator x = entrylist->begin(); x != entrylist->end(); ++x)
                    logfile << x->message;

                logfile.close();
            }
	*/
        }

        void ScribeClient::doSend()
        {
            pthread_mutex_lock(&m_wr_lock);
            vector<LogEntry>* tmp = m_producer;
            m_producer = m_consumer;
            m_consumer = tmp;
            pthread_mutex_unlock(&m_wr_lock);

            if (!m_consumer->empty()) {
                doRealSend(m_consumer);
                m_consumer->clear();
            }
        }
    } // namespace scribe
} //namespace cmdlog
