#include "idc.h"
#include "comm/inc_comm.h"

namespace sg_agent {

void IDC::Init() {
  if (!is_init_) {
    muduo::MutexLockGuard lock(init_lock_);
    if (!is_init_) {
      int_mask_ = ConvertMaskToInt(mask_);
      ip_mask_value_ = int_mask_ & GetIp4Value(ip_);
      is_init_ = true;
    }
  }
}

bool IDC::IsSameIdc(const std::string &ip) {
  Init();
  return ip_mask_value_ == (int_mask_ & GetIp4Value(ip));
}

int IDC::ConvertMaskToInt(const std::string &mask) {
  std::vector<std::string> vnum;
  SplitStringIntoVector(mask.c_str(), ".", vnum);
  if (4 != vnum.size()) {
    return -1;
  }

  int iMask = 0;
  for (int i = 0; i < 4; ++i) {
    iMask += (atoi(vnum[i].c_str()) << ((3 - i) * 8));
  }
  return iMask;
}

int IDC::GetIp4Value(const std::string &ip) {
  std::vector<std::string> vcIp;
  SplitStringIntoVector(ip.c_str(), ".", vcIp);
  if (4 != vcIp.size()) {
    return -1;
  }
  int address = 0;
  int filter_num = 0xFF;
  for (int i = 0; i < 4; i++) {
    int pos = i * 8;
    int vIp = atoi(vcIp[3 - i].c_str());
    address |= ((vIp << pos) & (filter_num << pos));
  }
  return address;
}

void IDC::set_region(const std::string &val) {
  region_ = val;
}
std::string IDC::get_region() const {
  return region_;
}

void IDC::set_idc(const std::string &val) {
  idc_ = val;
}
std::string IDC::get_idc() const {
  return idc_;
}

void IDC::set_center(const std::string &val) {
  center_ = val;
}
std::string IDC::get_center() const {
  return center_;
}

void IDC::set_ip(const std::string &val) {
  ip_ = val;
}
std::string IDC::get_ip() const {
  return ip_;
}

void IDC::set_mask(const std::string &val) {
  mask_ = val;
}
std::string IDC::get_mask() const {
  return mask_;
}

}
