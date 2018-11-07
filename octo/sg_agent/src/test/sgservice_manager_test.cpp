#include <gtest/gtest.h>

#include "SGAgent.h"
#include "util/sgservice_manager.h"

class SGServiceManagerTest : public testing::Test {
};

TEST_F(SGServiceManagerTest, Update) {
  SGService svr;
  svr.ip = "10.4.159.26";
  svr.port = 9001;
  svr.fweight = 10;
  svr.status = 2;
  std::vector<SGService> svrList1;
  std::vector<SGService> svrList2;
  svrList1.push_back(svr);
  svrList2.push_back(svr);

  svr.ip = "10.4.159.27";
  svrList1.push_back(svr);
  svrList2.push_back(svr);

  // chang
  svr.ip = "10.4.159.28";
  svrList1.push_back(svr);
  svr.fweight = 9;
  svrList2.push_back(svr);

  // add
  svr.ip = "10.4.159.29";
  svrList2.push_back(svr);
  svr.ip = "10.4.159.30";
  svrList2.push_back(svr);

  // del
  svr.ip = "10.4.159.31";
  svrList1.push_back(svr);

  SGServiceManager mgr;
  std::vector<SGService> vec_sgservice_add;
  std::vector<SGService> vec_sgservice_del;
  std::vector<SGService> vec_sgservice_chg;

  mgr.UpdateSvrList(svrList1,
                    vec_sgservice_add,
                    vec_sgservice_del,
                    vec_sgservice_chg);
  std::vector<SGService> list = mgr.getSvrList();
  EXPECT_EQ(4, list.size());
  EXPECT_EQ(4, vec_sgservice_add.size());
  EXPECT_EQ(0, vec_sgservice_del.size());
  EXPECT_EQ(0, vec_sgservice_chg.size());

  vec_sgservice_add.clear();
  vec_sgservice_del.clear();
  vec_sgservice_chg.clear();
  mgr.UpdateSvrList(svrList2,
                    vec_sgservice_add,
                    vec_sgservice_del,
                    vec_sgservice_chg);
  list = mgr.getSvrList();
  EXPECT_EQ(5, list.size());
  EXPECT_EQ(2, vec_sgservice_add.size());
  EXPECT_EQ(1, vec_sgservice_del.size());
  EXPECT_EQ(1, vec_sgservice_chg.size());

  std::cout << "add:" << std::endl;
  for (std::vector<SGService>::iterator iter = vec_sgservice_add.begin();
       iter != vec_sgservice_add.end(); ++iter) {
    std::cout << iter->ip << std::endl;
  }

  std::cout << "del:" << std::endl;
  for (std::vector<SGService>::iterator iter = vec_sgservice_del.begin();
       iter != vec_sgservice_del.end(); ++iter) {
    std::cout << iter->ip << std::endl;
  }

  std::cout << "chg:" << std::endl;
  for (std::vector<SGService>::iterator iter = vec_sgservice_chg.begin();
       iter != vec_sgservice_chg.end(); ++iter) {
    std::cout << iter->ip << std::endl;
  }
}
