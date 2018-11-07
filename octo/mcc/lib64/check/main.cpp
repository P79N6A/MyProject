#include <iostream>
#include "sg_agent_config_processor.h"
#include "sg_agent_fileconfig_processor.h"
#include "sg_agent_config_processor.h"
#include "file_config_listener.h"

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

    std::string appkey = "com.sankuai.inf.mcc_check";
    std::string filename = "test.conf";

    boost::shared_ptr<FileConfigProcessor> processor(new FileConfigProcessor());

    boost::shared_ptr<FileChangeListener> listener(new TestListener());

    // add app
    ret = processor -> add_app(appkey);
    std::cout << "add_app's ret = " << ret << std::endl;

    //add listener
    ret = processor -> addListener(filename, listener, appkey);
    std::cout << "addListener's ret = " << ret << std::endl;

    for (int i = 0; i < 100; i++) {
        // get API
        std::string res;
        res = processor -> get(filename, appkey);
        std::cout << "get config res: " << res << std::endl;
        sleep(2);
        break;
    }

    ConfigProcessor* processor_kv = ConfigProcessor::getInstance();

    ret = processor_kv -> add_app(appkey);
    std::cout << "add_app's ret = " << ret << std::endl;

    while(1)
    {
        // get API
        std::string res;
        res = processor_kv -> get("key0", appkey);
        std::cout << "get config res: key0 -> " << res << std::endl;

        sleep(5);
    }

    sleep(2000);

    return 0;
}
