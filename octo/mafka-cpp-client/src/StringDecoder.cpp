#include "StringDecoder.h"
#include <string.h>

namespace mafka
{

int StringDecoder::stringDecode(const char* srcContent, char*& dstContent, int& len) {
	/*
	 * 向Java版本靠齐，仿照目前的MafkaEncoderMessage中的StringDecoder进行操作。
	 * 第一个字节:0，下面4字节，hashcode，下面4个字节：消息长度，下面是消息内容。
     */
	dstContent = (char*)srcContent + 9;
	memcpy(&len, srcContent+5, 4);

	return 0;

}

}

