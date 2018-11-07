/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
#ifndef cplugin_config_common_TYPES_H
#define cplugin_config_common_TYPES_H

#include <Thrift.h>
#include <TApplicationException.h>
#include <protocol/TProtocol.h>
#include <transport/TTransport.h>




namespace cplugin_sgagent{

typedef struct _ConfigFile__isset {
  _ConfigFile__isset() : filepath(false), type(false), privilege(false), version(false), md5(false), filecontent(false), err_code(false), reserved(false) {}
  bool filepath;
  bool type;
  bool privilege;
  bool version;
  bool md5;
  bool filecontent;
  bool err_code;
  bool reserved;
} _ConfigFile__isset;

class ConfigFile {
 public:

  static const char* ascii_fingerprint; // = "67D4F421707B2BE3C892547CBD22427D";
  static const uint8_t binary_fingerprint[16]; // = {0x67,0xD4,0xF4,0x21,0x70,0x7B,0x2B,0xE3,0xC8,0x92,0x54,0x7C,0xBD,0x22,0x42,0x7D};

  ConfigFile() : filename(""), filepath(""), type(""), privilege(""), version(0), md5(""), filecontent(""), err_code(0), reserved("") {
  }

  virtual ~ConfigFile() throw() {}

  std::string filename;
  std::string filepath;
  std::string type;
  std::string privilege;
  int64_t version;
  std::string md5;
  std::string filecontent;
  int32_t err_code;
  std::string reserved;

  _ConfigFile__isset __isset;

  void __set_filename(const std::string& val) {
    filename = val;
  }

  void __set_filepath(const std::string& val) {
    filepath = val;
    __isset.filepath = true;
  }

  void __set_type(const std::string& val) {
    type = val;
    __isset.type = true;
  }

  void __set_privilege(const std::string& val) {
    privilege = val;
    __isset.privilege = true;
  }

  void __set_version(const int64_t val) {
    version = val;
    __isset.version = true;
  }

  void __set_md5(const std::string& val) {
    md5 = val;
    __isset.md5 = true;
  }

  void __set_filecontent(const std::string& val) {
    filecontent = val;
    __isset.filecontent = true;
  }

  void __set_err_code(const int32_t val) {
    err_code = val;
    __isset.err_code = true;
  }

  void __set_reserved(const std::string& val) {
    reserved = val;
    __isset.reserved = true;
  }

  bool operator == (const ConfigFile & rhs) const
  {
    if (!(filename == rhs.filename))
      return false;
    if (__isset.filepath != rhs.__isset.filepath)
      return false;
    else if (__isset.filepath && !(filepath == rhs.filepath))
      return false;
    if (__isset.type != rhs.__isset.type)
      return false;
    else if (__isset.type && !(type == rhs.type))
      return false;
    if (__isset.privilege != rhs.__isset.privilege)
      return false;
    else if (__isset.privilege && !(privilege == rhs.privilege))
      return false;
    if (__isset.version != rhs.__isset.version)
      return false;
    else if (__isset.version && !(version == rhs.version))
      return false;
    if (__isset.md5 != rhs.__isset.md5)
      return false;
    else if (__isset.md5 && !(md5 == rhs.md5))
      return false;
    if (__isset.filecontent != rhs.__isset.filecontent)
      return false;
    else if (__isset.filecontent && !(filecontent == rhs.filecontent))
      return false;
    if (__isset.err_code != rhs.__isset.err_code)
      return false;
    else if (__isset.err_code && !(err_code == rhs.err_code))
      return false;
    if (__isset.reserved != rhs.__isset.reserved)
      return false;
    else if (__isset.reserved && !(reserved == rhs.reserved))
      return false;
    return true;
  }
  bool operator != (const ConfigFile &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const ConfigFile & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _file_param_t__isset {
  _file_param_t__isset() : env(false), path(false), cmd(false), err(false), configFiles(false), groupId(false), ip(false), key(false) {}
  bool env;
  bool path;
  bool cmd;
  bool err;
  bool configFiles;
  bool groupId;
  bool ip;
  bool key;
} _file_param_t__isset;

class file_param_t {
 public:

  static const char* ascii_fingerprint; // = "FE510DD3008F2BE26D9ECB0EECC3069C";
  static const uint8_t binary_fingerprint[16]; // = {0xFE,0x51,0x0D,0xD3,0x00,0x8F,0x2B,0xE2,0x6D,0x9E,0xCB,0x0E,0xEC,0xC3,0x06,0x9C};

  file_param_t() : appkey(""), env(""), path(""), cmd(0), err(0), groupId(""), ip(""), key("") {
  }

  virtual ~file_param_t() throw() {}

  std::string appkey;
  std::string env;
  std::string path;
  int32_t cmd;
  int32_t err;
  std::vector<ConfigFile>  configFiles;
  std::string groupId;
  std::string ip;
  std::string key;

  _file_param_t__isset __isset;

  void __set_appkey(const std::string& val) {
    appkey = val;
  }

  void __set_env(const std::string& val) {
    env = val;
    __isset.env = true;
  }

  void __set_path(const std::string& val) {
    path = val;
    __isset.path = true;
  }

  void __set_cmd(const int32_t val) {
    cmd = val;
    __isset.cmd = true;
  }

  void __set_err(const int32_t val) {
    err = val;
    __isset.err = true;
  }

  void __set_configFiles(const std::vector<ConfigFile> & val) {
    configFiles = val;
    __isset.configFiles = true;
  }

  void __set_groupId(const std::string& val) {
    groupId = val;
  }

  void __set_ip(const std::string& val) {
    ip = val;
  }

  void __set_key(const std::string& val) {
    key = val;
  }

  bool operator == (const file_param_t & rhs) const
  {
    if (!(appkey == rhs.appkey))
      return false;
    if (__isset.env != rhs.__isset.env)
      return false;
    else if (__isset.env && !(env == rhs.env))
      return false;
    if (__isset.path != rhs.__isset.path)
      return false;
    else if (__isset.path && !(path == rhs.path))
      return false;
    if (__isset.cmd != rhs.__isset.cmd)
      return false;
    else if (__isset.cmd && !(cmd == rhs.cmd))
      return false;
    if (__isset.err != rhs.__isset.err)
      return false;
    else if (__isset.err && !(err == rhs.err))
      return false;
    if (__isset.configFiles != rhs.__isset.configFiles)
      return false;
    else if (__isset.configFiles && !(configFiles == rhs.configFiles))
      return false;
    if (!(groupId == rhs.groupId))
      return false;
    if (!(ip == rhs.ip))
      return false;
    if (!(key == rhs.key))
      return false;
    return true;
  }
  bool operator != (const file_param_t &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const file_param_t & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _ConfigGroup__isset {
  _ConfigGroup__isset() : name(false), createTime(false), updateTime(false), ips(false), state(false), version(false) {}
  bool name;
  bool createTime;
  bool updateTime;
  bool ips;
  bool state;
  bool version;
} _ConfigGroup__isset;

class ConfigGroup {
 public:

  static const char* ascii_fingerprint; // = "7FFE12CF23CCD4614098E0C4B6EE2C4D";
  static const uint8_t binary_fingerprint[16]; // = {0x7F,0xFE,0x12,0xCF,0x23,0xCC,0xD4,0x61,0x40,0x98,0xE0,0xC4,0xB6,0xEE,0x2C,0x4D};

  ConfigGroup() : appkey(""), env(""), id("0"), name(""), createTime(0), updateTime(0), state(0), version("") {
  }

  virtual ~ConfigGroup() throw() {}

  std::string appkey;
  std::string env;
  std::string id;
  std::string name;
  int64_t createTime;
  int64_t updateTime;
  std::vector<std::string>  ips;
  int32_t state;
  std::string version;

  _ConfigGroup__isset __isset;

  void __set_appkey(const std::string& val) {
    appkey = val;
  }

  void __set_env(const std::string& val) {
    env = val;
  }

  void __set_id(const std::string& val) {
    id = val;
  }

  void __set_name(const std::string& val) {
    name = val;
  }

  void __set_createTime(const int64_t val) {
    createTime = val;
  }

  void __set_updateTime(const int64_t val) {
    updateTime = val;
  }

  void __set_ips(const std::vector<std::string> & val) {
    ips = val;
  }

  void __set_state(const int32_t val) {
    state = val;
  }

  void __set_version(const std::string& val) {
    version = val;
    __isset.version = true;
  }

  bool operator == (const ConfigGroup & rhs) const
  {
    if (!(appkey == rhs.appkey))
      return false;
    if (!(env == rhs.env))
      return false;
    if (!(id == rhs.id))
      return false;
    if (!(name == rhs.name))
      return false;
    if (!(createTime == rhs.createTime))
      return false;
    if (!(updateTime == rhs.updateTime))
      return false;
    if (!(ips == rhs.ips))
      return false;
    if (!(state == rhs.state))
      return false;
    if (__isset.version != rhs.__isset.version)
      return false;
    else if (__isset.version && !(version == rhs.version))
      return false;
    return true;
  }
  bool operator != (const ConfigGroup &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const ConfigGroup & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};


class ConfigGroups {
 public:

  static const char* ascii_fingerprint; // = "A092A0479C4C782B7DBB8FA4D4290AA4";
  static const uint8_t binary_fingerprint[16]; // = {0xA0,0x92,0xA0,0x47,0x9C,0x4C,0x78,0x2B,0x7D,0xBB,0x8F,0xA4,0xD4,0x29,0x0A,0xA4};

  ConfigGroups() {
  }

  virtual ~ConfigGroups() throw() {}

  std::vector<ConfigGroup>  groups;

  void __set_groups(const std::vector<ConfigGroup> & val) {
    groups = val;
  }

  bool operator == (const ConfigGroups & rhs) const
  {
    if (!(groups == rhs.groups))
      return false;
    return true;
  }
  bool operator != (const ConfigGroups &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const ConfigGroups & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _ConfigNode__isset {
  _ConfigNode__isset() : file(false), swimlane(false) {}
  bool file;
  bool swimlane;
} _ConfigNode__isset;

class ConfigNode {
 public:

  static const char* ascii_fingerprint; // = "27F6047A6E1B8980C54588041081968C";
  static const uint8_t binary_fingerprint[16]; // = {0x27,0xF6,0x04,0x7A,0x6E,0x1B,0x89,0x80,0xC5,0x45,0x88,0x04,0x10,0x81,0x96,0x8C};

  ConfigNode() : appkey(""), env(""), path(""), swimlane("") {
  }

  virtual ~ConfigNode() throw() {}

  std::string appkey;
  std::string env;
  std::string path;
  ConfigFile file;
  std::string swimlane;

  _ConfigNode__isset __isset;

  void __set_appkey(const std::string& val) {
    appkey = val;
  }

  void __set_env(const std::string& val) {
    env = val;
  }

  void __set_path(const std::string& val) {
    path = val;
  }

  void __set_file(const ConfigFile& val) {
    file = val;
    __isset.file = true;
  }

  void __set_swimlane(const std::string& val) {
    swimlane = val;
    __isset.swimlane = true;
  }

  bool operator == (const ConfigNode & rhs) const
  {
    if (!(appkey == rhs.appkey))
      return false;
    if (!(env == rhs.env))
      return false;
    if (!(path == rhs.path))
      return false;
    if (__isset.file != rhs.__isset.file)
      return false;
    else if (__isset.file && !(file == rhs.file))
      return false;
    if (__isset.swimlane != rhs.__isset.swimlane)
      return false;
    else if (__isset.swimlane && !(swimlane == rhs.swimlane))
      return false;
    return true;
  }
  bool operator != (const ConfigNode &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const ConfigNode & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};


class ConfigData {
 public:

  static const char* ascii_fingerprint; // = "72E6780C220EBBBE64C5552D15905A7B";
  static const uint8_t binary_fingerprint[16]; // = {0x72,0xE6,0x78,0x0C,0x22,0x0E,0xBB,0xBE,0x64,0xC5,0x55,0x2D,0x15,0x90,0x5A,0x7B};

  ConfigData() : appkey(""), env(""), path(""), version(0), updateTime(0), data(""), dataType("") {
  }

  virtual ~ConfigData() throw() {}

  std::string appkey;
  std::string env;
  std::string path;
  int64_t version;
  int64_t updateTime;
  std::string data;
  std::string dataType;

  void __set_appkey(const std::string& val) {
    appkey = val;
  }

  void __set_env(const std::string& val) {
    env = val;
  }

  void __set_path(const std::string& val) {
    path = val;
  }

  void __set_version(const int64_t val) {
    version = val;
  }

  void __set_updateTime(const int64_t val) {
    updateTime = val;
  }

  void __set_data(const std::string& val) {
    data = val;
  }

  void __set_dataType(const std::string& val) {
    dataType = val;
  }

  bool operator == (const ConfigData & rhs) const
  {
    if (!(appkey == rhs.appkey))
      return false;
    if (!(env == rhs.env))
      return false;
    if (!(path == rhs.path))
      return false;
    if (!(version == rhs.version))
      return false;
    if (!(updateTime == rhs.updateTime))
      return false;
    if (!(data == rhs.data))
      return false;
    if (!(dataType == rhs.dataType))
      return false;
    return true;
  }
  bool operator != (const ConfigData &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const ConfigData & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _ConfigDataResponse__isset {
  _ConfigDataResponse__isset() : configData(false) {}
  bool configData;
} _ConfigDataResponse__isset;

class ConfigDataResponse {
 public:

  static const char* ascii_fingerprint; // = "9E686755390B76641D2F7ADE4402B3BA";
  static const uint8_t binary_fingerprint[16]; // = {0x9E,0x68,0x67,0x55,0x39,0x0B,0x76,0x64,0x1D,0x2F,0x7A,0xDE,0x44,0x02,0xB3,0xBA};

  ConfigDataResponse() : code(200) {
  }

  virtual ~ConfigDataResponse() throw() {}

  int32_t code;
  ConfigData configData;

  _ConfigDataResponse__isset __isset;

  void __set_code(const int32_t val) {
    code = val;
  }

  void __set_configData(const ConfigData& val) {
    configData = val;
    __isset.configData = true;
  }

  bool operator == (const ConfigDataResponse & rhs) const
  {
    if (!(code == rhs.code))
      return false;
    if (__isset.configData != rhs.__isset.configData)
      return false;
    else if (__isset.configData && !(configData == rhs.configData))
      return false;
    return true;
  }
  bool operator != (const ConfigDataResponse &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const ConfigDataResponse & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _DefaultConfigResponse__isset {
  _DefaultConfigResponse__isset() : defaultConfigs(false) {}
  bool defaultConfigs;
} _DefaultConfigResponse__isset;

class DefaultConfigResponse {
 public:

  static const char* ascii_fingerprint; // = "D00C13875A985BE784BFA6A3F9C1DB8D";
  static const uint8_t binary_fingerprint[16]; // = {0xD0,0x0C,0x13,0x87,0x5A,0x98,0x5B,0xE7,0x84,0xBF,0xA6,0xA3,0xF9,0xC1,0xDB,0x8D};

  DefaultConfigResponse() : code(200) {
  }

  virtual ~DefaultConfigResponse() throw() {}

  int32_t code;
  std::map<std::string, std::string>  defaultConfigs;

  _DefaultConfigResponse__isset __isset;

  void __set_code(const int32_t val) {
    code = val;
  }

  void __set_defaultConfigs(const std::map<std::string, std::string> & val) {
    defaultConfigs = val;
    __isset.defaultConfigs = true;
  }

  bool operator == (const DefaultConfigResponse & rhs) const
  {
    if (!(code == rhs.code))
      return false;
    if (__isset.defaultConfigs != rhs.__isset.defaultConfigs)
      return false;
    else if (__isset.defaultConfigs && !(defaultConfigs == rhs.defaultConfigs))
      return false;
    return true;
  }
  bool operator != (const DefaultConfigResponse &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const DefaultConfigResponse & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _GetMergeDataRequest__isset {
  _GetMergeDataRequest__isset() : swimlane(false) {}
  bool swimlane;
} _GetMergeDataRequest__isset;

class GetMergeDataRequest {
 public:

  static const char* ascii_fingerprint; // = "5AFFBE1C256658C11BC4F27DEAB8AC33";
  static const uint8_t binary_fingerprint[16]; // = {0x5A,0xFF,0xBE,0x1C,0x25,0x66,0x58,0xC1,0x1B,0xC4,0xF2,0x7D,0xEA,0xB8,0xAC,0x33};

  GetMergeDataRequest() : appkey(""), env(""), path(""), version(0), requestIp(""), swimlane("") {
  }

  virtual ~GetMergeDataRequest() throw() {}

  std::string appkey;
  std::string env;
  std::string path;
  int64_t version;
  std::string requestIp;
  std::string swimlane;

  _GetMergeDataRequest__isset __isset;

  void __set_appkey(const std::string& val) {
    appkey = val;
  }

  void __set_env(const std::string& val) {
    env = val;
  }

  void __set_path(const std::string& val) {
    path = val;
  }

  void __set_version(const int64_t val) {
    version = val;
  }

  void __set_requestIp(const std::string& val) {
    requestIp = val;
  }

  void __set_swimlane(const std::string& val) {
    swimlane = val;
    __isset.swimlane = true;
  }

  bool operator == (const GetMergeDataRequest & rhs) const
  {
    if (!(appkey == rhs.appkey))
      return false;
    if (!(env == rhs.env))
      return false;
    if (!(path == rhs.path))
      return false;
    if (!(version == rhs.version))
      return false;
    if (!(requestIp == rhs.requestIp))
      return false;
    if (__isset.swimlane != rhs.__isset.swimlane)
      return false;
    else if (__isset.swimlane && !(swimlane == rhs.swimlane))
      return false;
    return true;
  }
  bool operator != (const GetMergeDataRequest &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const GetMergeDataRequest & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _ConfigFileRequest__isset {
  _ConfigFileRequest__isset() : files(false) {}
  bool files;
} _ConfigFileRequest__isset;

class ConfigFileRequest {
 public:

  static const char* ascii_fingerprint; // = "C2E31E04BE83C4BE9E3DAED615DB1010";
  static const uint8_t binary_fingerprint[16]; // = {0xC2,0xE3,0x1E,0x04,0xBE,0x83,0xC4,0xBE,0x9E,0x3D,0xAE,0xD6,0x15,0xDB,0x10,0x10};

  ConfigFileRequest() {
  }

  virtual ~ConfigFileRequest() throw() {}

  std::vector<std::string>  hosts;
  file_param_t files;

  _ConfigFileRequest__isset __isset;

  void __set_hosts(const std::vector<std::string> & val) {
    hosts = val;
  }

  void __set_files(const file_param_t& val) {
    files = val;
    __isset.files = true;
  }

  bool operator == (const ConfigFileRequest & rhs) const
  {
    if (!(hosts == rhs.hosts))
      return false;
    if (__isset.files != rhs.__isset.files)
      return false;
    else if (__isset.files && !(files == rhs.files))
      return false;
    return true;
  }
  bool operator != (const ConfigFileRequest &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const ConfigFileRequest & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _ConfigFileResponse__isset {
  _ConfigFileResponse__isset() : hosts(false) {}
  bool hosts;
} _ConfigFileResponse__isset;

class ConfigFileResponse {
 public:

  static const char* ascii_fingerprint; // = "93CC3D1E71866966C22792AABF4C3815";
  static const uint8_t binary_fingerprint[16]; // = {0x93,0xCC,0x3D,0x1E,0x71,0x86,0x69,0x66,0xC2,0x27,0x92,0xAA,0xBF,0x4C,0x38,0x15};

  ConfigFileResponse() : code(200) {
  }

  virtual ~ConfigFileResponse() throw() {}

  int32_t code;
  std::vector<std::string>  hosts;

  _ConfigFileResponse__isset __isset;

  void __set_code(const int32_t val) {
    code = val;
  }

  void __set_hosts(const std::vector<std::string> & val) {
    hosts = val;
    __isset.hosts = true;
  }

  bool operator == (const ConfigFileResponse & rhs) const
  {
    if (!(code == rhs.code))
      return false;
    if (__isset.hosts != rhs.__isset.hosts)
      return false;
    else if (__isset.hosts && !(hosts == rhs.hosts))
      return false;
    return true;
  }
  bool operator != (const ConfigFileResponse &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const ConfigFileResponse & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _ConfigGroupResponse__isset {
  _ConfigGroupResponse__isset() : group(false), errMsg(false) {}
  bool group;
  bool errMsg;
} _ConfigGroupResponse__isset;

class ConfigGroupResponse {
 public:

  static const char* ascii_fingerprint; // = "3797C99549EA70DBAA219AB342B62548";
  static const uint8_t binary_fingerprint[16]; // = {0x37,0x97,0xC9,0x95,0x49,0xEA,0x70,0xDB,0xAA,0x21,0x9A,0xB3,0x42,0xB6,0x25,0x48};

  ConfigGroupResponse() : code(200), errMsg("") {
  }

  virtual ~ConfigGroupResponse() throw() {}

  int32_t code;
  ConfigGroup group;
  std::string errMsg;

  _ConfigGroupResponse__isset __isset;

  void __set_code(const int32_t val) {
    code = val;
  }

  void __set_group(const ConfigGroup& val) {
    group = val;
  }

  void __set_errMsg(const std::string& val) {
    errMsg = val;
  }

  bool operator == (const ConfigGroupResponse & rhs) const
  {
    if (!(code == rhs.code))
      return false;
    if (!(group == rhs.group))
      return false;
    if (!(errMsg == rhs.errMsg))
      return false;
    return true;
  }
  bool operator != (const ConfigGroupResponse &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const ConfigGroupResponse & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _ConfigGroupsResponse__isset {
  _ConfigGroupsResponse__isset() : groups(false), errMsg(false) {}
  bool groups;
  bool errMsg;
} _ConfigGroupsResponse__isset;

class ConfigGroupsResponse {
 public:

  static const char* ascii_fingerprint; // = "4E2C144D622BC7051A2CEE5ECB7D189D";
  static const uint8_t binary_fingerprint[16]; // = {0x4E,0x2C,0x14,0x4D,0x62,0x2B,0xC7,0x05,0x1A,0x2C,0xEE,0x5E,0xCB,0x7D,0x18,0x9D};

  ConfigGroupsResponse() : code(200), errMsg("") {
  }

  virtual ~ConfigGroupsResponse() throw() {}

  int32_t code;
  ConfigGroups groups;
  std::string errMsg;

  _ConfigGroupsResponse__isset __isset;

  void __set_code(const int32_t val) {
    code = val;
  }

  void __set_groups(const ConfigGroups& val) {
    groups = val;
  }

  void __set_errMsg(const std::string& val) {
    errMsg = val;
  }

  bool operator == (const ConfigGroupsResponse & rhs) const
  {
    if (!(code == rhs.code))
      return false;
    if (!(groups == rhs.groups))
      return false;
    if (!(errMsg == rhs.errMsg))
      return false;
    return true;
  }
  bool operator != (const ConfigGroupsResponse &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const ConfigGroupsResponse & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};


}

#endif
