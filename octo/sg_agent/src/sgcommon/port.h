#ifndef PORT_H_
#define PORT_H_

#include <stdint.h>
#include <unistd.h>

#define OUTAPI      extern "C" __attribute__((visibility("default")))

#define LOCALAPI    __attribute__((visibility("hidden")))

typedef void* HANDLE;

#endif
