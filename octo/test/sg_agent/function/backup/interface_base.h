#ifndef _INTERFACE_BASE_H_
#define _INTERFACE_BASE_H_

#include <gtest/gtest.h>
#include "test_sg_agent.h"
#include "test_zk_client.h"

class TestInterfaceBase: public testing::Test {
    public:
        virtual void SetUp() {}
        virtual void TearDown() {}

        static void SetUpTestCase();
        static void TearDownCase();
        static void InitHandler();

    protected:
        static SGAgentHandler sg_agent_handler_;
        static ZkClientOperation zk_operator_;

        static string ip_;
        static int port_;
        static string appkey_;
        static string zkserver_;
};

#endif
