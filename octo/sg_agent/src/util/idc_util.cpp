#include "idc_util.h"
#include "comm/tinyxml2.h"
#include "comm/log4cplus.h"
#include "comm/inc_comm.h"
#include <boost/make_shared.hpp>
namespace sg_agent {

bool IdcUtil::is_init_ = false;
bool IdcUtil::is_idc_xml_valid_ = false;
bool IdcUtil::is_idc_xml_check_ = false;
bool IdcUtil::is_config_has_init = false;
bool IdcUtil::is_thread_has_init = false;
bool IdcUtil::is_config_modified = false;
muduo::MutexLock IdcUtil::init_lock_;
muduo::MutexLock IdcUtil::init_config_lock;
muduo::MutexLock IdcUtil::s_swap_lock;
boost::shared_ptr<std::vector<boost::shared_ptr < IDC> > >IdcUtil::idcs_;
boost::shared_ptr<std::vector<boost::shared_ptr < IDC> > >IdcUtil::new_idcs_;
std::string IdcUtil::UNKNOWN = "unknown";
std::string IdcUtil::NOCENTER = "NOCENTER";

muduo::net::EventLoopThread IdcUtil::m_config_check;
muduo::net::EventLoop *IdcUtil::m_check_loop = NULL;
std::string IdcUtil::m_config_content = "";
std::string IdcUtil::m_config_md5 = "";
std::string IdcUtil::m_config_init_md5 = "";
const static std::string file_name = "idc.xml";
const static std::string file_path_name = "/opt/meituan/apps/sg_agent/";
const static std::string idc_xml_file_name_ = "/opt/meituan/apps/sg_agent/idc.xml";

void IdcUtil::Init() {
  if (!is_init_) {
    muduo::MutexLockGuard lock(init_lock_);
    if (!is_init_) {
      LoadIdcXml(true);
      is_idc_xml_valid_ = true;
      is_init_ = true;
    }
  }
}
bool IdcUtil::IsSameIdc(const boost::shared_ptr<IDC> &idc1, const boost::shared_ptr<IDC> &idc2) {
  return idc1->get_idc() == idc2->get_idc();
}

bool IdcUtil::IsSameCenter(const boost::shared_ptr<IDC> &idc1, const boost::shared_ptr<IDC> &idc2) {
  // exclude NOCENTER
  // NOCENTER不算做一个中心来处理
  return (NOCENTER != idc1->get_center()) && (idc1->get_center() == idc2->get_center());
}

boost::shared_ptr<IDC> IdcUtil::GetIdc(const std::string &ip) {
  Init();
  if (!is_idc_xml_valid_) {
    return boost::make_shared<IDC>();
  }

  // use a scope idcs shared_ptr for loop to avoid the unsafe multi-thread problem.
  boost::shared_ptr < std::vector < boost::shared_ptr < IDC > > > idcs_tmp;
  do {
    muduo::MutexLockGuard lock(s_swap_lock);
    idcs_tmp = idcs_;
  } while (0);

  for (std::vector < boost::shared_ptr < IDC > > ::const_iterator iter = idcs_tmp->begin();
      idcs_tmp->end() != iter;
  ++iter) {
    if ((*iter)->IsSameIdc(ip)) {
      return *iter;
    }
  }
  return boost::make_shared<IDC>();
}

void IdcUtil::LoadIdcXml(bool is_init) {
  LOG_INFO("loading " << idc_xml_file_name_);
  tinyxml2::XMLDocument conf_regions;
  tinyxml2::XMLError eResult = conf_regions.LoadFile(idc_xml_file_name_.c_str());

  if (tinyxml2::XML_SUCCESS != eResult) {
    LOG_ERROR("failed to load config: " << idc_xml_file_name_
                                        << ", errorCode = " << eResult);
    is_idc_xml_valid_ = false;
    return;
  }
  if (is_init) {
    idcs_ = make_shared < std::vector < boost::shared_ptr < IDC > > > ();
    SetInitConfigMd5(file_name, file_path_name);
  } else {
    new_idcs_ = make_shared < std::vector < boost::shared_ptr < IDC > > > ();
  }
  tinyxml2::XMLElement *xmlRegion = conf_regions.FirstChildElement("SGAgent")->FirstChildElement("Region");

  while (NULL != xmlRegion) {

    tinyxml2::XMLElement *xmlRegionName = xmlRegion->FirstChildElement("RegionName");
    std::string region_name = NULL != xmlRegionName ? xmlRegionName->GetText() : UNKNOWN;

    tinyxml2::XMLElement *xmlIDC = xmlRegion->FirstChildElement("IDC");
    while (NULL != xmlIDC) {
      tinyxml2::XMLElement *xmlItem = xmlIDC->FirstChildElement("Item");
      tinyxml2::XMLElement *idc_ptr = xmlIDC->FirstChildElement("IDCName");
      tinyxml2::XMLElement *center_ptr = xmlIDC->FirstChildElement("CenterName");
      while (NULL != xmlItem) {
        boost::shared_ptr<IDC> idc(new IDC());
        tinyxml2::XMLElement *ip_ptr = xmlItem->FirstChildElement("IP");
        tinyxml2::XMLElement *mask_ptr = xmlItem->FirstChildElement("MASK");
        if (NULL == ip_ptr) {
          LOG_ERROR("fail to parse idc.xml, ip is NULL.");
        } else if (NULL == mask_ptr) {
          LOG_ERROR("fail to parse idc.xml, mask is NULL.");
        } else {
          idc->set_region(region_name);
          idc->set_ip(ip_ptr->GetText());
          idc->set_mask(mask_ptr->GetText());
          idc->set_idc(NULL != idc_ptr ? idc_ptr->GetText() : UNKNOWN);
          idc->set_center(NULL != center_ptr ? center_ptr->GetText() : UNKNOWN);
          LOG_INFO("success load region = " << idc->get_region()
                                            << " idc = " << idc->get_idc()
                                            << " center = " << idc->get_center()
                                            << " ip = " << idc->get_ip()
                                            << " mask = " << idc->get_mask()
          );
          if (is_init) {
            idcs_->push_back(idc);
          } else {
            new_idcs_->push_back(idc);
          }
        }

        xmlItem = xmlItem->NextSiblingElement("Item");
      }

      xmlIDC = xmlIDC->NextSiblingElement("IDC");
    }
    xmlRegion = xmlRegion->NextSiblingElement("Region");
  }
}
void IdcUtil::Reset(boost::shared_ptr < std::vector < boost::shared_ptr < IDC > > > new_idcs) {
  LOG_INFO("reload the xml and update");
  do {
    muduo::MutexLockGuard lock(s_swap_lock);
    idcs_ = new_idcs;
  } while (0);

}
std::string IdcUtil::GetSameIdcZk(const char *zk_host, const std::string &ip) {
  std::vector<std::string> vec_str;
  int ret = SplitStringIntoVector(zk_host, ",", vec_str);
  if (0 >= ret) {
    LOG_ERROR("failed to split zk_host to vector, zk_host: " << zk_host);
    return std::string(zk_host);
  }

  boost::shared_ptr<IDC> ip_idc = GetIdc(ip);

  std::vector<std::string> vec_res;
  if (unlikely(ip_idc)) {
    for (std::vector<std::string>::const_iterator iter = vec_str.begin();
         iter != vec_str.end(); ++iter) {

      boost::shared_ptr<IDC> item_idc = GetIdc(*iter);
      if (IsSameIdc(item_idc, ip_idc)) {
        vec_res.push_back(*iter);
      }
    }
  } else {
    LOG_ERROR("fail to get the idc info of ip = " << ip);
  }

  std::string res;
  if (vec_res.empty()) {
    res = std::string(zk_host);
  } else {
    res = vec_res[0];
    for (std::vector<std::string>::const_iterator iter = vec_res.begin() + 1;
         iter != vec_res.end(); ++iter) {
      res += "," + *iter;
    }
  }
  return res;
}
void IdcUtil::ReloadIdcCfg() {

  LoadIdcXml(false);
  Reset(new_idcs_);
  ResetModifiedFlag(false);
}

void IdcUtil::StartCheck() {

  if (!is_thread_has_init) {
    muduo::MutexLockGuard lock(init_config_lock);
    LOG_INFO("IdcUtil Config monitor init check");
    m_check_loop = m_config_check.startLoop();
    m_check_loop->runEvery(CHECK_MONITOR_TIME, boost::bind(&IdcUtil::CheckConfig));
    is_thread_has_init = true;
  }
}
void IdcUtil::CheckConfig() {
  std::string file_Content = "";
  std::string tmp_md5 = "";
  int ret = loadFile(file_Content, file_name.c_str(), file_path_name.c_str());
  if (0 == ret) {
    m_config_content = file_Content;
    MD5 md5String(m_config_content);
    tmp_md5 = md5String.md5();
  } else {
    LOG_ERROR("load the idc config failed");
    return;
  }
  LOG_INFO("init idc md5 = " << m_config_init_md5
                             << ",m_config_md5 =  " << m_config_md5
                             << ", current tmp md5 = " << tmp_md5);

  //确保最终内存idc一致,监视时间调整为5分钟
  if ((tmp_md5 == m_config_md5) && (m_config_md5 == m_config_init_md5)) {
    LOG_INFO("the config is not modified,modified flag" << is_config_modified);
  } else {
    m_config_md5 = tmp_md5.c_str();
    is_config_modified = true;
    if (!is_config_has_init) {
      is_config_has_init = true;
      LOG_INFO("the init config,it is first load");
    } else {
      m_config_init_md5 = m_config_md5;
      LOG_INFO("config is modified " << is_config_modified);
    }
  }
}
void IdcUtil::SetInitConfigMd5(const std::string &load_name, const std::string &load_path) {

  if (load_name.empty() || load_path.empty()) {
    LOG_ERROR("load the para is empty");
    return;
  }
  std::string file_Content = "";
  std::string tmp_md5 = "";
  int ret = loadFile(file_Content, file_name.c_str(), file_path_name.c_str());
  if (0 == ret) {
    MD5 md5String(file_Content);
    m_config_init_md5 = md5String.md5();
    LOG_INFO("the init idc file md5  = " << m_config_init_md5);

  } else {
    LOG_ERROR("load the idc file failed,filename" << file_name << "file_path" << file_path_name);
    return;
  }
}

}
