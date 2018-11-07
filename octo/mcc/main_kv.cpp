#include <iostream>
#include "sg_agent_config_processor.h"
#include "config_instance.h"
#include "file_config_client.h"
#include "log.h"
#include "comm_helper.h"
#include "inc_comm.h"


using namespace std;

int main()
{


    int ret = 0;
    cout << "start" << endl;

    FileConfigClient fileClient;
    fileClient.init("test.notify");
    std::string strRet = fileClient.getFile("test");
    std::cout << "filecontent = " << strRet << std::endl;

    proc_conf_param_t conf;
    conf.appkey = "com.sankuai.inf.sg_agent";
    conf.env = "prod";
    conf.path = "/";

    boost::shared_ptr<ConfigProcessor> processor(new ConfigProcessor());

    // add app
    ret = processor -> add_app(conf.appkey);
    std::cout << "add_app's ret = " << ret << std::endl;

    while(1)
    {
        // get API
        std::string res;
        res = processor -> get("key", conf.appkey);
        std::cout << "get config res: key -> " << res << std::endl;

        sleep(5);
    }

    return 0;
}
