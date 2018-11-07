#include <iostream>
#include "sg_agent_config_processor.h"
#include "config_instance.h"
#include "file_config_client.h"
#include "log.h"
#include "comm_helper.h"
#include "inc_comm.h"


using namespace std;

class TestListener: public FileChangeListener {
    public:
        virtual void OnEvent(std::string filename,
                std::string oriFile, std::string newFile) {
            std::cout << "Listener's event: " <<
                filename << " is changed"
                << "; ori content = " << oriFile
                << "; new content = " << newFile << std::endl;
        }
};

int main()
{

    int ret = 0;
    cout << "start" << endl;

    //std::string appkey = "com.sankuai.inf.msgp";
    //std::string filename = "msgp.conf";
    //std::string appkey = "com.sankuai.inf.testMtConfig";
    //std::string filename = "test.conf";
    std::string appkey = "com.sankuai.fe.mta.parser";
    std::string filename = "demo";

    boost::shared_ptr<FileConfigProcessor> processor(new FileConfigProcessor());

    boost::shared_ptr<FileChangeListener> listener(new TestListener());

    // add app
    ret = processor -> add_app(appkey);
    std::cout << "add_app's ret = " << ret << std::endl;

    //add listener
    ret = processor -> addListener(filename, listener, appkey);
    std::cout << "addListener's ret = " << ret << std::endl;

    while(1)
    {

        // get API
        std::string res;
        res = processor -> get(filename, appkey);
        std::cout << "get config res: " << res << std::endl;
        sleep(20000);
    }
    sleep(2000);

    return 0;
}
