#include <iostream>
#include <string>

#include "idc_inc.h"

using namespace std;


int main() {
    RegionRouter* router = RegionRouter::getInstance();
    int ret = router -> init();
    if (0 != ret) {
        cout << "failed to init router" << endl;
    }
    string ip = "10.4.245.3";
    string regionName = router -> GetRegionName(ip);
    cout << "regionName: " << regionName << endl;
    string idcName = router -> GetIdcName(ip);
    cout << "idc: " << idcName << endl;

    return 0;
}
