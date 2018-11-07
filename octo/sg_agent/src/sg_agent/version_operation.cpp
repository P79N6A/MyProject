#include <string>
#include "version_operation.h"
#include "comm/log4cplus.h"
#include <sstream>

namespace sg_agent {
VersionOperation::VersionOperation() {
  // the old mthrift version is 1.7.0
  m_old_mtthrift_version.push_back(1);
  m_old_mtthrift_version.push_back(7);
  m_old_mtthrift_version.push_back(0);

  // the old pigeon version is 2.8.0
  m_old_pigeon_version.push_back(2);
  m_old_pigeon_version.push_back(8);
  m_old_pigeon_version.push_back(0);

  // the old cthrift version 2.6.0
  m_old_cthrift_version.push_back(2);
  m_old_cthrift_version.push_back(6);
  m_old_cthrift_version.push_back(0);
}

VersionOperation::~VersionOperation() {
}
/*
 * 注册完自己后，进行版本检查
 * */
bool VersionOperation::IsOldVersion(const std::string &version) {
  if (version.empty()) {
    return true;
  }
  std::string mtthrift_str("mtthrift");
  std::string cthrift_str("cthrift");
  std::string pigeon_str("pigeon");
  if (std::string::npos != version.find(mtthrift_str)) {
    LOG_INFO("version is mtthfit: " << version);
    return CompareMTVersion(version);
  } else if (('0' <= version[0] && '9' >= version[0]) || (std::string::npos != version.find(pigeon_str))) {
    LOG_INFO("version is pigeon: " << version);
    return ComparePigeonVersion(version);
  } else if (std::string::npos != version.find(cthrift_str)) {
    LOG_INFO("version is cthrift: " << version);
    return CompareCthriftVersion(version);
  } else {
    LOG_INFO("not need to check the version : " << version);
    return false;
  }
}

void VersionOperation::GetVersion(const std::string &strVersion, std::vector<int> *p_vec_version) {
  std::string strLast;
  std::stringstream ss;
  int i_val;
  for (int i = 0; i < strVersion.size(); i++) {
    if ('.' == strVersion[i]) {
      //字符串转换成整型
      ss << strLast;
      ss >> i_val;
      ss.str("");
      if (ss.eof()) {
        ss.clear();
      }
      p_vec_version->push_back(i_val);
      strLast.clear();
    } else if ('0' <= strVersion[i] && '9' >= strVersion[i]) {
      strLast += strVersion[i];
    }
  }

  ss << strLast;
  ss >> i_val;
  ss.str("");
  p_vec_version->push_back(i_val);
}

int VersionOperation::CompareVector(const std::vector<int> &a, const std::vector<int> &b) {
  int i_min_size = a.size() > b.size() ? b.size() : a.size();
  for (int i = 0; i < i_min_size; i++) {
    if (a[i] > b[i]) {
      return 1;
    } else if (a[i] < b[i]) {
      return -1;
    }
  }
  return 0;
}

bool VersionOperation::CompareCthriftVersion(const std::string &version) {
  std::vector<int> c;
  GetVersion(version, &c);
  int ret = CompareVector(c, m_old_cthrift_version);
  return (0 > ret);
}
bool VersionOperation::CompareMTVersion(const std::string &version) {
  std::vector<int> mt;
  GetVersion(version, &mt);
  int ret = CompareVector(mt, m_old_mtthrift_version);
  return (0 > ret);
}

bool VersionOperation::ComparePigeonVersion(const std::string &version) {
  std::vector<int> p;
  GetVersion(version, &p);
  int ret = CompareVector(p, m_old_pigeon_version);
  return (0 > ret);
}
}
