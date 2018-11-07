#include "Log.h"

namespace mafka
{

Logger g_Logger;

//muduo::AsyncLogging log("mafka_cthrift", 500*1024);
//muduo::AsyncLogging* g_asyncLog = NULL;
void asyncOutput(const char* msg, int len)
{
  g_Logger.WriteLog(msg, len);
}
muduo::Logger::LogLevel LogLevelConvert(const char *level);

void SetupLogger(std::string const& filename, std::string const& level, int file_size/*=104857600*/, int file_num/*=10*/)
{
    	g_Logger.SetFileName(filename.c_str());
	g_Logger.SetLogLevel(level.c_str()); 
	g_Logger.SetMaxFileSize(file_size); 
	g_Logger.SetMaxFileIndex(file_num); 
  		
	muduo::Logger::setLogLevel(LogLevelConvert(level.c_str()));
  	muduo::Logger::setTimeZone(muduo::TimeZone(8 * 3600, "CST"));
  	muduo::Logger::setOutput(asyncOutput);
}

muduo::Logger::LogLevel LogLevelConvert(const char *level)
{
	muduo::Logger::LogLevel ret = muduo::Logger::WARN;
	if(strcasecmp("DEBUG", level)==0)
	{	
		ret = muduo::Logger::DEBUG;
	}
	else if(strcasecmp("INFO", level)==0)
	{
		ret = muduo::Logger::INFO;
	}
	else if(strcasecmp("WARN", level)==0)
	{
		ret = muduo::Logger::WARN;
	}
	else if(strcasecmp("ERROR", level)==0)
	{
		ret = muduo::Logger::ERROR;
	}
	else if(strcasecmp("CLOSE", level)==0)
	{
		ret = muduo::Logger::FATAL;
	}
	return ret;
}

void SetLogFileName(std::string const& filename)
{
    g_Logger.SetFileName(filename.c_str());
}

void SetLogLevel(std::string const& level)
{
	g_Logger.SetLogLevel(level.c_str()); 
}

void SetLogFileSize(int file_size)
{
	g_Logger.SetMaxFileSize(file_size); 
}

void SetLogFileNum(int file_num)
{
	g_Logger.SetMaxFileIndex(file_num);
}



}

