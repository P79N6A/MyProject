#ifndef _PARSE_CONFIG_H
#define _PARSE_CONFIG_H
#include <string>
#include <map>
namespace cmdlog{
class Configuration{
public:
	Configuration();	
	static Configuration* Instance();
	void init(const char* filename);
	void test();
    const std::string get(const char *key) const;
private:
    // Disallow copying of instances of this class
	Configuration(const Configuration& );
	Configuration& operator=(const Configuration& );

	bool isSpace(char c);
	void PostTrim(std::string& key);
	bool PreAnalyseLine(const std::string & line, std::string & key, std::string & value);
	void lookupValue(const std::string name, std::string& value);
public:
	std::string _logLevel;
	std::string _logPath;
	std::string _logName;
	std::string _appKey;
	std::string _maxRollFileSize;
	std::string _maxRollFileCount;
	std::string _timeRollSchedule;
	std::string _QueueNonblock;
    std::string _maxHistory;
    std::string _appendType;
private:
	static std::map<std::string,std::string> _record;
};
}
#endif
