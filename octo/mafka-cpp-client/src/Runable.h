#ifndef __MAFKA_RUNABLE_H__
#define __MAFKA_RUNABLE_H__

namespace mafka
{

class Runable
{
public:
	Runable(){}
	virtual ~Runable(){}

public:
	virtual void Run() = 0;
};

}

#endif //__MAFKA_RUNABLE_H__


