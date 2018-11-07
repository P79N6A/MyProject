#include "call_back.h"
#include <stdio.h>

int WanporProcess(int i)
{
    printf("Process = %d\n",i);
    return i;
}

int main(int argc, char* argv[])
{
    Process(WanporProcess,100);
    return 0;
}
