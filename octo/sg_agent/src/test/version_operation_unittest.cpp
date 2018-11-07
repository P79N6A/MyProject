#include <limits.h>
#include <gtest/gtest.h>
#include "sg_agent/version_operation.h"

using namespace sg_agent;

class VersionOperationTest : public testing::Test {
 public:
  void SetUp() {
  }

  void TearDown() {
  }

 protected:
  VersionOperation version_;
};

TEST_F(VersionOperationTest, GetVersion) {
  const std::string version = "mtthrift-v1.7.0";
  std::vector<int> test;
  version_.GetVersion(version, &test);
  EXPECT_EQ(3, test.size());
}

TEST_F(VersionOperationTest, UniVersion) {
  const std::string version = "mtthrift-v1.7.0";
  EXPECT_FALSE(version_.IsOldVersion(version));

  const std::string version_2 = "mtthrift-v1.7.0-SNAPSHOT";
  EXPECT_FALSE(version_.IsOldVersion(version_2));

  std::string version_3 = "mtthrift-v1.7.3";
  EXPECT_FALSE(version_.IsOldVersion(version_3));

  version_3 = "mtthrift-v1.17.3";
  EXPECT_FALSE(version_.IsOldVersion(version_3));
};

TEST_F(VersionOperationTest, OldVersion) {
  std::string version = "mtthrift-v1.6.4-SNAPSHOT";
  EXPECT_TRUE(version_.IsOldVersion(version));

  version = "mtthrift-v1.6.3-SNAPSHOT";
  EXPECT_TRUE(version_.IsOldVersion(version));

  version = "mtthrift-v1.5.7";
  EXPECT_TRUE(version_.IsOldVersion(version));
}

TEST_F(VersionOperationTest, Pigeon) {
  EXPECT_TRUE(version_.IsOldVersion("2.7.9"));
	EXPECT_TRUE(version_.IsOldVersion("pigeon-v2.7.9"));
  EXPECT_TRUE(version_.IsOldVersion("1.1.0"));
	EXPECT_TRUE(version_.IsOldVersion("pigeon-v1.1.0"));

  EXPECT_FALSE(version_.IsOldVersion("2.8.0"));
	EXPECT_FALSE(version_.IsOldVersion("pigeon-v2.8.0"));
  EXPECT_FALSE(version_.IsOldVersion("2.8.1"));
  EXPECT_FALSE(version_.IsOldVersion("2.10.0"));
	EXPECT_FALSE(version_.IsOldVersion("pigeon-v2.8.1"));
	EXPECT_FALSE(version_.IsOldVersion("pigeon-v2.10.1"));
}

TEST_F(VersionOperationTest, other) {
  std::string version = "cthrift";
  EXPECT_FALSE(version_.IsOldVersion(version));

  version = "pthrift";
  EXPECT_FALSE(version_.IsOldVersion(version));

  version = "";
  EXPECT_TRUE(version_.IsOldVersion(version));
}
