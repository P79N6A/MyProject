#ifndef _INTERFACE_BASE_H_
#define _INTERFACE_BASE_H_

#include <gtest/gtest.h>
#include <limits.h>
#include <string>
#include "tinyxml2.h"
#include <time.h>
#include <vector>

class InterfaceBase: public testing::Test
{
    public:
        static void SetUpTestCase();

        //static void TearDownTestCase();


        static std::string ip_;
        static std::string appkey_s;
        static int port_;
        static std::string zkserver_;

        time_t start_time_;
};


#endif
