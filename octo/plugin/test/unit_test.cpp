//
// Created by huixiangbo on 2017/10/12.
//
#include <string>
#include <vector>
#include <gtest/gtest.h>
#include "../core/util/tinyxml2.h"

#define private public
#define protected public
#include "../core/host_process.h"
#undef private
#undef protected


using namespace std;
using namespace cplugin;
using namespace tinyxml2;
using testing::Types;


//测试固件(Test fixture)
//测试固件的作用在于管理两个或多个测试实例都会使用到的数据，使用测试固件完成上述测试，方法如下：
//首先我们需要定义一个固件类(fixture class)，一般固件类以FooTest的形式命名，其中Foo为被测类的名称：

class HostProcessTest : public testing::Test {
public:
	HostProcessTest() {
	  p_host_process_ = new HostProcess(2886, "sg_agent", "3.1.4");
	}
	~HostProcessTest(){
		if( p_host_process_ ){
			delete p_host_process_;
		}
	}


    /*
     使用SetUp()方法或默认构造函数作数据初始化操作，
     使用TearDown()方法或析构函数作数据清理操作，
     注意SetUp()和TearDown()的拼写；
     构造一个QueueTest对象(假设为t1)；
     调用t1.SetUp()初始化t1对象；
     第一个测试实例(IsEmptyInitially)使用t1进行测试；
     调用t1.TearDown()进行数据清理；
     销毁对象t1；
     创建一个新的QueueTest对象，对下一个测试实例DequeueWorks重复以上步骤。
     C++类具有可继承的特点，这样我们可以灵活地定义固件类，我们可以把多个固件类共有的特性抽象出来形成一个基类，以进一步达到代码复用、数据复用的效果
     */
    virtual void SetUp() {

    }
    virtual void TearDown() {

    }


protected:
    HostProcess *p_host_process_;
};

TEST_F(HostProcessTest, HandleTureReturn) {

EXPECT_TRUE(true);

}

/*
class IsPrimeParamTest : public::testing::TestWithParam<int>
{
public:
    IsPrimeParamTest() {

    }
    ~IsPrimeParamTest() {
    }

    virtual void SetUp() {

    }
    virtual void TearDown() {

    }

};

bool IsPrime(int n){
    return true;
}

TEST_P(IsPrimeParamTest, Negative)
{
    int n =  GetParam();
    EXPECT_FALSE(IsPrime(n));
}

void testP()
{
    INSTANTIATE_TEST_CASE_P(NegativeTest, IsPrimeParamTest, testing::Values(-1,-2,-5,-100,INT_MIN));
}

*/