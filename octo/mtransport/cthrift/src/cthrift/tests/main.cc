//
// Created by XiangZhang on 2017/9/22.
//

#include <gtest/gtest.h>

#include <cthrift/mcc_sdk/mcc_sdk.h>
using namespace std;
using namespace mcc_sdk;

using testing::Types;

int main(int argc, char **argv) {
    string str_err_info;
    if (InitMCCClient(&str_err_info, 50, 100)) {
        cerr << str_err_info << endl;
        return -1;
    }

    testing::InitGoogleTest(&argc, argv);
    RUN_ALL_TESTS();

    DestroyMCCClient();
    return 0;
}

