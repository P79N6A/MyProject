/*
  +----------------------------------------------------------------------+
  | PHP Version 5                                                        |
  +----------------------------------------------------------------------+
  | Copyright (c) 1997-2010 The PHP Group                                |
  +----------------------------------------------------------------------+
  | This source file is subject to version 3.01 of the PHP license,      |
  | that is bundled with this package in the file LICENSE, and is        |
  | available through the world-wide-web at the following url:           |
  | http://www.php.net/license/3_01.txt                                  |
  | If you did not receive a copy of the PHP license and are unable to   |
  | obtain it through the world-wide-web, please send a note to          |
  | license@php.net so we can mail you a copy immediately.               |
  +----------------------------------------------------------------------+
  | Author:                                                              |
  +----------------------------------------------------------------------+
*/

/* $Id: header 297205 2010-03-30 21:09:07Z johannes $ */

extern "C"{ 
#ifdef HAVE_CONFIG_H
#include "config.h"
#endif

#include "php.h"
#include "php_ini.h"
#include "ext/standard/info.h"
#include "php_sg_agent_sdk.h"
}

#include "php_sg_agent_config_client.h"
#include "sg_agent_config_processor.h"
#include <string>
#include <iostream>
using namespace std;


/* If you declare any globals in php_sg_agent_sdk.h uncomment this:
ZEND_DECLARE_MODULE_GLOBALS(sg_agent_sdk)
*/

/* True global resources - no need for thread safety here */
static int le_sg_agent_sdk;

/* {{{ sg_agent_sdk_functions[]
 *
 * Every user visible function must have an entry in sg_agent_sdk_functions[].
 */
const zend_function_entry sg_agent_sdk_functions[] = {
	//PHP_FE(confirm_sg_agent_sdk_compiled,	NULL)		/* For testing, remove later. */
	//PHP_FE(sg_agent_config_getConfig,	NULL)
	//PHP_FE(sg_agent_config_setConfig,	NULL)
	//PHP_FE(sg_agent_config_init,	NULL)
	PHP_FE(sg_agent_config_add_app,	NULL)
	PHP_FE(sg_agent_config_get,	NULL)
	PHP_FE(sg_agent_config_set,	NULL)
	{NULL, NULL, NULL}	/* Must be the last line in sg_agent_sdk_functions[] */
};
/* }}} */

/* {{{ sg_agent_sdk_module_entry
 */
zend_module_entry sg_agent_sdk_module_entry = {
#if ZEND_MODULE_API_NO >= 20010901
	STANDARD_MODULE_HEADER,
#endif
	"sg_agent_sdk",
	sg_agent_sdk_functions,
	PHP_MINIT(sg_agent_sdk),
	PHP_MSHUTDOWN(sg_agent_sdk),
	PHP_RINIT(sg_agent_sdk),		/* Replace with NULL if there's nothing to do at request start */
	PHP_RSHUTDOWN(sg_agent_sdk),	/* Replace with NULL if there's nothing to do at request end */
	PHP_MINFO(sg_agent_sdk),
#if ZEND_MODULE_API_NO >= 20010901
	"0.1", /* Replace with version number for your extension */
#endif
	STANDARD_MODULE_PROPERTIES
};
/* }}} */

#ifdef COMPILE_DL_SG_AGENT_SDK
ZEND_GET_MODULE(sg_agent_sdk)
#endif

/* {{{ PHP_INI
 */
/* Remove comments and fill if you need to have entries in php.ini
PHP_INI_BEGIN()
    STD_PHP_INI_ENTRY("sg_agent_sdk.global_value",      "42", PHP_INI_ALL, OnUpdateLong, global_value, zend_sg_agent_sdk_globals, sg_agent_sdk_globals)
    STD_PHP_INI_ENTRY("sg_agent_sdk.global_string", "foobar", PHP_INI_ALL, OnUpdateString, global_string, zend_sg_agent_sdk_globals, sg_agent_sdk_globals)
PHP_INI_END()
*/
/* }}} */

/* {{{ php_sg_agent_sdk_init_globals
 */
/* Uncomment this function if you have INI entries
static void php_sg_agent_sdk_init_globals(zend_sg_agent_sdk_globals *sg_agent_sdk_globals)
{
	sg_agent_sdk_globals->global_value = 0;
	sg_agent_sdk_globals->global_string = NULL;
}
*/
/* }}} */

/* {{{ PHP_MINIT_FUNCTION
 */
PHP_MINIT_FUNCTION(sg_agent_sdk)
{
	/* If you have INI entries, uncomment these lines 
	REGISTER_INI_ENTRIES();
	*/
	return SUCCESS;
}
/* }}} */

/* {{{ PHP_MSHUTDOWN_FUNCTION
 */
PHP_MSHUTDOWN_FUNCTION(sg_agent_sdk)
{
	/* uncomment this line if you have INI entries
	UNREGISTER_INI_ENTRIES();
	*/
	return SUCCESS;
}
/* }}} */

/* Remove if there's nothing to do at request start */
/* {{{ PHP_RINIT_FUNCTION
 */
PHP_RINIT_FUNCTION(sg_agent_sdk)
{
	return SUCCESS;
}
/* }}} */

/* Remove if there's nothing to do at request end */
/* {{{ PHP_RSHUTDOWN_FUNCTION
 */
PHP_RSHUTDOWN_FUNCTION(sg_agent_sdk)
{
	return SUCCESS;
}
/* }}} */

/* {{{ PHP_MINFO_FUNCTION
 */
PHP_MINFO_FUNCTION(sg_agent_sdk)
{
	php_info_print_table_start();
	php_info_print_table_header(2, "sg_agent_sdk support", "enabled");
	php_info_print_table_end();

	/* Remove comments if you have entries in php.ini
	DISPLAY_INI_ENTRIES();
	*/
}
/* }}} */


/* Remove the following function when you have succesfully modified config.m4
   so that your module can be compiled into PHP, it exists only for testing
   purposes. */

/* Every user-visible function in PHP should document itself in the source */
/* {{{ proto string confirm_sg_agent_sdk_compiled(string arg)
   Return a string to confirm that the module is compiled in */
//PHP_FUNCTION(confirm_sg_agent_sdk_compiled)
//{
//	char *arg = NULL;
//	int arg_len, len;
//	char *strg;
//
//	if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "s", &arg, &arg_len) == FAILURE) {
//		return;
//	}
//
//	len = spprintf(&strg, 0, "Congratulations! You have successfully modified ext/%.78s/config.m4. Module %.78s is now compiled into PHP.", "sg_agent_sdk", arg);
//	RETURN_STRINGL(strg, len, 0);
//}
/* }}} */
/* The previous line is meant for vim and emacs, so it can correctly fold and 
   unfold functions in source code. See the corresponding marks just before 
   function definition, where the functions purpose is also documented. Please 
   follow this convention for the convenience of others editing your code.
*/


/*
 * Local variables:
 * tab-width: 4
 * c-basic-offset: 4
 * End:
 * vim600: noet sw=4 ts=4 fdm=marker
 * vim<600: noet sw=4 ts=4
 */

//PHP_FUNCTION(sg_agent_config_getConfig)
//{
//    int arg_len;
//    
//    char* appkey;
//    int appkey_len;
//    char* env;
//    int env_len;
//    char* path; 
//    int path_len;
//
//    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "sss",
//        &appkey, &appkey_len,
//        &env, &env_len,
//        &path, &path_len) == FAILURE) {
//        return;
//    }
//    std::string s_appkey = appkey;
//    std::string s_env = env;
//    std::string s_path = path;
//    std::string ret = sg_agent_config_getConfig(s_appkey, s_env, s_path);
//
//    RETURN_STRINGL(ret.c_str(), strlen(ret.c_str()), 0);
//}
//
//PHP_FUNCTION(sg_agent_config_setConfig)
//{
//    int arg_len;
//    
//    char* appkey;
//    int appkey_len;
//    char* env;
//    int env_len;
//    char* path; 
//    int path_len;
//    char* conf; 
//    int conf_len;
//
//    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "ssss",
//        &appkey, &appkey_len,
//        &env, &env_len,
//        &path, &path_len,
//        &conf, &conf_len) == FAILURE) {
//        return;
//    }
//    std::string s_appkey = appkey;
//    std::string s_env = env;
//    std::string s_path = path;
//    std::string s_conf = conf;
//    int ret = sg_agent_config_setConfig(s_appkey, s_env, s_path, s_conf);
//    //printf("set ret = %d\n", ret);
//
//    RETURN_LONG(ret);
//}

//PHP_FUNCTION(sg_agent_config_init)
//{
//    int arg_len;
//    
//    char* appkey;
//    int appkey_len;
//    char* env;
//    int env_len;
//    char* path; 
//    int path_len;
//
//    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "sss",
//        &appkey, &appkey_len,
//        &env, &env_len,
//        &path, &path_len) == FAILURE) {
//        return;
//    }
//
//    std::string s_appkey = appkey;
//    std::string s_env = env;
//    std::string s_path = path;
//    //int ret = ConfigProcessor::getInstance()
//    //    -> init(s_appkey, s_env, s_path);
//    int ret = config_init(s_appkey, s_env, s_path);
//    printf("init ret = %d\n", ret);
//
//    RETURN_LONG(ret);
//}

PHP_FUNCTION(sg_agent_config_add_app)
{
    int arg_len;
    
    char* appkey;
    int appkey_len;
    char* env;
    int env_len;
    char* path;
    int path_len;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "sss",
        &appkey, &appkey_len,
        &env, &env_len,
        &path, &path_len) == FAILURE) {
        return;
    }
    std::string s_appkey = appkey;
    std::string s_env = env;
    std::string s_path = path;

    int ret = ConfigProcessor::getInstance()
        -> add_app(s_appkey, s_env, s_path);
    //cout << "add app ret value = " << ret << endl;

    RETURN_LONG(ret);
}

PHP_FUNCTION(sg_agent_config_get)
{
    int arg_len;
    
    char* key;
    int key_len;
    char* appkey;
    int appkey_len;
    char* env;
    int env_len;
    char* path;
    int path_len;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "ssss",
        &key, &key_len,
        &appkey, &appkey_len,
        &env, &env_len,
        &path, &path_len) == FAILURE) {
        return;
    }
    std::string s_key = key;
    std::string s_appkey = appkey;
    std::string s_env = env;
    std::string s_path = path;

    //std::string s_value= ConfigProcessor::getInstance()
    //    -> get(s_key, s_appkey, s_env, s_path);
    std::string s_value= ConfigProcessor::getInstance()
        -> get(s_key, s_appkey, s_env, s_path);
    int len = s_value.length();
    if (s_value.empty())
    {
        s_value = "";
        len = 0;
        //printf("s_value is empty\n");
    }
    else
    {
        //printf("s_value is not empty\n");
    }
    //printf("get valule = %s\n", s_value.c_str());
    //cout << "get value = " << s_value << "; len = " << s_value.length() << endl;

    RETURN_STRINGL(s_value.c_str(), len, 1);
}

PHP_FUNCTION(sg_agent_config_set)
{
    int arg_len;
    
    char* key;
    int key_len;
    char* value;
    int value_len;
    char* appkey;
    int appkey_len;
    char* env;
    int env_len;
    char* path;
    int path_len;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "sssss",
        &key, &key_len,
        &value, &value_len,
        &appkey, &appkey_len,
        &env, &env_len,
        &path, &path_len) == FAILURE) {
        return;
    }
    std::string s_key = key;
    std::string s_value = value;
    std::string s_appkey = appkey;
    std::string s_env = env;
    std::string s_path = path;

    int ret = ConfigProcessor::getInstance()
        -> set(s_key, s_value, s_appkey, s_env, s_path);
    //printf("set ret = %d\n", ret);

    RETURN_LONG(ret);
}
