#include <string>

#ifndef __GLOBAL_CONFIG_LISTENER_H__
#define __GLOBAL_CONFIG_LISTENER_H__

class GlobalConfigChangeListener {
    public:
        virtual void OnEvent(
                    std::string oriValue, std::string newValue) = 0;
};

#endif
