//
// Created by Lhmily on 08/25/2017.
//

#include <boost/bind.hpp>
#include <event.h>
#include "falcon_collector.h"
#include <curl/curl.h>
#include <comm/cJSON.h>
#include <comm/log4cplus.h>
#include <comm/inc_comm.h>
#include <boost/make_shared.hpp>

namespace sg_agent {
class FalconItem {
 public:
  FalconItem() : m_sumtime(0L), m_count(0L), m_rate_count(0), m_enable_value(false), m_enable_rate_count(false) {}
  std::string m_metric;
  std::string m_tags;

  bool m_enable_value;
  std::string m_value;

  bool m_enable_rate_count;
  unsigned int m_rate_count;

  unsigned long m_sumtime;
  unsigned int m_count;

  void SetValue(const std::string &value) {
	muduo::MutexLockGuard lock(m_lock);
	m_enable_value = true;
	m_value = value;
  }

  void IncRateCount(bool is_inc_rate) {
	muduo::MutexLockGuard lock(m_lock);
	m_enable_rate_count = true;
	if (is_inc_rate) {
	  ++m_rate_count;
	}
	++m_count;
  }

  void IncCount() {
	muduo::MutexLockGuard lock(m_lock);
	m_count++;
  }

  void IncSumtime(unsigned long time) {
	muduo::MutexLockGuard lock(m_lock);
	m_sumtime += time;
	++m_count;
  }

 private:
  muduo::MutexLock m_lock;

};

muduo::net::EventLoopThread FalconCollector::s_collector_thread;
muduo::net::EventLoopThread *FalconCollector::GetCollectorThread() {
  return &s_collector_thread;
}

double FalconCollector::s_interval = 60.0;
FalconItemPtr FalconCollector::s_data_ptr(new FalconItemMap());
std::string FalconCollector::s_falcon_url("http://127.0.0.1:1988/v1/push");
std::string FalconCollector::s_end_point("unknown");
muduo::MutexLock FalconCollector::s_cache_check_lock;
muduo::net::EventLoop *FalconCollector::s_collecotr_loop = GetCollectorThread()->startLoop();

void FalconCollector::StartCollect() {
  s_collecotr_loop->runEvery(s_interval, boost::bind(&FalconCollector::DoCollect));
}
void FalconCollector::DoCollect() {

#ifdef SG_AGENT_TEST
	extern bool is_run_falcon_task;
	if (!is_run_falcon_task) {
		LOG_DEBUG("sg_agent test logic, the is_run_falcon_task = " << is_run_falcon_task);
		return ;
	}
#endif

  if (!SetEndPoint()) {
	// alread log inside.
	return;
  }

  timeval now_time;
  gettimeofday(&now_time, NULL);

  FalconItemPtr data = GetAndReset();
  std::vector<FalconCounter> counter_list;
  for (FalconItemMap::const_iterator it = data->begin(); data->end() != it; ++it) {
	long meantime =
		(it->second->m_count > 0 && it->second->m_sumtime > 0) ? it->second->m_sumtime / it->second->m_count : -1L;
	FalconCounter counter;
	counter.SetMetric(it->second->m_metric);
	counter.SetTags(it->second->m_tags);
	counter.SetTime(now_time.tv_usec / 1000);

	if (meantime > 0) {
	  //upload meantime
	  counter.SetValue(boost::lexical_cast<std::string>(it->second->m_sumtime));
	} else if (it->second->m_enable_value) {
	  counter.SetValue(it->second->m_value);
	} else if (it->second->m_enable_rate_count) {
	  //upload the rate
	  float rate = 0 < it->second->m_count ? (it->second->m_rate_count * 1.0f / it->second->m_count * 100) : 0.0f;
	  try {
		counter.SetValue(boost::lexical_cast<std::string>(rate));
	  } catch (boost::bad_lexical_cast &e) {
		LOG_ERROR("boost::bad_lexical_cast for rate : " << rate
														<< ", reason : "
														<< std::string(e.what()));
		return;
	  }
	} else {
	  //upload the count
	  try {
		counter.SetValue(boost::lexical_cast<std::string>(it->second->m_count));
	  } catch (boost::bad_lexical_cast &e) {
		LOG_ERROR("boost::bad_lexical_cast for m_count : " << it->second->m_count
														   << " reason : "
														   << std::string(e.what()));
		return;
	  }
	}
	counter_list.push_back(counter);
  }
  if (!counter_list.empty()) {
	Upload(counter_list);
  }
}

bool FalconCollector::SetEndPoint() {
  if (s_end_point.empty() || "unknown" == s_end_point) {
	char host_name[256] = {0};
	gethostname(host_name, sizeof(host_name));
	std::string host_name_str(host_name);
	s_end_point = (std::string::npos != host_name_str.find(".sankuai.com")
		|| std::string::npos != host_name_str.find(".office.mos")) ? host_name_str.substr(0, host_name_str.find("."))
																   : host_name_str;
	if (s_end_point.empty() || "unknown" == s_end_point) {
	  LOG_WARN("fail to init falcon ENDPOINT.");
	  return false;
	}
  }
  return true;
}

void FalconCollector::Upload(std::vector<FalconCounter> &list) {
  cJSON *root;
  root = cJSON_CreateArray();
	if (NULL == root) {
		LOG_WARN("failed to create root object.");
		return;
	}

  for (std::vector<FalconCounter>::iterator it = list.begin(); list.end() != it; ++it) {
	if (it->GetMetric().empty()) {
	  // ignore
	  continue;
	}

	cJSON *item = cJSON_CreateObject();
	if (NULL == item) {
	  LOG_WARN("fail to create item object.");
	  continue;
	}
	cJSON_AddStringToObject(item, "ENDPOINT", s_end_point.c_str());
	cJSON_AddStringToObject(item, "metric", it->GetMetric().c_str());
	cJSON_AddStringToObject(item, "value", it->GetValue().c_str());
	cJSON_AddNumberToObject(item, "timestamp", it->GetTime() / 1000);
	cJSON_AddNumberToObject(item, "step", 60);
	cJSON_AddStringToObject(item, "counterType", "GAUGE");
	cJSON_AddStringToObject(item, "tags", it->GetTags().c_str());

	cJSON_AddItemToArray(root, item);
  }
  char *out = cJSON_Print(root);
  std::string post_data = out;
  SAFE_DELETE(out);
  cJSON_Delete(root);
  std::string response;
  HttpPost(s_falcon_url, post_data, &response);
  LOG_DEBUG("falcon upload response = " << response);
}

int FalconCollector::OnWriteData(void *buffer, size_t size, size_t nmemb, void *lpVoid) {
  std::string *str = dynamic_cast<std::string *>((std::string *) lpVoid);
  if (!str || !buffer) {
	return -1;
  }

  char *data = static_cast<char *>(buffer);
  str->append(data, size * nmemb);
  return nmemb;
}

int FalconCollector::HttpPost(const std::string &url, const std::string &post, std::string *response) {
  CURLcode res;
  CURL *curl = curl_easy_init();
  if (!curl) {
	return CURLE_FAILED_INIT;
  }

  curl_easy_setopt(curl, CURLOPT_URL, url.c_str());
  curl_easy_setopt(curl, CURLOPT_POST, 1);
  curl_easy_setopt(curl, CURLOPT_POSTFIELDS, post.c_str());
  curl_easy_setopt(curl, CURLOPT_READFUNCTION, NULL);
  curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, FalconCollector::OnWriteData);
  curl_easy_setopt(curl, CURLOPT_WRITEDATA, static_cast<void *>(response));
  curl_easy_setopt(curl, CURLOPT_NOSIGNAL, 1);
  curl_easy_setopt(curl, CURLOPT_CONNECTTIMEOUT, 3);
  curl_easy_setopt(curl, CURLOPT_TIMEOUT, 5); // timeout 5s
  res = curl_easy_perform(curl);
  curl_easy_cleanup(curl);
  return res;
}

FalconItemPtr FalconCollector::GetAndReset() {
  FalconItemPtr clone_ptr(new FalconItemMap());
	muduo::MutexLockGuard lock(s_cache_check_lock);
	//std::cout << "do GetAndReset " << std::endl;
	FalconItemPtr swap_tmp_ptr = s_data_ptr;
	
	s_data_ptr = clone_ptr;
	clone_ptr = swap_tmp_ptr;
  return clone_ptr;
}
std::string FalconCollector::GenCacheKey(const std::string &metric, const std::string &tags) {
  return metric + tags;
}

void FalconCollector::SetValue(const std::string &metric, const std::string &tags, const std::string &value) {
  // make sure the cache exist.
	muduo::MutexLockGuard lock(s_cache_check_lock);
	//std::cout<< "do SetValue" << std::endl;
  FalconItemPtr data_ptr_temp = s_data_ptr;

  MakeSureCacheExists(data_ptr_temp, metric, tags);

  std::string key = GenCacheKey(metric, tags);
  FalconItemMap::iterator found = data_ptr_temp->find(key);
  found->second->SetValue(value);

}

void FalconCollector::Count(const std::string &metric, const std::string &tags) {
	muduo::MutexLockGuard lock(s_cache_check_lock);
  FalconItemPtr data_ptr_temp = s_data_ptr;
	
  MakeSureCacheExists(data_ptr_temp, metric, tags);

  std::string key = GenCacheKey(metric, tags);
  FalconItemMap::iterator found = data_ptr_temp->find(key);
  found->second->IncCount();
}

void FalconCollector::RecordTime(const std::string &metric, const std::string &tags, unsigned long mills) {
	muduo::MutexLockGuard lock(s_cache_check_lock);
  FalconItemPtr data_ptr_temp = s_data_ptr;

  MakeSureCacheExists(data_ptr_temp, metric, tags);

  std::string key = GenCacheKey(metric, tags);
  FalconItemMap::iterator found = data_ptr_temp->find(key);
  found->second->IncSumtime(mills);
}

void FalconCollector::SetRate(const std::string &metric,
							  const std::string &tags,
							  bool is_inc_rate_count) {
	muduo::MutexLockGuard lock(s_cache_check_lock);
	//std::cout<< "do SetRate " << std::endl;
  FalconItemPtr data_ptr_temp = s_data_ptr;

  MakeSureCacheExists(data_ptr_temp, metric, tags);

  std::string key = GenCacheKey(metric, tags);
  FalconItemMap::iterator found = data_ptr_temp->find(key);
  found->second->IncRateCount(is_inc_rate_count);
}

void FalconCollector::MakeSureCacheExists(FalconItemPtr &ptr, const std::string &metric, const std::string &tags) {
  std::string key = GenCacheKey(metric, tags);
  FalconItemMap::iterator found = ptr->find(key);
	if (ptr->end() == found) {
		found = ptr->find(key);
		if (ptr->end() == found) {
			boost::shared_ptr<FalconItem> item_ptr(new FalconItem());
			item_ptr->m_metric = metric;
			item_ptr->m_tags = tags;
			ptr->insert(std::make_pair<std::string, boost::shared_ptr<FalconItem> >(key, item_ptr));
		}
	}
}
}
