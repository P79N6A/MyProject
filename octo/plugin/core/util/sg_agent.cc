//
// Created by huixiangbo  on 17/8/28.
//


#include "sg_agent.h"

using namespace std;

#include <boost/algorithm/string.hpp>
#include <boost/algorithm/string/regex.hpp>
#include <boost/foreach.hpp>
#include <boost/property_tree/ini_parser.hpp>
#include <boost/property_tree/xml_parser.hpp>
#include <sys/utsname.h>

typedef enum {
    PROD, STAGING, DEV, PPE, TEST
} Appenv;

using namespace std;
using namespace cplugin;
const string CpluginSgagent::kStrSgagentAppkey = "com.sankuai.inf.sg_agent";

const string
        CpluginSgagent::kStrOnlineSgagentSentinelHostURL = "mns.sankuai.com";
const string
        CpluginSgagent::kStrOnlineSgagentSentinelFullURL =
        "http://" + CpluginSgagent::kStrOnlineSgagentSentinelHostURL
        + "/api/servicelist";
const string
        CpluginSgagent::kStrOfflineSgagentSentinelHostURL =
        "mns.inf.test.sankuai.com";
const string CpluginSgagent::kStrOfflineSgagentSentinelFullURL =
        "http://" + CpluginSgagent::kStrOfflineSgagentSentinelHostURL
        + "/api/servicelist";

const string CpluginSgagent::kStrSgagentEnvFileWithPath =
        "/opt/meituan/apps/sg_agent/sg_agent_env.xml";
const string CpluginSgagent::kStrIDCRegionInfoFileFullPath =
        "/opt/meituan/apps/sg_agent/idc.xml.default";
const string CpluginSgagent::kStrSgagentEnvElement = "SGAgentConf.MnsPath";
const string CpluginSgagent::kStrOfficalEnvFileWithPath =
        "/data/webapps/appenv";
const string CpluginSgagent::kStrOfficalEnvElement = "env";
const string CpluginSgagent::kStrOfficalBackupEnvElement = "deployenv";

CpluginSgagent::ONOFFLINE
        CpluginSgagent::enum_onoffline_env_ = CpluginSgagent::OFFLINE;

bool CpluginSgagent::is_online_ =false;

string CpluginSgagent::str_env_;
string CpluginSgagent::str_octo_env_;
string CpluginSgagent::str_local_ip_;
bool CpluginSgagent::b_isMac_ = false;
string CpluginSgagent::str_host_;
string CpluginSgagent::str_hostname_;
string CpluginSgagent::str_sentinel_full_url_;
string CpluginSgagent::str_sentinel_http_request_;

MapRegionMapIdcInfo CpluginSgagent::map_region_map_idc_info_;
string CpluginSgagent::str_local_ip_region_;
string CpluginSgagent::str_local_ip_idc_;
string CpluginSgagent::str_local_ip_center_;
string CpluginSgagent::str_sentinel_host_url_;

CpluginSgagent g_cplugin_sgagent;
//get local_ip, host, hostname, on/off line info, octo env info
CpluginSgagent::CpluginSgagent(void) {
}

int CpluginSgagent::InitCpluginSgagent(){
    GetIsMacAndHostIPInfo();//fill str_local_ip_, b_isMac_, str_host_

    if(InitAppenv() != 0){
        LOG_ERROR << "init appenv failed ";
        return -1;
    }

    LOG_DEBUG << "enum_onoffline_env_ " << enum_onoffline_env_;

    string str_sentinel_host_url;
    if (ONLINE == enum_onoffline_env_) {
        str_sentinel_full_url_.assign(
                kStrOnlineSgagentSentinelFullURL);
        str_sentinel_host_url.assign(kStrOnlineSgagentSentinelHostURL);
    } else {
        str_sentinel_full_url_.assign(
                kStrOfflineSgagentSentinelFullURL);
        str_sentinel_host_url.assign(kStrOfflineSgagentSentinelHostURL);
    }

    LOG_DEBUG << "str_sentinel_full_url_ " << str_sentinel_full_url_
              << " str_sentinel_host_url "
              << str_sentinel_host_url;

    str_sentinel_host_url_ = str_sentinel_host_url;


    /*
    if(!InitIDCRegionInfo()){
        LOG_ERROR << "InitIDCRegionInfo failed";
        return -1;
    }
    */
    
    return 0;
}

int CpluginSgagent::InitAppenv() {
    string deployenv_str;
    string env_str;
    std::ifstream appenv_fin;

    try {
        appenv_fin.open(kStrOfficalEnvFileWithPath.c_str(), std::ios::in);
        if (!appenv_fin.is_open()) {
            LOG_ERROR << "failed to init gAppenv, there is not" << kStrOfficalEnvFileWithPath ;
            return -1;
        } else {
            std::string buffer_str;
            while (std::getline(appenv_fin,buffer_str) && (env_str.empty() || deployenv_str.empty())) {
                std::size_t pos = buffer_str.find_first_of("=");
                if (std::string::npos != pos) {
                    std::string key = buffer_str.substr(0, pos);
                    std::string value = buffer_str.substr(pos + 1);
                    boost::trim(key);
                    boost::trim(value);
                    if ("env" == key) {
                        env_str = value;
                        LOG_INFO << "parsing env: " << buffer_str;
                    } else if ("deployenv" == key) {
                        deployenv_str = value;
                        LOG_INFO << "parsing deployenv: " << buffer_str;
                    }
                }
                buffer_str.clear();
            }
        }
        appenv_fin.close();
    } catch (std::exception &e) {
        appenv_fin.close();
        LOG_ERROR << "fail to load " <<kStrOfficalEnvFileWithPath
                                  << "OR fetch deployenv/appenv failed, reason: " << e.what();
        return -1;
    }

    transform(deployenv_str.begin(), deployenv_str.end(), deployenv_str.begin(), ::tolower);
    transform(env_str.begin(), env_str.end(), env_str.begin(), ::tolower);

    LOG_INFO << "get env = " << env_str <<
                          ", deployenv = " << deployenv_str <<
                          " from " << kStrOfficalEnvFileWithPath ;

    LOG_INFO << "start to parse the host env, env = " << env_str << ", deployenv = " << deployenv_str;

    Appenv mAppenv;
    // 优先解释env字段，无法解释时，再解释deployenv。请勿优化以下if语句
    if ("prod" == env_str) {
        mAppenv = PROD;
    } else if ("staging" == env_str) {
        mAppenv = STAGING;
    } else if ("dev" == env_str) {
        mAppenv = DEV;
    } else if ("ppe" == env_str) {
        mAppenv = PPE;
    } else if ("test" == env_str) {
        mAppenv = TEST;
    } else if ("product" == deployenv_str || "prod" == deployenv_str) {
        mAppenv = PROD;
    } else if ("staging" == deployenv_str) {
        mAppenv = STAGING;
    } else if ("dev" == deployenv_str || "alpha" == deployenv_str) {
        mAppenv = DEV;
    } else if ("ppe" == deployenv_str || "prelease" == deployenv_str) {
        mAppenv = PPE;
    } else if ("qa" == deployenv_str || "test" == deployenv_str) {
        mAppenv = TEST;
    }else{
        LOG_ERROR << "fail to parse the host env, invalid appenv.";
        return -1;
    }

    switch(mAppenv){
        case PROD:
            str_env_ = "prod";
            str_octo_env_ = "prod";
            enum_onoffline_env_ = ONLINE;
            is_online_ = true;
            LOG_INFO << "success to init host env = prod (online)";
            break;
        case STAGING:
            str_env_ = "stage";
            str_octo_env_ = "stage";
            enum_onoffline_env_ = ONLINE;
            is_online_ = true;
            LOG_INFO << "success to init host env = staging (online)";
            break;
        case DEV:
            str_env_ = "prod";
            str_octo_env_ = "dev";
            enum_onoffline_env_ = OFFLINE;
            is_online_ = false;
            LOG_INFO << "success to init host env = dev (offline)";
            break;
        case PPE:
            str_env_ = "stage";
            str_octo_env_ = "ppe";
            enum_onoffline_env_ = OFFLINE;
            is_online_ = false;
            LOG_INFO << "success to init host env = ppe (offline)";
            break;
        case TEST:
            str_env_ = "test";
            str_octo_env_ = "test";
            enum_onoffline_env_ = OFFLINE;
            is_online_ = false;
            LOG_INFO << "success to init host env = test (offline)";
            break;
        default:
            LOG_ERROR << "fail to init host env.";
            return -1;
    }

    return 0;
}

bool CpluginSgagent::InitIDCRegionInfo(void) {

    map_region_map_idc_info_.clear();

    boost::property_tree::ptree bPTree4IDCRegionFile;
    string str_idc_name;
    string str_center;
    try {
        boost::property_tree::xml_parser::read_xml(CpluginSgagent::kStrIDCRegionInfoFileFullPath,
                                                   bPTree4IDCRegionFile);
        BOOST_FOREACH(boost::property_tree::ptree::value_type &v,
                      bPTree4IDCRegionFile.get_child("SGAgent")) { //loop every node under SGAgent
            if ("Region" == v.first) {
                BOOST_FOREACH(boost::property_tree::ptree::value_type &v_region, v
                        .second) {
                    boost::unordered_map<string, vector<IdcInfo> >
                            &map_idc2vecinfo =
                            map_region_map_idc_info_[v.second.get<string>(
                                    "RegionName")];
                    if ("IDC" == v_region.first) {
                        //fetch center name，assume ONLY ONE sub node named
                        // "CenterName"
                        try {
                            str_center.assign(v_region.second.get<string>(
                                    "CenterName"));
                        } catch (boost::property_tree::ptree_error e) {
                            LOG_DEBUG << "fetch CenterName failed, "
                                    "reason: " << e.what() << ", if old "
                                              "idc file, it's ok";
                            str_center.assign("UNKNOWN");  //Init
                        }
                        LOG_DEBUG << "str_center " << str_center;
                        vector<IdcInfo>
                                &vec_idc_info =
                                map_idc2vecinfo[v_region.second.get<string>(
                                        "IDCName")]; //assume IDCName MUST exist and
                        // match!!
                        BOOST_FOREACH(boost::property_tree::ptree::value_type
                                      &v_idc, v_region.second) {
                            if ("Item" == v_idc.first) {
                                vec_idc_info.push_back(IdcInfo(v_idc.second.get<
                                                                       string>("IP"),
                                                               v_idc.second
                                                                       .get<
                                                                               string>(
                                                                               "MASK"),
                                                               str_center));
                            }
                        }
                    }
                }
            }
        }
    } catch (boost::property_tree::ptree_error
             e) {
        LOG_ERROR << "fetch local idc info failed, reason: " << e.what()
                  << ", please "
                          "make sure " << CpluginSgagent::kStrIDCRegionInfoFileFullPath
                  << " "
                          "exist";
        return false;
    }
    //print idc region info
    MapRegionMapIdcInfoIter it = map_region_map_idc_info_.begin();
    while (it != map_region_map_idc_info_.end()) {
        LOG_DEBUG << "Region: " << it->first;
        boost::unordered_map<string, vector<IdcInfo> >::iterator it_idc_vec_info =
                                                                         (it->second).begin();
        while (it_idc_vec_info != (it->second).end()) {
            LOG_DEBUG << " Idc: " << it_idc_vec_info->first;
            vector<IdcInfo>::iterator it_vec = (it_idc_vec_info->second).begin();
            while (it_vec != (it_idc_vec_info->second).end()) {
                LOG_DEBUG << "   " + it_vec->IdcInfo2String();
                ++it_vec;
            }
            ++it_idc_vec_info;
        }
        ++it;
    }
    if (FetchRegionIDCOfIP(str_local_ip_, &str_local_ip_region_,
                           &str_local_ip_idc_, &str_local_ip_center_)) {
        LOG_ERROR << "fetch local ip region & idc & center info failed";
        return false;
    } else {
        LOG_DEBUG << "local ip " << str_local_ip_ << " in region: "
                  << str_local_ip_region_ << " idc: " << str_local_ip_idc_ << " "
                          "center: " << str_local_ip_center_;
    }

    return true;
}

int8_t CpluginSgagent::FetchRegionIDCOfIP(const string &str_ip,
                                          string *p_str_region,
                                          string
                                          *p_str_idc, string *p_str_center) {
    vector<IdcInfo>::iterator it_vec_idc_info;
    boost::unordered_map<string, vector<IdcInfo> >::iterator it_map_idc_vec_info;
    boost::unordered_map<string, boost::unordered_map<string, vector<IdcInfo> >
                                                              >::iterator it = map_region_map_idc_info_.begin();
    while (it != map_region_map_idc_info_.end()) {
        //LOG_DEBUG << "Region: " << it->first;
        it_map_idc_vec_info = (it->second).begin();
        while (it_map_idc_vec_info != (it->second).end()) {
            //LOG_DEBUG << "IDC: " << it_map_idc_vec_info->first;
            it_vec_idc_info = (it_map_idc_vec_info->second).begin();
            while (it_vec_idc_info != (it_map_idc_vec_info->second).end()) {
                if (it_vec_idc_info->IsSameNetSegment(str_ip)) {
                    p_str_region->assign(it->first);
                    p_str_idc->assign(it_map_idc_vec_info->first);
                    p_str_center->assign((it_map_idc_vec_info->second)[0].str_center);
                    //assume every idc has ip segment
                    LOG_DEBUG << "ip " << str_ip << " in region " << *p_str_region << ""
                            " idc " << *p_str_idc << " center " << *p_str_center;
                    return 0;
                }
                ++it_vec_idc_info;
            }
            ++it_map_idc_vec_info;
        }
        ++it;
    }
    LOG_WARN << "ip " << str_ip << " NOT hit idc info";
    return -1;
}

string CpluginSgagent::SGService2String(const cplugin_sgagent::SGService &sgservice) {
    string str_ret
            ("appkey:" + sgservice.appkey + " version:" + sgservice.version + " ip:"
             + sgservice.ip);
    string str_tmp;
    try {
        str_ret.append(" port:" + boost::lexical_cast<std::string>(sgservice.port));
    } catch (boost::bad_lexical_cast &e) {
        LOG_ERROR << "boost::bad_lexical_cast :" << e.what()
                  << "sgservice.port : " << sgservice.port;
    }
    try {
        str_ret.append(
                " weight:" + boost::lexical_cast<std::string>(sgservice.weight));
    } catch (boost::bad_lexical_cast &e) {
        LOG_ERROR << "boost::bad_lexical_cast :" << e.what()
                  << "sgservice.weight : " << sgservice.weight;
    }
    try {
        str_ret.append(
                " status:" + boost::lexical_cast<std::string>(sgservice.status));
    } catch (boost::bad_lexical_cast &e) {
        LOG_ERROR << "boost::bad_lexical_cast :" << e.what()
                  << "sgservice.status : " << sgservice.status;
    }
    try {
        str_ret.append(" role:" + boost::lexical_cast<std::string>(sgservice.role));
    } catch (boost::bad_lexical_cast &e) {
        LOG_ERROR << "boost::bad_lexical_cast :" << e.what()
                  << "sgservice.role : " << sgservice.role;
    }
    try {
        str_ret.append(
                " envir:" + boost::lexical_cast<std::string>(sgservice.envir));
    } catch (boost::bad_lexical_cast &e) {
        LOG_ERROR << "boost::bad_lexical_cast :" << e.what()
                  << "sgservice.envir : " << sgservice.envir;
    }
    try {
        str_ret.append(" lastUpdateTime:"
                       + boost::lexical_cast<std::string>(sgservice.lastUpdateTime)
                       + " extend:" + sgservice.extend);
    } catch (boost::bad_lexical_cast &e) {
        LOG_ERROR << "boost::bad_lexical_cast :" << e.what()
                  << "sgservice.lastUpdateTime : " << sgservice.lastUpdateTime;
    }
    try {
        str_ret.append(
                " fweight:" + boost::lexical_cast<std::string>(sgservice.fweight));
    } catch (boost::bad_lexical_cast &e) {
        LOG_ERROR << "boost::bad_lexical_cast :" << e.what()
                  << "sgservice.fweight : " << sgservice.fweight;
    }
    try {
        str_ret.append(" serverType:"
                       + boost::lexical_cast<std::string>(sgservice.serverType));
    } catch (boost::bad_lexical_cast &e) {
        LOG_ERROR << "boost::bad_lexical_cast :" << e.what()
                  << "sgservice.serverType : " << sgservice.serverType;
    }
    return str_ret;
}
void CpluginSgagent::IntranetIp(char ip[INET_ADDRSTRLEN]) {
    struct ifaddrs *ifAddrStruct = NULL;
    struct ifaddrs *ifa = NULL;
    void *tmpAddrPtr = NULL;
    int addrArrayLen = 32;
    char addrArray[addrArrayLen][INET_ADDRSTRLEN];
    getifaddrs(&ifAddrStruct);
    int index = 0;
    for (ifa = ifAddrStruct; ifa != NULL; ifa = ifa->ifa_next) {
        if (!ifa->ifa_addr) {
            continue;
        }
        if (0 == strcmp(ifa->ifa_name, "vnic"))
            continue;
        if (ifa->ifa_addr->sa_family == AF_INET) { // check it is IP4
            //tmpAddrPtr = &((struct sockaddr_in *) ifa->ifa_addr)->sin_addr;
            tmpAddrPtr =
                    &(reinterpret_cast<struct sockaddr_in *>(ifa->ifa_addr))->sin_addr;
            inet_ntop(AF_INET, tmpAddrPtr, addrArray[index], INET_ADDRSTRLEN);
            if (0 == strcmp(addrArray[index], "127.0.0.1"))
                continue;
            strcpy(ip, addrArray[index]);
            if (++index >= addrArrayLen - 1)
                break;
        }
    }
    if (index > 1) {
        int idx = 0;
        while (idx < index) {
            if (NULL != strstr(addrArray[idx], "10.")
                && 0 == strcmp(addrArray[idx], strstr(addrArray[idx], "10."))) {
                strcpy(ip, addrArray[idx]);
            }
            idx++;
        }
    }
    if (ifAddrStruct != NULL)
        freeifaddrs(ifAddrStruct);
    return;
}
//fill ip, isMac, host, hostname
void CpluginSgagent::GetIsMacAndHostIPInfo(void) {
    char ip[INET_ADDRSTRLEN] = {0};
    IntranetIp(ip);
    if (CPLUGIN_UNLIKELY(0 == strlen(ip))) {
        LOG_WARN << "Cannot get local ip, wait 5 secs";
        muduo::CurrentThread::sleepUsec(5000 * 1000);
        IntranetIp(ip);
    }
    if (CPLUGIN_UNLIKELY(0 == strlen(ip))) {
        LOG_WARN << "After wait 5 secs, still cannot get local ip, set to be 127"
                ".0.0.1";
        str_local_ip_.assign("127.0.0.1");
    } else {
        str_local_ip_.assign(ip);
        LOG_INFO << "local ip " << str_local_ip_;
    }
    char hostCMD[64];
    strncpy(hostCMD, "host ", 5);
    strncpy(hostCMD + 5, ip, INET_ADDRSTRLEN);
    FILE *fp = popen(hostCMD, "r");
    char hostInfo[256] = {0};
    if (CPLUGIN_LIKELY(!fgets(hostInfo, 256, fp))) {
        int iRet = ferror(fp);
        if (CPLUGIN_UNLIKELY(iRet)) {
            LOG_ERROR << "fgets error, iRet " << iRet;
            return;
        }
    }
    hostInfo[strlen(hostInfo) - 1] = '\0';  //del line token
    str_host_.assign(hostInfo);
    str_host_.assign(strToLower(str_host_));
    replace_all_distinct(" ", "%20", &str_host_);
    LOG_INFO << "host info: " << hostInfo;
    pclose(fp);
    memset(hostCMD, 0, sizeof(hostCMD));
    strncpy(hostCMD, "hostname ", 9);
    fp = popen(hostCMD, "r");
    char hostname[256] = {0};
    if (CPLUGIN_LIKELY(!fgets(hostname, 256, fp))) {
        int iRet = ferror(fp);
        if (CPLUGIN_UNLIKELY(iRet)) {
            LOG_ERROR << "fgets error, iRet " << iRet;
            return;
        }
    }
    hostname[strlen(hostname) - 1] = '\0';  //del line token
    str_hostname_.assign(hostname);
    str_hostname_.assign(strToLower(str_hostname_));
    replace_all_distinct(" ", "%20", &str_hostname_);
    LOG_INFO << "host name: " << hostname;
    pclose(fp);
    b_isMac_ = CheckIfMac(str_host_);
    LOG_DEBUG << "is mac " << b_isMac_;
}
//"com.sankuai.inf.sg_sentinel"
/*{"ret":200,"data":{"serviceList":[{"appkey":"com.sankuai.inf.sg_sentinel","version":"original","ip":"10.4.246.240","port":5266,"weight":10,"fweight":0.0,"status":2,"role":0,"env":3,"lastUpdateTime":1460009990,"extend":"","serverType":0},{"appkey":"com.sankuai.inf.sg_sentinel","version":"original","ip":"10.4.241.125","port":5266,"weight":10,"fweight":0.0,"status":2,"role":0,"env":3,"lastUpdateTime":1460035753,"extend":"","serverType":0},{"appkey":"com.sankuai.inf.sg_sentinel","version":"original","ip":"10.4.241.165","port":5266,"weight":10,"fweight":0.0,"status":2,"role":0,"env":3,"lastUpdateTime":1460009990,"extend":"","serverType":0}]}}*/
int8_t CpluginSgagent::ParseSentineSgagentList(const string &str_req,
                                               vector<cplugin_sgagent::SGService> *p_vec_sgservice,const string &app_key ) {
    rapidjson::Document reader;
    if ((reader.Parse(str_req.c_str())).HasParseError()) {
        LOG_WARN << "json parse string " << str_req << " failed";
        return -1;    //maybe NOT error
    }
    rapidjson::StringBuffer buffer;
    rapidjson::Writer<rapidjson::StringBuffer> writer(buffer);
    reader.Accept(writer);
    LOG_DEBUG << "receive body " << string(buffer.GetString());
    int i_ret;
    if (FetchInt32FromJson("ret", reader, &i_ret)) {
        return -1;
    }
    LOG_DEBUG << "i_ret " << i_ret;
    if (200 != i_ret) {
        LOG_ERROR << "fetch santine sgagent list failed, i_ret " << i_ret;
        return -1;
    }
    rapidjson::Value::MemberIterator it_data;
    if (FetchJsonValByKey4Doc(reader, "data", &it_data)
        || !((it_data->value).IsObject())) {
        LOG_ERROR << "FetchJsonValByKey4Doc Wrong OR data is NOT object";
        return -1;
    }
    rapidjson::Value &data_val = it_data->value;
    rapidjson::Value::MemberIterator it;
    if (FetchJsonValByKey4Val(data_val, "serviceList", &it)) {
        return -1;
    }
    rapidjson::Value &srv_list = it->value;
    if (!(srv_list.IsArray())) {
        LOG_ERROR << "srv_list NOT array";
        return -1;
    }
    rapidjson::Document::MemberIterator itr_sing;
    for (rapidjson::Value::ValueIterator itr = srv_list.Begin();
         itr != srv_list.End(); ++itr) {
        cplugin_sgagent::SGService sgservice;
        const rapidjson::Value &srvlist_info = *itr;
        if (FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                                  "appkey",
                                  &itr_sing)
            || !((itr_sing->value).IsString())
            || app_key != string((itr_sing->value).GetString
                ())) {
            LOG_ERROR
                    << "appkey NOT EXIST OR NOT string OR NOT com.sankuai.inf.sg_sentinel";
            continue;
        }
        //sgservice.appkey.assign((itr_sing->value).GetString());
        sgservice.appkey.assign(CpluginSgagent::kStrSgagentAppkey); //replace to be sgagent appkey!!
        if (FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                                  "version",
                                  &itr_sing)
            || !((itr_sing->value).IsString())) {
            LOG_ERROR << "version NOT EXIST OR NOT string";
            continue;
        }
        sgservice.version.assign((itr_sing->value).GetString());
        if (FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                                  "ip",
                                  &itr_sing)
            || !((itr_sing->value).IsString())) {
            LOG_ERROR << "version NOT EXIST OR NOT string";
            continue;
        }
        sgservice.ip.assign((itr_sing->value).GetString());
        if (FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                                  "port",
                                  &itr_sing)
            || !((itr_sing->value).IsInt())) {
            LOG_ERROR << "version NOT EXIST OR NOT int";
            continue;
        }
        sgservice.port = (itr_sing->value).GetInt();
        if (FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                                  "weight",
                                  &itr_sing)
            || !((itr_sing->value).IsInt())) {
            LOG_ERROR << "weight NOT EXIST OR NOT int";
            continue;
        }
        sgservice.weight = (itr_sing->value).GetInt();
        if (FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                                  "status",
                                  &itr_sing)
            || !((itr_sing->value).IsInt())) {
            LOG_ERROR << "status NOT EXIST OR NOT int";
            continue;
        }
        sgservice.status = (itr_sing->value).GetInt();
        if (FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                                  "role",
                                  &itr_sing)
            || !((itr_sing->value).IsInt())) {
            LOG_ERROR << "role NOT EXIST OR NOT int";
            continue;
        }
        sgservice.role = (itr_sing->value).GetInt();
        if (FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                                  "env",
                                  &itr_sing)
            || !((itr_sing->value).IsInt())) {
            LOG_ERROR << "envir NOT EXIST OR NOT int";
            continue;
        }
        sgservice.envir = (itr_sing->value).GetInt();
        if (FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                                  "lastUpdateTime",
                                  &itr_sing)
            || !((itr_sing->value).IsInt())) {
            LOG_ERROR << "lastUpdateTime NOT EXIST OR NOT int";
            continue;
        }
        sgservice.lastUpdateTime = (itr_sing->value).GetInt();
        if (-1
            == FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                                     "extend",
                                     &itr_sing)
            || !((itr_sing->value).IsString())) {
            LOG_ERROR << "extend NOT EXIST OR NOT string";
            continue;
        }
        sgservice.extend = (itr_sing->value).GetString();
        if (FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                                  "fweight",
                                  &itr_sing)) {
            LOG_ERROR << "fweight NOT EXIST OR NOT double";
            continue;
        }
        sgservice.fweight = (itr_sing->value).GetDouble();
        if (FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                                  "serverType",
                                  &itr_sing)
            || !((itr_sing->value).IsInt())) {
            LOG_ERROR << "serverType NOT EXIST OR NOT int";
            continue;
        }
        sgservice.serverType = (itr_sing->value).GetInt();
        LOG_DEBUG << "sgservice content: " << SGService2String(sgservice);
        p_vec_sgservice->push_back(sgservice);
    }
    return 0;
}
