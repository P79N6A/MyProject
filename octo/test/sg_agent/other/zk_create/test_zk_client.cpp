#include "test_zk_client.h"

#define SAFE_FREE(p) { if(p) { free(p); (p)=NULL; } }
#define MAX_BUF_SIZE 1024

ZkClientOperation::ZkClientOperation():timeout_(30000), zk_handler_(NULL), server_("192.168.4.252:2181"),base_zkpath_("/mns/sankuai/stage"), appkey_("com.sankuai.inf.testcase")
{
}

ZkClientOperation::~ZkClientOperation()
{
}

int ZkClientOperation::deinit()
{
    return closeZk();
}

int ZkClientOperation::init(const string& appkey)
{
    appkey_ = appkey;
    return connectToZk();
}

int ZkClientOperation::setZNode(const SGService& oservice)
{
    string strJson;
    SGService oTmp = const_cast<SGService&>(oservice);
    _SGService2Json(oTmp, strJson);

    //zookeeper path
    char newpath[MAX_BUF_SIZE] = {0};
    snprintf(newpath, sizeof(newpath), "%s/%s/provider/%s:%d",
            base_zkpath_.c_str(), appkey_.c_str(), oservice.ip.c_str(), oservice.port);

    //create or update
    struct Stat stat;
    int ret = zoo_exists(zk_handler_, newpath, 0, &stat);
    if(ret) {
        ret = zoo_create(zk_handler_, newpath, strJson.c_str(), strJson.size(), &ZOO_OPEN_ACL_UNSAFE, 0,  NULL, 0);
        if (ret) {
            cout << "Error for create" << endl;
        }
    }
    else {
        ret = zoo_set(zk_handler_, newpath, strJson.c_str(), strJson.size(), -1);
        if(ret)
        {
            cout << "Error for set" << endl;
        }
    } 

    //update service node last modified time
    CProviderNode oprovider;
    oprovider.appkey = oservice.appkey;
    oprovider.lastModifiedTime = time(NULL);
    setProvider(oprovider);
    return ret;
}

int ZkClientOperation::setProvider(const CProviderNode &oprovider)
{
    char zkPath[MAX_BUF_SIZE] = {0};
    snprintf(zkPath, sizeof(zkPath), "%s/%s/provider", base_zkpath_.c_str(), appkey_.c_str());
    
    string strJson = "";
    _ProviderNode2Json(oprovider, strJson);
    int ret = zoo_set(zk_handler_, zkPath, strJson.c_str(), strJson.size(), -1);
    if(ret)
    {
        cout << "ERR zoo_set provider failed " << endl;
    }
    return ret;
}

int ZkClientOperation::getProvider(CProviderNode &oprovider)
{
    char zkPath[MAX_BUF_SIZE] = {0};
    snprintf(zkPath, sizeof(zkPath), "%s/%s/provider",
             base_zkpath_.c_str(), appkey_.c_str());
    
    char buff[1024] = {0};
    int datalen = sizeof(buff);
    struct Stat stat;

    int ret = zoo_get(zk_handler_, zkPath, 0, buff, &datalen, &stat);
    if(ret)
    {
        cout << "ERR zoo_get provider fail: " << ret << " , zkpath : " << zkPath;
        return -1;
    }
    
    string strJson = buff;
    //stat.mtime用来表示zk中当前节点版本信息，用于判断是否有更新
    if(_Json2ProviderNode(strJson, stat.mtime, stat.version, stat.cversion,oprovider) < 0)
    {
        return -1;
    }

    return ret;
}


int ZkClientOperation::getZNode(vector<SGService> &serviceList)
{
    struct String_vector strings;
    string newpath = base_zkpath_ + "/" + appkey_ + "/provider";
    int ret = zoo_wget_children(zk_handler_, newpath.c_str(), NULL, 0, &strings);
    if(ZOK != ret)
    {
        cout << "zoo_wget_children fail, ret = " << ret << endl;
        return -2;
    }
    else if (strings.count == 0) {
        cout << "serviceList is empty" << endl;
        return -3;
    }

    struct Stat stat;
    char buff[MAX_BUF_SIZE] = {0};
    int datalen = sizeof(buff);
    for(int i = 0; i < strings.count; i++)
    {
        string provide = newpath + "/" + strings.data[i];
        memset(buff, 0, sizeof(buff));
        datalen = sizeof(buff);
        ret = zoo_get(zk_handler_, provide.c_str(), 0, buff, &datalen, &stat);
        if(ZOK != ret)
        {
            cout << "zoo_get fail: " << ret << ", zkpath: " << newpath << endl;
            deallocate_String_vector(&strings);
            return -1;
        }

        if(NULL == buff || 0 == datalen)
        {
            continue;
        }

        string strJson = buff;
        SGService oservice;
        if(_Json2SGService(strJson, oservice) < 0)
        {
            continue;
        }
        serviceList.push_back(oservice);
    }
    return 0;
}

int ZkClientOperation::deleteZNode(const SGService& oservice)
{

    char newpath[MAX_BUF_SIZE] = {0};
    snprintf(newpath, sizeof(newpath), "%s/%s/provider/%s:%d",
            base_zkpath_.c_str(), appkey_.c_str(), oservice.ip.c_str(), oservice.port);
    int ret = zoo_delete(zk_handler_, newpath, -1);
    if(ret) {
        cout << "Error for deleteZNode";
        exit(EXIT_FAILURE);
    }

    return ret;
}

int ZkClientOperation::connectToZk()
{
    //closeZk();

    //set log level.//
    zoo_set_debug_level(ZOO_LOG_LEVEL_WARN);

    int count = 0;
    int state = 5;
    do{
        cout << "start to connect ZK, count : " << count << endl;
        ++count;
        zk_handler_ = zookeeper_init(server_.c_str(), ZkClientOperation::connWatcher, timeout_, 0, NULL, 0);
        if(zk_handler_ == NULL){
            cout << "zookeeper_init return null with ip: " << server_;
            return -1;
        }
        sleep(1);

        state = zoo_state(zk_handler_);

    }while(state != ZOO_CONNECTED_STATE && count < 5);

    return state == ZOO_CONNECTED_STATE ? 0 : -2;
}

int ZkClientOperation::closeZk()
{
    if(zk_handler_)
    {
        cout << "close ZK connection !" << endl;
        zookeeper_close(zk_handler_);
        zk_handler_ = NULL;
    }
    return 0;
}

int ZkClientOperation::checkZk()
{
    int state = zoo_state(zk_handler_);
    if(ZOO_CONNECTED_STATE != state) {
        cout << "Error zk connection lost !" << endl;
        return -1;
    }
    return 0;
}

void ZkClientOperation::connWatcher(zhandle_t *zh, int type, int state,
        const char *path, void *watcher_ctx)
{
    if(ZOO_CONNECTED_STATE == state) {
        cout << "connWatcher() ZOO_CONNECTED_STATE";
    }
    else if (ZOO_AUTH_FAILED_STATE == state) {
        cout << "connWatcher() ZOO_AUTH_FAILED_STATE" << endl;
    } else if (ZOO_EXPIRED_SESSION_STATE == state) {
        cout << "connWatcher() ZOO_EXPIRED_SESSION_STATE" << endl;
    } else if (ZOO_CONNECTING_STATE == state) {
        cout << "connWatcher() ZOO_CONNECTING_STATE" << endl;
    } else if (ZOO_ASSOCIATING_STATE == state) {
        cout << "connWatcher() ZOO_ASSOCIATING_STATE" << endl;
    }
}
int ZkClientOperation::_Json2SGService(const std::string& strJson, SGService& oservice)
{
    cJSON* root = cJSON_Parse(strJson.c_str());
    if (NULL == root)
    {
        return -1;
    }
    oservice.appkey = cJSON_GetObjectItem(root, "appkey")->valuestring;
    oservice.version = cJSON_GetObjectItem(root, "version")->valuestring;
    oservice.ip = cJSON_GetObjectItem(root, "ip")->valuestring;
    oservice.port = cJSON_GetObjectItem(root, "port")->valueint;
    oservice.weight = cJSON_GetObjectItem(root, "weight")->valueint;
    oservice.status = cJSON_GetObjectItem(root, "status")->valueint;
    oservice.role = cJSON_GetObjectItem(root, "role")->valueint;
    oservice.envir = cJSON_GetObjectItem(root, "env")->valueint;
    oservice.lastUpdateTime = cJSON_GetObjectItem(root, "lastUpdateTime")->valueint;
    oservice.extend = cJSON_GetObjectItem(root, "extend")->valuestring;

    cJSON_Delete(root);
    return 0;
}


int ZkClientOperation::_SGService2Json(const SGService& oservice, std::string& strJson)
{
    cJSON *root;
    char* out;
    root = cJSON_CreateObject();
    cJSON_AddItemToObject(root, "appkey", cJSON_CreateString(oservice.appkey.c_str()));
    cJSON_AddItemToObject(root, "version", cJSON_CreateString(oservice.version.c_str()));
    cJSON_AddItemToObject(root, "ip", cJSON_CreateString(oservice.ip.c_str()));
    cJSON_AddNumberToObject(root, "port", oservice.port);
    cJSON_AddNumberToObject(root, "weight", oservice.weight);
    cJSON_AddNumberToObject(root, "status", oservice.status);
    cJSON_AddNumberToObject(root, "role", oservice.role);
    cJSON_AddNumberToObject(root, "env", oservice.envir);
    cJSON_AddNumberToObject(root, "lastUpdateTime", oservice.lastUpdateTime);
    cJSON_AddItemToObject(root, "extend", cJSON_CreateString(oservice.extend.c_str()));

    out = cJSON_Print(root);
    strJson = out;

    SAFE_FREE(out);
    cJSON_Delete(root);

    return 0;
}

int ZkClientOperation::_ProviderNode2Json(const CProviderNode& oprovider, std::string& strJson)
{
    cJSON *root;
    char* out;
    root = cJSON_CreateObject();
    cJSON_AddItemToObject(root, "appkey", cJSON_CreateString(oprovider.appkey.c_str()));
    cJSON_AddNumberToObject(root, "lastUpdateTime", oprovider.lastModifiedTime);
    
    out = cJSON_Print(root);
    strJson = out;
    
    SAFE_FREE(out);
    cJSON_Delete(root);
    
    return 0;
}

int ZkClientOperation::_Json2ProviderNode(const std::string& strJson, int mtime, int version, int cversion, CProviderNode& oprovider)
{
    //CHECK_JSON(strJson)
    cJSON* root = cJSON_Parse(strJson.c_str());
    if (NULL == root)
    {
        return -1;
    }
    oprovider.appkey = cJSON_GetObjectItem(root, "appkey")->valuestring;
    oprovider.mtime = mtime;
    oprovider.cversion = cversion;
    oprovider.version = version;
    oprovider.lastModifiedTime = cJSON_GetObjectItem(root, "lastUpdateTime")->valueint;
    cJSON_Delete(root);
    return 0;
}
