#include "log4cplus.h"

//root Logger
Logger root = log4cplus::Logger::getRoot();

//non root Logger
Logger stat_instance = log4cplus::Logger::getInstance(LOG4CPLUS_TEXT("statLogger"));
