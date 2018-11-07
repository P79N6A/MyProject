//
// Created by root on 8/15/17.
//

#ifndef CRANE_SRC_PLUGINDEF_H
#define CRANE_SRC_PLUGINDEF_H

#include <string>

typedef void* (*CreateFunc)();
typedef void (*DestroyFunc)(void*);

#define OUTAPI  extern "C" __attribute__((visibility("default")))

#define LOCALAPI    __attribute__((visibility("hidden")))

struct HostInfo {
    std::string name;
    std::string library_name;
    std::string hash;
};

struct PluginInfo {
    std::string name;
    std::string library_name;
    std::string hash;
};

#endif //CRANE_SRC_PLUGINDEF_H
