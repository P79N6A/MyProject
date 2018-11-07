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

#ifndef PHP_SG_AGENT_SDK_H
#define PHP_SG_AGENT_SDK_H

extern zend_module_entry sg_agent_sdk_module_entry;
#define phpext_sg_agent_sdk_ptr &sg_agent_sdk_module_entry

#ifdef PHP_WIN32
#	define PHP_SG_AGENT_SDK_API __declspec(dllexport)
#elif defined(__GNUC__) && __GNUC__ >= 4
#	define PHP_SG_AGENT_SDK_API __attribute__ ((visibility("default")))
#else
#	define PHP_SG_AGENT_SDK_API
#endif

#ifdef ZTS
#include "TSRM.h"
#endif

PHP_MINIT_FUNCTION(sg_agent_sdk);
PHP_MSHUTDOWN_FUNCTION(sg_agent_sdk);
PHP_RINIT_FUNCTION(sg_agent_sdk);
PHP_RSHUTDOWN_FUNCTION(sg_agent_sdk);
PHP_MINFO_FUNCTION(sg_agent_sdk);

//PHP_FUNCTION(confirm_sg_agent_sdk_compiled);	/* For testing, remove later. */
//PHP_FUNCTION(sg_agent_config_getConfig);	/* For testing, remove later. */
//PHP_FUNCTION(sg_agent_config_setConfig);	/* For testing, remove later. */
PHP_FUNCTION(sg_agent_config_add_app);	/* For testing, remove later. */
PHP_FUNCTION(sg_agent_config_get);	/* For testing, remove later. */
PHP_FUNCTION(sg_agent_config_set);	/* For testing, remove later. */
PHP_FUNCTION(sg_agent_config_destroy);	/* For testing, remove later. */

/* 
  	Declare any global variables you may need between the BEGIN
	and END macros here:     

ZEND_BEGIN_MODULE_GLOBALS(sg_agent_sdk)
	long  global_value;
	char *global_string;
ZEND_END_MODULE_GLOBALS(sg_agent_sdk)
*/

/* In every utility function you add that needs to use variables 
   in php_sg_agent_sdk_globals, call TSRMLS_FETCH(); after declaring other 
   variables used by that function, or better yet, pass in TSRMLS_CC
   after the last function argument and declare your utility function
   with TSRMLS_DC after the last declared argument.  Always refer to
   the globals in your function as SG_AGENT_SDK_G(variable).  You are 
   encouraged to rename these macros something shorter, see
   examples in any other php module directory.
*/

#ifdef ZTS
#define SG_AGENT_SDK_G(v) TSRMG(sg_agent_sdk_globals_id, zend_sg_agent_sdk_globals *, v)
#else
#define SG_AGENT_SDK_G(v) (sg_agent_sdk_globals.v)
#endif

#endif	/* PHP_SG_AGENT_SDK_H */


/*
 * Local variables:
 * tab-width: 4
 * c-basic-offset: 4
 * End:
 * vim600: noet sw=4 ts=4 fdm=marker
 * vim<600: noet sw=4 ts=4
 */
