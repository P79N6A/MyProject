#include "net_util.h"

#include <unistd.h>
#include <errno.h>
#include <glog/logging.h>

namespace cplugin {

ssize_t ReadNBytes(int fd, void* vptr, size_t n) {
  size_t nleft = n;
  ssize_t nread  = 0;
  char* ptr = static_cast<char*>(vptr);
  
  while(nleft > 0) {
    if ((nread = read(fd, ptr, nleft)) < 0) {
      if (errno == EINTR) {
        nread = 0; // call read again
      } else {
        LOG(ERROR) << "Read error, errno is " << errno;
        return -1;
      }
    } else if (0 == nread) {
      break; // EOF
    }
    nleft -= nread;
    ptr += nread;
  }
  return n - nleft;
}

ssize_t WriteNBytes(int fd, const void* vptr, size_t n) {
  size_t nleft = n;
  ssize_t nwritten = 0;
  const char* ptr = static_cast<const char*>(vptr);

  while (nleft > 0) {
    if ((nwritten = write(fd, ptr, nleft)) <= 0) {
      if (nwritten < 0 && errno == EINTR) {
        nwritten = 0; // call write again
      } else {
        return -1;
      }
    }
    nleft -= nwritten;
    ptr += nwritten;
  }
  return n;
}

} // namespace core
