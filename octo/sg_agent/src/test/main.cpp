#include <gtest/gtest.h>
#include <core/hostimpl.h>
//#include <gmock/gmock.h>
extern bool g_is_test;

int main(int argc, char **argv) {
  //testing::InitGoogleMock(&argc, argv);
  g_is_test = true;

  std::map<std::string, HANDLE> preload_map;
  std::vector<PluginInfo> plugins;
  PluginInfo info;
  info.library_name = "libmns.so";
  plugins.push_back(info);
  int ret = InitHost(preload_map, plugins);
  std::cout << "init sg_agent host, ret = " << ret << std::endl;
  testing::InitGoogleTest(&argc, argv);
  sleep(15);
  RUN_ALL_TESTS();
  return 0;
}