#ifndef UITL_IDC_H_
#define UITL_IDC_H_
#include <string>
#include <muduo/base/Mutex.h>

namespace sg_agent {

class IDC {
 public:
  IDC() : int_mask_(0),ip_mask_value_(0),is_init_(false) {}

  void set_region(const std::string &val);
  std::string get_region() const;

  void set_idc(const std::string &val);
  std::string get_idc() const;

  void set_center(const std::string &val);
  std::string get_center() const;

  void set_ip(const std::string &val);
  std::string get_ip() const;

  void set_mask(const std::string &val);
  std::string get_mask() const;

  bool IsSameIdc(const std::string &ip);

 private:
  std::string region_;
  std::string idc_;
  std::string center_;
  std::string ip_;
  std::string mask_;

  int int_mask_;
  int ip_mask_value_;
  bool is_init_;
  muduo::MutexLock init_lock_;

  void Init();
  int ConvertMaskToInt(const std::string &mask);
  int GetIp4Value(const std::string &ip);
};

}
#endif
