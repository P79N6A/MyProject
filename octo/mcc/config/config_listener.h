#include <string>

#ifndef __CONFIG_LISTENER_H__
#define __CONFIG_LISTENER_H__

class ConfigChangeListener {
    public:
        virtual void OnEvent(std::string key,
                    std::string oriValue, std::string newValue) = 0;
};

#endif
