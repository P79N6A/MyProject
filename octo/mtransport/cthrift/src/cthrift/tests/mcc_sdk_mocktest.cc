//
// Created by Xiang Zhang on 2017/9/22.
//

#include <cstdlib>
//#include <unistd.h>
#include <dlfcn.h>

#include <gtest/gtest.h>

#include <cthrift/mcc_sdk/mcc_sdk.h>

using namespace std;
using namespace mcc_sdk;

using testing::Types;


bool b_mock;
//mock sock connect
typedef int (*connect_func_t)(int sockfd,
                              const struct sockaddr *arr,
                              socklen_t addrlen);

//connect_func_t connect_func = (connect_func_t)dlsym(RTLD_NEXT, "connect");

int mock_connect_errno;

extern "C" int connect(int sockfd,
                       const struct sockaddr *addr,
                       socklen_t addrlen) {
    if(b_mock) {
        errno = mock_connect_errno;
        return errno == 0 ? 0 : -1;
    }
}

//mock read file
/*typedef ssize_t (*read_func_t) (int fd, void *buf, size_t count);

read_func_t read_func = (read_func_t)dlsym(RTLD_NEXT, "read");
int mock_read_errno;

extern "C" ssize_t read(int fd, void *buf, size_t count) {
    if (b_mock) {
        errno = mock_read_errno;
        return errno == 0 ? 0 : -1;
	}
   else {
        return read_func(fd, buf, count);
   }
}*/

struct TestP{
    bool b_mock;
    int mock_errno;
    TestP(bool _b, int _err) : b_mock(_b), mock_errno(_err){
    }
};

class MockBadTest : public::testing::TestWithParam<TestP>
{
};

//运行case前，请先清理本地磁盘缓存.
TEST_P(MockBadTest, HandleReturnError) {
    TestP param = GetParam();
    b_mock = param.b_mock;
    mock_connect_errno = param.mock_errno;

    string str_err_info, str_file_content;
    string str_appkey("com.sankuai.inf.newct"), str_file_name("test.txt");
    EXPECT_EQ(-1, GetFileCfg(str_appkey, str_file_name, &str_file_content, &str_err_info));
	EXPECT_TRUE(!str_err_info.empty());
    cout << "err info: " << str_err_info << endl;

	str_err_info = "";
    string str_key("hawktest"), str_zk_value;
    EXPECT_EQ(-1, GetCfg(str_appkey, str_key, &str_zk_value, &str_err_info));
	EXPECT_TRUE(!str_err_info.empty());
    cout << "zk err info: " << str_err_info << endl;

    map<string, string> ret_map;
	str_err_info = "";
    EXPECT_EQ(-1, GetGlobalCfg(str_appkey, &ret_map, &str_err_info));
	EXPECT_TRUE(!str_err_info.empty());
    cout << "zk err info: " << str_err_info << endl;
    sleep(5);
}

INSTANTIATE_TEST_CASE_P(MockBad,
        MockBadTest,
        ::testing::Values(TestP(false, 0), TestP(true, -1), TestP(true, 10)));

