#include "StringEncoder.h"
#include <string.h>
#include <stdlib.h>


namespace mafka
{

int StringEncoder::stringEncode(const char* srcContent, int src_len, char*& dstContent, int& dst_len) {
	/*
	 * 向Java版本靠齐，仿照目前的MafkaEncoderMessage中的StringDecoder进行操作。
	 * 第一个字节:0，下面4个字节，hashcode，下面4个字节：消息长度，下面是消息内容。
     */
	dstContent = (char*) calloc(src_len + 9, sizeof(char));
	int index = 0;
	char firstByte = 0;
	memcpy(dstContent + index, &firstByte, 1);
	index += 1;
	int hashCode = 1000;
	memcpy(dstContent + index, &hashCode, 4); 
	index += 4;

	memcpy(dstContent + index, &src_len, 4);
	index += 4;
	memcpy(dstContent + index, srcContent, src_len);
	dst_len = index + src_len;
	return 0;

}

}

