#ifndef __MCC_COMM_HELPER_H_
#define __MCC_COMM_HELPER_H_
#include <sys/stat.h>
#include <sys/types.h>

static const int MAX_PATH_LEN = 512;
static int mkdirs(const char* muldir)
{
    if (NULL == muldir)
    {
        return -1;
    }
    int len = strlen(muldir);
    if (MAX_PATH_LEN <= strlen(muldir))
    {
        return -1;
    }

    char path[MAX_PATH_LEN];
    strncpy(path, muldir, len);
    path[len] = '\0';
    for(int i = 0; i < len; i++)
    {
        if('/' == muldir[i] && 0 != i)
        {
            path[i] = '\0';
            if(access(path, 0)!=0)
            {
                int ret = mkdir(path, S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);
            }
            path[i]='/';
        }
    }
    if(len>0 && access(path,0)!=0)
    {
        int ret = mkdir(path, S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);
        //mkdir(path, S_IREAD | S_IWRITE);
    }
    return 0;
}

#endif
