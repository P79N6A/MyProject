#ifndef AUTO_LOCK_H_
#define	AUTO_LOCK_H_

#include <pthread.h>
namespace cplugin {

class AutoLock{
 public:
  AutoLock(pthread_mutex_t* lock) : lock_(lock) {
    pthread_mutex_lock(lock_);
  }

  ~AutoLock() {
    pthread_mutex_unlock(lock_);
  }

 private:
  pthread_mutex_t* lock_;
};

} // namespace cplugin 

#endif // AUTO_LOCK_H_
