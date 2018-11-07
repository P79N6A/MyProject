//
// Created by Chao Shu on 16/4/23.
//

#include <boost/algorithm/string.hpp>
#include <boost/property_tree/xml_parser.hpp>
#include <boost/unordered/unordered_map.hpp>
#include <boost/lexical_cast.hpp>
#include <boost/random.hpp>

#include <cthrift/cthrift_sgagent.h>
#include <cthrift/mns_sdk/mns_sdk.h>

#include <clog/log.h>

using namespace std;
using namespace cthrift;
using namespace mns_sdk;

void Job(const vector<SGService> &vec_add,
         const vector<SGService> &vec_del,
         const vector<SGService> &vec_chg,
         const string& appkey) {
  cout << "recv add.size " << vec_add.size() << endl;
  for (int i = 0; i < static_cast<int>(vec_add.size()); i++) {
    cout << "[" << i << "]" << ": "
         << CthriftSgagent::SGService2String(vec_add[i]) << endl;
  }

  cout << "recv del.size " << vec_del.size() << endl;
  for (size_t i = 0; i < vec_del.size(); i++) {
    cout << "[" << i << "]" << ": "
         << CthriftSgagent::SGService2String(vec_del[i]) << endl;
  }

  cout << "recv chg.size " << vec_chg.size() << endl;
  for (size_t i = 0; i < vec_chg.size(); i++) {
    cout << "[" << i << "]" << ": "
         << CthriftSgagent::SGService2String(vec_chg[i]) << endl;
  }

  cout << "appkey " << appkey << endl;
}

void JobList(const vector<SGService> &vec_add,
             const vector<SGService> &vec_del,
             const vector<SGService> &vec_chg,
             const string& appkey) {
  cout << "AddUpdateSvrListCallback appkey " << appkey << endl;
}

int8_t FakeIPByIDCInfo(const IdcInfo &idc_info, string *p_fake_str) {
  //every item create one random ip
  vector<string> vec_str;
  boost::split(vec_str, idc_info.str_ip_mask, boost::is_any_of("."));
  if (4 != vec_str.size()) {
    cerr << idc_info.str_ip_mask << " NOT correct mask" << endl;
    return -1;
  }

  vector<string>::iterator it_vec2 = vec_str.begin();
  int8_t i8_mask_num = 0;

  while (it_vec2 != vec_str.end()) {
    if (0 == boost::lexical_cast<int>(*it_vec2)) {
      break;
    }

    ++i8_mask_num;
    ++it_vec2;
  }

  if (4 <= i8_mask_num) {
    cerr << "No 0 in ip mask " << idc_info.str_ip_mask
         << " NOT support this kind of mask"
         << endl;
    return -1;
  }

  vec_str.clear();
  boost::split(vec_str, idc_info.str_ip_prefix, boost::is_any_of("."));
  if (4 != vec_str.size()) {
    cerr << idc_info.str_ip_prefix << " NOT correct ip" << endl;
    return -1;
  }

  int i = 0;
  for (; i < i8_mask_num; i++) {
    *p_fake_str += vec_str[i] + ".";  //Not 0 ip copy
  }

  boost::uniform_int<> uni_dist(0, 255);
  boost::variate_generator<boost::minstd_rand, boost::uniform_int<> > uni
      (boost::minstd_rand(static_cast<const uint32_t>(time(0))),
       uni_dist);
  while (3 >= i) {

    try{
      *p_fake_str += boost::lexical_cast<string>(uni()); //random num [0,
      // 255]
    } catch(boost::bad_lexical_cast & e){
      cerr << "uni() :" << e.what();
      continue;
    }

    if (i < 3) {
      *p_fake_str += ".";   //last one No need .
    }

    ++i;
  }

  return 0;
}

//TODO ONLY support 255 + 0 mask
void TestIDCRegion(void) {
  boost::unordered_map<string, boost::unordered_map<string, vector<IdcInfo> >
  >::iterator it = CthriftSgagent::map_region_map_idc_info_[0].begin();

  string str_hit_region;
  string str_hit_idc;

  while (it != CthriftSgagent::map_region_map_idc_info_[0].end()) {
    cout << "Region: " << it->first << endl;

    boost::unordered_map<string, vector<IdcInfo> >::iterator it_idc_vec_info =
        (it->second).begin();
    while (it_idc_vec_info != (it->second).end()) {
      cout << " Idc: " << it_idc_vec_info->first << endl;

      vector<IdcInfo>::iterator it_vec = (it_idc_vec_info->second).begin();
      while (it_vec != (it_idc_vec_info->second).end()) {
        string str_fake_ip;
        if (FakeIPByIDCInfo(*it_vec, &str_fake_ip)) {
          return;
        }

        cout << "fake region " << it->first << " idc " <<
             it_idc_vec_info->first << " center " << it_vec->str_center
             << " ip " << str_fake_ip << endl;

        bool b_is_same_region = false;
        bool b_is_same_idc = false;
        if (CheckIfSameRegionIDCWithLocalIP(str_fake_ip,
                                            &b_is_same_region,
                                            &b_is_same_idc)) {
          cerr << "CheckIfSameRegionIDCWithLocalIP faield, fake_ip "
               << str_fake_ip;
          return;
        }

        if (b_is_same_region) {
          if (it->first != CthriftSgagent::str_local_ip_region_) {
            cerr << "str_local_ip_region_ "
                 << CthriftSgagent::str_local_ip_region_ << " compared region "
                 << it->first << endl;
            return;
          }

          if (!(str_hit_region.empty()) && str_hit_region != it->first) {
            cerr << "already in region " << str_hit_region << endl;
            return;
          }

          if (str_hit_region.empty()) {
            cout << "Your local ip " << CthriftSgagent::str_local_ip_
                 << " is in"
                     " region " << it->first << endl;

            str_hit_region.assign(it->first);
          }
        }

        if (b_is_same_idc) {
          if (it_idc_vec_info->first != CthriftSgagent::str_local_ip_idc_) {
            cerr << "str_local_ip_idc_ " << CthriftSgagent::str_local_ip_idc_
                 << " compared region " << it_idc_vec_info->first << endl;
            return;
          }

          if (!(str_hit_idc.empty()) && str_hit_idc != it_idc_vec_info->first) {
            cerr << "already in idc " << str_hit_idc << endl;
            return;
          }

          if (str_hit_idc.empty()) {
            cout << "Your local ip " << CthriftSgagent::str_local_ip_
                 << " is in"
                     " idc " << it_idc_vec_info->first << endl;

            str_hit_idc.assign(it_idc_vec_info->first);
          }
        }

        ++it_vec;
      }

      ++it_idc_vec_info;
    }
    ++it;
  }

  if (str_hit_region.empty() || str_hit_idc.empty()) {
    cerr << "Error: region " << str_hit_region.empty() << " idc "
         << str_hit_idc.empty() << endl;
  }
}

int main(void) {
  //注意：请使用业务自身的appkey进行cat初始化！！！！！
  catClientInit("com.sankuai.inf.newct");

  //统一日志
  CLOG_INIT();
  //初始化mns_sdk
  InitMNS();

  TestIDCRegion();

  string str_octo_env;
  if (GetOctoEnv(&str_octo_env)) {
    cerr << "GetEnv failed" << endl;
    return -1;
  }

  cout << "octo_env " << str_octo_env << endl;

  if (CTHRIFT_UNLIKELY(StartSvr("com.sankuai.inf.newct",
                                7776, 0))) {    //只是注册了一个ip:port,如果该网络地址未真实存在,将在OCTO管理界面上显示未启动,但注册本身没有问题.
    cerr << "reg svr failed" << endl;
  } else {
    cout << "reg svr success" << endl;
  }

  vector<string> service_list;
  service_list.push_back("name1");
  service_list.push_back("name2");
  if (CTHRIFT_UNLIKELY(StartSvr("com.sankuai.inf.newct", service_list,
                                17776, 0, "thrift"))) {    //只是注册了一个ip:port,如果该网络地址未真实存在,将在OCTO管理界面上显示未启动,但注册本身没有问题.
    cerr << "service list reg svr failed" << endl;
  } else {
    cout << "service list reg svr success" << endl;
  }

  boost::function<void(
      const vector<SGService> &vec_add,
      const vector<SGService> &vec_del,
      const vector<SGService> &vec_chg,
      const string& appkey)> job(boost::bind(&Job, _1, _2, _3, _4));

  if (CTHRIFT_UNLIKELY(StartClient("com.sankuai.inf.newct",
                                   "com.sankuai.inf.newct.client",
                                   "thrift",
                                   "",
                                   job))) {
    cerr << "StartClient failed" << endl;
  } else {
    cout << "start client success" << endl;
  }


  boost::function<void(
      const vector<SGService> &vec_add,
      const vector<SGService> &vec_del,
      const vector<SGService> &vec_chg,
      const string& appkey)> job1(boost::bind(&JobList, _1, _2, _3, _4));
  string err_info = "";
  if (CTHRIFT_UNLIKELY(AddUpdateSvrListCallback("com.sankuai.inf.newct", job1, &err_info))) {
    cout << "AddUpdateSvrListCallback failed: " << err_info  << endl;
  } else {
    cout << "start client success" << endl;
  }
  sleep(120);

  DestroyMNS();
  CLOG_CLOSE();
}
