#include <limits.h>
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include "interface_base.h"
#include "sgagent_shutdown.h"
#include "test_worker.h"

class Shutdown: public InterfaceBase
{
    protected:
        virtual void SetUp()
        {
            start_time_ = time(NULL);
            sg_agent_handler_.init(appkey_s, ip_, port_);
            usleep(500000);
            sg_worker_handler_.init(ip_, 5267);
        }

        virtual void TearDown()
        {
            const time_t end_time = time(NULL);
            EXPECT_TRUE(end_time - start_time_ <= 3) << "SgAgentTimeout testcase took too long.";
        }

        SGAgentHandler sg_agent_handler_;
        SGWorkerHandler sg_worker_handler_;
};

TEST_F(Shutdown, shutdownAgentWorker)
{
    //EXPECT_FALSE(sg_worker_handler_.client_->shutdown("test"));
    FILE *before = popen("/sbin/pidof sg_agent_worker", "r");
    char buf[200] = {0};
    while(fgets(buf, 200, before) != NULL);
    int beforePID = atoi(buf);
    
    pclose(before);
    sleep(1);
    FILE *after = popen("/sbin/pidof sg_agent_worker", "r");
    memset(buf, 0, 200);
    while(fgets(buf, 200, after) != NULL) ;
    int afterPID = atoi(buf);
    pclose(after);
   // EXPECT_NE(beforePID, afterPID);
};

TEST_F(Shutdown, shutdownAgent)
{
    EXPECT_FALSE(sg_agent_handler_.client_->shutdown("test"));
    FILE *before = popen("/sbin/pidof sg_agent", "r");
    char buf[200] = {0};
    while(fgets(buf, 200, before) != NULL);
    int beforePID = atoi(buf);
    FILE *workerBefore = popen("/sbin/pidof sg_agent_worker", "r");
    memset(buf, 0, 200);
    while(fgets(buf, 200, workerBefore));
    int workerBeforePID = atoi(buf);
    pclose(before);
    pclose(workerBefore);  

    sleep(1);

    FILE *after = popen("/sbin/pidof sg_agent", "r");
    memset(buf, 0, 200);
    while(fgets(buf, 200, after) != NULL) ;
    int afterPID = atoi(buf);
    FILE *workerAfter = popen("/sbin/pidof sg_agent_worker", "r");
    memset(buf, 0, 200);
    while(fgets(buf, 200, workerAfter) != NULL);
    int workerAfterPID = atoi(buf);
    pclose(after);
    //EXPECT_NE(beforePID, afterPID);
    //delelte the agent worker pid
};
