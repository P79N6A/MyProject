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
#include "remote/monitor/monitor_collector.h"
#include <unistd.h>

using namespace std;
using namespace sg_agent;

class INCCOMM : public testing::Test {
};

TEST_F(INCCOMM, checkPortnotConnected) {
  std::string ip = "10.4.244.156";
  int port = 52688;
  bool ret = isPortOpen(ip, port);
  cout << "ret = " << ret;
  EXPECT_FALSE(ret);
}

TEST_F(INCCOMM, checkPortConnected4Timeout) {
  std::string ip = "10.32.108.251";
  int port = 5266;
  bool ret = isPortOpen(ip, port);
  cout << "ret = " << ret;

  EXPECT_FALSE(ret);
}

TEST_F(INCCOMM, calcuCpuAndRamUtil) {

  int run_item =0;
  while(run_item++ <3){

    std::string cmd = "ps aux |grep unittest | awk '{print( $2"  " $3"  " $4"  " $11);}'>>calcuProcCpuUtil";
    system(cmd.c_str());
    float cpu_util = SgMonitorCollector::GetInstance()->CalcuProcCpuUtil(getpid());
    std::cout<<"pid is: "<<getpid()<<", cpu_util:"<<cpu_util<<", ram util"<<GetProcMemUtil(getpid())<<std::endl;
    sleep(2);

}
  //EXPECT_TRUE
}

