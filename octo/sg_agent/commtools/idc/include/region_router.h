#ifndef __mns_region_router__H__
#define __mns_region_router__H__
#include <string>
#include <vector>

#include "regions.h"

const static std::string DefaultMask = "255.255.0.0";
const static std::string DefaultXML = "/opt/meituan/apps/sg_agent/idc.xml";

class RegionRouter {
    public:
        bool CheckSameRegion(const std::string& ip1, const std::string& ip2);

        bool CheckSameIDC(const std::string& ip1, const std::string& ip2);

        int init(std::string confFile = "");

        Regions getRegions();

        int getRegionsSize();

        std::string GetRegionName(std::string& ip);

        std::string GetIdcName(std::string& ip);

        static RegionRouter* getInstance();
    private:
        static Regions regions;

        static RegionRouter* regionRouter;

        static int defaultMask;
        static bool isInited;
};

#endif
