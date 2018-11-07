#include "StringUtil.h"

namespace mafka
{

std::string StringUtil::EMPTY_STRING;

void StringUtil::TrimString(std::string& str, bool left/* = true*/, bool right/* = true*/)
{
	static const std::string delims = " \t\r";
	if(right)
		str.erase(str.find_last_not_of(delims)+1); // trim right
	if(left)
		str.erase(0, str.find_first_not_of(delims)); // trim left
}

bool StringUtil::StartsWith(std::string const& str, std::string const& pattern)
{
	return str.find(pattern) == 0;
}

}

