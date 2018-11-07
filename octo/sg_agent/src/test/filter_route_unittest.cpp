#include <limits.h>
#include <gtest/gtest.h>
#include "util/sgagent_filter.h"
#include "util/sg_agent_def.h"

using namespace std;


class ServiceFilter: public testing::Test
{
    public:
        static void SetUpTestCase()
        {
            /**
            * @Brief 构造routeData 数据
            */
            CRouteData high;
            high.id = "high-level";
            high.appkey = appkey_;
            high.name = "dynamic auto group";
            high.category = 0;
            high.status = 1;
            high.env = 2;
            high.updateTime = 1428414222;
            high.createTime = 1428411322;
            high.reserved = ""; 
            high.priority = 5;
            //provider
            vector<string> provider_1;
            provider_1.push_back("\"10.32.4.163:8920\"");
            provider_1.push_back("\"10.33.3.163:8920\"");
            high.provider = provider_1;
            //consumer
            Consumer consumer_1;
            vector<string> ips_1;
            ips_1.push_back("\"10.16.*\"");
            consumer_1.ips = ips_1;
            high.consumer = consumer_1;

            CRouteData low;
            low.id = "low-level";
            low.appkey = appkey_;
            low.name = "动态自动归组";
            low.category = 0;
            low.status = 1;
            low.env = 2;
            low.updateTime = 1428431222;
            low.createTime = 1428431222;
            low.reserved = "route_limit=1";
            low.priority = 3;
            //provider
            vector<string> provider_2;
            provider_2.push_back("10.32.5.163:*");
            low.provider = provider_2;
            //consumer
            Consumer consumer_2;
            vector<string> ips_2;
            ips_2.push_back("\"10.32.*\"");
            consumer_2.ips = ips_2;
            low.consumer = consumer_2;
            routeList.push_back(high);
            routeList.push_back(low);

            CRouteData autoRoute;
            autoRoute.id = "auto-level";
            autoRoute.appkey = appkey_;
            autoRoute.name = "动态自动归组";
            autoRoute.category = 1;
            autoRoute.status = 1;
            autoRoute.env = 2;
            autoRoute.updateTime = 1428431222;
            autoRoute.createTime = 1428431222;
            autoRoute.priority = 3;
            locRouteList.push_back(autoRoute);

            CRouteData autoLimitRoute;
            autoLimitRoute.id = "auto-level";
            autoLimitRoute.appkey = appkey_;
            autoLimitRoute.name = "动态自动归组";
            autoLimitRoute.category = 1;
            autoLimitRoute.status = 1;
            autoLimitRoute.env = 2;
            autoLimitRoute.updateTime = 1428431222;
            autoLimitRoute.createTime = 1428431222;
            autoLimitRoute.reserved = "route_limit:1";
            autoLimitRoute.priority = 3;
            locLimitRouteList.push_back(autoLimitRoute);

            /**
            * @Brief 构造SGService类型
            */
            SGService sg_1;
            sg_1.ip = "10.32.3.163";
            sg_1.port = 8920;
            sg_1.appkey = appkey_;
            sg_1.weight = 10;
            sg_1.fweight = 10;
            sg_1.status = 2;

            SGService sg_2;
            sg_2.ip = "10.33.4.163";
            sg_2.port = 8920;
            sg_2.appkey = appkey_;
            sg_2.weight = 10;
            sg_2.fweight = 10;
            sg_2.status = 2;
            SGService sg_3;
            sg_3.ip = "10.16.5.163";
            sg_3.port = 8920;
            sg_3.appkey = appkey_;
            sg_3.weight = 10;
            sg_3.fweight = 10;
            sg_3.status = 2;
            SGService sg_4;
            sg_4.ip = "10.1.5.163";
            sg_4.port = 8920;
            sg_4.appkey = appkey_;
            sg_4.weight = 10;
            sg_4.fweight = 10;
            sg_4.status = 2;
            serviceList.push_back(sg_1);
            serviceList.push_back(sg_2);
            serviceList.push_back(sg_3);
            serviceList.push_back(sg_4);
        }

        static void TearDownTestCase()
        {
        }

        static bool isInServiceList(vector<SGService>& servicelist, const SGService &service)
        {
            for(vector<SGService>::iterator itor = servicelist.begin(); itor != servicelist.end(); itor++)
            {
                if(*itor == service)
                    return true;
            }
            return false;
        }

    protected:
        static string appkey_;
        static vector<SGService> serviceList;
        static vector<CRouteData> routeList;
        static vector<CRouteData> locRouteList;
        static vector<CRouteData> locLimitRouteList;
};

string ServiceFilter::appkey_ = "com.sankuai.inf.logCollector";
vector<SGService> ServiceFilter::serviceList;
vector<CRouteData> ServiceFilter::routeList;
vector<CRouteData> ServiceFilter::locRouteList;
vector<CRouteData> ServiceFilter::locLimitRouteList;

TEST_F(ServiceFilter, userDefineFilterTest)
{
    string localIp = "10.32.4.252";
    EXPECT_EQ(0, sg_agent::SGAgent_filter::SortRouteList(routeList));
    vector<SGService> list = serviceList;
    sg_agent::SGAgent_filter::FilterRoute(list, locRouteList, localIp);
    sg_agent::SGAgent_filter::FilterWeight(list, sg_agent::IdcThresHold);
    std::cout << "sameIDCFilterTest : " << std::endl;
    for (vector<SGService>::iterator iter = list.begin();
            iter != list.end(); ++iter) {
        std::cout << iter -> ip << "\t" << iter -> weight << std::endl;
    }

    EXPECT_EQ(2, list.size());
};

TEST_F(ServiceFilter, sameIDCFilterTest)
{
    string localIp = "10.32.4.252";
    EXPECT_EQ(0, sg_agent::SGAgent_filter::SortRouteList(locRouteList));
    vector<SGService> list = serviceList;
    sg_agent::SGAgent_filter::FilterRoute(list, locRouteList, localIp);
    sg_agent::SGAgent_filter::FilterWeight(list, sg_agent::IdcThresHold);
    std::cout << "sameIDCFilterTest : " << std::endl;
    for (vector<SGService>::iterator iter = list.begin();
            iter != list.end(); ++iter) {
        std::cout << iter -> ip << "\t" << iter -> weight << std::endl;
    }

    EXPECT_EQ(2, list.size());
};

TEST_F(ServiceFilter, diffIDCFilterTest)
{
    string localIp = "10.64.4.252";
    EXPECT_EQ(0, sg_agent::SGAgent_filter::SortRouteList(locRouteList));

    vector<SGService> list = serviceList;
    sg_agent::SGAgent_filter::FilterRoute(list, locRouteList, localIp);
    for (vector<SGService>::iterator iter = list.begin();
            iter != list.end(); ++iter) {
        std::cout << iter -> ip << "\t" << iter -> weight << "\t" << iter -> fweight << std::endl;
    }
    sg_agent::SGAgent_filter::filterBackup(list);
    std::cout << "diffIDCFilterTest : " << std::endl;
    for (vector<SGService>::iterator iter = list.begin();
            iter != list.end(); ++iter) {
        std::cout << iter -> ip << "\t" << iter -> weight << "\t" << iter -> fweight << std::endl;
    }

    EXPECT_EQ(4, list.size());
};

TEST_F(ServiceFilter, diffIDCFilterLimitTest)
{
    string localIp = "10.64.4.252";
    EXPECT_EQ(0, sg_agent::SGAgent_filter::SortRouteList(locLimitRouteList));

    vector<SGService> list = serviceList;
    sg_agent::SGAgent_filter::FilterRoute(list, locLimitRouteList, localIp);
    for (vector<SGService>::iterator iter = list.begin();
            iter != list.end(); ++iter) {
        std::cout << iter -> ip << "\t" << iter -> weight << "\t" << iter -> fweight << std::endl;
    }
    sg_agent::SGAgent_filter::filterBackup(list);
    std::cout << "diffIDCFilterTest : " << std::endl;
    for (vector<SGService>::iterator iter = list.begin();
            iter != list.end(); ++iter) {
        std::cout << iter -> ip << "\t" << iter -> weight << "\t" << iter -> fweight << std::endl;
    }

    EXPECT_EQ(0, list.size());
};

TEST_F(ServiceFilter, diffRegionFilterTest)
{
    string localIp = "10.1.4.252";
    EXPECT_EQ(0, sg_agent::SGAgent_filter::SortRouteList(locRouteList));
    vector<SGService> list = serviceList;
    sg_agent::SGAgent_filter::FilterRoute(list, locRouteList, localIp);
    sg_agent::SGAgent_filter::FilterWeight(list, sg_agent::IdcThresHold);
    std::cout << "diffRegionFilterTest : " << std::endl;
    for (vector<SGService>::iterator iter = list.begin();
            iter != list.end(); ++iter) {
        std::cout << iter -> ip << "\t" << iter -> weight << std::endl;
    }

    EXPECT_EQ(1, list.size());
};
