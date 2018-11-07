// =====================================================================================
// 
//       Filename:  main.cpp
// 
//    Description:  learn msg
// 
//        Version:  1.0
//        Created:  2015-04-17 11时41分55秒
//       Revision:  none
// 
// =====================================================================================

#include "util/msgparam.h"
#include "comm/inc_comm.h"
#include <gtest/gtest.h>

class SelfFunc : public testing::Test {
 public:
  void printMap(std::map<int, long> &mymap) {
    for (std::map<int, long>::iterator it = mymap.begin(); it != mymap.end(); ++it)
      std::cout << it->first << " => " << it->second << '\n';
  }
};

TEST_F(SelfFunc, getps_sg_agent) {
  int rss = 0;
  double cpu = 1.0;
  // TODO check cpu and rss

  //getps("/opt/meituan/apps/sg_agent/sg_agent", rss, cpu);
  EXPECT_GT(6.0, cpu);
  std::cout << "sg_agent rss: " << rss << ", cpu:" << cpu << std::endl;
}
