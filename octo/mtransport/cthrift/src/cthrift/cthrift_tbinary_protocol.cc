//
// Created by Chao Shu on 16/2/24.
//

#include "cthrift_tbinary_protocol.h"

using namespace cthrift;

uint32_t CthriftTBinaryProtocol::readOctoInfo(void) {
  CLOG_STR_DEBUG("readOctoInfo");

  string str_field_name;
  apache::thrift::protocol::TType field_type;
  int16_t i16_field_id;
  //bool b_get_mtrace = false;
  uint32_t u32_read = 0;

  while (1) {
    readFieldBegin (str_field_name , field_type , i16_field_id);
    if (apache::thrift::protocol::T_STOP == field_type) {
      CLOG_STR_DEBUG("read to end");
      break;
    }

    switch (i16_field_id) {
      /*case CthriftSgagent::kUUIDFieldID:
        string str_uuid;
        u32_read += readString (str_uuid);
        cthrift_context_info_.SetUUID (str_uuid);

        b_get_uuid = true;
        break;
      case CthriftSgagent::kMTraceFieldID:

        //TODO

        b_get_mtrace = true;
        break;*/
      default:
        u32_read += skip (field_type);
        break;
    }

    u32_read += readFieldEnd ();
  }

  //TODO mtrace

  //cthrift_context_info_.SetNeedPack (b_get_uuid);

  return u32_read;
}


uint32_t CthriftTBinaryProtocol::readMessageEnd(void) {
  CLOG_STR_DEBUG("readMessageEnd");
  uint32_t u32_ret = TBinaryProtocolT<TTransport>::readMessageEnd ();
  if (u32_ret) {
    CLOG_STR_ERROR("TBinaryProtocol::readMessageEnd ret " << u32_ret);
    return u32_ret;
  }

  return readOctoInfo ();
}


uint32_t CthriftTBinaryProtocol::writeMessageBegin(const std::string&
name ,
                                                   const TMessageType messageType ,
                                                   const int32_t seqid) {
  CLOG_STR_DEBUG("writeMessageBegin");
  uint32_t u32_ret =
      TBinaryProtocolT<TTransport>::writeMessageBegin (name , messageType ,
                                                       seqid);
  if (u32_ret) {
    CLOG_STR_ERROR("TBinaryProtocol::writeMessageBegin ret " << u32_ret);
    return u32_ret;
  }

  return writeOctoInfo (name , messageType , seqid);
}


uint32_t CthriftTBinaryProtocol::writeOctoInfo(const std::string& name ,
                                               const TMessageType messageType ,
                                               const int32_t seqid) {
  uint32_t u32_offset = 0;
  //TODO write mtrace info
  return u32_offset;
}


uint32_t CthriftTBinaryProtocol::writeMtraceInfo(const std::string& name ,
                                                 const TMessageType messageType ,
                                                 const int32_t seqid) {
return 0;
  //TODO

  /*if (true == mtrace_info_.b_inited) {
    LOG_DEBUG << "resend mtrace info";
    //TODO
  } else {
    LOG_DEBUG << "init mtrace info";
    //TODO
  }*/
}

/*uint32_t CthriftTBinaryProtocol::writeCthriftContextInfo(void) {
  uint32_t u32_offset = 0;

  if (cthrift_context_info_.NeedPackUUID ()) {
    cthrift_context_info_.GeneteUuid ();
    u32_offset = writeFieldBegin ("uuid" ,
                                  ::apache::thrift::protocol::T_STRING ,
                                  CthriftSgagent::kUUIDFieldID);
    u32_offset += writeString (cthrift_context_info_.GetStrUUID ());
    cthrift_context_info_.ClearUUID ();
    u32_offset += writeFieldEnd ();
  }


  return u32_offset;
}


bool CthriftTBinaryProtocol::HasMtraceInfo(void) {
  if (string_buf_size_ > sizeof (int8_t) + sizeof (int16_t)) {
    int8_t i8_type;
    readByte (i8_type);  //No need fetch result, always 1 in thrift-0.8
    LOG_DEBUG << "field type " << i8_type;

    if (T_STRUCT == i8_type) {
      int8_t i8_first;
      readByte (i8_first);
      int8_t i8_second;
      readByte (i8_second);

      int16_t i16_id = ((i8_first & 0xff) << 8) | (i8_second & 0xff);
      LOG_DEBUG << "i16_id " << i16_id;

      if (kMTraceFieldID == i16_id) {
        return true;
      }
    }
  } else {
    LOG_DEBUG << "No mtrace info, maybe original thrift";
  }

  return false;
}



boost::shared_ptr<TProtocol>
CthriftTBinaryProtocolFactoryT<Transport_>::getProtocol(boost::shared_ptr
                                                            <TTransport> trans) {
  boost::shared_ptr<Transport_> specific_trans =
      boost::dynamic_pointer_cast<Transport_> (trans);
  TProtocol* prot;
  if (specific_trans) {
    prot = new CthriftTBinaryProtocol (specific_trans ,
                                                    string_limit_ ,
                                                    container_limit_ ,
                                                    strict_read_ ,
                                                    strict_write_);
  } else {
    prot = new OctoTBinaryProtocol (trans , string_limit_ , container_limit_ ,
                                    strict_read_ , strict_write_);
  }

  return boost::shared_ptr<TProtocol> (prot);
}*/
