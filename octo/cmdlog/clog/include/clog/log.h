#ifndef _LOG_H_
#define _LOG_H_
#include <string>
#include <map>
#include <sstream>
#include <pthread.h>
#include <string.h>

//TODO:宏定义简化
//文件名不显示全路径
#define PUTTAGS cmdlog::CLog::putTags
#define SUBCATEGORY cmdlog::CLog::subCategory
#define CLOG_INIT(path) CMDLOG_INIT(path)
#define CLOG_CLOSE() CMDLOG_CLOSE()
#define CLOG_STR_ERROR(logEvent) CMDLOG_ERROR(logEvent)
#define CLOG_STR_WARN(logEvent) CMDLOG_WARN(logEvent)
#define CLOG_STR_INFO(logEvent) CMDLOG_INFO(logEvent)
#define CLOG_STR_DEBUG(logEvent) CMDLOG_DEBUG(logEvent)
#define CLOG_ERROR(fmt, args...)\
    do {\
    cmdlog::CLog* LOG = cmdlog::CLog::getLogger();\
    LOG->error("%s:%d " fmt,__FILE__,__LINE__,##args);\
    }while(0)

#define CLOG_WARN(fmt, args...)\
    do {\
    cmdlog::CLog* LOG = cmdlog::CLog::getLogger();\
    LOG->warn("%s:%d " fmt,__FILE__,__LINE__,##args);\
    }while(0)

#define CLOG_INFO(fmt, args...)\
    do {\
    cmdlog::CLog* LOG = cmdlog::CLog::getLogger();\
    LOG->info("%s:%d " fmt,__FILE__,__LINE__,##args);\
    }while(0)

#define CLOG_DEBUG(fmt, args...)\
    do {\
    cmdlog::CLog* LOG = cmdlog::CLog::getLogger();\
    LOG->debug("%s:%d " fmt,__FILE__,__LINE__,##args);\
    }while(0)

#define CMDLOG_INIT(path)\
    do {\
    cmdlog::CLog* LOG = cmdlog::CLog::getLogger();\
	LOG->init(path);\
    }while(0)
	
#define CMDLOG_CLOSE()\
    do {\
    cmdlog::CLog* LOG = cmdlog::CLog::getLogger();\
    LOG->shutDown();\
    }while(0)

#define CMDLOG_ERROR(logEvent)\
    do {\
    OSTRINGSTREAM(_log4cplus_buf);\
    _log4cplus_buf << __FILE__ << ":" << __LINE__ << " " <<logEvent;\
    cmdlog::CLog* LOG = cmdlog::CLog::getLogger();\
    LOG->error(_log4cplus_buf.str());\
    }while(0)
#define CMDLOG_WARN(logEvent)\
    do {\
    OSTRINGSTREAM(_log4cplus_buf);\
    _log4cplus_buf << __FILE__ << ":" << __LINE__ << " " <<logEvent;\
    cmdlog::CLog* LOG = cmdlog::CLog::getLogger();\
    LOG->warn(_log4cplus_buf.str());\
    }while(0)
#define CMDLOG_INFO(logEvent)\
    do {\
    OSTRINGSTREAM(_log4cplus_buf);\
    _log4cplus_buf << __FILE__ << ":" << __LINE__ << " " <<logEvent;\
    cmdlog::CLog* LOG = cmdlog::CLog::getLogger();\
    LOG->info(_log4cplus_buf.str());\
    }while(0)
#define CMDLOG_DEBUG(logEvent)\
    do {\
    OSTRINGSTREAM(_log4cplus_buf);\
    _log4cplus_buf << __FILE__ << ":" << __LINE__ << " " <<logEvent;\
    cmdlog::CLog* LOG = cmdlog::CLog::getLogger();\
    LOG->debug(_log4cplus_buf.str());\
    }while(0)


#define OSTRINGSTREAM(var) \
    std::stringstream var


namespace cmdlog {

#define DEFAULT_FILE_SIZE_MB (512)
#define MAX_FILE_SIZE_MB (4096)
#define MIN_FILE_SIZE_MB (512)
#define DEFAULT_FILE_COUNT_DAILY  (5)
#define MAX_FILE_COUNT_DAILY   (10)
#define MIN_FILE_COUNT_DAILY   (3)
#define DEFAULT_FILE_HISTORY  (14)
#define MAX_FILE_HISTORY  (60)
#define MIN_FILE_HISTORY  (3)
#define OPT_FILE_PATH    "/opt/logs/"
#define DATA_FILE_PATH   "/data/applogs/"
#define TMP_FILE_PATH    "/opt/tmp/"
#define PATTERN_PREFIX   "%D{%Y-%m-%d %H:%M:%S.%q} "
#define PATTERN_MID      " [%p] %t "
#define PATTERN_SUFFIX   " %m%n"
#define XMDT_PREFIX      "#XMDT#{"
#define XMDT_SUFFIX      "}#XMDT#"
#define DEFAULT_LOG_NAME "program"
#define DEBUG_LOG_SUFFIX  ".log.debug"
#define INFO_LOG_SUFFIX  ".log.info"
#define WARN_LOG_SUFFIX  ".log.warn"
#define ERROR_LOG_SUFFIX ".log.error"
#define DEFAULT_APP_KEY  "%t"
#define MAX_PID_STR_LENGTH   (10)
#define MAX_BUFFER_LENGTH   (4096)
#define TBLOG_PATH "/root/clog/clog.log"

#define LEVEL_CONF_PATH "config/log.conf"
#define LEVEL_DEBUG_STR "debug"
#define LEVEL_DEBUG (1)
#define LEVEL_INFO_STR "info"
#define LEVEL_INFO  (2)
#define LEVEL_WARN_STR "warn"
#define LEVEL_WARN  (3)
#define LEVEL_ERROR_STR "error"
#define LEVEL_ERROR (4)
#define LEVEL_OFF_STR "off"
#define LEVEL_OFF (5)

// 单例模式
class CLog
{
    public:
   	static CLog* getLogger();
	void init(const char* filename = NULL) ;
	void setLevel(const std::string &glevel);
    void setLevelForRemoteLog(const std::string &glevel = "error");
	static void setConfigPath(const char* path);
	void shutDown(void);
	void debug(const std::string &log_info);
	void debug(const char* log_info, ...);
	void info(const std::string &log_info);
	void info(const char* log_info, ...);
	void warn(const std::string &log_info);
	void warn(const char* log_info, ...);
	void error(const std::string &log_info);
	void error(const char* log_info, ...);

    //如下函数声明为static ，是为了封装C接口时比较方便
	static std::string putTags(std::map<std::string, std::string> &log_map, std::string message = "");
    static void putTags(char * dst,const char* ctag);
    static const std::string subCategory(const std::string& sub);
    static const std::string subCategory(const char* sub);

    private:
    std::string pattern_local; //log4cplus 中的本地日志格式pattern
    std::string pattern_remote; //log4cplus 中的远程本地日志格式pattern
	std::string app_key;
	std::string log_name;
	std::string log_path;
	int level; // level for local file log
    int slevel; // level for remote scribe log
    long long maxFileSize;
    int maxBackupIndex;
    int maxHistory;
    bool asyncQueueNonblock;
    int appendType;
    int rateLimit;
	bool appendDisk;
	bool appendLogAgent;

    static const std::string k_traceid; 
	static char* config_path;

    private:
	CLog();
    ~CLog();
    CLog(const CLog&);
    CLog& operator = (const CLog&);
	void parseConfigure(void);
	void setAppendType(int);
	static std::string getTraceId();
    void setRollFileRule(const std::string& size, const std::string& totalCount, const std::string& history,const std::string& type);
    void setAsyncNonblockOpt(const std::string& flag);
    void setRateLimitForRemoteLog(const std::string& rateLimit);
    void setAppKey(const std::string& app_key_);
    void setLogPath(const std::string& log_path_);
    void setLogName(const std::string& log_name_);
    void setLogParaFromConfig(void); // 设置appKey logPath logName ...
    void buildAppender(void);
	typedef std::map<std::string, std::string>::iterator STRING_MAP_IT;
};
}
#endif
