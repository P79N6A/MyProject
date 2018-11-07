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
#include <gtest/gtest.h>
#include <iostream>
#include "comm/inc_comm.h"
using namespace std;

class GetIp : public testing::Test {
};

TEST_F(GetIp, getIntranet) {
  char ip[INET_ADDRSTRLEN];
  char mask[INET_ADDRSTRLEN];
  int ret = getIntranet(ip, mask);
  cout << "ip = " << ip << ", mask = " << mask << endl;
  EXPECT_EQ(0, ret);
}
