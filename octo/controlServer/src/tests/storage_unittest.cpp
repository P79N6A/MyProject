//
// Created by Xiang Zhang on 2017/9/11.
//

#include <string>
#include <vector>
#include <iostream>
#include <sys/time.h>

#include <gtest/gtest.h>

#include "../storage.h"

using namespace std;
using namespace Controller;
using testing::Types;

struct history{
    int plugin_id;
    int op_type;
    op_status flag;
    string content;
    history(int p_id, int op, op_status f, string c)
            : plugin_id(p_id),op_type(op),
              flag(f),content(c){
    }
};

class DBRecordHistoryTest : public::testing::TestWithParam<history> {

};

TEST_P(DBRecordHistoryTest, HandleTrueReturn) {
    Storage db("control", "10.4.227.177", "root", "123456", 3306);
    EXPECT_EQ(0, db.init());

    history p = GetParam();
    int his_id = db.recordHistory(p.plugin_id, p.op_type, p.flag, p.content);
    EXPECT_TRUE(his_id != -1);
    std::cout << "new history Id is: " << his_id << std::endl;
}

INSTANTIATE_TEST_CASE_P(DBRecordHistory,
        DBRecordHistoryTest,
        ::testing::Values(history(-2, 0, PROCESSING, "test insert hist"), history(-2, 1, PROCESSING, "test insert hist1"),
                          history(-2, 3, PROCESSING, "test insert hist2"), history(-2, 4, PROCESSING, "test insert hist4")));

struct TestRegion {
    string region;
    string center;
    TestRegion(const string& r, const string& c)
            :region(r), center(c) {

    }
};

class DBGetRegionTest : public::testing::TestWithParam<TestRegion> {
};

TEST_P(DBGetRegionTest, HandleZeroReturn) {
    Storage db("control", "10.4.227.177", "root", "123456", 3306);
    EXPECT_EQ(0, db.init());
    vector<string> region_list, idc_list;
    EXPECT_EQ(0, db.getAllIdc(region_list));
    EXPECT_TRUE(!region_list.empty());

    TestRegion p = GetParam();
    EXPECT_EQ(0, db.getIdc(p.region, p.center, idc_list));
    EXPECT_TRUE(idc_list.empty());
}

INSTANTIATE_TEST_CASE_P(DBGetRegion,
        DBGetRegionTest,
        ::testing::Values(TestRegion("emptyregion","emptycenter"), TestRegion("emptyregion","emptycenter"),
                          TestRegion("goodregion","goodcenter"), TestRegion("goodregion","goodcenter")));

TEST(DBPdlTEST, HandleZeroReturn) {
    Storage db("control", "10.4.227.177", "root", "123456", 3306);
    EXPECT_EQ(0, db.init());

    //first set pdl and owt
    EXPECT_EQ(0, db.setPdl("meituan.testowt.testpdl", "meituan.testowt"));
    EXPECT_EQ(0, db.setPdl("meituan.testowt2.testpdl", "meituan.testowt2"));
    //second set the same pdl and owt
    sleep(1);
    EXPECT_EQ(0, db.setPdl("meituan.testowt.testpdl", "meituan.testowt"));

    vector<string> first_list;
    EXPECT_EQ(0, db.getPdl("meituan.testowt", first_list));
    EXPECT_EQ(1, first_list.size());
    vector<string> second_list;
    EXPECT_EQ(0, db.getPdl("meituan.testowt2", second_list));
    EXPECT_EQ(1, second_list.size());
}


struct TestTuple {
    string env;
    string pdl;
    string idc;
    TestTuple(const string& e, const string& p, const string& i)
            :env(e), pdl(p), idc(i){
    }
};

class DBTupleTest : public::testing::TestWithParam<TestTuple> {
};

TEST_P(DBTupleTest, HandleTrueReturn) {
    Storage db("control", "10.4.227.177", "root", "123456", 3306);
    EXPECT_EQ(0, db.init());

    //first set tuple record.
    TestTuple p = GetParam();
    EXPECT_TRUE(db.setTuple(p.env, p.pdl, p.idc) > -1);
    //set same data again
    EXPECT_TRUE(db.setTuple(p.env, p.pdl, p.idc) > -1);

    int id = db.getTupleId(p.env, p.pdl, p.idc);
    cout << "TupleId: " << id << endl;
    EXPECT_TRUE(id != -1);
}

INSTANTIATE_TEST_CASE_P(DBTuple,
        DBTupleTest,
        ::testing::Values(TestTuple("prod", "pdltest", "idctest"), TestTuple("test", "pdltest1", "idctest1")));


class DBTupleBadTest : public::testing::TestWithParam<TestTuple> {
};

TEST_P(DBTupleBadTest, HandleFalseReturn) {
    Storage db("control", "10.4.227.177", "root", "123456", 3306);
    EXPECT_EQ(0, db.init());

    TestTuple p = GetParam();
    int id = db.getTupleId(p.env, p.pdl, p.idc);
    EXPECT_EQ(-1, id);
}

INSTANTIATE_TEST_CASE_P(DBTuple,
        DBTupleBadTest,
        ::testing::Values(TestTuple("pr", "pdltest", "idctest"), TestTuple("", "pdltest1", "idctest1"),
                          TestTuple("test", "pdltest1", ""), TestTuple("", "", "idctest1")));

TEST(DBAffiliationTest, HandleTrueReturn){
    Storage db("control", "10.4.227.177", "root", "123456", 3306);
    EXPECT_EQ(0, db.init());
//first
    vector<ip_host> list1;
    list1.push_back(ip_host("219.223.192.168", "host1", CORE_SRV));
    list1.push_back(ip_host("219.223.192.169", "host2", NON_CORE_SRV));
    list1.push_back(ip_host("219.223.192.168", "host1", CORE_SRV));
    EXPECT_EQ(0, db.setIPAffiliation(list1, 0));

//second
    vector<ip_host> list2;
    list2.push_back(ip_host("219.223.192.170", "host3", NON_CORE_SRV));
    list2.push_back(ip_host("219.223.192.171", "host4", NON_CORE_SRV));
    list2.push_back(ip_host("219.223.192.170", "host3", CORE_SRV));
    EXPECT_EQ(0, db.setIPAffiliation(list2, 1));

//first
    EXPECT_EQ(0, db.setIPAffiliation(list1, 3));


//test get
    EXPECT_EQ(3, db.getIPAffiliation("219.223.192.168"));
    EXPECT_EQ(1, db.getIPAffiliation("219.223.192.170"));
    EXPECT_EQ(-1, db.getIPAffiliation("319.223.192.170"));

    db.deleteExpiredIP(1000000);
}

struct TestPlugin{
    string name;
    string md5;
    string version;
    string lib_content;
    TestPlugin(const string& n, const string& m,
               const string& v="", const string& c="")
            : name(n), md5(m), version(v), lib_content(c){
    }
};

class DBPluginTest : public::testing::TestWithParam<TestPlugin> {
};

TEST_P(DBPluginTest, HandleTureReturn) {
    Storage db("control", "10.4.227.177", "root", "123456", 3306);
    EXPECT_EQ(0, db.init());

    TestPlugin p = GetParam();
    int id = db.setPlugin(p.name, p.md5, p.version, p.lib_content);
    //cout << "1111111111pluginid: " << id << endl;
    EXPECT_TRUE(id > -1);
    sleep(1);
    db.setPlugin("plugin_name1", "md5_01");

    id = -1;
    id = db.getPluginId("badmd5");
    EXPECT_TRUE(-1 == id);
    id = db.getPluginId(p.md5);
    EXPECT_TRUE(id > -1);
}

INSTANTIATE_TEST_CASE_P(DBPlugin,
        DBPluginTest,
        ::testing::Values(TestPlugin("plugin_name1", "md5_01"), TestPlugin("plugin_name2", "md5_02", "plugin_version", ".sottest"),
                          TestPlugin("plugin_name2", "md5_02", "plugin_version", ".sottest")));

struct TestRule{
    int plugin_id;
    int tuple_id;
    int op_type;
    TestRule(int plugin, int par, int op)
            : plugin_id(plugin),tuple_id(par),op_type(op){
    }
};

class DBRuleTest : public::testing::TestWithParam<TestRule> {
};

TEST_P(DBRuleTest, HandleTureReturn) {
    Storage db("control", "10.4.227.177", "root", "123456", 3306);
    EXPECT_EQ(0, db.init());

    TestRule p = GetParam();
    int id = db.setRule(p.plugin_id, p.tuple_id, -1, "unitplugin_name", p.op_type);
    EXPECT_TRUE(id > -1);

    int plugin_id = -1, tuple = -1, op = -1;
    string name, md5, *badname = NULL;
    EXPECT_EQ(0, db.getUpdateInfo(id, &plugin_id, &tuple, &op, &name, &md5));
    cout << "id: " << id << "  " <<  plugin_id << " " << tuple << " " << op << " " << name << " " << md5 << endl;
    EXPECT_EQ(-1, db.getUpdateInfo(1111, &plugin_id, &tuple, &op, badname, &md5));
}

INSTANTIATE_TEST_CASE_P(DBRule,
        DBRuleTest,
        ::testing::Values(TestRule(1, 1, 0), TestRule(2, 1, 1), TestRule(2, 2, 2), TestRule(1, 1, 1)));

TEST(DBReportTest, HandleTrueReturn) {
    Storage db("control", "10.4.227.177", "root", "123456", 3306);
    EXPECT_EQ(0, db.init());

    int plugin_id = -10, his_id = 1183301;
    string ip = "testip";
    EXPECT_EQ(0, db.setReport(his_id, plugin_id, ip, UPDATING, "beginning"));
    EXPECT_EQ(0, db.setReport(-100, plugin_id, ip, UPDATING, "failed: ip not good"));
    sleep(1);
    EXPECT_EQ(0, db.setReport(his_id, plugin_id, ip, SUCCESS, ""));

    //EXPECT_TRUE(db.isTaskCompleted(his_id));
}

TEST(DBRegularCheckTest, HandleTrueReturn) {
    Storage db("control", "10.4.227.177", "root", "123456", 3306);
    EXPECT_EQ(0, db.init());

    string ip_add = "10.20.109.67", plugin_name = "unitplugin_name";
    int plugin_id = -1, task_id = -1, op_type = -1;
    string ret_md5 = "";

    struct timeval start, end;
    gettimeofday(&start, NULL);
    EXPECT_EQ(0, db.checkPlugin(ip_add, plugin_name, &plugin_id, &task_id, &op_type, &ret_md5));
    gettimeofday(&end, NULL);
    cout << "total time take: " << (end.tv_sec  - start.tv_sec)*1000 + (end.tv_usec - start.tv_usec)/1000.0 << "ms" << endl;
    EXPECT_TRUE(!ret_md5.empty());

    cout << "p_id: " << plugin_id << " task_id: " << task_id << " op_type: " << op_type << " md5: " << ret_md5 << endl;

//EXPECT_TRUE(db.isTaskCompleted(his_id));
}

TEST(DBisFetchOpsNowTest, HandleFalseReturn) {
    Storage db("control", "10.4.227.177", "root", "123456", 3306);
    EXPECT_EQ(0, db.init());

    EXPECT_FALSE(db.isFetchOpsNow());
}

struct TestHealth{
    string ip_addr;
    string plugin_name;
    int16_t status;
    TestHealth(string ip, string p_name, int16_t st)
            : ip_addr(ip),plugin_name(p_name),status(st){
    }
};

class DBHealthTest : public::testing::TestWithParam<TestHealth> {
};

TEST_P(DBHealthTest, HandleGoodReturn) {
    Storage db("control", "10.4.227.177", "root", "123456", 3306);
    EXPECT_EQ(0, db.init());

    TestHealth p = GetParam();
    EXPECT_EQ(0, db.updatePluginHealth(p.ip_addr, p.plugin_name, p.status));
}

INSTANTIATE_TEST_CASE_P(DBHealth,
        DBHealthTest,
        ::testing::Values(TestHealth("10.333.333.333", "test_plugin", 1), TestHealth("10.333.333.333", "test_plugin", 2),
                          TestHealth("10.333.333.331", "test_plugin", 3), TestHealth("10.333.333.331", "test_plugin2", 4)));

TEST(DBgetAllTupleId, HandleFalseReturn) {
    Storage db("control", "10.4.227.177", "root", "123456", 3306);
    EXPECT_EQ(0, db.init());

    vector<int> tuple_id_list;
    db.getAllTupleId(tuple_id_list);
    EXPECT_FALSE(tuple_id_list.empty());
}

TEST(DBgetPluginNameByTupleId, HandleFalseReturn) {
    Storage db("control", "10.4.227.177", "root", "123456", 3306);
    EXPECT_EQ(0, db.init());

    vector<string> plugin_name_list;
    db.getPluginsByTuple(-1, 6, plugin_name_list);
    EXPECT_FALSE(plugin_name_list.empty());
}

TEST(DBfindLeftPluginTest, HandleTrueReturn) {
    Storage db("control", "10.4.227.177", "root", "123456", 3306);
    EXPECT_EQ(0, db.init());

    string plugin_name = "cr_agent";
    int tuple_id = 70491;
    int plugin_id = -1, task_id = -1;
    string ret_md5 = "";

    struct timeval start, end;
    gettimeofday(&start, NULL);
    EXPECT_EQ(0, db.findLeftPlugin(plugin_name, tuple_id, &plugin_id, &task_id, &ret_md5));
    gettimeofday(&end, NULL);
    cout << "total time take: " << (end.tv_sec  - start.tv_sec)*1000 + (end.tv_usec - start.tv_usec)/1000.0 << "ms" << endl;
    EXPECT_TRUE(!ret_md5.empty());

    cout << "p_id: " << plugin_id << " task_id: " << task_id << " md5: " << ret_md5 << endl;

//EXPECT_TRUE(db.isTaskCompleted(his_id));
}