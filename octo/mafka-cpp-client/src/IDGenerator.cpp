#include "IDGenerator.h"
#include "StringConverter.h"
#include "TimeUtil.h"

#include "Common.h"

namespace mafka
{

IDGenerator::IDGenerator()
{
	std::string ip_address;
	GetPublicIPAddress(ip_address);
	m_id = ip_address +
				"_" +
				StringConverter::Int64ToString(TimeUtil::TimeNow()) +
				"_" +
				StringConverter::IntToString(GetTid());
}

IDGenerator::~IDGenerator()
{

}

std::string const& IDGenerator::GetID() const
{
	return m_id;
}

}

