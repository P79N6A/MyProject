#include <iostream>
#include "sg_agent_config_processor.h"


using namespace std;

int main() {
    cout << "start" << endl;

    std::string appkey = "com.sankuai.inf.mcc_test";
    std::string env = "stage";
    std::string path = "/";

    int ret = ConfigProcessor::getInstance()
        -> add_app(appkey, env, path);
    std::cout << "init ret: " << ret << std::endl;

    std::string value = ConfigProcessor::getInstance()
        -> get("key", appkey, env, path);
    std::cout << "get value: " << value << std::endl;

    ret = ConfigProcessor::getInstance()
        -> set("key", "211", appkey, env, path);
    std::cout << "get ret: " << ret << std::endl;

    value = ConfigProcessor::getInstance()
        -> get("key", appkey, env, path);
    std::cout << "get value: " << value << std::endl;

    //std::string value = ConfigProcessor::getInstance() -> get("bbb");
    //std::cout << "get value: " << value << std::endl;

    /*
    //SGAgentClient client(protocol);
    int ret = sg_agent_config_init();
    std::cout << "init ret = " << ret << std::endl;

    // get API
    std::string res;
    res = sg_agent_config_getConfig(conf.appkey, conf.env, conf.path);
    std::cout << "get config res: " << res << std::endl;

    // set API
    conf.__set_conf("{\"octo\":\"222\",\"xzj_test\":\"bbb\",\"bbb\":\"bbb\",\"test\":\"test1\"}");
    ret = sg_agent_config_setConfig(conf.appkey, conf.env, conf.path, conf.conf);
    std::cout << "set ret: " << ret << std::endl;
    */

    //test update API
    /*
    ConfigNode node;
    node.__set_appkey("test.notify");
    node.__set_env("prod");
    //node.__set_path("/test1/aaa");
    node.__set_path("/test1/aaa");

    std::vector<ConfigNode> nodes;
    nodes.push_back(node);
    ConfigUpdateRequest req;
    req.__set_nodes(nodes);
    std::cout << "nodes' size = " << nodes.size() << std::endl;

    ret = client -> updateConfig(req);
    std::cout << "update ret: " << ret << std::endl;
    */

    //sg_agent_config_destroy();;
    return 0;
}
