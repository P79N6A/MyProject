#include <sys/stat.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>
#include <signal.h>
#include <iostream>
#include <stdio.h>
#include <protocol/TBinaryProtocol.h>
#include <transport/TBufferTransports.h>
#include <transport/TSocket.h>
#include <openssl/md5.h>
#include <sys/select.h>
#include <sys/time.h>
#include <sys/types.h>          /* See NOTES */
#include <sys/socket.h>
#include <strings.h>
#include <errno.h>
#include <map>
#include <event2/event.h>
#include <event2/buffer.h>
#include <event2/http.h>
#include <event2/util.h>
#include <event2/event_struct.h>
#include "HttpClient.h"
#include "log4cplus.h"
#include "Config.h"
#include "CommonTool.h"
#include "DyupsDataManager.h"
#include "OriginalUpstreamManager.h"
#include "TengineTriger.h"
#include "HlbPreparation.h"
#include "SgAgentClient.h"
#include "HlbManager.h"

using namespace inf::hlb;

void generic_handler(struct evhttp_request *req, void *arg) {
    struct evbuffer *buf = evbuffer_new();
    if(!buf) {
        LOG_ERROR("failed to create response buffer");
        return;
    }

    char * request_uri = strdup((char *)evhttp_request_get_uri(req));
    struct evkeyvalq query;
    evhttp_parse_query(request_uri, &query);
    free(request_uri);

    int ret = 0;
    int code = HTTP_OK;
    const char * reload_value = evhttp_find_header(&query, "reload");
    if(reload_value)
    {
        if(0 == strcmp(reload_value, "nginx"))
        {
            //重启nginx
            ret = HlbManager::ReloadTengine();
            if(0 == ret)
            {
                evbuffer_add_printf(buf, "Server Responsed. nginx reload success\n");
            }
            else 
            {
                code = HTTP_INTERNAL;
                evbuffer_add_printf(buf, "Server Responsed. nginx reload failure\n");
            }
        }
        else if(0 == strcmp(reload_value, "log4cplus"))
        {
            //设置log4cplus的日志级别
            const char * level_value = evhttp_find_header(&query, "level");
            if(level_value)
            {
                if(0 == strcmp("debug", level_value))
                {
                    root.setLogLevel(log4cplus::DEBUG_LOG_LEVEL);
                    evbuffer_add_printf(buf, "Server Responsed. log4cplus level is set debug\n");
                }
                else if(0 == strcmp("info", level_value))
                {
                    root.setLogLevel(log4cplus::INFO_LOG_LEVEL);
                    evbuffer_add_printf(buf, "Server Responsed. log4cplus level is set info\n");
                }
                else if(0 == strcmp("warn", level_value))
                {
                    root.setLogLevel(log4cplus::WARN_LOG_LEVEL);
                    evbuffer_add_printf(buf, "Server Responsed. log4cplus level is set warn\n");
                }
                else if(0 == strcmp("error", level_value))
                {
                    root.setLogLevel(log4cplus::ERROR_LOG_LEVEL);
                    evbuffer_add_printf(buf, "Server Responsed. log4cplus level is set error\n");
                }
                else 
                {
                    code = HTTP_BADREQUEST;
                    evbuffer_add_printf(buf, "Server Responsed. level_value is invalid\n");
                }
            }
            else 
            {
                code = HTTP_BADREQUEST;
                evbuffer_add_printf(buf, "Server Responsed. level_value is not appointed\n");
            }
        }
        else 
        {
            code = HTTP_BADREQUEST;
            evbuffer_add_printf(buf, "Server Responsed. reload_value is invalid\n");
        }
    }
    else
    {
        code = HTTP_BADREQUEST;
        evbuffer_add_printf(buf, "Server Responsed. reload_value is not appointed\n");
    }

    evhttp_send_reply(req, code, "", buf);
    evbuffer_free(buf);
}

void * HlbManager::DyupsDataUpdater(void * args) {
    DyupsDataManager::instance().DyupsDataPullTimer();
    LOG_INFO("[HlbManager::DyupsDataUpdater] DyupsDataPullTimer END");
    return NULL;
}

void * HlbManager::OriginalUpstreamUpdater(void * args) {
    OriginalUpstreamManager::instance().OriginalUpstreamPullTimer();
    LOG_INFO("[HlbManager::OriginalUpstreamUpdater] OriginalUpstreamPullTimer END");
    return NULL;
}

int HlbManager::ReloadTengine() {
    //reload触发文件落地
    if (0!=DyupsDataManager::instance().dypusDataToFile() ) {
        LOG_ERROR("[HlbManager::ReloadNginxHandler] call dypusDataToFile ERROR");
        return -1;
    }
    if (0!=TengineTriger::instance().reloadTengine()) {
        LOG_ERROR("[HlbManager::ReloadNginxHandler] call reloadTengine ERROR");
        return -1;
    }
    return 0;
}

int main(int argc, char ** argv)
{
    if(parse_arg(argc,argv) <0) {
        return 0;
    }
    
    log4cplus::PropertyConfigurator::doConfigure(LOG4CPLUS_TEXT("log4cplus.conf"));
    if(! HlbConfig::instance().initialization("Hlb.xml") ) {
        LOG_ERROR("[MAIN] HlbConfig Init ERROR");
        return -1;
    }
    LOG_INFO("[MAIN] HlbConfig Init success!");
    
    if (! HlbPreparation::instance().initialization()) {
        LOG_ERROR("[MAIN] HlbPreparation initialization ERROR");
        return -1;
    }

    if(! DyupsDataManager::instance().initialization() ) {
        LOG_ERROR("[MAIN] DyupsDataManager initialization ERROR");
        return -1;
    }
    LOG_INFO("[MAIN] DyupsDataManager Init success!");

    if(! OriginalUpstreamManager::instance().initialization() ) {
        LOG_ERROR("[MAIN] OriginalUpstreamManager initialization ERROR");
        return -1;
    }
    LOG_INFO("[MAIN] OriginalUpstreamManager Init success!");
    
    if (! TengineTriger::instance().initialization() ) {
        LOG_ERROR("[MAIN] TengineTriger initialization ERROR");
        return -1;
    }
    LOG_INFO("[MAIN] TengineTriger Init success!");
    
    SgAgentClientCollector* sgagent_client = new SgAgentClientCollector();
    if (! sgagent_client->registService()) {
        LOG_WARN("[MAIN] registService FAILED");
    } else {
        LOG_INFO("[MAIN] registService SUCCEED");
    }
        
    pthread_t updatetid;
    pthread_create(&updatetid, NULL, HlbManager::DyupsDataUpdater, NULL);
    
    pthread_t upstreamtid;
    pthread_create(&upstreamtid, NULL, HlbManager::OriginalUpstreamUpdater, NULL);

    const char * http_addr = "127.0.0.1";
    short http_port = HlbConfig::instance().m_hlbManagerHttpPort;
    struct event_base * base = event_base_new();
    struct evhttp * http_server = evhttp_new(base);
    if(!http_server) {
        LOG_ERROR("[MAIN] evhttp_new ERROR!");
        return -1;
    }

    int ret = evhttp_bind_socket(http_server, http_addr, http_port);
    if(0 != ret) {
        LOG_ERROR("[MAIN] evhttp_bind_socket ERROR");
        return -1;
    }

    evhttp_set_gencb(http_server, generic_handler, NULL);
    LOG_INFO("[MAIN] http server start OK!");

    event_base_dispatch(base);
    evhttp_free(http_server);

    pthread_join(updatetid, NULL);
    pthread_join(upstreamtid, NULL);
    return 0;
}
