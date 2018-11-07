namespace cpp Controller
namespace java Controller

exception InvalidOperation {
    1: i32 whatOp,
    2: string why
}

enum Operation {
    TEST = 10000,
    INSTALL = 0,
    START = 1,
    STOP = 2,
    RESTART = 3,
    UPGRADE = 4,
    ROLLBACK = 5,
    REMOVE = 6
}

enum HealthEnum {
    Alive = 0,
    Dead = 1,
    Stop = 2
}

//machine location info
struct Location {
    1: required string region,
    2: optional string center,
    3: optional string idc
}
//department name and business in it
struct Department {
    1: required string owt,
    2: optional string pdl
}

struct Plugin {
    1: required string name,
    2: required string version,
    3: required string md5,
    4: optional i64 id
}
struct PluginHealth {
	1: required string name,
	2: required HealthEnum status
}
//plugin performance in machine
struct Performance {
    1: required string cpu,
    2: required string memory,
    3: required string io,
    4: required string network,
    5: optional string future
}
//plugin version info
struct PluginVersion {
    1: required string hostname,
    2: required string version,
    3: required string timestamp,
    4: optional string future
}

//plugin version info
struct CPluginInfo {
    1: required i32 cpu,
    2: required i32 mem,
    3: required string ver,
    4: required string startTime,
    5: required string timestamp,
}

//plugin moniter request
struct MoniterRequest {
    1: required string ip_addr,
    2: required map<string, string> agent_info,
    3: optional map<string, string> extend,
}

//plugin moniter response
struct MoniterResponse {
    1: required i32 ret,
    2: optional map<string, string> extend,
}

service ControllerService {
    /*
    *interface for MSGP*
    */
    i32 installPlugin(1:Plugin p, 2:list<string> ip_addr_list),
    i32 startPlugin(1:Plugin p, 2:list<string> ip_addr_list),
    i32 stopPlugin(1:Plugin p, 2:list<string> ip_addr_list),
    i32 restartPlugin(1:Plugin p, 2:list<string> ip_addr_list),
    //op:upgrade or rollback
    i32 handlePlugin(1:Operation op, 2:Plugin p, 3:string ip_addr) throws (1:InvalidOperation ouch),
    i32 handlePluginList(1:Operation op, 2:Plugin p, 3:list<string> ip_addr_list) throws (1:InvalidOperation ouch),
    i32 handlePluginUnion(1:Operation op, 2:Plugin p, 3:Department d, 4:Location l, 5:string env)  throws (1:InvalidOperation ouch),
    //get performance info for some plugin
    list<Performance> checkPerformance(1:Plugin p, 2:list<string> ip_addr_list),

    //get plugin version distribution
    list<PluginVersion> getVersionList(1:Plugin p, 2:list<string> ip_addr_list),
    list<PluginVersion> getVersionUnion(1:Plugin p, 2:Department d, 3:Location l),

    /*
    *interface for plugin_machine*
    */
    //PC report plugin version when it upgrade or rollback successed
    i32 reportVersion(1:string ip_addr, 2:i32 plugin_id, 3:i32 task_id, 4:string err_message),

    //regular check plugin version, determine whether to update plugin.
    //list<Plugin> regularCheckPlugin(1:string ip_addr, 2:list<Plugin> plugin_list),
    i32 regularCheckPlugin(1:string ip_addr, 2:list<Plugin> plugin_list),

    i32 reportHealth(1:string ip_addr, 2:list<PluginHealth> status),

    //report host location && cplugin info.
    i32 reportCpluginInfo(1:string ip_addr, 2:string hostname, 3:Location l, 4:CPluginInfo info),

    MoniterResponse reportMoniterInfo(1:MoniterRequest request)
}
