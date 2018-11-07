#include <sstream>
#include <iostream>
#include <algorithm>
#include <pthread.h>
#include <memory>
#include <stdarg.h>
#include <fstream>
#include <stdlib.h>
#include <stdio.h>
#include <cstdlib>
#include <assert.h>

#ifdef _WIN32
#include <process.h>
#else
#include <unistd.h>
#endif
#include <log4cplus/fileappender.h>
#include <log4cplus/layout.h>
#include <log4cplus/fileappender.h>
#include <log4cplus/asyncappender.h>
#include <log4cplus/loggingmacros.h>
#include <log4cplus/devinfohelper.h>
#include <clog/log.h>
#include <log4cplus/parse_config.h>
#include <log4cplus/tblog.h>

#include <unistd.h>
#include <stdint.h>
#include <pthread.h>
#include <sys/time.h>
#include <sys/vfs.h>
#include <sys/stat.h>
#include <sys/types.h>

///////////////////////////////////////////////////////////////////////////////
//// File LOCAL definitions
/////////////////////////////////////////////////////////////////////////////////
namespace {
	static void setTbLog(const std::string &tblevel) {//使用TBLog作为内部log
        std::string level = "CLOSE";
        if ("DEBUG" == tblevel || "INFO" == tblevel || "WARN" == tblevel || "ERROR" == tblevel) {
           level = tblevel;
        }
		TBSYS_LOGGER.setLogLevel(level.c_str());
		std::string logPath = TBLOG_PATH;
		TBSYS_LOGGER.setFileName(logPath.c_str());
	}
}

namespace cmdlog {

static log4cplus::Logger debug_pLogger;
static log4cplus::Logger info_pLogger;
static log4cplus::Logger warn_pLogger;
static log4cplus::Logger error_pLogger;
static log4cplus::Logger log_agent_pLogger;
static std::string rpc_traceId = "abcd-0000-0000-0000-0000-user-defined";

const std::string CLog::k_traceid = "traceID";
char* CLog::config_path = strdup(LEVEL_CONF_PATH);

CLog::CLog()
    : app_key(DEFAULT_APP_KEY)
    , log_name(DEFAULT_LOG_NAME)
    , log_path("")
    , level(LEVEL_DEBUG)
    , slevel(LEVEL_ERROR)
    , maxFileSize(512 * 1024 * 1024)
    , maxBackupIndex(5)
    , maxHistory(10)
    , asyncQueueNonblock(true)
    , appendType(1)
    , rateLimit(0)
{
}

void CLog::setLogPath(const std::string& log_path_)
{
    // set log_path
    std::fstream _file;
    if (log_path_.empty())
    {
        _file.open(OPT_FILE_PATH,std::ios::in);
        if (_file)
        {
           log_path = OPT_FILE_PATH;
        }
        else
        {
            _file.open(DATA_FILE_PATH,std::ios::in);
            if (_file)
            {
                log_path = DATA_FILE_PATH;
            }
            else
            {
                log_path = TMP_FILE_PATH;
           }
        }
    } else {
        log_path.assign(log_path_);
    }
}

void CLog::setLogName(const std::string& log_name_)
{
    if (!log_name_.empty())
        log_name.assign(log_name_);
}

void CLog::setAppKey(const std::string& app_key_)
{
    // app_key取不到，使用pid代替
    // pid_t 类型实际是 int类型;参照 /usr/include/bits/types.h
    // /sys/types.h  bits/types.h  bits/typesizes.h
    if (app_key_.empty()) {
    	char pid_str[MAX_PID_STR_LENGTH];
        int cn;
    	cn = snprintf(pid_str, sizeof(pid_str), "%d", getpid());
        if (cn >=0 && cn < MAX_PID_STR_LENGTH)
        {
    	    app_key = pid_str;
        }
    } else {
        app_key.assign(app_key_);
    }
}

void CLog::setLogParaFromConfig()
{
    parseConfigure();
    //set pattern
    this->pattern_local = std::string(PATTERN_PREFIX) + "- -" + std::string(PATTERN_MID) + log_name + std::string(PATTERN_SUFFIX);
    this->pattern_remote = std::string(PATTERN_PREFIX) + "%H " + app_key + std::string(PATTERN_MID) + log_name + std::string(PATTERN_SUFFIX);
    if (log_path[log_path.size()-1] != '/')
    {
        log_path += "/";
    }
    log_path += app_key;
    log_path += "/";
    log_path += log_name;

    TBSYS_LOG(DEBUG,"config para log_path: %s level: %d  slevel: %d  name:  %s app_key: %s  type: %d  size: %ul  count: %d  history: %d  rateLimit: %d",this->log_path.c_str(),this->level,this->slevel,this->log_name.c_str(),this->app_key.c_str(),this->appendType,this->maxFileSize,this->maxBackupIndex,this->maxHistory,this->rateLimit);
}

// TODO: 进一步封装，去除重复代码
void CLog::buildAppender()
{
    if (appendDisk && level < LEVEL_OFF){  // append to disk
        TBSYS_LOG(DEBUG,"build local appenders");
		log4cplus::SharedAppenderPtr  debug_append(new log4cplus::DailyRollingFileAppender(LOG4CPLUS_TEXT(log_path  + DEBUG_LOG_SUFFIX),log4cplus::DAILY,true,maxFileSize,maxBackupIndex,maxHistory,app_key,appendType,false,true,false));
		std::auto_ptr<log4cplus::Layout> debug_layout(new log4cplus::PatternLayout(pattern_local));
		debug_append->setLayout(debug_layout);
		log4cplus::AsyncAppenderPtr debug_async(new log4cplus::AsyncAppender (debug_append, 100, asyncQueueNonblock));

		log4cplus::SharedAppenderPtr  info_append(new log4cplus::DailyRollingFileAppender(LOG4CPLUS_TEXT(log_path  + INFO_LOG_SUFFIX),log4cplus::DAILY,true,maxFileSize,maxBackupIndex,maxHistory,app_key,appendType,false,true,false));
		std::auto_ptr<log4cplus::Layout> info_layout(new log4cplus::PatternLayout( pattern_local));
		info_append->setLayout(info_layout);
		log4cplus::AsyncAppenderPtr info_async(new log4cplus::AsyncAppender (info_append, 100, asyncQueueNonblock));

		log4cplus::SharedAppenderPtr  warn_append(new log4cplus::DailyRollingFileAppender(LOG4CPLUS_TEXT(log_path  + WARN_LOG_SUFFIX),log4cplus::DAILY,true,maxFileSize,maxBackupIndex,maxHistory,app_key,appendType,false,true,false));
		std::auto_ptr<log4cplus::Layout> warn_layout(new log4cplus::PatternLayout( pattern_local));
		warn_append->setLayout(warn_layout);
		log4cplus::AsyncAppenderPtr warn_async(new log4cplus::AsyncAppender (warn_append, 100, asyncQueueNonblock));

		log4cplus::SharedAppenderPtr  error_append(new log4cplus::DailyRollingFileAppender(LOG4CPLUS_TEXT(log_path  + ERROR_LOG_SUFFIX),log4cplus::DAILY,true,maxFileSize,maxBackupIndex,maxHistory,app_key,appendType,false,true,false));
		std::auto_ptr<log4cplus::Layout> error_layout(new log4cplus::PatternLayout( pattern_local));
		error_append->setLayout(error_layout);
		log4cplus::AsyncAppenderPtr error_async(new log4cplus::AsyncAppender (error_append, 100, asyncQueueNonblock));
// add appender into logger
		log4cplus::Logger::Logger::getInstance(LOG4CPLUS_TEXT("DEBUG")).addAppender(log4cplus::SharedAppenderPtr(debug_async.get()));
		log4cplus::Logger::Logger::getInstance(LOG4CPLUS_TEXT("INFO")).addAppender(log4cplus::SharedAppenderPtr(info_async.get()));
		log4cplus::Logger::Logger::getInstance(LOG4CPLUS_TEXT("WARN")).addAppender(log4cplus::SharedAppenderPtr(warn_async.get()));
		log4cplus::Logger::Logger::getInstance(LOG4CPLUS_TEXT("ERROR")).addAppender(log4cplus::SharedAppenderPtr(error_async.get()));

		debug_pLogger = log4cplus::Logger::Logger::getInstance(LOG4CPLUS_TEXT("DEBUG"));
		info_pLogger = log4cplus::Logger::Logger::getInstance(LOG4CPLUS_TEXT("INFO"));
		warn_pLogger = log4cplus::Logger::Logger::getInstance(LOG4CPLUS_TEXT("WARN"));
		error_pLogger = log4cplus::Logger::Logger::getInstance(LOG4CPLUS_TEXT("ERROR"));
   } // end appendDisk

    if (appendLogAgent && slevel < LEVEL_OFF){
        TBSYS_LOG(DEBUG,"build remote appender");
		log4cplus::SharedAppenderPtr  debug_append_scribe(new log4cplus::ScribeAppender(app_key,this->rateLimit));
		std::auto_ptr<log4cplus::Layout> debug_layout1(new log4cplus::PatternLayout(pattern_remote));
		debug_append_scribe->setLayout(debug_layout1);
		log4cplus::AsyncAppenderPtr debug_async_scribe(new log4cplus::AsyncAppender (debug_append_scribe, 1000, asyncQueueNonblock));
		log4cplus::Logger::Logger::getInstance(LOG4CPLUS_TEXT("LOGAGENT")).addAppender(log4cplus::SharedAppenderPtr(debug_async_scribe.get()));

		log_agent_pLogger = log4cplus::Logger::Logger::getInstance(LOG4CPLUS_TEXT("LOGAGENT"));
	} // end appendLogAgent

}

CLog::~CLog()
{
    if (config_path != NULL)
    {
        free(config_path);
        config_path = NULL;
    }
}

CLog* CLog::getLogger()
{
    static CLog instance;
    return &instance;
}

void CLog::setLevel(const std::string &glevel)
{
    TBSYS_LOG(DEBUG,"local level: %s",glevel.c_str());
    if (glevel == LEVEL_DEBUG_STR) {
        level = LEVEL_DEBUG;
    } else if (glevel == LEVEL_INFO_STR) {
        level = LEVEL_INFO;
    } else if (glevel == LEVEL_WARN_STR) {
        level = LEVEL_WARN;
    } else if (glevel == LEVEL_ERROR_STR) {
        level = LEVEL_ERROR;
    } else {
        level = LEVEL_OFF;
    }
}

void CLog::setLevelForRemoteLog(const std::string &glevel)
{
    TBSYS_LOG(DEBUG,"remote level: %s",glevel.c_str());
    if (glevel == LEVEL_DEBUG_STR) {
        slevel = LEVEL_DEBUG;
    } else if (glevel == LEVEL_INFO_STR) {
        slevel = LEVEL_INFO;
    } else if (glevel == LEVEL_WARN_STR) {
        slevel = LEVEL_WARN;
    } else if (glevel == LEVEL_ERROR_STR) {
        slevel = LEVEL_ERROR;
    }
    TBSYS_LOG(DEBUG,"remote level is : %d",this->slevel);
}

void CLog::setConfigPath(const char* path)
{
	if (path == NULL)
		config_path = strdup(LEVEL_CONF_PATH);
    else 
        config_path = strdup(path);

    TBSYS_LOG(DEBUG,"set config path %s",config_path);
}

void CLog::shutDown(void)
{
    log4cplus::Logger::shutdown();
}

void CLog::init(const char* filename)
{
    setConfigPath(filename);
    setLogParaFromConfig();
    log4cplus::initialize();
    buildAppender();
}

void CLog::setAppendType(int appendType_){
    switch(appendType_)
    {
        case 1:
            appendDisk = true;
            appendLogAgent = false;
            break;
        case 2:
            appendDisk = false;
            appendLogAgent = true;
            break;
        case 3:
            appendDisk = true;
            appendLogAgent = true;
            break;
        default:
            appendDisk = true;
            appendLogAgent = false;
    }
	TBSYS_LOG(DEBUG,"set append type: %d  disk %d, agent %d",appendType_,appendDisk,appendLogAgent);
}

void CLog::setRollFileRule(const std::string& size, const std::string& totalNum, const std::string& history, const std::string& type){
    int size_mb = 0;
    if (size.empty()){
        size_mb = DEFAULT_FILE_SIZE_MB;
    }else{
        size_mb = std::min(MAX_FILE_SIZE_MB,atoi(size.c_str()));
        size_mb = std::max(MIN_FILE_SIZE_MB,size_mb);
    }
    maxFileSize = static_cast<long long>(size_mb) * 1024 * 1024;

    if (totalNum.empty()){
        maxBackupIndex = DEFAULT_FILE_COUNT_DAILY;
    }else{
        maxBackupIndex = std::min(MAX_FILE_COUNT_DAILY,atoi(totalNum.c_str()));
        maxBackupIndex = std::max(MIN_FILE_COUNT_DAILY,maxBackupIndex);
    }
    maxBackupIndex = maxBackupIndex - 1;

    if (history.empty()){
        maxHistory = DEFAULT_FILE_HISTORY;
    }else{
        maxHistory = std::min(MAX_FILE_HISTORY,atoi(history.c_str()));
        maxHistory = std::max(MIN_FILE_HISTORY,maxHistory);
    }

    appendType = atoi(type.c_str());
	setAppendType(appendType);
}

void CLog::setAsyncNonblockOpt(const std::string& flag){
    if (flag == "false" || flag == "FALSE")
        asyncQueueNonblock = false;
    else
        asyncQueueNonblock = true;
}

void CLog::setRateLimitForRemoteLog(const std::string& rate){
   if (rate != "")
        rateLimit = atoi(rate.c_str());
}

void CLog::parseConfigure(void){
    cmdlog::Configuration *conf = cmdlog::Configuration::Instance();
	if (config_path == NULL)
		config_path = strdup(LEVEL_CONF_PATH);

    conf->init(config_path);

    setTbLog(conf->get("clog_debug_level"));
    
    TBSYS_LOG(DEBUG,"config path %s",config_path);

    setAppKey(conf->get("app_key"));
    setLogPath(conf->get("path"));
    setLogName(conf->get("name"));
    setLevel(conf->get("level"));
    setLevelForRemoteLog(conf->get("remote_level"));
    setRollFileRule(conf->get("size"),conf->get("count"),conf->get("history"),conf->get("type"));
    setAsyncNonblockOpt(conf->get("nonblock"));
    setRateLimitForRemoteLog(conf->get("rate"));
    TBSYS_LOG(DEBUG,"config para level: %s name:  %s app_key: %s  type: %s",conf->get("level").c_str(),log_name.c_str(),app_key.c_str(),conf->get("type").c_str());
}

void CLog::debug(const std::string &log_info)
{
    if (LEVEL_DEBUG == level) {
		if (appendDisk)
        	LOG4CPLUS_DEBUG(debug_pLogger, log_info);
    }

    if (LEVEL_DEBUG == slevel) {
		if (appendLogAgent)
			LOG4CPLUS_DEBUG(log_agent_pLogger, log_info);
    }
}

void CLog::debug(const char* log_info, ...)
{
    char buf[MAX_BUFFER_LENGTH] = {0};
    if (LEVEL_DEBUG == level || LEVEL_DEBUG == slevel) {
        va_list args;
        va_start(args, log_info);
        vsnprintf(buf, sizeof(buf), log_info, args);
        va_end(args);
    }

    if (LEVEL_DEBUG == level) {
		if (appendDisk)
        	debug_pLogger.forcedLog(log4cplus::DEBUG_LOG_LEVEL, buf);
    }

    if  (LEVEL_DEBUG == slevel) {
		if (appendLogAgent)
			log_agent_pLogger.forcedLog(log4cplus::DEBUG_LOG_LEVEL, buf);
    }
}

void CLog::info(const std::string &log_info)
{
    if (LEVEL_INFO >= level) {
		if (appendDisk){
			if (LEVEL_DEBUG == level) {
				LOG4CPLUS_INFO(debug_pLogger, log_info);
				LOG4CPLUS_INFO(info_pLogger, log_info);
			} else if (LEVEL_INFO == level) {
				LOG4CPLUS_INFO(info_pLogger, log_info);
			}
		}
    }

    if (LEVEL_INFO >= slevel) {
		if (appendLogAgent)
            LOG4CPLUS_INFO(log_agent_pLogger, log_info);
    }
}

void CLog::info(const char* log_info, ...)
{
    char buf[MAX_BUFFER_LENGTH] = {0};
    if (LEVEL_INFO >= level || LEVEL_INFO >= slevel) {
        va_list args;
        va_start(args, log_info);
        vsnprintf(buf, sizeof(buf), log_info, args);
        va_end(args);
    }

    if (LEVEL_INFO >= level){
		if (appendDisk){
			if (LEVEL_DEBUG == level) {
				debug_pLogger.forcedLog(log4cplus::INFO_LOG_LEVEL, buf);
				info_pLogger.forcedLog(log4cplus::INFO_LOG_LEVEL, buf);
			} else if (LEVEL_INFO == level) {
				info_pLogger.forcedLog(log4cplus::INFO_LOG_LEVEL, buf);
			}
		}
    }

    if (LEVEL_INFO >= slevel) {
		if (appendLogAgent)
        	log_agent_pLogger.forcedLog(log4cplus::INFO_LOG_LEVEL, buf);
    }
}

void CLog::warn(const std::string &log_info)
{
    if (LEVEL_WARN >= level) {
		if (appendDisk){
			if (LEVEL_DEBUG == level) {
				LOG4CPLUS_WARN(debug_pLogger, log_info);
				LOG4CPLUS_WARN(info_pLogger, log_info);
				LOG4CPLUS_WARN(warn_pLogger, log_info);
			} else if (LEVEL_INFO == level) {
				LOG4CPLUS_WARN(info_pLogger, log_info);
				LOG4CPLUS_WARN(warn_pLogger, log_info);
			} else if (LEVEL_WARN == level) {
				LOG4CPLUS_WARN(warn_pLogger, log_info);
			}
		}
    }
    if (LEVEL_WARN >= slevel) {
		if (appendLogAgent) LOG4CPLUS_WARN(log_agent_pLogger, log_info);
    }
}

void CLog::warn(const char* log_info, ...)
{
    char buf[MAX_BUFFER_LENGTH] = {0};
    if (LEVEL_WARN >= level || LEVEL_WARN >= slevel) {
        va_list args;
        va_start(args, log_info);
        vsnprintf(buf, sizeof(buf), log_info, args);
        va_end(args);
    }

    if (LEVEL_WARN >= level) {
		if (appendDisk){
			if (LEVEL_DEBUG == level) {
				debug_pLogger.forcedLog(log4cplus::WARN_LOG_LEVEL, buf);
				info_pLogger.forcedLog(log4cplus::WARN_LOG_LEVEL, buf);
				warn_pLogger.forcedLog(log4cplus::WARN_LOG_LEVEL, buf);
			} else if (LEVEL_INFO == level) {
				info_pLogger.forcedLog(log4cplus::WARN_LOG_LEVEL, buf);
				warn_pLogger.forcedLog(log4cplus::WARN_LOG_LEVEL, buf);
			} else if (LEVEL_WARN == level) {
				warn_pLogger.forcedLog(log4cplus::WARN_LOG_LEVEL, buf);
			}
		}
    }

    if (LEVEL_WARN >= slevel) {
		if (appendLogAgent) log_agent_pLogger.forcedLog(log4cplus::WARN_LOG_LEVEL, buf);
    }
}

void CLog::error(const std::string &log_info)
{
    if (LEVEL_ERROR >= level) {
		if (appendDisk){
			if (LEVEL_DEBUG == level) {
				LOG4CPLUS_ERROR(debug_pLogger, log_info);
				LOG4CPLUS_ERROR(info_pLogger, log_info);
				LOG4CPLUS_ERROR(warn_pLogger, log_info);
				LOG4CPLUS_ERROR(error_pLogger, log_info);
			} else if (LEVEL_INFO == level) {
				LOG4CPLUS_ERROR(info_pLogger, log_info);
				LOG4CPLUS_ERROR(warn_pLogger, log_info);
				LOG4CPLUS_ERROR(error_pLogger, log_info);
			} else if (LEVEL_WARN == level) {
				LOG4CPLUS_ERROR(warn_pLogger, log_info);
				LOG4CPLUS_ERROR(error_pLogger, log_info);
			} else if (LEVEL_ERROR == level) {
				LOG4CPLUS_ERROR(error_pLogger, log_info);
			}
		}
    }

    if (LEVEL_ERROR >= slevel) {
		if (appendLogAgent) LOG4CPLUS_ERROR(log_agent_pLogger, log_info);
    }
}

void CLog::error(const char* log_info, ...)
{
    char buf[MAX_BUFFER_LENGTH] = {0};
    if (LEVEL_ERROR >= level || LEVEL_ERROR >= slevel) {
        va_list args;
        va_start(args, log_info);
        vsnprintf(buf,sizeof(buf),log_info,args);
        va_end(args);
    }

    if (LEVEL_ERROR >= level) {
		if (appendDisk){
			if (LEVEL_DEBUG == level) {
				debug_pLogger.forcedLog(log4cplus::ERROR_LOG_LEVEL, buf);
				info_pLogger.forcedLog(log4cplus::ERROR_LOG_LEVEL, buf);
				warn_pLogger.forcedLog(log4cplus::ERROR_LOG_LEVEL, buf);
				error_pLogger.forcedLog(log4cplus::ERROR_LOG_LEVEL, buf);
			} else if (LEVEL_INFO == level) {
				info_pLogger.forcedLog(log4cplus::ERROR_LOG_LEVEL, buf);
				warn_pLogger.forcedLog(log4cplus::ERROR_LOG_LEVEL, buf);
				error_pLogger.forcedLog(log4cplus::ERROR_LOG_LEVEL, buf);
			} else if (LEVEL_WARN == level) {
				warn_pLogger.forcedLog(log4cplus::ERROR_LOG_LEVEL, buf);
				error_pLogger.forcedLog(log4cplus::ERROR_LOG_LEVEL, buf);
			} else if (LEVEL_ERROR == level) {
				error_pLogger.forcedLog(log4cplus::ERROR_LOG_LEVEL, buf);
			}
		}
    }

    if (LEVEL_ERROR >= slevel) {
		if (appendLogAgent) log_agent_pLogger.forcedLog(log4cplus::ERROR_LOG_LEVEL, buf);
    }
}
// 获取traceid 是线程安全的
std::string CLog::getTraceId()
{
	return rpc_traceId;
}

std::string CLog::putTags(std::map<std::string, std::string> &log_map, std::string message)
{   
    TBSYS_LOG(DEBUG,"put tags for string map");
    std::string info("");
    info.reserve(100);
    STRING_MAP_IT iter_id = log_map.find(k_traceid);
    //保证traceid 写在最前面
    if (iter_id != log_map.end()) {
        std::string key = iter_id->first;
        std::string value = iter_id->second;
        info += key;
        info += "=";
        info += value; 
        info += " ";
    } 

    STRING_MAP_IT it = log_map.begin();

    while(it != log_map.end())
    {
        if (it->first == k_traceid)
        {
            it++;
            continue;
        }
        std::string key = it->first;
        std::string value = it->second;

        info += key;
	    info += "=";
        info += it->second;
        info += " ";
        it++;
    }

    // erase last character
    if (!info.empty())
    {
        info.erase (info.end()-1,info.end());  
    }

    TBSYS_LOG(DEBUG,"tags info is %s", info.c_str());

    std::string res;

    if (!info.empty())
    {
        res.reserve(info.size() + 256);
        res.append(XMDT_PREFIX).append(info).append(XMDT_SUFFIX).append(message);
    }

    return res;

}

void CLog::putTags(char* res,const char* ctag)
{
	std::string traceId = cmdlog::CLog::getTraceId();
    sprintf(res,"%s%s%s%s%s",XMDT_PREFIX,traceId.c_str()," ",ctag,XMDT_SUFFIX);
}

const std::string CLog::subCategory(const std::string& sub)
{
    std::string cat = "__subcategory__=";
    std::string res = XMDT_PREFIX + cat + sub + XMDT_SUFFIX;
    return res;
}

const std::string CLog::subCategory(const char* sub)
{
    std::string cat = "__subcategory__=";
    std::string res = XMDT_PREFIX + cat + sub + XMDT_SUFFIX;
    return res;
}

}
