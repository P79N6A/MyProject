#include "util/switch_operate.h"
#include <gtest/gtest.h>
using namespace sg_agent;

class SGAgentSwitchTest : public testing::Test {
 public:
  static const int ty = 17;
};

TEST_F(SGAgentSwitchTest, initSwitch) {
  bool isOpen = false;
  int ret = SGAgentSwitch::initSwitch(ty, isOpen);
  EXPECT_EQ(0, ret);
  EXPECT_TRUE(isOpen);
}

TEST_F(SGAgentSwitchTest, setSwitch) {
  bool isOpen = false;
  int ret = SGAgentSwitch::setSwitch(ty, isOpen);
  EXPECT_EQ(0, ret);
  EXPECT_FALSE(isOpen);

  bool isOpen_2 = true;
  ret = SGAgentSwitch::_readSwitchFile(ty, isOpen_2);
  EXPECT_EQ(0, ret);
  EXPECT_FALSE(isOpen);
}

TEST_F(SGAgentSwitchTest, readDefaultFile) {
  bool isOpen = false;
  int ret = SGAgentSwitch::_readDefaultFile(ty, isOpen);
  EXPECT_EQ(0, ret);
  EXPECT_TRUE(isOpen);
}

TEST_F(SGAgentSwitchTest, writeSwitchFile) {
  bool isOpen = false;
  int ret = SGAgentSwitch::_writeSwitchFile(ty, isOpen);
  EXPECT_EQ(0, ret);
  ret = SGAgentSwitch::_readSwitchFile(ty, isOpen);
  EXPECT_FALSE(isOpen);

  isOpen = true;
  ret = SGAgentSwitch::_writeSwitchFile(ty, isOpen);
  EXPECT_EQ(0, ret);
  ret = SGAgentSwitch::_readSwitchFile(ty, isOpen);
  EXPECT_TRUE(isOpen);
}
