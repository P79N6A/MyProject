#include <gtest/gtest.h>

#include <boost/algorithm/string.hpp>
#include <boost/property_tree/xml_parser.hpp>
#include <boost/unordered/unordered_map.hpp>
#include <boost/lexical_cast.hpp>
#include <boost/random.hpp>

#include <cthrift/cthrift_sgagent.h>
#include <cthrift/mns_sdk/mns_sdk.h>
#include <cthrift/mcc_sdk/mcc_sdk.h>

#include <sys/time.h>

using namespace std;
using namespace cthrift;
using namespace mns_sdk;
using namespace mcc_sdk;

using testing::Types;

TEST(MNSMCCTEST, HandleZeroReturn)
{
    string str_err_info;
    EXPECT_EQ(0, InitMCCClient(&str_err_info, 50, 100));

    //init mns_sdk
    InitMNS();

    //check mcc_sdk work thread alive.
    string str_appkey("com.sankuai.inf.newct");
    string str_file_name("test.txt");

    string str_file_content;
    EXPECT_EQ(0, GetFileCfg(str_appkey,
                            str_file_name,
                            &str_file_content,
                            &str_err_info));

    cout << "file content " << str_file_content << endl;

    DestroyMCCClient();
    DestroyMNS();
}

int main(int argc, char** argv) {
	CLOG_INIT();
    testing::InitGoogleTest(&argc, argv);
    RUN_ALL_TESTS();
	CLOG_CLOSE();
	return 0;
}
