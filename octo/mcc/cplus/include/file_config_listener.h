#include <string>

#ifndef __FILE_CONFIG_LISTENER_H__
#define __FILE_CONFIG_LISTENER_H__

class FileChangeListener {
    public:
        virtual void OnEvent(std::string filename,
                    std::string oriFile, std::string newFile) = 0;
};

#endif
