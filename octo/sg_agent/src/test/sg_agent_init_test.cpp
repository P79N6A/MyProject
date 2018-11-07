#include <gtest/gtest.h>
#include "util/global_def.h"
extern GlobalVar* g_global_var;

class SGAgentInitTest : public testing::Test {

};
TEST_F(SGAgentInitTest, g_global_var_test) {
	EXPECT_FALSE(g_global_var->gEnvStr.empty());
	EXPECT_NE(0, g_global_var->gEnv);


	EXPECT_TRUE(g_global_var->isOpenMNSCache);

	EXPECT_EQ(0, g_global_var->gOpenConfig);
	EXPECT_EQ(1, g_global_var->gOpenMtConfig);
	EXPECT_EQ(1, g_global_var->gOpenCommonLog);
	EXPECT_EQ(1, g_global_var->gOpenModuleInvoke);
	EXPECT_EQ(1, g_global_var->gOpenQuota);
	EXPECT_EQ(1, g_global_var->gOpenSelfCheck);
	EXPECT_EQ(1, g_global_var->gOpenFileConfig);
	EXPECT_EQ(1, g_global_var->gOpenHlb);
	EXPECT_EQ(1, g_global_var->gOpenAuth);
	EXPECT_EQ(0, g_global_var->gOpenSwitchEnv);
	EXPECT_EQ(0, g_global_var->gOpenAutoSwitchStage);
	EXPECT_EQ(1, g_global_var->gOpenAutoRoute);
	EXPECT_EQ(1, g_global_var->gOpenUnifiedProtoChange4LocalAppkey);
	EXPECT_EQ(0, g_global_var->gOpenUnifiedProtoChange4MTthrift);
			
			if(PROD != g_global_var->gAppenv && STAGING != g_global_var->gAppenv) {
				// offine
				EXPECT_EQ(0, g_global_var->gOpenAutoSwitchTest);
			}else {
				// online

				EXPECT_EQ(0, g_global_var->gOpenAutoSwitchTest);
			} 


}
