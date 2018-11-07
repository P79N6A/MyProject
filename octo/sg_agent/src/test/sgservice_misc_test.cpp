#include <gtest/gtest.h>

#include "SGAgent.h"
#include "util/sgservice_misc.h"

class MiscTest : public testing::Test {
};

TEST_F(MiscTest, SGServiceCompare) {
  SGService svr1;
  SGService svr2;
  svr1.ip = "10.4.159.26";
  svr1.port = 9001;
  svr2.ip = "10.4.159.26";

  svr2.port = 9002;
  EXPECT_EQ(true, SGServiceMisc::SGServiceCompare(svr1, svr2));

  svr2.port = 9001;
  EXPECT_EQ(false, SGServiceMisc::SGServiceCompare(svr1, svr2));

  svr2.port = 9000;
  EXPECT_EQ(false, SGServiceMisc::SGServiceCompare(svr1, svr2));

  svr2.ip = "10.4.159.27";
  svr2.port = 9001;
  EXPECT_EQ(true, SGServiceMisc::SGServiceCompare(svr1, svr2));

  svr2.ip = "10.4.159.24";
  svr2.port = 9001;
  EXPECT_EQ(false, SGServiceMisc::SGServiceCompare(svr1, svr2));
}
