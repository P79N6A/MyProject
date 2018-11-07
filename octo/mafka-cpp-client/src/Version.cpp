#include "Version.h"
#include <string.h>

const char* GetVersion()
{
	return VERSION;
}


const int LANGUAGE_LENGTH = 256;
static char g_language[LANGUAGE_LENGTH] = {'c', 'p', 'p', '\0'};

void SetLanguage(const char* lang)
{
	strncpy(g_language, lang, LANGUAGE_LENGTH-1);
	g_language[LANGUAGE_LENGTH-1] = '\0';
}

const char* GetLanguage()
{
	return g_language;
}

