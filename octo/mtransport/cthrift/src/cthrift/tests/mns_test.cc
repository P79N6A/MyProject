#include <gtest/gtest.h>

#include <boost/algorithm/string.hpp>
#include <boost/property_tree/xml_parser.hpp>
#include <boost/unordered/unordered_map.hpp>
#include <boost/lexical_cast.hpp>
#include <boost/random.hpp>

#include <cthrift/cthrift_sgagent.h>
#include <cthrift/mns_sdk/mns_sdk.h>

#include <sys/time.h>

using namespace std;
using namespace cthrift;
using namespace mns_sdk;

using testing::Types;

void Testperformance(void) {
    LocateInfo locateInfo;
    std::string ip_addr = "10.129.1.11";

	int i = 0;
	struct timeval start, end;
	gettimeofday(&start, NULL);
	while(i++ < 100000) {
    GetLocateInfoByIP(ip_addr, &locateInfo);
	}
	gettimeofday(&end, NULL);
	cout << "total time take: " << (end.tv_sec  - start.tv_sec)*1000 + (end.tv_usec - start.tv_usec)/1000.0 << endl;
    cout << "TestGetLocateInfoByIp: " << locateInfo.str_idc << " " << locateInfo.str_region << " " << locateInfo.str_center << endl;
}

class GetLocateInfoByIpTest : public::testing::TestWithParam<string>
{
};

TEST_P(GetLocateInfoByIpTest, HandleZeroReturn)
{
    LocateInfo locateInfo;
	string str_ip = GetParam();
	EXPECT_EQ(0, GetLocateInfoByIP(str_ip, &locateInfo));

    cout << "TestGetLocateInfoByIp: " << str_ip << ". result: " << locateInfo.str_idc << " " << locateInfo.str_region << " " << locateInfo.str_center << endl;
}

INSTANTIATE_TEST_CASE_P(GetLocateInfo, 
			GetLocateInfoByIpTest, 
			::testing::Values("10.129.1.109", "10.99.12.111", "10.10.253.0", "10.3.0.0", "10.1.0.0", "10.12.0.0"));

int main(int argc, char** argv) {
	CLOG_INIT();
	InitMNS();
	//testing::InitGoogleTest(&argc, argv);
	//RUN_ALL_TESTS();
	Testperformance();
    DestroyMNS();
	CLOG_CLOSE();
	return 0;
}
