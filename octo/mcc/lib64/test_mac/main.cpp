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

    //std::string appkey = "com.sankuai.inf.msgp";
    //std::string filename = "msgp.conf";
    //std::string appkey = "com.sankuai.inf.testMtConfig";
    //std::string filename = "test.conf";
    //std::string appkey = "com.sankuai.inf.mcc_check";
    //std::string appkey = "com.sankuai.inf.msgp";
    std::string appkey_agent = "com.sankuai.inf.sg_agent";
    //std::string appkey = "com.sankuai.waimai.rabbitmqplugin";
    std::string filename = "test.conf";
    cout << "input appkey: " << endl;
    cin >> appkey_agent;
    cout << "input filename: " << endl;
    cin >> filename;
    //filename = "settings.xml";

    boost::shared_ptr<FileConfigProcessor> processor(new FileConfigProcessor());

    boost::shared_ptr<FileChangeListener> listener(new TestListener());

    // add app
    ret = processor -> add_app(appkey_agent);
    std::cout << "add_app's ret = " << ret << std::endl;

    //add listener
    ret = processor -> addListener(filename, listener, appkey_agent);
    std::cout << "addListener's ret = " << ret << std::endl;

    while(1)
    {

        // get API
        std::string res;
        res = processor -> get(filename, appkey_agent);
        std::cout << "get config res: " << res << std::endl;
        sleep(2);
        break;
    }

    //boost::shared_ptr<ConfigProcessor> processor_kv(new ConfigProcessor());

    /**
    ConfigProcessor* processor_kv = ConfigProcessor::getInstance();

    ret = processor_kv -> add_app(appkey);
    std::cout << "add_app's ret = " << ret << std::endl;

    ret = processor_kv -> add_app(appkey_agent);
    std::cout << "add_app's ret = " << ret << std::endl;

    while(1)
    {
        // get API
        std::string res;
        res = processor_kv -> get("msgp.config.admin", appkey);
        std::cout << "get config res: msgp.config.admin -> " << res << std::endl;
        sleep(1);

        res = processor_kv -> get("key", appkey_agent);
        std::cout << "get config res: key -> " << res << std::endl;

        sleep(1);
    }
    */

    sleep(2);

    return 0;
}
