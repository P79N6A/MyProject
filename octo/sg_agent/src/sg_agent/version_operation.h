#ifndef __mt_thrift_version__H__
#define __mt_thrift_version__H__

#include <vector>

namespace sg_agent {

class VersionOperation {
 public:
  VersionOperation();
  ~VersionOperation();
  bool IsOldVersion(const std::string &version);
  //mthrift < 1.7.0
  bool CompareMTVersion(const std::string &version);
  //piegon < 2.8
  bool ComparePigeonVersion(const std::string &version);
  //cthrift < 2.6.0
  bool CompareCthriftVersion(const std::string &version);
  //字符串中的版本号信息提取到vector
  void GetVersion(const std::string &version_str, std::vector<int> *p_vec_version);
  //暂不支持两个长度不等的判断
  int CompareVector(const std::vector<int> &a, const std::vector<int> &b);
 private:
  std::vector<int> m_old_mtthrift_version;
  std::vector<int> m_old_pigeon_version;
  std::vector<int> m_old_cthrift_version;
};

}

#endif
