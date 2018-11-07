//
// Created by huixiangbo  on 17/8/28.
//

#ifndef _SGAGENT_H
#define _SGAGENT_H

#include <boost/property_tree/detail/xml_parser_error.hpp>
#include <boost/property_tree/ini_parser.hpp>
#include "cplugin_common.h"
namespace cplugin {
    using namespace apache::thrift;
    using namespace apache::thrift::protocol;
    using namespace apache::thrift::transport;
    struct IdcInfo {
        std::string str_ip_prefix; //still full ip
        std::string str_ip_mask;
        std::string str_center;
        IdcInfo(const std::string &str_ip_prefix_tmp,
                const std::string &str_ip_mask_tmp, const std::string
                &str_center_tmp) :
                str_ip_prefix(str_ip_prefix_tmp), str_ip_mask(str_ip_mask_tmp),
                str_center(str_center_tmp) { ; }
        bool IsSameNetSegment(const std::string &str_ip1) {
            int i_mask = mask_to_int(str_ip_mask);
            return (i_mask & get_ipv4(str_ip1)) == (i_mask & get_ipv4(str_ip_prefix));
        }
        std::string IdcInfo2String(void) const {
            return std::string(
                    "ip_prefix: " + str_ip_prefix + " ip_mask: " + str_ip_mask + " center: "
                    + str_center);
        }
    };
    typedef boost::unordered_map<std::string, boost::unordered_map<std::string,
            std::vector<
            IdcInfo> > >
    MapRegionMapIdcInfo;
    typedef boost::unordered_map<std::string, boost::unordered_map<std::string,
            std::vector<
            IdcInfo> > >::iterator
            MapRegionMapIdcInfoIter;
    class CpluginSgagent {
    private:
        static void IntranetIp(char ip[INET_ADDRSTRLEN]);
        static void GetIsMacAndHostIPInfo(void); //fill ip, isMac, host,hostname

        static bool CheckIfMac(const std::string &str_host) {
            return (std::string::npos != str_host.find("macbook")
                    || std::string::npos != str_host.find("mac.local"));
        }
        enum ONOFFLINE {
            ONLINE,
            OFFLINE
        };

        static bool InitIDCRegionInfo(void);
        static int8_t
        FetchRegionIDCOfIP(const std::string &str_ip,
                           std::string *p_str_region,
                           std::string
                           *p_str_idc, std::string
                           *p_str_center);

    public:
        static const std::string kStrSgagentAppkey;
        static const std::string kStrOnlineSgagentSentinelHostURL;
        static const std::string kStrOnlineSgagentSentinelFullURL;
        static const std::string kStrOfflineSgagentSentinelHostURL;
        static const std::string kStrOfflineSgagentSentinelFullURL;

        static const std::string kStrSgagentEnvFileWithPath;
        static const std::string kStrSgagentEnvElement;
        static const std::string kStrOfficalEnvFileWithPath;
        static const std::string kStrOfficalEnvElement;
        static const std::string kStrOfficalBackupEnvElement;

        static ONOFFLINE enum_onoffline_env_;
        static bool is_online_;
        static std::string str_env_;
        static std::string str_octo_env_;
        static std::string str_local_ip_;
        static bool b_isMac_;
        static std::string str_host_;
        static std::string str_hostname_;
        static std::string str_sentinel_full_url_;
        static std::string str_sentinel_host_url_;
        static std::string str_sentinel_http_request_;
        static const std::string kStrIDCRegionInfoFileFullPath;
        static MapRegionMapIdcInfo map_region_map_idc_info_;
        static std::string str_local_ip_region_;
        static std::string str_local_ip_idc_;
        static std::string str_local_ip_center_;
        CpluginSgagent(void);

        static std::string SGService2String(const cplugin_sgagent::SGService &sgservice);
        static int8_t ParseSentineSgagentList
                (const std::string &str_req, std::vector<cplugin_sgagent::SGService> *p_vec_sgservice, const std::string &app_key);

        static void OCTOInfo(void) {
            LOG_INFO << "env " << str_env_  << " local "
                    "ip " <<
                     str_local_ip_ <<
                     " "
                             "is "
                             "Mac " << b_isMac_ << " host " << str_host_ << " hostname "
                     << str_hostname_ << " sentinel url " << str_sentinel_full_url_
                     << " sentinel http request " << str_sentinel_http_request_;
        }

        int InitCpluginSgagent();

        int InitAppenv();
    };
}
extern  cplugin::CpluginSgagent g_cplugin_sgagent;

#endif //_SGAGENT_H
