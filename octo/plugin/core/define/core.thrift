
namespace cpp cplugin 

struct THostInfo {
  1: required string name;
  2: required string library_name;
  3: required string hash;
}

struct TPluginInfo {
  1: required string name;
  2: required string library_name;
  3: required string hash;
}

struct TInfos {
  1: required THostInfo host_info;
  2: required list<TPluginInfo> plugin_infos;
}

service Core {
  i32 DownloadConfig(1:string version);
  i32 Start()
  i32 Stop()
  i32 Upgrade()
  i32 StartAfterStoppingSgagent()
  i32 StopCPluginAndStartSgagent()

  void HeartBeat()
  string GetSgagentInfos()
  string GetVersion()
}
