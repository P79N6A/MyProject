#include <iostream>
#include "file_config_client.h"
#include "file_config_listener.h"
#include "kv_config_client.h"
#include "global_config_listener.h"

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

class GlobalKVListener: public GlobalConfigChangeListener {
    public:
        virtual void OnEvent(
                std::string oriValue, std::string newValue) {
            std::cout << "Listener's event: "
                << "ori content = " << oriValue
                << "; new content = " << newValue << std::endl;
        }
};

int main()
{

    int ret = 0;
    cout << "start" << endl;

    std::string appkey = "com.sankuai.inf.mcc_test";
    std::string filename = "test.conf";
    std::string key = "key";

    // init file client
    boost::shared_ptr<FileConfigClient> processor(new FileConfigClient());

    boost::shared_ptr<FileChangeListener> listener(new TestListener());

    // add app
    ret = processor -> init(appkey);
    std::cout << "init's ret = " << ret << std::endl;

    // add listener
    ret = processor -> addListener(filename, listener);
    std::cout << "addListener's ret = " << ret << std::endl;

    // init kv client
    boost::shared_ptr<KVConfigClient> kvprocessor(new KVConfigClient());

    //boost::shared_ptr<ConfigChangeListener> kvlistener(new KVListener());
    boost::shared_ptr<GlobalConfigChangeListener> kvlistener(new GlobalKVListener());

    // add app
    ret = kvprocessor -> Init(appkey);
    std::cout << "init's ret = " << ret << std::endl;

    // add listener
    ret = kvprocessor -> AddGlobalListener(kvlistener);
    std::cout << "addGlobalListener's ret = " << ret << std::endl;
    while(1)
    {

        // get API
        std::string res;
        res = processor -> getFile(filename);
        std::cout << "get file res: " << res << std::endl;

        res = kvprocessor -> GetValue(key);
        std::cout << "get config res: " << res << std::endl;

        sleep(20000);
    }
    sleep(2000);

    return 0;
}
