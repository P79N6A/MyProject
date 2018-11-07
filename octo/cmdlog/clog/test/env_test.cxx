#include <gtest/gtest.h>
#include <log4cplus/devinfohelper.h>
#include "interface_base.h"
class Env:public InterfaceBase
{
  public:
    cmdlog::DevInfo devinfo;
  protected:
    virtual void SetUp()
    {
    }
    virtual void TearDown()
    {
    }
};

TEST_F(Env,instance)
{
  bool isOnline = devinfo.isOnlineDev();
  EXPECT_EQ(isOnline,true);
}

