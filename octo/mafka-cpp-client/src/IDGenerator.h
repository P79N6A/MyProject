#ifndef __MAFKA_ID_GENERATOR_H__
#define __MAFKA_ID_GENERATOR_H__

#include <string>

namespace mafka
{


class IDGenerator
{

public:
	IDGenerator();
	virtual ~IDGenerator();

public:
	std::string const& GetID() const;

private:
	std::string m_id;

};



}



#endif //__MAFKA_ID_GENERATOR_H__