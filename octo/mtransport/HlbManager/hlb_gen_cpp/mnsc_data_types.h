/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
#ifndef mnsc_data_TYPES_H
#define mnsc_data_TYPES_H

#include <Thrift.h>
#include <TApplicationException.h>
#include <protocol/TProtocol.h>
#include <transport/TTransport.h>

#include "sgagent_common_types.h"




typedef class  ::SGService SGService;

typedef std::map<std::string, std::string>  HttpProperties;

typedef struct _MNSResponse__isset {
  _MNSResponse__isset() : defaultMNSCache(false), version(false) {}
  bool defaultMNSCache;
  bool version;
} _MNSResponse__isset;

class MNSResponse {
 public:

  static const char* ascii_fingerprint; // = "607E13C093F57A1B6FDE8C4DB92A8534";
  static const uint8_t binary_fingerprint[16]; // = {0x60,0x7E,0x13,0xC0,0x93,0xF5,0x7A,0x1B,0x6F,0xDE,0x8C,0x4D,0xB9,0x2A,0x85,0x34};

  MNSResponse() : code(200), version("") {
  }

  virtual ~MNSResponse() throw() {}

  int32_t code;
  std::vector<SGService>  defaultMNSCache;
  std::string version;

  _MNSResponse__isset __isset;

  void __set_code(const int32_t val) {
    code = val;
  }

  void __set_defaultMNSCache(const std::vector<SGService> & val) {
    defaultMNSCache = val;
    __isset.defaultMNSCache = true;
  }

  void __set_version(const std::string& val) {
    version = val;
    __isset.version = true;
  }

  bool operator == (const MNSResponse & rhs) const
  {
    if (!(code == rhs.code))
      return false;
    if (__isset.defaultMNSCache != rhs.__isset.defaultMNSCache)
      return false;
    else if (__isset.defaultMNSCache && !(defaultMNSCache == rhs.defaultMNSCache))
      return false;
    if (__isset.version != rhs.__isset.version)
      return false;
    else if (__isset.version && !(version == rhs.version))
      return false;
    return true;
  }
  bool operator != (const MNSResponse &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const MNSResponse & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _AppKeyListResponse__isset {
  _AppKeyListResponse__isset() : appKeyList(false) {}
  bool appKeyList;
} _AppKeyListResponse__isset;

class AppKeyListResponse {
 public:

  static const char* ascii_fingerprint; // = "93CC3D1E71866966C22792AABF4C3815";
  static const uint8_t binary_fingerprint[16]; // = {0x93,0xCC,0x3D,0x1E,0x71,0x86,0x69,0x66,0xC2,0x27,0x92,0xAA,0xBF,0x4C,0x38,0x15};

  AppKeyListResponse() : code(200) {
  }

  virtual ~AppKeyListResponse() throw() {}

  int32_t code;
  std::vector<std::string>  appKeyList;

  _AppKeyListResponse__isset __isset;

  void __set_code(const int32_t val) {
    code = val;
  }

  void __set_appKeyList(const std::vector<std::string> & val) {
    appKeyList = val;
    __isset.appKeyList = true;
  }

  bool operator == (const AppKeyListResponse & rhs) const
  {
    if (!(code == rhs.code))
      return false;
    if (__isset.appKeyList != rhs.__isset.appKeyList)
      return false;
    else if (__isset.appKeyList && !(appKeyList == rhs.appKeyList))
      return false;
    return true;
  }
  bool operator != (const AppKeyListResponse &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const AppKeyListResponse & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _HttpPropertiesResponse__isset {
  _HttpPropertiesResponse__isset() : propertiesMap(false) {}
  bool propertiesMap;
} _HttpPropertiesResponse__isset;

class HttpPropertiesResponse {
 public:

  static const char* ascii_fingerprint; // = "ED513D89046EBA5323735F654E017436";
  static const uint8_t binary_fingerprint[16]; // = {0xED,0x51,0x3D,0x89,0x04,0x6E,0xBA,0x53,0x23,0x73,0x5F,0x65,0x4E,0x01,0x74,0x36};

  HttpPropertiesResponse() : code(200) {
  }

  virtual ~HttpPropertiesResponse() throw() {}

  int32_t code;
  std::map<std::string, HttpProperties>  propertiesMap;

  _HttpPropertiesResponse__isset __isset;

  void __set_code(const int32_t val) {
    code = val;
  }

  void __set_propertiesMap(const std::map<std::string, HttpProperties> & val) {
    propertiesMap = val;
    __isset.propertiesMap = true;
  }

  bool operator == (const HttpPropertiesResponse & rhs) const
  {
    if (!(code == rhs.code))
      return false;
    if (__isset.propertiesMap != rhs.__isset.propertiesMap)
      return false;
    else if (__isset.propertiesMap && !(propertiesMap == rhs.propertiesMap))
      return false;
    return true;
  }
  bool operator != (const HttpPropertiesResponse &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const HttpPropertiesResponse & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _UpstreamResponse__isset {
  _UpstreamResponse__isset() : upstreams(false), ext(false) {}
  bool upstreams;
  bool ext;
} _UpstreamResponse__isset;

class UpstreamResponse {
 public:

  static const char* ascii_fingerprint; // = "20A57526680CA4CBC28C94966623FAC2";
  static const uint8_t binary_fingerprint[16]; // = {0x20,0xA5,0x75,0x26,0x68,0x0C,0xA4,0xCB,0xC2,0x8C,0x94,0x96,0x66,0x23,0xFA,0xC2};

  UpstreamResponse() : code(200), ext("") {
  }

  virtual ~UpstreamResponse() throw() {}

  int32_t code;
  std::map<std::string, std::string>  upstreams;
  std::string ext;

  _UpstreamResponse__isset __isset;

  void __set_code(const int32_t val) {
    code = val;
  }

  void __set_upstreams(const std::map<std::string, std::string> & val) {
    upstreams = val;
  }

  void __set_ext(const std::string& val) {
    ext = val;
  }

  bool operator == (const UpstreamResponse & rhs) const
  {
    if (!(code == rhs.code))
      return false;
    if (!(upstreams == rhs.upstreams))
      return false;
    if (!(ext == rhs.ext))
      return false;
    return true;
  }
  bool operator != (const UpstreamResponse &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const UpstreamResponse & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};



#endif