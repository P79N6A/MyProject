#ifndef THRIFTSERIALIZE_H_
#define THRIFTSERIALIZE_H_

#include <protocol/TBinaryProtocol.h>
#include <transport/TTransportUtils.h>
#include "log4cplus.h"
#include "inc_comm.h"

using namespace apache::thrift::transport;  // NOLINT
using namespace apache::thrift::protocol;  // NOLINT

template <typename Type>
std::string Thrift2String(const Type& object) 
{
    boost::shared_ptr<TMemoryBuffer> membuffer(new TMemoryBuffer());
    boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(membuffer));
    try
    {
        object.write(protocol.get());
    }
    catch(TException& e)
    {
        LOG_ERROR( "Thrift2String catch error : " << e.what());
        return "";
    }

    return membuffer -> getBufferAsString();
}

template <typename Type>
bool String2Thrift(const std::string& buffer, Type* object) 
{
    try
    {
        boost::shared_ptr<TMemoryBuffer> membuffer(new TMemoryBuffer());
        membuffer->write((const uint8_t*)buffer.data(), buffer.size());

        boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(membuffer));
        object -> read(protocol.get());
    }
    catch(TException& e)
    {
        LOG_ERROR( "String2Thrift catch error : " << e.what());
        return false;
    }

    return true;
}

#endif  // SERIALIZE_H_
