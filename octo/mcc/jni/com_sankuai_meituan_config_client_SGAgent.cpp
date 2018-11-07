#include "com_sankuai_meituan_config_client_SGAgent.h"
#include "sg_agent_config_client.h"
#include "msgparam.h"
#include <string>
#include <iostream>
using namespace std;

static proc_conf_param_t genSetParamObj(JNIEnv* env, jstring appkey, jstring j_env,
            jstring path, jstring data)
{
    const char* ch_appkey = (env)->GetStringUTFChars(appkey, 0);
    std::string s_appkey = std::string(ch_appkey);

    const char* ch_env = (env)->GetStringUTFChars(j_env, 0);
    std::string s_env = std::string(ch_env);

    const char* ch_path = (env)->GetStringUTFChars(path, 0);
    std::string s_path = std::string(ch_path);

    const char* ch_data = (env)->GetStringUTFChars(data, 0);
    std::string s_data = std::string(ch_data);

    proc_conf_param_t c_conf;
    c_conf.__set_appkey(s_appkey);
    c_conf.__set_env(s_env);
    c_conf.__set_path(s_path);
    c_conf.__set_conf(s_data);

    c_conf.__set_cmd(MQ_SET_CONF);
    return c_conf;
}

static proc_conf_param_t genGetParamObj(JNIEnv* env, jstring appkey,
            jstring j_env, jstring path)
{
    proc_conf_param_t c_node;
    const char* ch_appkey = (env)->GetStringUTFChars(appkey, 0);
    std::string s_appkey = std::string(ch_appkey);

    const char* ch_env = (env)->GetStringUTFChars(j_env, 0);
    std::string s_env = std::string(ch_env);

    const char* ch_path = (env)->GetStringUTFChars(path, 0);
    std::string s_path = std::string(ch_path);

    c_node.__set_appkey(s_appkey);
    c_node.__set_env(s_env);
    c_node.__set_path(s_path);

    c_node.__set_cmd(MQ_GET_CONF);

    return c_node;
}

/*
 * Class:     com_sankuai_meituan_config_client_SGAgent
 * Method:    setConfig
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_sankuai_meituan_config_client_SGAgent_setConfig
  (JNIEnv* env, jclass, jstring appkey, jstring j_env,
            jstring path, jstring data)
{
    //proc_conf_param_t c_conf = genSetParamObj(env, appkey, j_env, path, data);
    //return sg_agent_config_client::getInstance() -> setConfig(c_conf);

    proc_conf_param_t c_conf = genSetParamObj(env, appkey, j_env, path, data);
    int ret = 0;
    boost::shared_ptr<sg_agent_config_client>
        sg_agent_client(new sg_agent_config_client);
    ret = sg_agent_client -> init();
    if (0 != ret)
    {
        return ret;
    }

    ret = sg_agent_client -> setConfig(c_conf);
    sg_agent_client -> destroy();
    return ret;
}

/*
 * Class:     com_sankuai_meituan_config_client_SGAgent
 * Method:    get
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_sankuai_meituan_config_client_SGAgent_get
  (JNIEnv * env, jclass, jstring appkey, jstring j_env, jstring path)
{
    //proc_conf_param_t c_node = genGetParamObj(env, appkey, j_env, path);
    //std::string ret = "";
    //sg_agent_config_client::getInstance() -> getConfig(ret, c_node);
    //return StringToJString(env, ret);

    proc_conf_param_t c_node = genGetParamObj(env, appkey, j_env, path);
    int ret = 0;
    boost::shared_ptr<sg_agent_config_client>
        sg_agent_client(new sg_agent_config_client);
    ret = sg_agent_client -> init();
    if (0 != ret)
    {
        std::string errJson
            = "{\"ret\":-202101,\"msg\":\"failed to connect\"}";
        return StringToJString(env, errJson);
    }

    std::string sRet = "";
    ret = sg_agent_client -> getConfig(sRet, c_node);
    sg_agent_client -> destroy();
    return StringToJString(env, sRet);
}

/*
 * Class:     com_sankuai_meituan_config_client_SGAgent
 * Method:    fileConfigAddApp
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_sankuai_meituan_config_client_SGAgent_fileConfigAddApp
  (JNIEnv * env, jclass, jstring appkey) {
    const char* ch_appkey = (env)->GetStringUTFChars(appkey, 0);
    std::string s_appkey = std::string(ch_appkey);
    int ret = FileConfigProcessor::getInstance() -> add_app(s_appkey);
    return ret;
}

/*
 * Class:     com_sankuai_meituan_config_client_SGAgent
 * Method:    fileConfigGet
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jbyteArray JNICALL Java_com_sankuai_meituan_config_client_SGAgent_fileConfigGet
  (JNIEnv * env, jclass, jstring appkey, jstring filename) {
    const char* ch_appkey = (env)->GetStringUTFChars(appkey, 0);
    std::string s_appkey = std::string(ch_appkey);
    const char* ch_filename = (env)->GetStringUTFChars(filename, 0);
    std::string s_filename = std::string(ch_filename);

    std::string sRet = FileConfigProcessor::getInstance()
        -> get(s_filename, s_appkey);

    jbyteArray content = env
        -> NewByteArray(strlen(sRet.c_str()));
    env-> SetByteArrayRegion(
        content, 0, strlen(sRet.c_str()), (jbyte*)sRet.c_str());
    return content;
}

JNIEXPORT jint JNICALL Java_com_sankuai_meituan_config_client_SGAgent_resetUserCallBack
  (JNIEnv * env, jobject objCallBack, jobject objInterface) {
    g_cbData.m_pEnv->DeleteGlobalRef(objInterface);
    g_cbData.m_pEnv->DeleteGlobalRef(objCallBack);
    return 0;
}

JNIEXPORT jint JNICALL Java_com_sankuai_meituan_config_client_SGAgent_addListener
(JNIEnv * env, jclass obj, jstring appkey, jobject cb) {
    const char* ch_appkey = (env)->GetStringUTFChars(appkey, 0);
    std::string s_appkey = std::string(ch_appkey);

    g_cbData.m_pEnv = env;
    g_cbData.m_objCallBack = env->NewGlobalRef(obj);
    g_cbData.m_objInterface = env->NewGlobalRef(cb);


    boost::shared_ptr<FileChangeListener> listener(new TestListener());
    int ret = FileConfigProcessor::getInstance() -> addListener(JNI_LISTENER,
            listener, s_appkey);
    return ret;
}
