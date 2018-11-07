#ifndef __MAFKA_STRING_ENCODER_H__
#define __MAFKA_STRING_ENCODER_H__


namespace mafka
{


class StringEncoder
{
public:
	static int stringEncode(const char* srcContent, int src_len, char*& dstContent, int& dst_len);
	
};


}


#endif //__MAFKA_STRING_ENCODER_H__

