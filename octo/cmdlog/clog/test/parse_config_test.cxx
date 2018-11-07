#include <log4cplus/parse_config.h>
#include <gtest/gtest.h>
#include "interface_base.h"

class Parse: public InterfaceBase
{
  public:
    cmdlog::Configuration *conf; 
  protected:
    virtual void SetUp()
    {
      conf = cmdlog::Configuration::Instance();
      conf->init("./log.conf");
    }

    virtual void TearDown()
    {
    }
};

TEST_F(Parse, instance)
{
  //conf -> test();
  // 增加判断语句，可以参考文档, 比如
  EXPECT_EQ(conf->get("level"),"info");
  EXPECT_EQ(conf->get("app_key"),"com.sankuai.pic");
  EXPECT_EQ(conf->get("name"),"ipp_srv");
  EXPECT_EQ(conf->get("type"),"2");
  EXPECT_EQ(conf->get("history"),"");
  EXPECT_EQ(conf->get("size"),"1024");
  EXPECT_EQ(conf->get("count"),"10");
  int i = 1;
  EXPECT_EQ(1, 1);
}
