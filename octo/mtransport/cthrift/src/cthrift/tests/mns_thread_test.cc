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

void Job(const vector<SGService> &vec_add,
		 const vector<SGService> &vec_del,
		 const vector<SGService> &vec_chg,
		 const string& appkey) {
	cout << "recv add.size " << vec_add.size() << endl;
	for (int i = 0; i < static_cast<int>(vec_add.size()); i++) {
		cout << "[" << i << "]" << ": "
			 << CthriftSgagent::SGService2String(vec_add[i]) << endl;
	}
	cout << "recv del.size " << vec_del.size() << endl;
	for (size_t i = 0; i < vec_del.size(); i++) {
		cout << "[" << i << "]" << ": "
			 << CthriftSgagent::SGService2String(vec_del[i]) << endl;
	}
	cout << "recv chg.size " << vec_chg.size() << endl;
	for (size_t i = 0; i < vec_chg.size(); i++) {
		cout << "[" << i << "]" << ": "
			 << CthriftSgagent::SGService2String(vec_chg[i]) << endl;
	}

   cout << "appkey" << appkey << endl;
}

void JobTEST(const vector<SGService> &vec_add,
		 const vector<SGService> &vec_del,
		 const vector<SGService> &vec_chg,
		 const string& appkey) {
   cout << "AddUpdateSvrListCallback appkey" << appkey << endl;
}

TEST(MNSMULTITHREADTEST, HandleZeroReturn)
{
	InitMNS();
	boost::function<void(
		const vector<SGService> &vec_add,
		const vector<SGService> &vec_del,
		const vector<SGService> &vec_chg,
        const string& appkey)> job(boost::bind(&Job, _1, _2, _3, _4));
	EXPECT_EQ(0, StartClient("com.sankuai.inf.newct",
				"com.sankuai.inf.newct.client",
				"thrift",
				"",
				job));

	std::vector<SGService> svr_list;
	EXPECT_EQ(0, getSvrList("com.sankuai.inf.newct",
				"com.sankuai.inf.newct.client",
				"thrift",
				"",
				&svr_list));
	EXPECT_TRUE(!svr_list.empty());

	string err_info = "";
	boost::function<void(
		const vector<SGService> &vec_add,
		const vector<SGService> &vec_del,
		const vector<SGService> &vec_chg,
        const string& appkey)> job1(boost::bind(&JobTEST, _1, _2, _3, _4));
	EXPECT_EQ(0, AddUpdateSvrListCallback("com.sankuai.inf.newct", job1, &err_info));
	EXPECT_TRUE(err_info.empty());

	EXPECT_EQ(0, StartClient("com.sankuai.inf.mnsctest",
				"com.sankuai.inf.newct.client",
				"thrift",
				"",
				job));
        int count = 0;
	while (count++ < 1000) {
	  string str_octo_env;
  	  EXPECT_EQ(0, GetOctoEnv(&str_octo_env));
          EXPECT_TRUE(!str_octo_env.empty());
        }
      
	sleep(10);
	DestroyMNS();
}

void testThreadFunc(muduo::CountDownLatch *p_countdown) {
	RUN_ALL_TESTS();
	p_countdown->countDown();
}

int main(int argc, char** argv) {
	CLOG_INIT();
	testing::InitGoogleTest(&argc, argv);
	int thread_num = 5;
	muduo::CountDownLatch countdown_thread_finish(thread_num);
	for (int i = 0; i < thread_num; ++i) {
		muduo::net::EventLoopThread *pt =
				new muduo::net::EventLoopThread;

		pt->startLoop()->runInLoop(boost::bind(testThreadFunc,
											   &countdown_thread_finish));

		cout << endl;
	}
	countdown_thread_finish.wait();
	std::cout << "test finish" << std::endl;
	CLOG_CLOSE();
	return 0;
}
