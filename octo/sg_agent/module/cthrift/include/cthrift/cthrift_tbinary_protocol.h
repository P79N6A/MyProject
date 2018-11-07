//
// Created by Chao Shu on 16/12/18.
//

#ifndef CTHRIFT_CTHRIFT_TBINARY_PROTOCOL_H
#define CTHRIFT_CTHRIFT_TBINARY_PROTOCOL_H

#include <thrift/protocol/TProtocol.h>
#include <thrift/protocol/TVirtualProtocol.h>

#include "cthrift_common.h"
#include "cthrift_transport.h"
#include "cthrift_sgagent.h"

namespace cthrift {

enum UseType {
  CTHRIFT_SERVER,
  CTHRIFT_CLIENT,
  UNDEFINED
};

template<class Transport_>
class CthriftTBinaryProtocolT
    : public TVirtualProtocol<CthriftTBinaryProtocolT<Transport_> > {
 protected:
  UseType use_type_;

  static const int32_t VERSION_MASK = 0xffff0000;
  static const int32_t VERSION_1 = 0x80010000;
  // VERSION_2 (0x80020000)  is taken by TDenseProtocol.

 public:
  CthriftTBinaryProtocolT(boost::shared_ptr <Transport_> trans,
                          const UseType &use_type = UNDEFINED) :
      TVirtualProtocol<CthriftTBinaryProtocolT<Transport_> >(trans),
      trans_(trans.get()),
      string_limit_(cthrift::GetStringLimit()),
      container_limit_(0),
      strict_read_(false),
      strict_write_(true),
      string_buf_(NULL),
      string_buf_size_(0), use_type_(use_type) {
    if (0 == g_atomic_i32_seq_id.get()) {
      g_atomic_i32_seq_id.getAndSet(1);   //tiny chance NOT safe
    }
  }

  CthriftTBinaryProtocolT(boost::shared_ptr <Transport_> trans,
                          int32_t string_limit,
                          int32_t container_limit,
                          bool strict_read,
                          bool strict_write) :
      TVirtualProtocol<CthriftTBinaryProtocolT<Transport_> >(trans),
      trans_(trans.get()),
      string_limit_(string_limit),
      container_limit_(container_limit),
      strict_read_(strict_read),
      strict_write_(strict_write),
      string_buf_(NULL),
      string_buf_size_(0) {}

  ~CthriftTBinaryProtocolT() {
    if (string_buf_ != NULL) {
      std::free(string_buf_);
      string_buf_size_ = 0;
    }
  }

  void setStringSizeLimit(int32_t string_limit) {
    string_limit_ = string_limit;
  }

  void setContainerSizeLimit(int32_t container_limit) {
    container_limit_ = container_limit;
  }

  void setStrict(bool strict_read, bool strict_write) {
    strict_read_ = strict_read;
    strict_write_ = strict_write;
  }

  /**
   * Writing functions.
   */

  inline int32_t GetSeqID(void);

  /*ol*/ inline uint32_t writeMessageBegin(const std::string &name,
                                    const TMessageType messageType,
                                    const int32_t seqid);

  /*ol*/ inline uint32_t writeMessageEnd();


  inline uint32_t writeStructBegin(const char* name);

  inline uint32_t writeStructEnd();

  inline uint32_t writeFieldBegin(const char* name,
                           const TType fieldType,
                           const int16_t fieldId);

  inline uint32_t writeFieldEnd();

  inline uint32_t writeFieldStop();

  inline uint32_t writeMapBegin(const TType keyType,
                         const TType valType,
                         const uint32_t size);

  inline uint32_t writeMapEnd();

  inline uint32_t writeListBegin(const TType elemType, const uint32_t size);

  inline uint32_t writeListEnd();

  inline uint32_t writeSetBegin(const TType elemType, const uint32_t size);

  inline uint32_t writeSetEnd();

  inline uint32_t writeBool(const bool value);

  inline uint32_t writeByte(const int8_t byte);

  inline uint32_t writeI16(const int16_t i16);

  inline uint32_t writeI32(const int32_t i32);

  inline uint32_t writeI64(const int64_t i64);

  inline uint32_t writeDouble(const double dub);

  inline uint32_t writeString(const std::string& str);

  inline uint32_t writeBinary(const std::string& str);

  /**
   * Reading functions
   */


  /*ol*/ uint32_t readMessageBegin(std::string &name,
                                   TMessageType &messageType,
                                   int32_t &seqid);

  /*ol*/ uint32_t readMessageEnd();

  inline uint32_t readStructBegin(std::string& name);

  inline uint32_t readStructEnd();

  inline uint32_t readFieldBegin(std::string& name,
                          TType &fieldType,
                          int16_t &fieldId);

  inline uint32_t readFieldEnd();

  inline uint32_t readMapBegin(TType& keyType,
                        TType &valType,
                        uint32_t &size);

  inline uint32_t readMapEnd();

  inline uint32_t readListBegin(TType& elemType, uint32_t& size);

  inline uint32_t readListEnd();

  inline uint32_t readSetBegin(TType& elemType, uint32_t& size);

  inline uint32_t readSetEnd();

  inline uint32_t readBool(bool& value);
  // Provide the default readBool() implementation for std::vector<bool>
  using TVirtualProtocol< CthriftTBinaryProtocolT<Transport_> >::readBool;

  inline uint32_t readByte(int8_t& byte);

  inline uint32_t readI16(int16_t& i16);

  inline uint32_t readI32(int32_t& i32);

  inline uint32_t readI64(int64_t& i64);

  inline uint32_t readDouble(double& dub);

  inline uint32_t readString(std::string& str);

  inline uint32_t readBinary(std::string& str);

 protected:
  uint32_t readStringBody(std::string &str, int32_t sz);

  Transport_ *trans_;

  int32_t string_limit_;
  int32_t container_limit_;

  // Enforce presence of version identifier
  bool strict_read_;
  bool strict_write_;

  // Buffer for reading strings, save for the lifetime of the protocol to
  // avoid memory churn allocating memory on every string read
  uint8_t *string_buf_;
  int32_t string_buf_size_;

};

//typedef CthriftTBinaryProtocolT<TTransport> CthriftTBinaryProtocol;

typedef CthriftTBinaryProtocolT<CthriftTransport> CthriftTBinaryProtoWithCthriftTrans;

typedef CthriftTBinaryProtocolT<TMemoryBuffer> CthriftTBinaryProtocolWithTMemoryBuf;

template<>
inline uint32_t CthriftTBinaryProtocolT<CthriftTransport>::
writeMessageBegin(const std::string &name,
                  const TMessageType messageType,
                  const int32_t seqid);


template<>
inline int32_t CthriftTBinaryProtocolT<TMemoryBuffer>::GetSeqID(void);

/**
 * Constructs binary protocol handlers
 */
template<class Transport_>
class CthriftTBinaryProtocolFactoryT: public TProtocolFactory {
 public:
  CthriftTBinaryProtocolFactoryT() :
      string_limit_(0),
      container_limit_(0),
      strict_read_(false),
      strict_write_(true) {}

  CthriftTBinaryProtocolFactoryT(int32_t string_limit, int32_t container_limit,
                                 bool strict_read, bool strict_write) :
      string_limit_(string_limit),
      container_limit_(container_limit),
      strict_read_(strict_read),
      strict_write_(strict_write) {}

  virtual ~CthriftTBinaryProtocolFactoryT() {}

  void setStringSizeLimit(int32_t string_limit) {
    string_limit_ = string_limit;
  }

  void setContainerSizeLimit(int32_t container_limit) {
    container_limit_ = container_limit;
  }

  void setStrict(bool strict_read, bool strict_write) {
    strict_read_ = strict_read;
    strict_write_ = strict_write;
  }

  boost::shared_ptr <TProtocol>
  getProtocol(boost::shared_ptr <TTransport> trans) {
    boost::shared_ptr <Transport_> specific_trans =
        boost::dynamic_pointer_cast<Transport_>(trans);
    TProtocol *prot;
    if (specific_trans) {
      prot =
          new CthriftTBinaryProtocolT<Transport_>(specific_trans,
                                                  string_limit_,
                                                  container_limit_,
                                                  strict_read_,
                                                  strict_write_);
    } else {
      prot = new CthriftTBinaryProtocolT<TTransport>(trans, string_limit_, container_limit_,
                                        strict_read_, strict_write_);
    }

    return boost::shared_ptr<TProtocol>(prot);
  }

 private:
  int32_t string_limit_;
  int32_t container_limit_;
  bool strict_read_;
  bool strict_write_;

};

typedef CthriftTBinaryProtocolFactoryT<TTransport>
    CthriftTBinaryProtocolFactory;

}

#include "cthrift_tbinary_protocol.tcc"

#endif // #ifndef _THRIFT_PROTOCOL_TBINARYPROTOCOL_H_
