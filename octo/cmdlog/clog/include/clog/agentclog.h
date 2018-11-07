#ifndef __AGENT_CMDLOG__
#define __AGENT_CMDLOG__

void *open_clog(const char *configPath);
void LOG_ERROR(void *handle, const char *mes, ...);
void LOG_INFO(void *handle, const char *mes, ...);
void LOG_WARN(void *handle, const char *mes, ...);
void LOG_DEBUG(void *handle, const char *mes, ...);
void putTags(char *buf, const char *mes);
void subCategory(char *buf, const char *sub);
void close_clog(void *handle);


#endif
