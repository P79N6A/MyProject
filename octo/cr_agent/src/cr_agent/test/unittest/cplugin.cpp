#include <sys/prctl.h>
#include <pthread.h>
#include <unistd.h>
#include <iostream>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <netdb.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string>
#include <vector>
#include <map>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/select.h>
#include <sys/time.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/epoll.h>
#include <errno.h>
#include <fcntl.h>
#include <dlfcn.h>
#define DLL_FILE_NAME "/opt/meituan/apps/cr_agent/libcragent.so"

#include "../../../plugin/plugindef.h"
typedef void *HANDLE;
int main(int argc, char *argv[]) {
	typedef bool (*InitTest_A)(const std::map<std::string, HANDLE>,
			const std::vector<PluginInfo>);
	void *handle;
	char *error;

	handle = dlopen(DLL_FILE_NAME, RTLD_NOW);
	if (handle == NULL) {
		fprintf(stderr, "Failed to open libaray %s error:%s\n", DLL_FILE_NAME, dlerror());
		return -1;
	} else {

		printf("打开成功");
	}

	std::map<std::string, HANDLE> pre_map;
	std::vector<PluginInfo> pre_cplugin;
	InitTest_A a = reinterpret_cast<InitTest_A>(dlsym(handle, "InitHost"));

	if (NULL == a) {
		printf("\r\n this is the %s", dlerror());
		printf("\r\n this is func failed \r\n");

	} else {
		printf("\r\n sucess the func dlsym");
		a(pre_map, pre_cplugin);

	}
	while (1) {
		sleep(5);
	}
}

