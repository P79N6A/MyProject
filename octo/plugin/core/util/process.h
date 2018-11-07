#ifndef SETPROCTITLE_H_ 
#define SETPROCTITLE_H_

#include <unistd.h>
#include <sys/types.h>

namespace cplugin {

void InitProcTitle(int argc, char** argv);
void SetProcTitle(const char* title);

void CloseParentFd(int fd);

void GetPidByNameAndKill(const char* name);

char GetCentOSVersion();

void SavePid_X(const char* name);

void getErrorForSystemStatus(pid_t status);

} // namespace cplugin

#endif // SETPROCTITLE_H_
