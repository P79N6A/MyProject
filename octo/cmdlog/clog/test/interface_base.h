#ifndef _INTERFACE_BASE_H_
#define _INTERFACE_BASE_H_

#include <gtest/gtest.h>

class InterfaceBase: public testing::Test
{
    public:
        static void SetUpTestCase();

};

#endif
