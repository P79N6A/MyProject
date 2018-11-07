#ifndef __MAFKA_COMMON_H__
#define __MAFKA_COMMON_H__

#include <string>
#include <map>


namespace mafka
{


typedef char int8_t;
typedef short int16_t;
typedef int int32_t;
typedef long long int64_t;

typedef unsigned char uint8_t;
typedef unsigned short uint16_t;
typedef unsigned int uint32_t;
typedef unsigned long long uint64_t;

typedef unsigned int uint;

typedef long long timestamp_t;
const timestamp_t MAX_ABS_TIME = 0x7fffffffffffffff;

const uint64_t UINT64_MAX = uint64_t(-1);
const uint64_t UINT64_OVERFLOW = uint64_t(0x8000000000000000);
const int RETRY_COUNT_MAX = 3;

int GetTid();

void GetPublicIPAddress(std::string& ip_address);

void setStringByMap(std::map<std::string, std::string> srcMap, std::string& tarStr);
}


#endif //__MAFKA_COMMON_H__

