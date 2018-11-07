#ifndef __MAFKA_COMPLETE_HANDLER_H__
#define __MAFKA_COMPLETE_HANDLER_H__

namespace mafka
{

class CompletionHandler
{
public:
	virtual ~CompletionHandler(){}

public:
	virtual void OnComplete(int error_code, int partition, const char* buf, int len) = 0;
};

}



#endif //__MAFKA_COMPLETE_HANDLER_H__