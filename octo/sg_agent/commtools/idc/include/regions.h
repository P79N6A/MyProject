#ifndef __mns_regions__H__
#define __mns_regions__H__
#include <vector>
#include <string>

struct IDC {
    int id;
    std::string name;
    std::string ip;
    std::string center;
    int mask;
};

struct Region {
    int id;
    std::string name;
    std::vector<IDC> idcs;
};

class Regions {
    public:
        std::vector<Region> regions;

        // -1: not find region; else return region's id
        int getRegionID(std::string ip);

        // beijing/shanghai: not find region; else return unknown
        std::string getRegionName(std::string ip);

        // -1: not find idc; else return idc's id
        int getIDCID(std::string ip);

        // return idcName; else return unknown
        std::string getIdcName(std::string ip);
};
#endif
