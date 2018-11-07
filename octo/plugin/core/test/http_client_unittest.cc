#include <gtest/gtest.h>
#include <iostream>

#include "core/http_client.h"

using namespace std;
using namespace core;

class HttpClientTest : public testing::Test {
 public:
  HttpClientTest() {}
  CHttpClient client;
};

TEST_F(HttpClientTest, Get) {
  const string url = "http://baidu.com";
  string response;
  EXPECT_EQ(0, client.Get(url, &response));
}

TEST_F(HttpClientTest, Post) {
  const string url = "http://baidu.com";
  const string data = "nothing";
  string response;
  EXPECT_EQ(0, client.Post(url, data, &response));
}
