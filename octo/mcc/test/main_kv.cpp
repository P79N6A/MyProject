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

	//log_init(LL_DEBUG, "sg_agent_sdk", "/var/sankuai/logs/sg_agent/");
	log_init(LL_DEBUG, "sg_agent_sdk", "./logs/sg_agent/");

	int i;
	char _strbuf[200*1024];

	char *_log_str = "You got something wrong here";
	for(i = 0; i < 1; i++)
	{
		strncat(_strbuf, _log_str, strlen(_log_str));
	}
	//common_test();

	for(i = 0; i < 10; i++)
	{
		//sleep(1);
		if(0 == (i% 5))
			CLOG_NOTICE("%s [value:%d] [xx:%d]", _strbuf, i, 10);
		else if(1 == i % 5)
			CLOG_DEBUG("%s [value:%d]", _strbuf, i); 
		else if (2 == i % 5)
			CLOG_TRACE("%s [value:%d]", _strbuf, i);
		else if (3 == i % 5)
			CLOG_WARN("%s [value:%d]", _strbuf, i);
		else
			CLOG_ERROR("%s [value:%d]", _strbuf, i);
    }

    FileConfigClient fileClient;
    fileClient.init("test.notify");
    std::string strRet = fileClient.getFile("test");
    std::cout << "filecontent = " << strRet << std::endl;

    proc_conf_param_t conf;
    conf.appkey = "test.notify";
    conf.env = "prod";
    conf.path = "/test1/aaa";

    boost::shared_ptr<ConfigProcessor> processor(new ConfigProcessor());
    //int ret = processor -> init(conf.appkey, conf.env, conf.path);
    //std::cout << "init ret = " << ret << std::endl;

    while(1)
    {
        // add app
        ret = processor -> add_app(conf.appkey);
        std::cout << "add_app's ret = " << ret << std::endl;
        // get API
        std::string res;
        res = processor -> get("bbb", conf.appkey);
        std::cout << "get config res: bbb -> " << res << std::endl;

        // set API
        ret = processor -> set("bbb", "bbbb", conf.appkey);
        std::cout << "set config ret: " << ret << std::endl;

        // get API
        res = processor -> get("bbb", conf.appkey);
        std::cout << "get config res: bbb -> " << res << std::endl;

        // add app
        ret = processor -> add_app("com.sankuai.inf.sg_agent");
        std::cout << "add_app's ret = " << ret << std::endl;
        // get API
        res = processor -> get("version", conf.appkey);
        std::cout << "get config res: version -> " << res << std::endl;
        sleep(2);
        break;
    }

    return 0;
}
