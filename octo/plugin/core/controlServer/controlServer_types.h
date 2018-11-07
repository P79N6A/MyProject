/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
#ifndef controlServer_TYPES_H
#define controlServer_TYPES_H

#include <Thrift.h>
#include <TApplicationException.h>
#include <protocol/TProtocol.h>
#include <transport/TTransport.h>



namespace Controller {

struct Operation {
  enum type {
    TEST = 10000,
    INSTALL = 0,
    START = 1,
    STOP = 2,
    RESTART = 3,
    UPGRADE = 4,
    ROLLBACK = 5
  };
};

extern const std::map<int, const char*> _Operation_VALUES_TO_NAMES;

struct HealthEnum {
  enum type {
    Alive = 0,
    Dead = 1,
    Stop = 2,
    InComplete = 3
  };
};

extern const std::map<int, const char*> _HealthEnum_VALUES_TO_NAMES;

typedef struct _InvalidOperation__isset {
  _InvalidOperation__isset() : whatOp(false), why(false) {}
  bool whatOp;
  bool why;
} _InvalidOperation__isset;

class InvalidOperation : public ::apache::thrift::TException {
 public:

  static const char* ascii_fingerprint; // = "3F5FC93B338687BC7235B1AB103F47B3";
  static const uint8_t binary_fingerprint[16]; // = {0x3F,0x5F,0xC9,0x3B,0x33,0x86,0x87,0xBC,0x72,0x35,0xB1,0xAB,0x10,0x3F,0x47,0xB3};

  InvalidOperation() : whatOp(0), why("") {
  }

  virtual ~InvalidOperation() throw() {}

  int32_t whatOp;
  std::string why;

  _InvalidOperation__isset __isset;

  void __set_whatOp(const int32_t val) {
    whatOp = val;
  }

  void __set_why(const std::string& val) {
    why = val;
  }

  bool operator == (const InvalidOperation & rhs) const
  {
    if (!(whatOp == rhs.whatOp))
      return false;
    if (!(why == rhs.why))
      return false;
    return true;
  }
  bool operator != (const InvalidOperation &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const InvalidOperation & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _Location__isset {
  _Location__isset() : center(false), idc(false) {}
  bool center;
  bool idc;
} _Location__isset;

class Location {
 public:

  static const char* ascii_fingerprint; // = "4BF81DD46A7371532E49811022D58D36";
  static const uint8_t binary_fingerprint[16]; // = {0x4B,0xF8,0x1D,0xD4,0x6A,0x73,0x71,0x53,0x2E,0x49,0x81,0x10,0x22,0xD5,0x8D,0x36};

  Location() : region(""), center(""), idc("") {
  }

  virtual ~Location() throw() {}

  std::string region;
  std::string center;
  std::string idc;

  _Location__isset __isset;

  void __set_region(const std::string& val) {
    region = val;
  }

  void __set_center(const std::string& val) {
    center = val;
    __isset.center = true;
  }

  void __set_idc(const std::string& val) {
    idc = val;
    __isset.idc = true;
  }

  bool operator == (const Location & rhs) const
  {
    if (!(region == rhs.region))
      return false;
    if (__isset.center != rhs.__isset.center)
      return false;
    else if (__isset.center && !(center == rhs.center))
      return false;
    if (__isset.idc != rhs.__isset.idc)
      return false;
    else if (__isset.idc && !(idc == rhs.idc))
      return false;
    return true;
  }
  bool operator != (const Location &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const Location & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _Department__isset {
  _Department__isset() : pdl(false) {}
  bool pdl;
} _Department__isset;

class Department {
 public:

  static const char* ascii_fingerprint; // = "5B708A954C550ECA9C1A49D3C5CAFAB9";
  static const uint8_t binary_fingerprint[16]; // = {0x5B,0x70,0x8A,0x95,0x4C,0x55,0x0E,0xCA,0x9C,0x1A,0x49,0xD3,0xC5,0xCA,0xFA,0xB9};

  Department() : owt(""), pdl("") {
  }

  virtual ~Department() throw() {}

  std::string owt;
  std::string pdl;

  _Department__isset __isset;

  void __set_owt(const std::string& val) {
    owt = val;
  }

  void __set_pdl(const std::string& val) {
    pdl = val;
    __isset.pdl = true;
  }

  bool operator == (const Department & rhs) const
  {
    if (!(owt == rhs.owt))
      return false;
    if (__isset.pdl != rhs.__isset.pdl)
      return false;
    else if (__isset.pdl && !(pdl == rhs.pdl))
      return false;
    return true;
  }
  bool operator != (const Department &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const Department & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _Plugin__isset {
  _Plugin__isset() : id(false) {}
  bool id;
} _Plugin__isset;

class Plugin {
 public:

  static const char* ascii_fingerprint; // = "1B53D3FAC5A5CFD45E7E867DA6314FAB";
  static const uint8_t binary_fingerprint[16]; // = {0x1B,0x53,0xD3,0xFA,0xC5,0xA5,0xCF,0xD4,0x5E,0x7E,0x86,0x7D,0xA6,0x31,0x4F,0xAB};

  Plugin() : name(""), version(""), md5(""), id(0) {
  }

  virtual ~Plugin() throw() {}

  std::string name;
  std::string version;
  std::string md5;
  int64_t id;

  _Plugin__isset __isset;

  void __set_name(const std::string& val) {
    name = val;
  }

  void __set_version(const std::string& val) {
    version = val;
  }

  void __set_md5(const std::string& val) {
    md5 = val;
  }

  void __set_id(const int64_t val) {
    id = val;
    __isset.id = true;
  }

  bool operator == (const Plugin & rhs) const
  {
    if (!(name == rhs.name))
      return false;
    if (!(version == rhs.version))
      return false;
    if (!(md5 == rhs.md5))
      return false;
    if (__isset.id != rhs.__isset.id)
      return false;
    else if (__isset.id && !(id == rhs.id))
      return false;
    return true;
  }
  bool operator != (const Plugin &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const Plugin & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};


class PluginHealth {
 public:

  static const char* ascii_fingerprint; // = "D6FD826D949221396F4FFC3ECCD3D192";
  static const uint8_t binary_fingerprint[16]; // = {0xD6,0xFD,0x82,0x6D,0x94,0x92,0x21,0x39,0x6F,0x4F,0xFC,0x3E,0xCC,0xD3,0xD1,0x92};

  PluginHealth() : name(""), status((HealthEnum::type)0) {
  }

  virtual ~PluginHealth() throw() {}

  std::string name;
  HealthEnum::type status;

  void __set_name(const std::string& val) {
    name = val;
  }

  void __set_status(const HealthEnum::type val) {
    status = val;
  }

  bool operator == (const PluginHealth & rhs) const
  {
    if (!(name == rhs.name))
      return false;
    if (!(status == rhs.status))
      return false;
    return true;
  }
  bool operator != (const PluginHealth &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const PluginHealth & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _Performance__isset {
  _Performance__isset() : future(false) {}
  bool future;
} _Performance__isset;

class Performance {
 public:

  static const char* ascii_fingerprint; // = "93A1111D3E071522253C5379C504D11B";
  static const uint8_t binary_fingerprint[16]; // = {0x93,0xA1,0x11,0x1D,0x3E,0x07,0x15,0x22,0x25,0x3C,0x53,0x79,0xC5,0x04,0xD1,0x1B};

  Performance() : cpu(""), memory(""), io(""), network(""), future("") {
  }

  virtual ~Performance() throw() {}

  std::string cpu;
  std::string memory;
  std::string io;
  std::string network;
  std::string future;

  _Performance__isset __isset;

  void __set_cpu(const std::string& val) {
    cpu = val;
  }

  void __set_memory(const std::string& val) {
    memory = val;
  }

  void __set_io(const std::string& val) {
    io = val;
  }

  void __set_network(const std::string& val) {
    network = val;
  }

  void __set_future(const std::string& val) {
    future = val;
    __isset.future = true;
  }

  bool operator == (const Performance & rhs) const
  {
    if (!(cpu == rhs.cpu))
      return false;
    if (!(memory == rhs.memory))
      return false;
    if (!(io == rhs.io))
      return false;
    if (!(network == rhs.network))
      return false;
    if (__isset.future != rhs.__isset.future)
      return false;
    else if (__isset.future && !(future == rhs.future))
      return false;
    return true;
  }
  bool operator != (const Performance &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const Performance & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _PluginVersion__isset {
  _PluginVersion__isset() : future(false) {}
  bool future;
} _PluginVersion__isset;

class PluginVersion {
 public:

  static const char* ascii_fingerprint; // = "2748901DF3E03B56075825ABF0FCFD25";
  static const uint8_t binary_fingerprint[16]; // = {0x27,0x48,0x90,0x1D,0xF3,0xE0,0x3B,0x56,0x07,0x58,0x25,0xAB,0xF0,0xFC,0xFD,0x25};

  PluginVersion() : hostname(""), version(""), timestamp(""), future("") {
  }

  virtual ~PluginVersion() throw() {}

  std::string hostname;
  std::string version;
  std::string timestamp;
  std::string future;

  _PluginVersion__isset __isset;

  void __set_hostname(const std::string& val) {
    hostname = val;
  }

  void __set_version(const std::string& val) {
    version = val;
  }

  void __set_timestamp(const std::string& val) {
    timestamp = val;
  }

  void __set_future(const std::string& val) {
    future = val;
    __isset.future = true;
  }

  bool operator == (const PluginVersion & rhs) const
  {
    if (!(hostname == rhs.hostname))
      return false;
    if (!(version == rhs.version))
      return false;
    if (!(timestamp == rhs.timestamp))
      return false;
    if (__isset.future != rhs.__isset.future)
      return false;
    else if (__isset.future && !(future == rhs.future))
      return false;
    return true;
  }
  bool operator != (const PluginVersion &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const PluginVersion & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};


class CPluginInfo {
 public:

  static const char* ascii_fingerprint; // = "DDD972AEE79884DF4FD5FA5C0DD859AE";
  static const uint8_t binary_fingerprint[16]; // = {0xDD,0xD9,0x72,0xAE,0xE7,0x98,0x84,0xDF,0x4F,0xD5,0xFA,0x5C,0x0D,0xD8,0x59,0xAE};

  CPluginInfo() : cpu(0), mem(0), ver(""), startTime(""), timestamp("") {
  }

  virtual ~CPluginInfo() throw() {}

  int32_t cpu;
  int32_t mem;
  std::string ver;
  std::string startTime;
  std::string timestamp;

  void __set_cpu(const int32_t val) {
    cpu = val;
  }

  void __set_mem(const int32_t val) {
    mem = val;
  }

  void __set_ver(const std::string& val) {
    ver = val;
  }

  void __set_startTime(const std::string& val) {
    startTime = val;
  }

  void __set_timestamp(const std::string& val) {
    timestamp = val;
  }

  bool operator == (const CPluginInfo & rhs) const
  {
    if (!(cpu == rhs.cpu))
      return false;
    if (!(mem == rhs.mem))
      return false;
    if (!(ver == rhs.ver))
      return false;
    if (!(startTime == rhs.startTime))
      return false;
    if (!(timestamp == rhs.timestamp))
      return false;
    return true;
  }
  bool operator != (const CPluginInfo &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const CPluginInfo & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _MoniterRequest__isset {
  _MoniterRequest__isset() : extend(false) {}
  bool extend;
} _MoniterRequest__isset;

class MoniterRequest {
 public:

  static const char* ascii_fingerprint; // = "B83AF6782420F04BAD45797C12B66A1B";
  static const uint8_t binary_fingerprint[16]; // = {0xB8,0x3A,0xF6,0x78,0x24,0x20,0xF0,0x4B,0xAD,0x45,0x79,0x7C,0x12,0xB6,0x6A,0x1B};

  MoniterRequest() : ip_addr("") {
  }

  virtual ~MoniterRequest() throw() {}

  std::string ip_addr;
  std::map<std::string, std::string>  agent_info;
  std::map<std::string, std::string>  extend;

  _MoniterRequest__isset __isset;

  void __set_ip_addr(const std::string& val) {
    ip_addr = val;
  }

  void __set_agent_info(const std::map<std::string, std::string> & val) {
    agent_info = val;
  }

  void __set_extend(const std::map<std::string, std::string> & val) {
    extend = val;
    __isset.extend = true;
  }

  bool operator == (const MoniterRequest & rhs) const
  {
    if (!(ip_addr == rhs.ip_addr))
      return false;
    if (!(agent_info == rhs.agent_info))
      return false;
    if (__isset.extend != rhs.__isset.extend)
      return false;
    else if (__isset.extend && !(extend == rhs.extend))
      return false;
    return true;
  }
  bool operator != (const MoniterRequest &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const MoniterRequest & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _MoniterResponse__isset {
  _MoniterResponse__isset() : extend(false) {}
  bool extend;
} _MoniterResponse__isset;

class MoniterResponse {
 public:

  static const char* ascii_fingerprint; // = "D00C13875A985BE784BFA6A3F9C1DB8D";
  static const uint8_t binary_fingerprint[16]; // = {0xD0,0x0C,0x13,0x87,0x5A,0x98,0x5B,0xE7,0x84,0xBF,0xA6,0xA3,0xF9,0xC1,0xDB,0x8D};

  MoniterResponse() : ret(0) {
  }

  virtual ~MoniterResponse() throw() {}

  int32_t ret;
  std::map<std::string, std::string>  extend;

  _MoniterResponse__isset __isset;

  void __set_ret(const int32_t val) {
    ret = val;
  }

  void __set_extend(const std::map<std::string, std::string> & val) {
    extend = val;
    __isset.extend = true;
  }

  bool operator == (const MoniterResponse & rhs) const
  {
    if (!(ret == rhs.ret))
      return false;
    if (__isset.extend != rhs.__isset.extend)
      return false;
    else if (__isset.extend && !(extend == rhs.extend))
      return false;
    return true;
  }
  bool operator != (const MoniterResponse &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const MoniterResponse & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

} // namespace

#endif
