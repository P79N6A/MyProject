#ifndef SERIALIZE_H_
#define SERIALIZE_H_

#include <config.h>
#include <protocol/TBinaryProtocol.h>
#include <transport/TTransport.h>

template<typename ThriftStruct>
std::string ThriftToString(const ThriftStruct& ts) 
{
  using namespace apache::thrift::transport;  // NOLINT
  using namespace apache::thrift::protocol;  // NOLINT
  TMemoryBuffer* buffer = new TMemoryBuffer;
  boost::shared_ptr<TTransport> trans(buffer);
  TBinaryProtocol protocol(trans);
  ts.write(&protocol);
  uint8_t* buf;
  uint32_t size;
  buffer->getBuffer(&buf, &size);
  return std::string((char*)buf, (unsigned int)size);  // NOLINT
}

template<typename ThriftStruct>
bool StringToThrift(const std::string& buff, ThriftStruct* ts) 
{
  using namespace apache::thrift::transport;  // NOLINT
  using namespace apache::thrift::protocol;  // NOLINT
  TMemoryBuffer* buffer = new TMemoryBuffer;
  buffer->write((const uint8_t*)buff.data(), buff.size());
  boost::shared_ptr<TTransport> trans(buffer);
  TBinaryProtocol protocol(trans);
  ts->read(&protocol);
  return true;
}

#endif  // SERIALIZE_H_
