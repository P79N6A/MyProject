#include "sg_agent/task_context.h"
#include <gtest/gtest.h>

using namespace sg_agent;

class TaskContextTest : public testing::Test {
 public:
  TaskContextTest() : context_(3) {}
  TaskContext<int, int> context_;
};

TEST_F(TaskContextTest, GetSet) {
  EXPECT_EQ(NULL, context_.get_response());
  context_.set_response(4);
  EXPECT_EQ(4, *context_.get_response());
}
