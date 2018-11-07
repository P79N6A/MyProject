#ifndef __MAFKA_STRING_DECODER_H__
#define __MAFKA_STRING_DECODER_H__


namespace mafka
{


class StringDecoder
{
public:
	static int stringDecode(const char* srcContent, char*& dstContent, int& len);
	
};


}


#endif //__MAFKA_STRING_DECODER_H__

