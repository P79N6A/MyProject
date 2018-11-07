//
// Created by Xiang Zhang on 2017/9/18.
//

#include <string>
#include <vector>

#include <gtest/gtest.h>

#include "../ops_fetcher.h"

using namespace std;
using namespace Controller;
using testing::Types;

class OpsFetchPdlTest : public::testing::TestWithParam<string> {
};

TEST_P(OpsFetchPdlTest, HandleTrueReturn) {
    ops_fetcher fetcher;
    ASSERT_TRUE(fetcher.initCurl());

    string name = GetParam();
    vector<string> ret;
    EXPECT_EQ(0, fetcher.getPdl(name, ret));
    EXPECT_TRUE((fetcher.getHttpbuf()).size() > 0);
    EXPECT_TRUE(!ret.empty());

    /*cout << "param: " << name << endl;
    for (int i = 0; i < ret.size(); i++) {
        cout << ret[i] << ";";
    }*/
}

INSTANTIATE_TEST_CASE_P(OpsFetchPdl,
        OpsFetchPdlTest,
        ::testing::Values("owts", "pdls"));


class OpsFetchPdlBadTest : public::testing::TestWithParam<string> {
};

TEST_P(OpsFetchPdlBadTest, HandleFalseReturn) {
    ops_fetcher fetcher;
    ASSERT_TRUE(fetcher.initCurl());

    string name = GetParam();
    vector<string> ret;
    EXPECT_EQ(-1, fetcher.getPdl(name, ret));
    EXPECT_TRUE((fetcher.getHttpbuf()).size() > 0);
    EXPECT_TRUE(ret.empty());

}

INSTANTIATE_TEST_CASE_P(OpsFetchPdl,
        OpsFetchPdlBadTest,
        ::testing::Values("aowts", "pdl"));


struct TestParam{
    string owt;
    string pdl;
    string env;
    TestParam(const string& o, const string& p, const string& e)
            :owt(o), pdl(p), env(e) {

    }
};

class OpsFetchIpTest: public::testing::TestWithParam<TestParam> {
};

TEST_P(OpsFetchIpTest, HandleTrueReturn) {
    ops_fetcher fetcher;
    ASSERT_TRUE(fetcher.initCurl());
    vector<string> pret;
    EXPECT_EQ(0, fetcher.getPdl("pdls", pret));

    TestParam p = GetParam();
    map<string, vector<ip_host> > ret;
    EXPECT_EQ(0, fetcher.getIpList("meituan", p.owt, p.pdl, p.env, ret));
    EXPECT_TRUE((fetcher.getHttpbuf()).size() > 0);

    for (map<string, vector<ip_host> >::const_iterator itr = ret.begin();
            ret.end() != itr; ++itr) {
        cout << "IDC: " << itr->first << " size: " << (itr->second).size() <<endl;
        cout << (ret[itr->first][0]).rank_ << endl;
    }
}

INSTANTIATE_TEST_CASE_P(OpsFetchIp,
        OpsFetchIpTest,
        ::testing::Values(TestParam("adp", "adretr", ""), TestParam("adp", "adretr", "prod"), TestParam("adp", "adretr", "test")));
        //::testing::Values(TestParam("inf", "", ""), TestParam("adp", "adretr", ""), TestParam("adp", "adretr", "prod"), TestParam("adp", "adretr", "test")));


class OpsFetchIpBadTest: public::testing::TestWithParam<TestParam> {
};

TEST_P(OpsFetchIpBadTest, HandleFalseReturn) {
    ops_fetcher fetcher;
    ASSERT_TRUE(fetcher.initCurl());

    TestParam p = GetParam();
    map<string, vector<ip_host> > ret;
    EXPECT_EQ(-1, fetcher.getIpList("meituan", p.owt, p.pdl, p.env, ret));
    EXPECT_TRUE((fetcher.getHttpbuf()).size() > 0);
    EXPECT_TRUE(ret.empty());

}

INSTANTIATE_TEST_CASE_P(OpsFetchIp,
        OpsFetchIpBadTest,
        ::testing::Values(TestParam("adp9", "", ""), TestParam("adp", "0adretr", ""), TestParam("adp", "adretr", "0prod"), TestParam("1adp", "a2dretr", "t1est")));


TEST(OpsFetchSrvRank, HandleZeroReturn) {
    get_zero_clock();
    ops_fetcher fetcher;
    ASSERT_TRUE(fetcher.initCurl());
    srv_rank ret = NON_CORE_SRV;
    EXPECT_EQ(0, fetcher.getHostRank("yf-inf-databus-xm-relay02", ret));
    EXPECT_TRUE(CORE_SRV == ret);
    EXPECT_EQ(-1, fetcher.getHostRank("baddx-sys-ops01", ret));
}

TEST(OpsFetchCluster, HandleTureReturn) {
    get_zero_clock();
    ops_fetcher fetcher;
    ASSERT_TRUE(fetcher.initCurl());

    vector<string> ret_list;
    EXPECT_EQ(0, fetcher.getCluster(ret_list, "data"));
    EXPECT_TRUE(4 == ret_list.size());
}
