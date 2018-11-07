#include <gtest/gtest.h>
#include <clog/log.h>
#include "interface_base.h"
class Single:public InterfaceBase 
{
  public:
    cmdlog::CLog *cc1;
  protected:
    virtual void SetUp()
    {
        cc1 = cmdlog::CLog::getLogger(); 
    }
    virtual void TearDown()
    {
    }
};

TEST_F(Single,instance)
{
  cmdlog::CLog *cc2 = cmdlog::CLog::getLogger();
  EXPECT_EQ(cc1,cc2);
}
      
