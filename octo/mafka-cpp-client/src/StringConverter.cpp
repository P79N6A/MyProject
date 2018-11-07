#include "StringConverter.h"

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

namespace mafka
{


int StringConverter::ParseInt(std::string const& str)
{
	return atoi(str.c_str());
}

double StringConverter::ParseFloat(std::string const& str)
{
	return atof(str.c_str());
}

std::string StringConverter::IntToString(int val)
{
	char buf[128];
	memset(buf, 0, sizeof(buf));
	snprintf(buf, sizeof(buf), "%d", val);
	return std::string(buf);
}

std::string StringConverter::UintToString(uint32_t val)
{
	char buf[128];
	memset(buf, 0, sizeof(buf));
	snprintf(buf, sizeof(buf), "%u", val);
	return std::string(buf);
}

std::string StringConverter::Int64ToString(int64_t val)
{
	char buf[128];
	memset(buf, 0, sizeof(buf));
	snprintf(buf, sizeof(buf),"%lld",val);
	return std::string(buf);
}

std::string StringConverter::Uint64ToString(uint64_t val)
{
	char buf[128];
	memset(buf, 0, sizeof(buf));
	snprintf(buf, sizeof(buf),"%llu",val);
	return std::string(buf);
}

}

