#ifndef NET_UTIL_H_
#define NET_UTIL_H_

#include <errno.h>
#include <sys/types.h>

namespace cplugin {

ssize_t ReadNBytes(int fd, void* vptr, size_t n); 

ssize_t WriteNBytes(int fd, const void* vptr, size_t n); 

} // namespace cplugin

#endif // NET_UTIL_H_
