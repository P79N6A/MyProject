/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
#include "core_types.h"

namespace cplugin {

int _kOperationValues[] = {
  Operation::TEST,
  Operation::INSTALL,
  Operation::START,
  Operation::STOP,
  Operation::RESTART,
  Operation::UPGRADE,
  Operation::ROLLBACK,
  Operation::REMOVE
};
const char* _kOperationNames[] = {
  "TEST",
  "INSTALL",
  "START",
  "STOP",
  "RESTART",
  "UPGRADE",
  "ROLLBACK",
  "REMOVE"
};
const std::map<int, const char*> _Operation_VALUES_TO_NAMES(::apache::thrift::TEnumIterator(8, _kOperationValues, _kOperationNames), ::apache::thrift::TEnumIterator(-1, NULL, NULL));

const char* PluginAction::ascii_fingerprint = "B45838E2218560A2104366E601423438";
const uint8_t PluginAction::binary_fingerprint[16] = {0xB4,0x58,0x38,0xE2,0x21,0x85,0x60,0xA2,0x10,0x43,0x66,0xE6,0x01,0x42,0x34,0x38};

uint32_t PluginAction::read(::apache::thrift::protocol::TProtocol* iprot) {

  uint32_t xfer = 0;
  std::string fname;
  ::apache::thrift::protocol::TType ftype;
  int16_t fid;

  xfer += iprot->readStructBegin(fname);

  using ::apache::thrift::protocol::TProtocolException;

  bool isset_name = false;
  bool isset_version = false;
  bool isset_md5 = false;
  bool isset_op = false;
  bool isset_plugin_id = false;
  bool isset_task_id = false;

  while (true)
  {
    xfer += iprot->readFieldBegin(fname, ftype, fid);
    if (ftype == ::apache::thrift::protocol::T_STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
        if (ftype == ::apache::thrift::protocol::T_STRING) {
          xfer += iprot->readString(this->name);
          isset_name = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 2:
        if (ftype == ::apache::thrift::protocol::T_STRING) {
          xfer += iprot->readString(this->version);
          isset_version = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 3:
        if (ftype == ::apache::thrift::protocol::T_STRING) {
          xfer += iprot->readString(this->md5);
          isset_md5 = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 4:
        if (ftype == ::apache::thrift::protocol::T_I32) {
          int32_t ecast0;
          xfer += iprot->readI32(ecast0);
          this->op = (Operation::type)ecast0;
          isset_op = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 5:
        if (ftype == ::apache::thrift::protocol::T_I32) {
          xfer += iprot->readI32(this->plugin_id);
          isset_plugin_id = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 6:
        if (ftype == ::apache::thrift::protocol::T_I32) {
          xfer += iprot->readI32(this->task_id);
          isset_task_id = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      default:
        xfer += iprot->skip(ftype);
        break;
    }
    xfer += iprot->readFieldEnd();
  }

  xfer += iprot->readStructEnd();

  if (!isset_name)
    throw TProtocolException(TProtocolException::INVALID_DATA);
  if (!isset_version)
    throw TProtocolException(TProtocolException::INVALID_DATA);
  if (!isset_md5)
    throw TProtocolException(TProtocolException::INVALID_DATA);
  if (!isset_op)
    throw TProtocolException(TProtocolException::INVALID_DATA);
  if (!isset_plugin_id)
    throw TProtocolException(TProtocolException::INVALID_DATA);
  if (!isset_task_id)
    throw TProtocolException(TProtocolException::INVALID_DATA);
  return xfer;
}

uint32_t PluginAction::write(::apache::thrift::protocol::TProtocol* oprot) const {
  uint32_t xfer = 0;
  xfer += oprot->writeStructBegin("PluginAction");
  xfer += oprot->writeFieldBegin("name", ::apache::thrift::protocol::T_STRING, 1);
  xfer += oprot->writeString(this->name);
  xfer += oprot->writeFieldEnd();
  xfer += oprot->writeFieldBegin("version", ::apache::thrift::protocol::T_STRING, 2);
  xfer += oprot->writeString(this->version);
  xfer += oprot->writeFieldEnd();
  xfer += oprot->writeFieldBegin("md5", ::apache::thrift::protocol::T_STRING, 3);
  xfer += oprot->writeString(this->md5);
  xfer += oprot->writeFieldEnd();
  xfer += oprot->writeFieldBegin("op", ::apache::thrift::protocol::T_I32, 4);
  xfer += oprot->writeI32((int32_t)this->op);
  xfer += oprot->writeFieldEnd();
  xfer += oprot->writeFieldBegin("plugin_id", ::apache::thrift::protocol::T_I32, 5);
  xfer += oprot->writeI32(this->plugin_id);
  xfer += oprot->writeFieldEnd();
  xfer += oprot->writeFieldBegin("task_id", ::apache::thrift::protocol::T_I32, 6);
  xfer += oprot->writeI32(this->task_id);
  xfer += oprot->writeFieldEnd();
  xfer += oprot->writeFieldStop();
  xfer += oprot->writeStructEnd();
  return xfer;
}

const char* THostInfo::ascii_fingerprint = "2748901DF3E03B56075825ABF0FCFD25";
const uint8_t THostInfo::binary_fingerprint[16] = {0x27,0x48,0x90,0x1D,0xF3,0xE0,0x3B,0x56,0x07,0x58,0x25,0xAB,0xF0,0xFC,0xFD,0x25};

uint32_t THostInfo::read(::apache::thrift::protocol::TProtocol* iprot) {

  uint32_t xfer = 0;
  std::string fname;
  ::apache::thrift::protocol::TType ftype;
  int16_t fid;

  xfer += iprot->readStructBegin(fname);

  using ::apache::thrift::protocol::TProtocolException;

  bool isset_name = false;
  bool isset_library_name = false;
  bool isset_hash = false;

  while (true)
  {
    xfer += iprot->readFieldBegin(fname, ftype, fid);
    if (ftype == ::apache::thrift::protocol::T_STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
        if (ftype == ::apache::thrift::protocol::T_STRING) {
          xfer += iprot->readString(this->name);
          isset_name = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 2:
        if (ftype == ::apache::thrift::protocol::T_STRING) {
          xfer += iprot->readString(this->library_name);
          isset_library_name = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 3:
        if (ftype == ::apache::thrift::protocol::T_STRING) {
          xfer += iprot->readString(this->hash);
          isset_hash = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 4:
        if (ftype == ::apache::thrift::protocol::T_STRING) {
          xfer += iprot->readString(this->version);
          this->__isset.version = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      default:
        xfer += iprot->skip(ftype);
        break;
    }
    xfer += iprot->readFieldEnd();
  }

  xfer += iprot->readStructEnd();

  if (!isset_name)
    throw TProtocolException(TProtocolException::INVALID_DATA);
  if (!isset_library_name)
    throw TProtocolException(TProtocolException::INVALID_DATA);
  if (!isset_hash)
    throw TProtocolException(TProtocolException::INVALID_DATA);
  return xfer;
}

uint32_t THostInfo::write(::apache::thrift::protocol::TProtocol* oprot) const {
  uint32_t xfer = 0;
  xfer += oprot->writeStructBegin("THostInfo");
  xfer += oprot->writeFieldBegin("name", ::apache::thrift::protocol::T_STRING, 1);
  xfer += oprot->writeString(this->name);
  xfer += oprot->writeFieldEnd();
  xfer += oprot->writeFieldBegin("library_name", ::apache::thrift::protocol::T_STRING, 2);
  xfer += oprot->writeString(this->library_name);
  xfer += oprot->writeFieldEnd();
  xfer += oprot->writeFieldBegin("hash", ::apache::thrift::protocol::T_STRING, 3);
  xfer += oprot->writeString(this->hash);
  xfer += oprot->writeFieldEnd();
  if (this->__isset.version) {
    xfer += oprot->writeFieldBegin("version", ::apache::thrift::protocol::T_STRING, 4);
    xfer += oprot->writeString(this->version);
    xfer += oprot->writeFieldEnd();
  }
  xfer += oprot->writeFieldStop();
  xfer += oprot->writeStructEnd();
  return xfer;
}

const char* TPluginInfo::ascii_fingerprint = "2748901DF3E03B56075825ABF0FCFD25";
const uint8_t TPluginInfo::binary_fingerprint[16] = {0x27,0x48,0x90,0x1D,0xF3,0xE0,0x3B,0x56,0x07,0x58,0x25,0xAB,0xF0,0xFC,0xFD,0x25};

uint32_t TPluginInfo::read(::apache::thrift::protocol::TProtocol* iprot) {

  uint32_t xfer = 0;
  std::string fname;
  ::apache::thrift::protocol::TType ftype;
  int16_t fid;

  xfer += iprot->readStructBegin(fname);

  using ::apache::thrift::protocol::TProtocolException;

  bool isset_name = false;
  bool isset_library_name = false;
  bool isset_hash = false;

  while (true)
  {
    xfer += iprot->readFieldBegin(fname, ftype, fid);
    if (ftype == ::apache::thrift::protocol::T_STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
        if (ftype == ::apache::thrift::protocol::T_STRING) {
          xfer += iprot->readString(this->name);
          isset_name = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 2:
        if (ftype == ::apache::thrift::protocol::T_STRING) {
          xfer += iprot->readString(this->library_name);
          isset_library_name = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 3:
        if (ftype == ::apache::thrift::protocol::T_STRING) {
          xfer += iprot->readString(this->hash);
          isset_hash = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 4:
        if (ftype == ::apache::thrift::protocol::T_STRING) {
          xfer += iprot->readString(this->version);
          this->__isset.version = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      default:
        xfer += iprot->skip(ftype);
        break;
    }
    xfer += iprot->readFieldEnd();
  }

  xfer += iprot->readStructEnd();

  if (!isset_name)
    throw TProtocolException(TProtocolException::INVALID_DATA);
  if (!isset_library_name)
    throw TProtocolException(TProtocolException::INVALID_DATA);
  if (!isset_hash)
    throw TProtocolException(TProtocolException::INVALID_DATA);
  return xfer;
}

uint32_t TPluginInfo::write(::apache::thrift::protocol::TProtocol* oprot) const {
  uint32_t xfer = 0;
  xfer += oprot->writeStructBegin("TPluginInfo");
  xfer += oprot->writeFieldBegin("name", ::apache::thrift::protocol::T_STRING, 1);
  xfer += oprot->writeString(this->name);
  xfer += oprot->writeFieldEnd();
  xfer += oprot->writeFieldBegin("library_name", ::apache::thrift::protocol::T_STRING, 2);
  xfer += oprot->writeString(this->library_name);
  xfer += oprot->writeFieldEnd();
  xfer += oprot->writeFieldBegin("hash", ::apache::thrift::protocol::T_STRING, 3);
  xfer += oprot->writeString(this->hash);
  xfer += oprot->writeFieldEnd();
  if (this->__isset.version) {
    xfer += oprot->writeFieldBegin("version", ::apache::thrift::protocol::T_STRING, 4);
    xfer += oprot->writeString(this->version);
    xfer += oprot->writeFieldEnd();
  }
  xfer += oprot->writeFieldStop();
  xfer += oprot->writeStructEnd();
  return xfer;
}

const char* TInfos::ascii_fingerprint = "77766324C7E0FE867E2AD83CC118DDE2";
const uint8_t TInfos::binary_fingerprint[16] = {0x77,0x76,0x63,0x24,0xC7,0xE0,0xFE,0x86,0x7E,0x2A,0xD8,0x3C,0xC1,0x18,0xDD,0xE2};

uint32_t TInfos::read(::apache::thrift::protocol::TProtocol* iprot) {

  uint32_t xfer = 0;
  std::string fname;
  ::apache::thrift::protocol::TType ftype;
  int16_t fid;

  xfer += iprot->readStructBegin(fname);

  using ::apache::thrift::protocol::TProtocolException;

  bool isset_host_info = false;
  bool isset_plugin_infos = false;

  while (true)
  {
    xfer += iprot->readFieldBegin(fname, ftype, fid);
    if (ftype == ::apache::thrift::protocol::T_STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
        if (ftype == ::apache::thrift::protocol::T_STRUCT) {
          xfer += this->host_info.read(iprot);
          isset_host_info = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 2:
        if (ftype == ::apache::thrift::protocol::T_LIST) {
          {
            this->plugin_infos.clear();
            uint32_t _size1;
            ::apache::thrift::protocol::TType _etype4;
            iprot->readListBegin(_etype4, _size1);
            this->plugin_infos.resize(_size1);
            uint32_t _i5;
            for (_i5 = 0; _i5 < _size1; ++_i5)
            {
              xfer += this->plugin_infos[_i5].read(iprot);
            }
            iprot->readListEnd();
          }
          isset_plugin_infos = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      default:
        xfer += iprot->skip(ftype);
        break;
    }
    xfer += iprot->readFieldEnd();
  }

  xfer += iprot->readStructEnd();

  if (!isset_host_info)
    throw TProtocolException(TProtocolException::INVALID_DATA);
  if (!isset_plugin_infos)
    throw TProtocolException(TProtocolException::INVALID_DATA);
  return xfer;
}

uint32_t TInfos::write(::apache::thrift::protocol::TProtocol* oprot) const {
  uint32_t xfer = 0;
  xfer += oprot->writeStructBegin("TInfos");
  xfer += oprot->writeFieldBegin("host_info", ::apache::thrift::protocol::T_STRUCT, 1);
  xfer += this->host_info.write(oprot);
  xfer += oprot->writeFieldEnd();
  xfer += oprot->writeFieldBegin("plugin_infos", ::apache::thrift::protocol::T_LIST, 2);
  {
    xfer += oprot->writeListBegin(::apache::thrift::protocol::T_STRUCT, static_cast<uint32_t>(this->plugin_infos.size()));
    std::vector<TPluginInfo> ::const_iterator _iter6;
    for (_iter6 = this->plugin_infos.begin(); _iter6 != this->plugin_infos.end(); ++_iter6)
    {
      xfer += (*_iter6).write(oprot);
    }
    xfer += oprot->writeListEnd();
  }
  xfer += oprot->writeFieldEnd();
  xfer += oprot->writeFieldStop();
  xfer += oprot->writeStructEnd();
  return xfer;
}

} // namespace
