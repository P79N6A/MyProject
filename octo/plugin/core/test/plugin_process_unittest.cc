#include <gtest/gtest.h>
#include <iostream>

#include "util/tinyxml2.h"
#define private public
#define protected public
#include "core/plugin_process.h"
#undef private
#undef protected

using namespace std;
using namespace core;
using namespace tinyxml2;

class PluginProcessTest : public testing::Test {
 public:
  PluginProcessTest() : plugin_process_(2888) {}
 protected:
  PluginProcess plugin_process_;
};

TEST_F(PluginProcessTest, Parse) {
  const string xml = "<version/>";
  XMLDocument doc;
  doc.Parse(xml.c_str());
  XMLElement* root = doc.RootElement();
  PluginInfo info;
  EXPECT_FALSE(plugin_process_.ParsePluginTree(root, &info));

  const string xml_plugin = "<Plugin Name=\"server\" LibraryName=\"libserver.so\" Hash=\"7E9D3354F42D84DEFACFA1575309B43C\"> \
                               <Children> \
                                 <Plugin Name=\"sgagent\" LibraryName=\"libsgagent_v2.so\" Hash=\"8E9D3354F42D84DEFACFA1575309B432\"></Plugin> \
                               </Children> \ 
                             </Plugin>";
  doc.Parse(xml_plugin.c_str());
  root = doc.RootElement();
  EXPECT_TRUE(plugin_process_.ParsePluginTree(root, &info));
}
