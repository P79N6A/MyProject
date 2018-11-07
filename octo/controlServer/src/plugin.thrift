namespace java cplugin
namespace cpp cplugin

enum Action {
    TEST = 10000,
    INSTALL = 0,
    START = 1,
    STOP = 2,
    RESTART = 3,
    UPGRADE = 4,
    ROLLBACK = 5,
    REMOVE = 6
}

struct PluginAction {
    1: required string name,
    2: required string version,
    3: required string md5,
    4: required Action op,
	5: required i32 plugin_id,
	6: required i32 task_id
}
struct THostInfo {
1: required string name;
2: required string library_name;
3: required string hash;
4: optional string version;
}

struct TPluginInfo {
1: required string name;
2: required string library_name;
3: required string hash;
4: optional string version;
}

struct TInfos {
1: required THostInfo host_info;
2: required list<TPluginInfo> plugin_infos;
}

service Core {
i32 KeepAlive();
i32 Start(1:string plugin_name, 2:i32 plugin_id, 3:i32 task_id);
i32 ReStart(1:string plugin_name, 2:i32 plugin_id, 3:i32 task_id);
i32 Stop(1:string plugin_name, 2:i32 plugin_id, 3:i32 task_id);
i32 Remove(1:string plugin_name,  2:i32 plugin_id, 3:i32 task_id);
i32 Upgrade(1:string plugin_name, 2:string plugin_version, 3:i32 plugin_id, 4:i32 task_id);
i32 StartNew(1:string plugin_name, 2:string plugin_version, 3:i32 plugin_id, 4:i32 task_id);
i32 RollBack(1:string plugin_name, 2:string plugin_version, 3:i32 plugin_id, 4:i32 task_id);

map <string,TInfos> GetPluginInfos();
string GetVersion();
i32 notifyPluginAction(1:list<PluginAction> plugin_list)
}