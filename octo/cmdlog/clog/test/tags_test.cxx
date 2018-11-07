#include "../include/clog/log.h"
#include "interface_base.h"

#include <gtest/gtest.h>
#include <map>

class Tag:public InterfaceBase 
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

TEST_F(Tag,instance)
{
    std::map<std::string, std::string> test_map;
    std::string key;
    char key_arr[10];
    test_map["traceID"] = "123-123";
    
    for (int i=0; i<10; i++) 
    {
       snprintf(key_arr, sizeof(key_arr), "%d", i);  
       test_map[key_arr] = "0";
    }


	typedef std::map<std::string, std::string>::iterator STRING_MAP_IT;
    STRING_MAP_IT it_b = test_map.begin();
    STRING_MAP_IT it_e = test_map.end();
    STRING_MAP_IT it = test_map.find("traceID");

    ASSERT_NE(it_b,it);
    ASSERT_EQ(11,test_map.size()); 

    test_map.erase(it);

    ASSERT_NE(it,it_b); 
    ASSERT_EQ(10,test_map.size()); 
    ASSERT_EQ(test_map["traceID"],it->second);
    ASSERT_NE(it_b,it);
}
      
