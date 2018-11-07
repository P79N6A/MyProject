#include <ctype.h>
#include <string.h>
#include <fstream>
#include <iostream>

#include <log4cplus/parse_config.h>

namespace cmdlog{
std::map<std::string,std::string> Configuration::_record = std::map<std::string,std::string>();

Configuration::Configuration():_logLevel(""),_logPath(""){
}
Configuration* Configuration::Instance(){
	static Configuration Config;
	return &Config;
}

bool Configuration::isSpace(char c){
	if (' ' == c || '\t' == c)
		return true;
	return false; 
}

void Configuration::PostTrim(std::string& key){
	if (key.empty())
		return;
	unsigned int start_pos = 0,end_pos = 0;
	for (unsigned int i = 0; i < key.size(); ++i)
	{
		/* code */
		if(!isSpace(key[i])){
			start_pos = i;
			break;
		}
	}
	if (start_pos == key.size())  //only space
		return;

	for (unsigned int i = key.size() - 1; i >= start_pos; --i)
	{
		/* code */
		if(!isSpace(key[i])){
			end_pos = i;
			break;
		}
	}

	key = key.substr(start_pos,end_pos - start_pos + 1);
}

bool Configuration::PreAnalyseLine(const std::string & line, std::string & key, std::string & value){
	if (line.empty())
		return false;
	int start_pos = 0;
	int end_pos = line.size() - 1;
	int pos;
	pos = line.find('#');
	if (-1 != pos){
		if(0 == pos) return false;//isComment
		end_pos = pos - 1; // cut string before # 
	}

	std::string new_line = line.substr(start_pos, end_pos + 1 - start_pos); //delete comment 	
	pos = line.find('=');
	if (-1 == pos) return false; 
	key = new_line.substr(0,pos);
	value = new_line.substr(pos+1);

	PostTrim(key);
	if (key.empty()){ 
		return false;
	}
	
	PostTrim(value);
	if (value.empty()){
		return false;
	}
	return true;
}

void Configuration::init(const char* filename){
    _record.clear();
    std::ifstream fin;
    std::string line, key, value;
    fin.open(filename, std::ifstream::in);
    if (fin.is_open()) {   
        while(getline(fin, line)) {
           if(PreAnalyseLine(line,key,value)){
            	_record[key] = value;
            }
        }
    }
    fin.close();
    lookupValue("level",_logLevel);
    lookupValue("path",_logPath);   
    lookupValue("name",_logName);   
    lookupValue("app_key",_appKey);   
    lookupValue("size",_maxRollFileSize);
    lookupValue("count",_maxRollFileCount);
    lookupValue("period",_timeRollSchedule);
    lookupValue("nonblock",_QueueNonblock);
    lookupValue("history",_maxHistory);
    lookupValue("type",_appendType);
}

void Configuration::lookupValue(const std::string name, std::string& value){
    if (_record.find(name) == _record.end()) 
        return;
    value = _record[name];
    if (strcmp(name.c_str(),"path") == 0 && value.size() > 0 && value[value.size()-1]!='/') 
        value += '/';
}

const std::string Configuration::get(const char *key) const{
    std::string name(key);
    if (_record.find(name) == _record.end()) return "";
    return _record[name]; 
}

void Configuration::test(){
	//std::cout << "level "<<_logLevel<<std::endl;
	//std::cout << "path "<<_logPath<<std::endl;
}

}
