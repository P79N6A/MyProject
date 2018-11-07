//
// Created by huixiangbo on 2017/10/13.
//
int g_argc;
char** g_argv;
#include <gtest/gtest.h>

int main(int argc, char **argv) {

    g_argc = argc;
	g_argv = argv;
    testing::InitGoogleTest(&argc, argv);

    RUN_ALL_TESTS();

    return 0;
}
