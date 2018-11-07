#include <clog/log.h>
#include <stdarg.h>
#include <stdio.h>
#include <string.h>
#include <iostream>
#include <map>

extern "C" void *open_clog(const char *configPath);
extern "C" void LOG_ERROR(void *handle, const char *mes, ...);
extern "C" void LOG_WARN(void *handle, const char *mes, ...);
extern "C" void LOG_INFO(void *handle, const char *mes, ...);
extern "C" void LOG_DEBUG(void *handle, const char *mes, ...);
extern "C" void putTags(char* buf, const char *mes);
extern "C" void subCategory(char* buf, const char *sub);
extern "C" void close_clog(void* handle);

class AgentHandler{
    public:
       cmdlog::CLog *logger;

};

void *open_clog(const char *configPath){
    AgentHandler *handle = NULL;
    handle = new AgentHandler;
    if (!handle)
        goto end;     
    handle->logger = cmdlog::CLog::getLogger();
    handle->logger->setConfigPath(configPath);
	handle->logger->init();
end:    
    return handle;
}

void LOG_ERROR(void *handle,const char *mes,...){
    AgentHandler *h = NULL;
    h = reinterpret_cast<AgentHandler*>(handle);
    va_list args;
    va_start(args, mes);
    char buf[MAX_BUFFER_LENGTH] = {0};
    vsnprintf(buf, sizeof(buf), mes, args);
    va_end(args);
    h->logger->error(buf);
}

void LOG_WARN(void *handle,const char *mes,...){
    AgentHandler *h = NULL;
    h = reinterpret_cast<AgentHandler*>(handle);
    va_list args;
    va_start(args, mes);
    char buf[MAX_BUFFER_LENGTH] = {0};
    vsnprintf(buf, sizeof(buf), mes, args);
    va_end(args);
    h->logger->warn(buf);
}

void LOG_INFO(void *handle,const char *mes,...){
    AgentHandler *h = NULL;
    h = reinterpret_cast<AgentHandler*>(handle);
    va_list args;
    va_start(args, mes);
    char buf[MAX_BUFFER_LENGTH] = {0};
    vsnprintf(buf, sizeof(buf), mes, args);
    va_end(args);
    h->logger->info(buf);
}

void LOG_DEBUG(void *handle,const char *mes,...){
    AgentHandler *h = NULL;
    h = reinterpret_cast<AgentHandler*>(handle);
    va_list args;
    va_start(args, mes);
    char buf[MAX_BUFFER_LENGTH] = {0};
    vsnprintf(buf, sizeof(buf), mes, args);
    va_end(args);
    h->logger->debug(buf);
}

void putTags(char* buf, const char *mes){
	memset(buf, 0, sizeof(buf));
    cmdlog::CLog::putTags(buf,mes);
}

void subCategory(char* buf, const char *sub){
	memset(buf, 0, sizeof(buf));
    std::string res = cmdlog::CLog::subCategory(sub); 
    strcpy(buf,res.c_str());
}

void close_clog(void *handle)
{
    if (!handle)
        return;
    AgentHandler *h = reinterpret_cast<AgentHandler*>(handle);
    h->logger->shutDown();
    delete h;
}
