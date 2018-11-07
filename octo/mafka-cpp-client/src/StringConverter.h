#ifndef __MAFKA_STRING_CONVERTER_H__
#define __MAFKA_STRING_CONVERTER_H__

#include <string>
#include "Common.h"

namespace mafka
{

class StringConverter
{
public:
	static int ParseInt(std::string const& str);
	static double ParseFloat(std::string const& str);

	static std::string IntToString(int val);
	static std::string UintToString(uint32_t val);

	static std::string Int64ToString(int64_t val);
	static std::string Uint64ToString(uint64_t val);
};

}


#endif //__MAFKA_STRING_CONVERTER_H__

