#include "call_back.h"


int Process(FileChange cb, std::string filename, std::string oriFile, std::string newFile) {
    if(!cb) {
        return -1;
    }

    cb(filename, oriFile, newFile);
    return 0;
}
