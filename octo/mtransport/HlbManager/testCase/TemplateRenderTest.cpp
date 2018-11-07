
#include <iostream>
#include <sys/time.h>

#include "../hlb_gen_cpp/quota_common_types.h"
#include "../TemplateRender.h"

using namespace std;
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;

using namespace boost;
using namespace std;
using namespace inf::hlb;

int main(int argc, char** argv) {
  cout<<"============= TEST TemplateRender =================\n";
  string appkey= "com.sankuai.inf.jinluTestHTTP";
  DegradeAction action;
  action.__set_id( "1234567890");
  action.__set_env(3);
  action.__set_providerAppkey( "com.sankuai.inf.jinluTestHTTP");
  action.__set_consumerAppkey( "com.sankuai.inf.jinluTestHTTP2222");
  action.__set_method( "all");
  action.__set_degradeRatio(0.7);
  DegradeStrategy::type degradeStrategy = (DegradeStrategy::type)0;
  action.__set_degradeStrategy( degradeStrategy);
  action.__set_timestamp(1446550645597);
  action.__set_degradeRedirect("");
  DegradeEnd::type end = (DegradeEnd::type)0;
  action.__set_degradeEnd( end);

  TemplateRender* render = new TemplateRender();
  std::cout <<"\n===== getDegradeLua =====\n"
      << render->getDegradeLua( appkey, action)
      <<"\n===== getDegradeLua END =====\n";

  std::cout <<"\n===== getNormalLua =====\n"
      << render->getNormalLua( appkey)
      <<"\n===== getNormalLua END =====\n";

  return 0;
}
