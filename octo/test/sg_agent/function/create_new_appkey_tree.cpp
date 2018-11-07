#include "test_zk_client.h"
#include <gtest/gtest.h>

TEST(CreateNewAppkeyTree, Basic)
{
    ZkClientOperation zk_client;
    zk_client.init("com.sankuai.cos.mtconfig", "192.168.4.252:2181");
    zk_client.createOctoTree();
}
