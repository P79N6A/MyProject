#ifndef __MAFKA_STRING_UTIL_H__
#define __MAFKA_STRING_UTIL_H__

#include <string>

namespace mafka
{


class StringUtil
{
public:
	static void TrimString(std::string& str, bool left = true, bool right = true);
	static bool StartsWith(std::string const& str, std::string const& pattern);
	static std::string EMPTY_STRING;
};


}


#endif //__MAFKA_STRING_UTIL_H__

