#include <string>
#ifdef __cplusplus
extern "C"
{
#endif
    typedef int (*FileChange)(std::string filename, std::string oriFile, std::string newFile);
    int Process(FileChange cb, std::string filename, std::string oriFile, std::string newFile);
#ifdef __cplusplus
}
#endif
