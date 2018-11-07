//
// Created by Xiang Zhang on 2017/10/30.
//
#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include "../storage.h"

using ::testing::AtLeast;
using testing::Return;
using namespace Controller;
class MockStorage : public Storage {
public:
    MockStorage(const std::string& db_name, const std::string& db_server,
                const std::string& db_user, const std::string& db_pass)
            :Storage(db_name, db_server, db_user, db_pass, 3306) {
    }
    MOCK_METHOD4(recordHistory, int64_t(int plugin_id, int op_type, op_status flag, const std::string& content));


};


TEST(MOCKTest, Storage) {
    MockStorage mock_st("control", "10.4.227.177", "root", "123456");
    Storage st("control", "10.4.227.177", "root", "123456", 3306);
    st.init();
}
