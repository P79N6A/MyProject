#ifndef __MAFKA_MESSAGE_HANDLER_H__
#define __MAFKA_MESSAGE_HANDLER_H__

namespace mafka
{

enum ConsumeStatus
{
    CONSUME_SUCCESS = 0,
    CONSUME_LATER = 1,
    CONSUME_FAILURE = 2
};


class MessageHandler
{
public:
	virtual ~MessageHandler(){}

public:
	virtual ConsumeStatus OnRecvMessage(int partition, const char* buf, int len) = 0;
};

}



#endif //__MAFKA_MESSAGE_HANDLER_H__
