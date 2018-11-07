#include <stdio.h>
#include <sys/timeb.h>
#include <sys/time.h>
#include <stdlib.h>
#include <signal.h>
#include <unistd.h>
#include <pthread.h>
#if defined(WIN32)
# define  TIMEB    _timeb
# define  ftime    _ftime
#else
#define TIMEB timeb
#endif

long long count = 1;
pthread_mutex_t *mutex;
pthread_t m_pid;
bool is_threadsafe = true;
void threadsafe(){
    if(mutex){
        pthread_mutex_destroy(mutex);
        free(mutex);
        mutex = NULL;
    }
    mutex = (pthread_mutex_t *)malloc(sizeof(pthread_mutex_t));
    pthread_mutex_init(mutex, NULL);
}

int time_interval()
{
    struct TIMEB ts1,ts2;
    time_t t_sec,ti,t_ms;
    ftime(&ts1);//开始计时
   
    {
        int i;
        for(i=0;i<10;i++)
        {
            void *p=malloc(10000);
            free(p);
        }
    }

    ftime(&ts2);//停止计时
    t_sec=ts2.time-ts1.time;//计算秒间隔
    t_ms=ts2.millitm-ts1.millitm;//计算毫秒间隔
    ti=t_sec*1000+t_ms;

    return ti;
}

void proc1(int signo){
   printf("count = %lld %d\n",count,pthread_self()); 
   if (mutex) pthread_mutex_lock(mutex);
   count = 1; 
   if (mutex) pthread_mutex_unlock(mutex);
   printf("after proc1 count = %lld %d\n",count,pthread_self()); 
}

void* setTimer(void* arg){
    signal(SIGALRM,proc1);
    itimerval timerval;
    timerval.it_interval.tv_sec  = timerval.it_value.tv_sec = 1;
    timerval.it_interval.tv_usec = timerval.it_value.tv_usec = 0;
    setitimer(ITIMER_REAL, &timerval, NULL);
    return NULL;
}

int main()
{
    int ti=time_interval();
    if (is_threadsafe) 
        threadsafe();
    printf("time interval=%d %lu \n",ti,pthread_self());
    pthread_create(&m_pid,NULL,setTimer,NULL);
    while(1){
        //if (mutex) pthread_mutex_lock(mutex);
        count++;
        //if (mutex) pthread_mutex_unlock(mutex);
    }
    
    return 0;
}
