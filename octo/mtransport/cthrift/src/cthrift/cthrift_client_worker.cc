//
// Created by Chao Shu on 16/3/6.
//

#include "cthrift_uniform_protocol.h"
#include "cthrift_tbinary_protocol.h"
#include "cthrift_client_worker.h"
#include "cthrift_kms.h"

using namespace std;
using namespace muduo::net;
using namespace cthrift;

const int32_t CthriftClientWorker::kI32HighWaterSize = 64 * 1024; //64K
const int8_t CthriftClientWorker::kI8TimeWheelNum = 2;

void ConnInfo::UptSgservice(const SGService &sgservice) {
  double d_old_weight = CthriftSgagent::FetchOctoWeight(sgservice_.fweight,
                                                        static_cast<double>(sgservice_.weight));
  double d_new_weight = CthriftSgagent::FetchOctoWeight(sgservice.fweight,
                                                        static_cast<double>(sgservice.weight));

  CLOG_STR_DEBUG("d_old_weight " << d_old_weight << " d_new_weight "
            << d_new_weight);

  if (!CheckDoubleEqual(d_old_weight, d_new_weight)) {
    CLOG_STR_DEBUG("need update weight buf");

    //real conn NOT erase, just del index
    p_map_weight_tcpclientwp_->erase(it_map_weight_tcpclientwp_index_);

    it_map_weight_tcpclientwp_index_ =
        p_map_weight_tcpclientwp_->insert(std::make_pair(
            d_new_weight,
            sp_tcpclient_));
  }

  sgservice_ = sgservice;
}

void ConnInfo::setSp_tcpclient_(const TcpClientSharedPtr &sp_tcpclient) {
  if (CTHRIFT_UNLIKELY(sp_tcpclient_.get())) {
    CLOG_STR_ERROR("client ip: " << (sp_tcpclient_->connection()
        ->peerAddress()).toIp() << " port: "
              << (sp_tcpclient_->connection()->peerAddress()).toPort()
              << " replace");

    p_map_weight_tcpclientwp_->erase(it_map_weight_tcpclientwp_index_);
  }

  sp_tcpclient_ = sp_tcpclient;

  double d_weight = CthriftSgagent::FetchOctoWeight(sgservice_.fweight,
                                                    static_cast<double>(sgservice_.weight));
  CLOG_STR_DEBUG("dweight " << d_weight);

  it_map_weight_tcpclientwp_index_ =
      p_map_weight_tcpclientwp_->insert(std::make_pair(d_weight,
                                                       sp_tcpclient_));
}


CthriftClientWorker::CthriftClientWorker(const std::string &str_svr_appkey,
                                         const std::string &str_cli_appkey,
                                         const std::string &str_serviceName_filter,
                                         const int32_t &i32_port_filter,
                                         const int32_t &i32_timeout,
                                         const bool &b_auth,
                                         const std::string& token)
    : cond_avaliable_conn_ready_(mutexlock_avaliable_conn_ready_),
      str_svr_appkey_(str_svr_appkey),
      str_client_appkey_(str_cli_appkey),
      str_serviceName_filter_(str_serviceName_filter),
      i32_port_filter_(i32_port_filter),
      i32_timeout_(i32_timeout),
      i8_destructor_flag_(0),
      b_auth_(b_auth),
      str_auth_token_(token),
      unzip_buf_(0),
      p_async_event_loop_(NULL),
      tools(str_cli_appkey){  //atomic_avaliable_conn_num_ defalut init by value
  //start real worker thread
  sp_event_thread_ =
      boost::make_shared<muduo::net::EventLoopThread>(EventLoopThread::ThreadInitCallback(), "cthrift_cli_IO");
  p_event_loop_ = sp_event_thread_->startLoop();
  p_event_loop_->runInLoop(boost::bind(&CthriftClientWorker::InitWorker,
                                       this)); //will use event_loop in

  boost::shared_ptr <TMemoryBuffer>
      tmp_buf = boost::make_shared<TMemoryBuffer>();
  sp_p_tmemorybuffer_ = new boost::shared_ptr<TMemoryBuffer>();
  *sp_p_tmemorybuffer_ = tmp_buf;

  boost::shared_ptr <CthriftTBinaryProtocolWithTMemoryBuf>
      tmp_prot = boost::make_shared<
      CthriftTBinaryProtocolWithTMemoryBuf>(*sp_p_tmemorybuffer_);

  sp_p_cthrift_tbinary_protocol_ =
      new boost::shared_ptr<CthriftTBinaryProtocolWithTMemoryBuf>();  //A memory buffer is a tranpsort, NO SERVER/CLIENT specified since just serial
  *sp_p_cthrift_tbinary_protocol_ = tmp_prot;
}

void
CthriftClientWorker::OnConn4Sentinel(const muduo::net::TcpConnectionPtr &conn) {
  CLOG_STR_INFO(conn->localAddress().toIpPort() << " -> "
           << conn->peerAddress().toIpPort() << " is "
           << (conn->connected() ? "UP" : "DOWN"));

  if (conn->connected()) {
    boost::shared_ptr <HttpContext> tmp = boost::make_shared<HttpContext>();
    conn->setContext(tmp);
    conn->setTcpNoDelay(true);

    //maybe send a few duplicate request, but will stop when sentinel addrs filled, acceptable.
    if (CTHRIFT_UNLIKELY(1 >= map_ipport_spconninfo_.size())) {
      CLOG_STR_DEBUG("sgagent still NOT fill sentinel address");

      muduo::net::Buffer buf;
      buf.append(CthriftSgagent::str_sentinel_http_request_);

      CLOG_STR_DEBUG("Send appkey tags buf " << buf.toStringPiece().data());

      conn->send(&buf);
    }
  } else {
    //http服务端关闭了链接后，这里的tcp链接应该close tcpclient，避免fd close_wait
    if(sp_tcpclient_sentinel_) {
      sp_tcpclient_sentinel_->disconnect();
      sp_tcpclient_sentinel_.reset();
    }
  }
}

void
CthriftClientWorker::OnMsg4Sentinel(const muduo::net::TcpConnectionPtr &conn,
                                    muduo::net::Buffer *buf,
                                    muduo::Timestamp receiveTime) {	
  CLOG_STR_DEBUG("OnMsg buf " << (buf->toStringPiece()).data());

  if (CTHRIFT_UNLIKELY((conn->getContext()).empty())) {
    CLOG_STR_ERROR("address: " << (conn->peerAddress()).toIpPort() << " "
        "context empty");    //NOT clear here
    return;
  }

  HttpContextSharedPtr pConnInfo;
  try {
    pConnInfo = boost::any_cast<HttpContextSharedPtr>(conn->getContext());
  } catch (boost::bad_any_cast e) {
    CLOG_STR_ERROR("bad_any_cast:" << e.what());
    return;
  }

  if (buf->readableBytes() < pConnInfo->u32_want_len) {
    CLOG_STR_DEBUG("NOT enough");
    return;
  }

  if (!ParseHttpRequest(&(pConnInfo->u32_want_len),
                        buf,
                        &(pConnInfo->http_context),
                        muduo::Timestamp::now())) {
    CLOG_STR_ERROR("parseRequest failed");
    return;
  }

  if ((pConnInfo->http_context).gotAll()) {
    const muduo::net::HttpRequest
        &httpReq = (pConnInfo->http_context).request();
    const string &str_ori_body = httpReq.body();

    if (0 == str_ori_body.size()) {
      CLOG_STR_ERROR("No body from sentinel");
      return;
    }

    //decompress
    string str_body;
    string strCotentCode = httpReq.getHeader("Content-Encoding");
    if (string::npos != strCotentCode.find("gzip")) {
      CLOG_STR_DEBUG("strCotentCode " << strCotentCode);

      uLong ulBodyLen = 200000;    //fix
      int iRet
          =
          Httpgzdecompress(reinterpret_cast<unsigned char *>(const_cast<char *>(str_ori_body.c_str())),
                           static_cast<uLong>(str_ori_body.size()),
                           reinterpret_cast<unsigned char *>(unzip_buf_),
                           &ulBodyLen);
      if (0 > iRet) {
        CLOG_STR_ERROR("Httpgzdecompress failed, iRet " << iRet);
        return;
      }

      CLOG_STR_DEBUG("ulBodyLen " << ulBodyLen);
      str_body.assign(unzip_buf_, ulBodyLen);
    } else {
      str_body.assign(str_ori_body);
    }

    CLOG_STR_DEBUG("str_body " << str_body);
    (pConnInfo->http_context).reset();

    vector <SGService> vec_sgservice;
    if (CthriftSgagent::ParseSentineSgagentList(str_body, &vec_sgservice)) {
      return;
    }

    if(CTHRIFT_LIKELY(!CheckLocalSgagentHealth())) {
      UpdateSvrList(vec_sgservice);
    }
  }
}

int8_t CthriftClientWorker::CheckRegion(const double &d_weight) {
  if (d_weight < CthriftSgagent::kDSecondRegionMin) {
    return 3;
  } else if (d_weight < CthriftSgagent::kDFirstRegionMin) {
    return 2;
  }

  return 1;
}

bool ConnInfo::CheckConnHealthy(void) const {
  if (CTHRIFT_UNLIKELY(!(sp_tcpclient_.get()))) {
    CLOG_STR_ERROR("sp_tcpconn invalid appkey: " << sgservice_.appkey
              << " ip:"  << sgservice_.ip << " port: " << sgservice_.port);
    return false;
  }

  muduo::net::TcpConnectionPtr sp_tcpconn = sp_tcpclient_->connection();
  if (CTHRIFT_UNLIKELY(!sp_tcpconn || !(sp_tcpconn.get()))) {
    CLOG_STR_ERROR("sp_tcpconn invalid appkey: " << sgservice_.appkey
              << " ip:"  << sgservice_.ip << " port: " << sgservice_.port);
    return false;
  }

  if (CTHRIFT_UNLIKELY(!(sp_tcpconn->connected()))) {
    CLOG_STR_DEBUG("address: " << (sp_tcpconn->peerAddress()).toIpPort()
              << "NOT connected");
    return false;
  }

  if (CTHRIFT_UNLIKELY((sp_tcpconn->getContext()).empty())) {
    CLOG_STR_ERROR("address: " << (sp_tcpconn->peerAddress()).toIpPort() << " "
        "context empty");    //NOT clear here
    return false;
  }

  Context4WorkerSharedPtr sp_context;
  try {
    sp_context = boost::any_cast<Context4WorkerSharedPtr>
        (sp_tcpconn->getContext());
  } catch (boost::bad_any_cast e) {
    CLOG_STR_ERROR("bad_any_cast:" << e.what() << " peer address "
              << (sp_tcpconn->peerAddress()).toIpPort());
    return false;
  }

  if (CTHRIFT_UNLIKELY(sp_context->b_highwater || sp_context->b_occupied)) {
    CLOG_STR_WARN("address: " << (sp_tcpconn->peerAddress()).toIpPort() <<
             " b_highwater " << sp_context->b_highwater << " b_occupied "
             << sp_context->b_occupied << " ignore");
    return false;
  }

  return true;
}

int8_t CthriftClientWorker::ChooseNextReadyConn(TcpClientWeakPtr *p_wp_tcpcli) {
  if (CTHRIFT_UNLIKELY(0 == p_multimap_weight_wptcpcli_->size())) {
    CLOG_STR_ERROR("multimap_weight_wptcpcli_ empty");
    return -1;
  }

  string str_default_sgagent_port;

  try{
    str_default_sgagent_port = boost::lexical_cast<std::string>(CthriftSgagent::kU16DefaultSgagentPort);
  } catch(boost::bad_lexical_cast & e) {

    CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
              << "CthriftSgagent::kU16DefaultSgagentPort " << CthriftSgagent::kU16DefaultSgagentPort);
  }

  UnorderedMapIpPort2ConnInfoSP iter_ipport_spconninfo;

  if (CthriftSgagent::kStrSgagentAppkey == str_svr_appkey_) {
    iter_ipport_spconninfo = map_ipport_spconninfo_.find
        (CthriftSgagent::str_local_ip_ + ":" +   //NOT be "127.0.0.1" in normal
            // case, but be when cannot fetch local ip
            str_default_sgagent_port);

    if (map_ipport_spconninfo_.end() != iter_ipport_spconninfo
        && iter_ipport_spconninfo->second->CheckConnHealthy()) {
      CLOG_STR_DEBUG("sgagent rpc, local agent work, use it");

      TcpClientWeakPtr wp_tcpcli
          (iter_ipport_spconninfo->second->getSp_tcpclient_());
      *p_wp_tcpcli = wp_tcpcli;

      return 0;
    }

    CLOG_STR_INFO("sgagent rpc, local agent NOT work, use sentinel");
  }

  boost::unordered_map<double, vector<TcpClientWeakPtr> > map_weight_vec;
  vector<double> vec_weight;

  double d_last_weight = -1.0;
  double d_total_weight = 0.0;
  int8_t i8_stop_region = 2;  //init not necessary, but for safe
  string str_port;

  muduo::net::TcpConnectionPtr sp_tcpconn;

  MultiMapIter iter = p_multimap_weight_wptcpcli_->begin();
  while (p_multimap_weight_wptcpcli_->end() != iter) {
    TcpClientSharedPtr sp_tcpcli((iter->second).lock());
    if (CTHRIFT_UNLIKELY(!sp_tcpcli || !(sp_tcpcli.get()))) {
      CLOG_STR_ERROR("tcpclient NOT avaliable");

      p_multimap_weight_wptcpcli_->erase(iter++);
      continue;
    }

    sp_tcpconn = sp_tcpcli->connection();
    if (CTHRIFT_UNLIKELY(!sp_tcpconn)) {
      CLOG_STR_INFO("NOT connected yet");
      ++iter;
      continue;
    }

    CLOG_STR_DEBUG("Address: " << (sp_tcpconn->peerAddress()).toIpPort() << " "
        "weight " << iter->first);

    try{
      str_port = boost::lexical_cast<std::string>((sp_tcpconn->peerAddress()).toPort());
    } catch(boost::bad_lexical_cast & e) {

      CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
                << "tcp connnect peer port : " << (sp_tcpconn->peerAddress()).toPort());

      ++iter;
      continue;
    }

    iter_ipport_spconninfo = map_ipport_spconninfo_.find(
        (sp_tcpconn->peerAddress()).toIp() + ":" + str_port);
    if (CTHRIFT_UNLIKELY(
        iter_ipport_spconninfo == map_ipport_spconninfo_.end())) {
      CLOG_STR_ERROR("Not find ip:"
                << (sp_tcpconn->peerAddress()).toIp() << " port:"
                << str_port << " in map_ipport_spconninfo_");

      p_multimap_weight_wptcpcli_->erase(iter++);
      continue;
    }

    if (!(iter_ipport_spconninfo->second->CheckConnHealthy())) {
      ++iter;
      continue;
    }

    //weight random choose algorithm
    //1. sum all weight(one weight = single weight * same weight conn num)
    //2. random total_weight,get a random_weight
    //3. choose random_weight region from all weight.
    if (!CheckDoubleEqual(d_last_weight, iter->first)) { //new weight
      if (CTHRIFT_LIKELY(
          !CheckDoubleEqual(d_last_weight, -1.0))) { //NOT init
        if (i8_stop_region <= CheckRegion(iter->first)) {  //if already get
          // conn and next region reach, stop
          CLOG_STR_DEBUG("stop region " << i8_stop_region << " "
              "iter->first " << iter->first);
          break;
        }

        d_total_weight += d_last_weight * static_cast<double>
        (map_weight_vec[d_last_weight]
                .size
                    ());
      } else {
        i8_stop_region =
            static_cast<int8_t>(CheckRegion(iter->first)
                + 1); //set stop region by the first weight

        CLOG_STR_DEBUG("i8_stop_region set to be " << i8_stop_region);
      }

      vec_weight.push_back(iter->first);
      d_last_weight = iter->first;
    }

    map_weight_vec[iter->first].push_back(iter->second);
    ++iter;
  }

  if (CTHRIFT_UNLIKELY(0 == vec_weight.size())) {
    CLOG_STR_INFO("Not avaliable conn can be choosed, maybe all occupied");
    return 1;
  }
  //将所有候选节点的权重进行求和：d_total_weight
  //将权重看作一条线段，d_total_weight是线段长度
  //不同权重的节点占据这条线段的不同部分
  //产生0~d_total_weight一个随机数，落入的权重线段某个区域; 选择该区域归属的节点列表；从这个节点列表中随机选择一个节点。
  d_total_weight +=
      d_last_weight * static_cast<double>(map_weight_vec[d_last_weight].size
          ());

  CLOG_STR_DEBUG("d_total_weight " << d_total_weight);
  //伪随机数在小范围下不均匀（0~1)，放大1000倍解决该问题
  d_total_weight *= static_cast<double>(1000.0);

  double d_choose = 0.0;
  double d_tmp = 0.0;
  //产生的随机数落入权重线段某个区域，后面的while其实是在找这个“区域”
  double d_random_weight = fmod(static_cast<double>(rand()), d_total_weight);
  vector<double>::iterator it_vec = vec_weight.begin();
  while (vec_weight.end() != it_vec) {
    //伪随机数在小范围下不均匀（0~1)，放大1000倍进行平滑处理
    d_tmp += (*it_vec) * static_cast<double>(map_weight_vec[*it_vec].size()) * static_cast<double>(1000.0);
    if (d_tmp > d_random_weight) {
      d_choose = *it_vec;
      break;
    }

    ++it_vec;
  }

  boost::unordered_map < double, vector < TcpClientWeakPtr > > ::iterator
  it_map = map_weight_vec.find(d_choose);
  if (CTHRIFT_UNLIKELY(it_map == map_weight_vec.end())) {
    CLOG_STR_ERROR("not find weight " << d_choose);
    return -1;
  }

  if (1 == (it_map->second).size()) {
    *p_wp_tcpcli = *((it_map->second).begin());
  } else {
    CLOG_STR_DEBUG((it_map->second).size() << " conn need be choose one "
        "equally");

    *p_wp_tcpcli = (it_map->second)[rand() % ((it_map->second).size())];
  }




  return 0;
}

void
CthriftClientWorker::UpdateAuthToken(const std::string&  token){
  CLOG_STR_DEBUG("origin str_auth_token_ :" << str_auth_token_ << "  token " << token );
  str_auth_token_ = token;
}

void
CthriftClientWorker::UpdateSvrList(const vector <SGService> &vec_sgservice) {
  vector<SGService>::const_iterator it_vec;
  boost::unordered_map<string, SGService>::iterator it_map_sgservice;

  string str_port;
  string str_sgagent_default_port;

  try{
    str_sgagent_default_port = boost::lexical_cast<std::string>(CthriftSgagent::kU16DefaultSgagentPort);
  } catch(boost::bad_lexical_cast & e) {

    CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
              << "CthriftSgagent::kU16DefaultSgagentPort " << CthriftSgagent::kU16DefaultSgagentPort);

    return;
  }

  vector <SGService> vec_sgservice_add;
  vector <SGService> vec_sgservice_del;
  vector <SGService> vec_sgservice_chg;

  if (CTHRIFT_UNLIKELY(
      0 == map_ipport_sgservice_.size() && 0 == vec_sgservice.size())) {
    CLOG_STR_WARN("Init svr list but empty srvlist");
  } else if (CTHRIFT_UNLIKELY(0 == map_ipport_sgservice_.size())) {
    it_vec = vec_sgservice.begin();
    CLOG_STR_INFO("Init svr list for appkey " << it_vec->appkey);

    while (it_vec != vec_sgservice.end()) {
      if (CTHRIFT_UNLIKELY(2 != it_vec->status)) {
        CLOG_STR_DEBUG("svr info: "
                  << CthriftSgagent::SGService2String(*it_vec) << " IGNORED");
        ++it_vec;
        continue;
      }

      try{
        str_port = boost::lexical_cast<std::string>(it_vec->port);
      } catch(boost::bad_lexical_cast & e) {

        CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
                  << "it_vec->port " << it_vec->port);

        ++it_vec;
        continue;
      }

      map_ipport_sgservice_.insert(make_pair(it_vec->ip + ":" + str_port,
                                             *it_vec));

      vec_sgservice_add.push_back(*(it_vec++));
    }
  } else if (CTHRIFT_UNLIKELY(0 == vec_sgservice.size())) {
    CLOG_STR_WARN("vec_sgservice empty");

    it_map_sgservice = map_ipport_sgservice_.begin();
    while (it_map_sgservice != map_ipport_sgservice_.end()) {    //exclude local sgagent since sentinel list NOT include it
      if(CthriftSgagent::str_local_ip_ + ":" + str_sgagent_default_port
            != it_map_sgservice->first){
        vec_sgservice_del.push_back(it_map_sgservice->second);
        map_ipport_sgservice_.erase(it_map_sgservice++);
      }else{
        it_map_sgservice++;
      }
    }
  } else {
    boost::unordered_map <string, SGService>
        map_tmp_locate_del(map_ipport_sgservice_);
    map_tmp_locate_del.erase(CthriftSgagent::str_local_ip_ + ":"
                                 + str_sgagent_default_port); //exclude local sgagent

    it_vec = vec_sgservice.begin();
    while (it_vec != vec_sgservice.end()) {
      if (CTHRIFT_UNLIKELY(2 != it_vec->status)) {
        CLOG_STR_DEBUG("svr info: "
                  << CthriftSgagent::SGService2String(*it_vec) << " IGNORED");
        ++it_vec;
        continue;
      }

      try{
        str_port = boost::lexical_cast<std::string>(it_vec->port);
      } catch(boost::bad_lexical_cast & e) {

        CLOG_STR_DEBUG("boost::bad_lexical_cast :" << e.what()
                  << "it_vec->port " << it_vec->port);

        ++it_vec;
        continue;
      }

      string str_key(it_vec->ip + ":" + str_port);
      it_map_sgservice = map_ipport_sgservice_.find(str_key);
      if (map_ipport_sgservice_.end() == it_map_sgservice) {
        CLOG_STR_DEBUG("ADD svr list info: "
                  << CthriftSgagent::SGService2String(*it_vec));

        vec_sgservice_add.push_back(*it_vec);
        map_ipport_sgservice_.insert(make_pair(str_key, *it_vec));
      } else {
        map_tmp_locate_del.erase(str_key);

        if (it_map_sgservice->second != *it_vec) {
          CLOG_STR_DEBUG("UPDATE svr list. old info: "
                    << CthriftSgagent::SGService2String(it_map_sgservice->second));
          CLOG_STR_DEBUG(" new info: "
                    << CthriftSgagent::SGService2String(*it_vec));

          it_map_sgservice->second = *it_vec;

          vec_sgservice_chg.push_back(*it_vec);

          map_tmp_locate_del.erase(str_key);
        }
      }

      ++it_vec;
    }

    if (map_tmp_locate_del.size()) {
      CLOG_STR_DEBUG("DEL svr list");

      it_map_sgservice = map_tmp_locate_del.begin();
      while (it_map_sgservice != map_tmp_locate_del.end()) {
        CLOG_STR_DEBUG("del svr info: "
                  << CthriftSgagent::SGService2String(it_map_sgservice->second));

        vec_sgservice_del.push_back(it_map_sgservice->second);
        map_ipport_sgservice_.erase((it_map_sgservice++)->first);
      }
    }
  }

  AddSrv(vec_sgservice_add);
  DelSrv(vec_sgservice_del);
  ChgSrv(vec_sgservice_chg);
}

void CthriftClientWorker::InitSgagentHandlerThread(void) {
  /*sp_cthrift_transport_sgagent_ =
      boost::make_shared<CthriftTransport>(CthriftSgagent::kStrSgagentAppkey,
                                           kI32DefultSgagentTimeoutMS,
                                           str_client_appkey_);*/

  //cthrift_client->client_worker->cthrift_client->client_worker, NO dead loop since when appkey == sgagent, will NOT call here again. ONLY a shared_ptr of cthrift_client left finally.
  sp_cthrift_client_ =
      boost::make_shared<CthriftClient>(CthriftSgagent::kStrSgagentAppkey,
                                        str_client_appkey_,
                                        kI32DefultSgagentTimeoutMS*4);
  //kI32DefultSgagentTimeoutMS*4: sg无缓存时一次拉取服务列表>120ms，因此设置4*50ms为sg_cthrift_client超时时间

  //TProtocol is abstract class, use concrete class pointer
  /*boost::shared_ptr<TProtocol>
      protocol(new CthriftTBinaryProtocol(sp_cthrift_transport_sgagent_));*/

  sp_sgagent_client_ =
      boost::make_shared<SGAgentClient>(sp_cthrift_client_->GetCthriftProtocol());

  /*sp_cthrift_transport_sgagent_->open();*/

  p_event_loop_sgagent_->runInLoop(boost::bind(&CthriftClientWorker::GetSvrList,
                                               this));
  p_event_loop_sgagent_->runEvery(CthriftSgagent::kDGetSvrListIntervalSecs,
                                  boost::bind(&CthriftClientWorker::GetSvrList,
                                              this));
}


void CthriftClientWorker::InitAuthTokenUpdateThread(void) {

  p_event_loop_sgagent_->runInLoop(boost::bind(&CthriftClientWorker::GetAuthToken,
                                               this));
  p_event_loop_sgagent_->runEvery(CthriftSgagent::kDGetAuthTokenIntervalSecs,
                                  boost::bind(&CthriftClientWorker::GetAuthToken,
                                              this));
}

void CthriftClientWorker::InitSentinel(void) {
  if(CTHRIFT_LIKELY(CthriftSgagent::b_is_open_sentinel_)) {
    //conn to sentinel
    sp_tcpclient_sentinel_ = boost::make_shared<muduo::net::TcpClient>(
            p_event_loop_,
            CthriftSgagent::sentinel_url_addr_,
            "sentinel tcpclient");

    sp_tcpclient_sentinel_->setConnectionCallback(boost::bind(&CthriftClientWorker::OnConn4Sentinel,
                                                              this,
                                                              _1));
    sp_tcpclient_sentinel_->setMessageCallback(boost::bind(&CthriftClientWorker::OnMsg4Sentinel,
                                                           this,
                                                           _1,
                                                           _2,
                                                           _3));
    sp_tcpclient_sentinel_->enableRetry();
    sp_tcpclient_sentinel_->connect();
  }
}

void CthriftClientWorker::UnInitSentinel(void) {

  if(CTHRIFT_LIKELY(CheckLocalSgagentHealth())){
      CLOG_STR_DEBUG("sgagent rpc, local agent work, use it, remove sentinel");

      if(sp_tcpclient_sentinel_) {
        sp_tcpclient_sentinel_->disconnect();
        sp_tcpclient_sentinel_.reset();
      }

      vector <SGService> vec_sgservice;
      UpdateSvrList(vec_sgservice);
      return ;
    }
}

bool CthriftClientWorker::CheckLocalSgagentHealth(void){
  string str_default_sgagent_port;

  try{
    str_default_sgagent_port = boost::lexical_cast<std::string>(CthriftSgagent::kU16DefaultSgagentPort);
  } catch(boost::bad_lexical_cast & e) {

    CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
              << "CthriftSgagent::kU16DefaultSgagentPort " << CthriftSgagent::kU16DefaultSgagentPort);
  }

  UnorderedMapIpPort2ConnInfoSP iter_ipport_spconninfo;

  if (CthriftSgagent::kStrSgagentAppkey == str_svr_appkey_) {
    iter_ipport_spconninfo = map_ipport_spconninfo_.find
            (CthriftSgagent::str_local_ip_ + ":" + str_default_sgagent_port);

    if (map_ipport_spconninfo_.end() != iter_ipport_spconninfo
        && iter_ipport_spconninfo->second->CheckConnHealthy()) {
      CLOG_STR_DEBUG("sgagent rpc, local agent work, use it");

      return true;
    }
  }

  return false;
}

void CthriftClientWorker::CheckLocalSgagent(void){

    if (CthriftSgagent::kStrSgagentAppkey != str_svr_appkey_){
        return;
    }

    if(CTHRIFT_LIKELY(CheckLocalSgagentHealth())){
        CLOG_STR_DEBUG("sgagent rpc, local agent work, return");

        if(map_ipport_spconninfo_.size() > 1){
           p_event_loop_->runAfter(kDRetryIntervalSec,
                                  boost::bind(&CthriftClientWorker::UnInitSentinel, this));
        }

        return ;
    }

    // to init InitSentinel
    CLOG_STR_WARN("sgagent rpc, local agent NOT work, init sentinel");

    if(map_ipport_spconninfo_.size() <= 1 && 0 == i8_destructor_flag_){
      InitSentinel();
    }
}

void CthriftClientWorker::InitWorker(void) {
  p_multimap_weight_wptcpcli_ =
      new multimap<double, TcpClientWeakPtr, WeightSort>;//exit del, safe

  if (CthriftSgagent::kStrSgagentAppkey == str_svr_appkey_) {
    unzip_buf_ = new char[200000]; //200k for sentinel content

    SGService sgservice;
    CthriftSgagent::PackDefaultSgservice(CthriftSgagent::kStrSgagentAppkey,
                                         CthriftSgagent::str_local_ip_,
                                         CthriftSgagent::kU16DefaultSgagentPort,
                                         &sgservice);
    vector <SGService> vec_sgservice;
    vec_sgservice.push_back(sgservice);

    UpdateSvrList(vec_sgservice);
    //启动时，本地sg可用50ms后开始初始化哨兵；避免初始化哨兵占用work线程，造成第一次拉取服务列表超时.
    p_event_loop_->runAfter(0.05,
                            boost::bind(&CthriftClientWorker::CheckLocalSgagent, this));

  } else {
    sp_event_thread_sgagent_ =
        boost::make_shared<muduo::net::EventLoopThread>(EventLoopThread::ThreadInitCallback(), "cthrift_sg_io");

    p_event_loop_sgagent_ = sp_event_thread_sgagent_->startLoop();
    p_event_loop_sgagent_->runInLoop(boost::bind(&CthriftClientWorker::InitSgagentHandlerThread,
                                                 this));

    if(b_auth_ && str_auth_token_.empty()){
      CLOG_STR_INFO("b_auth && str_auth_token_ empty");
      p_event_loop_sgagent_->runInLoop(boost::bind(&CthriftClientWorker::InitAuthTokenUpdateThread,
                                                   this));
    }

    CLOG_STR_INFO("start sgagent handle thread");

    //cthrift client version collection
    //catclient only init once
    if(CTHRIFT_LIKELY(CthriftSgagent::b_is_open_cat_)) {

      CthriftSgagent::cat_appkey_ = str_client_appkey_;
      pthread_once(&CthriftSgagent::cat_once_, &CthriftSgagent::InitCat);
      p_event_loop_sgagent_->runEvery(60*5, boost::bind(
              &CthriftSgagent::VersionCollection, "OctoService.Version", cthrift::version));
    }
  }
}

bool CthriftClientWorker::FilterAll(const SGService& sg) {
  return sg.serviceInfo.find(str_serviceName_filter_) == sg.serviceInfo.end()
         || i32_port_filter_ != sg.port;
}

bool CthriftClientWorker::FilterService(const SGService& sg) {
  return sg.serviceInfo.find(str_serviceName_filter_) == sg.serviceInfo.end();
}

bool CthriftClientWorker::FilterPort(const SGService& sg) {
  return i32_port_filter_ != sg.port;
}

void CthriftClientWorker::GetAuthToken(void){
  std::string &token = tools.GetLocalTokenString_X();

  if( CthriftSgagent::b_is_open_cat_ ){
    logEvent("cthrift.client.auth", str_client_appkey_.c_str() , CAT_SUCCESS, NULL);
  }

  p_event_loop_->runInLoop(boost::bind(&CthriftClientWorker::UpdateAuthToken,
                                       this,
                                       token));
}

void CthriftClientWorker::GetSvrList(void) {
  ProtocolResponse _return;

  try {
    CLOG_STR_DEBUG("str_svr_appkey_ " << str_svr_appkey_);

    ProtocolRequest request;
    if(!CthriftSgagent::str_swimlane_.empty()){
      request.__set_swimlane(CthriftSgagent::str_swimlane_);
    }
    request.__set_remoteAppkey(str_svr_appkey_);
    request.__set_protocol("thrift");
    request.__set_localAppkey(str_client_appkey_);
    sp_sgagent_client_ -> getServiceListByProtocol(_return, request);
  } catch (TException &tx) {
    CLOG_STR_ERROR("service getsvrlist error " << tx.what());

    //sp_cthrift_transport_sgagent_->close(); //NOT close, cthrift transport will recover
    return;
  }
  if (CTHRIFT_LIKELY(str_serviceName_filter_.empty() && -1 == i32_port_filter_)) {
    //正常case
    CLOG_STR_DEBUG("recv vec_sgservice.size " << _return.servicelist.size());
    for (size_t i = 0; i < _return.servicelist.size(); i++) {
      CLOG_STR_DEBUG("[" << i << "]" << ": "
                         << CthriftSgagent::SGService2String(_return.servicelist[i]));
    }
  } else {
    //过滤case
    vector<SGService> &filter_list = _return.servicelist;
    if (!str_serviceName_filter_.empty() && -1 != i32_port_filter_) {
      filter_list.erase(remove_if(filter_list.begin(), filter_list.end(),
                                  boost::bind(&CthriftClientWorker::FilterAll, this, _1)),
                                _return.servicelist.end());
    } else if (str_serviceName_filter_.empty()) {
      filter_list.erase(remove_if(filter_list.begin(), filter_list.end(),
                                  boost::bind(&CthriftClientWorker::FilterPort, this, _1)),
                                _return.servicelist.end());
    } else {
      filter_list.erase(remove_if(filter_list.begin(), filter_list.end(),
                                  boost::bind(&CthriftClientWorker::FilterService, this, _1)),
                                _return.servicelist.end());
    }
    CLOG_STR_DEBUG("filter serviceName vec_sgservice.size " << _return.servicelist.size());
  }
  p_event_loop_->runInLoop(boost::bind(&CthriftClientWorker::UpdateSvrList,
                                       this,
                                       _return.servicelist));
}

void CthriftClientWorker::AddSrv(const vector <SGService> &vec_add_sgservice) {
  string str_port;
  vector <SGService> vec_chg_sgservice;
  MultiMapIter it_multimap;

  vector<SGService>::const_iterator it_sgservice = vec_add_sgservice
      .begin();
  while (it_sgservice != vec_add_sgservice.end()) {
    const SGService &sgservice = *it_sgservice;

    try{
      str_port = boost::lexical_cast<std::string>(sgservice.port);
    } catch(boost::bad_lexical_cast & e) {

      CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
                << "sgservice.port " << sgservice.port);

      ++it_sgservice;
      continue;
    }

    ConnInfoSharedPtr
        &sp_conninfo = map_ipport_spconninfo_[sgservice.ip + ":" + str_port];

    if (CTHRIFT_UNLIKELY(sp_conninfo.get())) {
      CLOG_STR_WARN("svr " << CthriftSgagent::SGService2String(sgservice)
               << " already exist in map_ipport_sptcpcli, just change it");

      vec_chg_sgservice.push_back(sgservice);
      ++it_sgservice;

      continue;
    }

    sp_conninfo = boost::make_shared<ConnInfo>(sgservice,
                                               p_multimap_weight_wptcpcli_);

    boost::shared_ptr <muduo::net::TcpClient>
        sp_tcp_cli_tmp = boost::make_shared<muduo::net::TcpClient>(
        p_event_loop_,
        muduo::net::InetAddress(
            sgservice.ip,
            static_cast<uint16_t>(sgservice.port)),
        "client worker for appkey "
            + sgservice.appkey);

    sp_conninfo->setSp_tcpclient_(sp_tcp_cli_tmp);//will set weight buf inside

    TcpClientSharedPtr &sp_tcpcli = sp_conninfo->getSp_tcpclient_();

    sp_tcpcli->setConnectionCallback(boost::bind(&CthriftClientWorker::OnConn,
                                                 this,
                                                 _1));

    sp_tcpcli->setMessageCallback(boost::bind(&CthriftClientWorker::OnMsg,
                                              this,
                                              _1,
                                              _2,
                                              _3));

    sp_tcpcli->setWriteCompleteCallback(boost::bind(&CthriftClientWorker::OnWriteComplete,
                                                    this,
                                                    _1));

    sp_tcpcli->enableRetry();
    sp_tcpcli->connect();

    ++it_sgservice;
  }

  if (vec_chg_sgservice.size()) {
    CLOG_STR_ERROR("Add trans to Chg");
    ChgSrv(vec_chg_sgservice);
  }
}

void CthriftClientWorker::DelSrv(const vector <SGService> &vec_del_sgservice) {
  string str_port;

  vector<SGService>::const_iterator it_sgservice = vec_del_sgservice.begin();
  while (it_sgservice != vec_del_sgservice.end()) {
    const SGService &sgservice = *it_sgservice;

    try{
      str_port = boost::lexical_cast<std::string>(sgservice.port);
    } catch(boost::bad_lexical_cast & e) {

      CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
                << "sgservice.port " << sgservice.port);

      ++it_sgservice;
      continue;
    }

    //TODO grace exit??
    //tcpclient exit will close connection, conninfo exit will clear weight buf
    map_ipport_spconninfo_.erase(sgservice.ip + ":" + str_port);

    ++it_sgservice;
  }
}

void CthriftClientWorker::ChgSrv(const vector <SGService> &vec_chg_sgservice) {
  string str_port;
  string str_key;

  vector <SGService> vec_add_sgservice;

  vector<SGService>::const_iterator
      it_sgservice = vec_chg_sgservice.begin();
  while (it_sgservice != vec_chg_sgservice.end()) {
    const SGService &sgservice = *it_sgservice;


    try{
      str_port = boost::lexical_cast<std::string>(sgservice.port);
    } catch(boost::bad_lexical_cast & e) {

      CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
                << "sgservice.port " << sgservice.port);

      ++it_sgservice;
      continue;
    }

    str_key.assign(sgservice.ip + ":" + str_port);
    UnorderedMapStr2SpConnInfoIter
        it_map = map_ipport_spconninfo_.find(str_key);
    if (it_map == map_ipport_spconninfo_.end()) {
      CLOG_STR_WARN("Not find " << str_key << " for appkey "
               << sgservice.appkey
               << " in map_ipport_spconninfo_, readd it");

      vec_add_sgservice.push_back(sgservice);
    } else {
      it_map->second->UptSgservice(sgservice);
    }

    ++it_sgservice;
  }

  if (vec_add_sgservice.size()) {
    CLOG_STR_ERROR("Chg trans to Add");
    AddSrv(vec_add_sgservice);
  }
}

void CthriftClientWorker::OnConn(const muduo::net::TcpConnectionPtr &conn) {
  CLOG_STR_INFO(conn->localAddress().toIpPort() << " -> "
           << conn->peerAddress().toIpPort() << " is "
           << (conn->connected() ? "UP" : "DOWN"));

  if (conn->connected()) {

    string str_port;

    try{
      str_port = boost::lexical_cast<std::string>((conn->peerAddress()).toPort());
    } catch(boost::bad_lexical_cast & e) {

      CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
                << "toPort " << (conn->peerAddress()).toPort()
                << "conn peerAddr " << (conn->peerAddress()).toIpPort());

      conn->shutdown();
      return;
    }

    //check in map
    UnorderedMapIpPort2ConnInfoSP unordered_map_iter =
        map_ipport_spconninfo_.find(
            (conn->peerAddress()).toIp() + ":" + str_port);
    if (CTHRIFT_UNLIKELY(
        unordered_map_iter == map_ipport_spconninfo_.end())) {
      CLOG_STR_ERROR("conn peerAddr " << (conn->peerAddress()).toIpPort()
                << " localaddr " << (conn->localAddress()).toIpPort()
                << " NOT find key in map_ipport_spconninfo_");

      conn->shutdown();
      return;
    }

    conn->setTcpNoDelay(true);
    conn->setHighWaterMarkCallback(boost::bind(&CthriftClientWorker::OnHighWaterMark,
                                               this,
                                               _1,
                                               _2),
                                   kI32HighWaterSize); //every conn, 64K buff

    boost::shared_ptr <ConnContext4Worker> conn_context_ptr =
        boost::make_shared<ConnContext4Worker>(unordered_map_iter->second);
    conn->setContext(conn_context_ptr);

    Context4WorkerSharedPtr tmp;
    try {
      tmp = boost::any_cast<Context4WorkerSharedPtr>(conn->getContext());
    } catch (boost::bad_any_cast e) {
      CLOG_STR_ERROR("bad_any_cast:" << e.what());
      return;
    }

    //tmp->t_last_conn_time_ = time(0);

    if (CTHRIFT_UNLIKELY(1 == atomic_avaliable_conn_num_.incrementAndGet())) {
      muduo::MutexLockGuard lock(mutexlock_avaliable_conn_ready_);
      cond_avaliable_conn_ready_.notifyAll();
    }
  } else {
    if (CTHRIFT_UNLIKELY((conn->getContext()).empty())) {
      CLOG_STR_WARN("conn context empty, maybe shutdown when conn");
    } else {
      Context4WorkerSharedPtr sp_context;
      try {
        sp_context =
            boost::any_cast<Context4WorkerSharedPtr>(conn->getContext());
      } catch (boost::bad_any_cast e) {
        CLOG_STR_ERROR("bad_any_cast:" << e.what());
        return;
      }

      /*//clear send queue
      for (int i = 0; i < static_cast<int>((sp_context->queue_send).size());
           i++) {
        map_id_sharedcontextsp_.erase((sp_context->queue_send).front());
        (sp_context->queue_send).pop();
      }*/

      //make sure this conn NOT decrement num before, and then check current
      // available num
      if (!(sp_context->b_highwater) && !(sp_context->b_occupied)
          && (0 >= (atomic_avaliable_conn_num_.decrementAndGet()))) {
        atomic_avaliable_conn_num_.getAndSet(0); //adjust for safe

        CLOG_STR_WARN("atomic_avaliable_conn_num_ 0");
      }
    }
  }

  CheckLocalSgagent();
}

void CthriftClientWorker::HandleThriftMsg(const muduo::net::TcpConnectionPtr &conn, const std::string& ret_msg, const int32_t &length, uint8_t *buf) {

    //deserial seqid for map requset
    (*sp_p_tmemorybuffer_)->resetBuffer(buf, static_cast<uint32_t>(length), TMemoryBuffer::COPY);

    int32_t i32_seqid = (*sp_p_cthrift_tbinary_protocol_)->GetSeqID();

    if (CTHRIFT_UNLIKELY(0 >= i32_seqid)) {
        CLOG_STR_ERROR("seqid " << i32_seqid << " str_appkey " <<
                        str_svr_appkey_  << " close connection to " << (conn->peerAddress()).toIpPort());

        conn->shutdown();
        return;
    }

    string str_id;
    try{
        str_id = boost::lexical_cast<std::string>(i32_seqid);
    } catch(boost::bad_lexical_cast & e) {

        CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
                        << "seqid " << i32_seqid << " str_appkey " << str_svr_appkey_
                        << " close connection to " << (conn->peerAddress()).toIpPort());

        conn->shutdown();
        return;
    }

    MapID2SharedPointerIter
            map_iter = map_id_sharedcontextsp_.find(str_id);
    if (CTHRIFT_UNLIKELY(map_id_sharedcontextsp_.end() == map_iter)) {
        CLOG_STR_ERROR("Not find id " << str_id << " maybe timeout"
                        << " connection to " << (conn->peerAddress()).toIpPort());

    } else {
        SharedContSharedPtr &sp_shared = map_iter->second;

        CLOG_STR_DEBUG("id " << str_id << " send & recv cost "
                       << timeDifference(Timestamp::now(), sp_shared->timestamp_cliworker_send)<< " secs");
        if (sp_shared->async_flag) {
            //copy一份recv数据，避免异步回调线程与IO线程竞争读/写
            //mutex不能保证这里逻辑的正确性，因为OnMsg一直被调用，muduo::buffer一直被重复填充数据
            //rpc通信的message可能较大，采用栈上存储存在撑爆线程栈风险
            uint32_t buf_size = static_cast<uint32_t>(length);
            uint8_t* recv_buf = reinterpret_cast<uint8_t*>(std::malloc(buf_size));
            std::memcpy(recv_buf, buf, buf_size);

            p_async_event_loop_->runInLoop(boost::bind(&CthriftClientWorker::AsyncCallback,
                                                       this, buf_size, recv_buf, sp_shared)); //will use event_loop in
        } else if (sp_shared->IsTimeout()) {
            CLOG_STR_WARN("seq id " << str_id << " already expire, discard the msg"
                                  << " upper_spanid " << sp_shared->str_upper_spanid_
                                  << " upper_traceid " << sp_shared->str_upper_traceid_);
            ReportTimeToCat(false,  timeDifference(Timestamp::now(), sp_shared->timestamp_start) * 1000);
        } else {      //when transport timeout during write readbuf, still
            // safe
            sp_shared->ResetReadBuf(buf, static_cast<uint32_t>(length));
            muduo::MutexLockGuard lock(*(sp_shared->p_mutexlock_conn_ready));
            sp_shared->p_cond_ready_read->notifyAll();

            //int clientStatus = 0;   //NOT used by cmtrace, just fill
            //CLIENT_RECV(clientStatus);

            ReportTimeToCat(true,  timeDifference(Timestamp::now(), sp_shared->timestamp_start) * 1000);
            CLOG_STR_DEBUG("write and notify for id " << str_id
                                                      << " done");
        }

        //异步、同步删除map中item都在这里处理；异步化的回调中传递了shared_ptr，这里删除后异步回调中内存仍然可用
        map_id_sharedcontextsp_.erase(sp_shared->str_id);
    }

}

void CthriftClientWorker::HandleUniformMsg(const muduo::net::TcpConnectionPtr &conn, const int32_t &length, uint8_t *buf) {

    std::string ret_msg;
    size_t req_size = length;
    uint8_t *p_ui8_req_buf = buf;

    CthriftUniformRequest request((int32_t)req_size, p_ui8_req_buf);



    if(!request.UnPackRequest()) {
        CLOG_STR_WARN("unPackRequest ERROR from conn" << conn->peerAddress().toIpPort());
        conn->shutdown();
        return;
    }

    const Header* phead = request.GetHeader();
    if(phead->__isset.responseInfo){
      if(StatusCode::Success != phead->responseInfo.status){
        ret_msg = phead->responseInfo.message;
      }
    }



    HandleThriftMsg(conn, ret_msg, request.GetBodyLength(), (uint8_t*)request.GetBody());
}

void CthriftClientWorker::HandleHessianMsg(const muduo::net::TcpConnectionPtr &conn) {
    conn->shutdown();
    CLOG_STR_WARN("have no support  Hessian protocol : " << conn->peerAddress().toIpPort());

}

void CthriftClientWorker::HandleMsg( const muduo::net::TcpConnectionPtr &conn,
                                     Context4WorkerSharedPtr &sp_context_worker,
                                     muduo::net::Buffer *buffer){

    const int32_t length = sp_context_worker->i32_want_size;
    uint8_t *p_ui8_req_buf = reinterpret_cast<uint8_t *>(const_cast<char *>(buffer->peek()));

    cthrift_protocol_type type = CthriftUniformRequest::GetProtocolType(p_ui8_req_buf);
    switch (type){
        case CTHRIFT_HESSIAN_PROTOCOL:
            HandleHessianMsg(conn);
            return;
        case CTHRIFT_UNIFORM_PROTOCOL:
            HandleUniformMsg(conn, length, p_ui8_req_buf);
            break;
        case CTHRIFT_THRIFT_PROTOCOL:
            HandleThriftMsg(conn, "", length, p_ui8_req_buf);
            break;
        default:
            CLOG_STR_ERROR("ERROR protocol for " << conn->peerAddress().toIpPort());
            conn->shutdown();
            return;
    }


    buffer->retrieve(static_cast<size_t>(length));
    sp_context_worker->enum_state = kExpectFrameSize;

    if (CTHRIFT_UNLIKELY(buffer->readableBytes())) {
        CLOG_STR_DEBUG("still " << buffer->readableBytes()
                                << " left in receive buf");
    } else {
        CLOG_STR_DEBUG("retrieve all");
        sp_context_worker->b_occupied = false;
    }
}

void CthriftClientWorker::OnMsg(const muduo::net::TcpConnectionPtr &conn,
                                muduo::net::Buffer *buffer,
                                muduo::Timestamp receiveTime) {
  CLOG_STR_DEBUG((conn->peerAddress()).toIpPort() << " msg received "
            << (buffer->toStringPiece()).data() << " len " << buffer->readableBytes());

  if (CTHRIFT_UNLIKELY((conn->getContext()).empty())) {
    CLOG_STR_ERROR("peer address " << conn->peerAddress().toIpPort()
              << " context empty");
    conn->shutdown();
    return;
  }

  Context4WorkerSharedPtr sp_context_worker;
  try {
    sp_context_worker =
        boost::any_cast<Context4WorkerSharedPtr>(conn->getContext());
  } catch (boost::bad_any_cast e) {
    CLOG_STR_ERROR("bad_any_cast:" << e.what());
    return;
  }

  while (1) {
    if (sp_context_worker->enum_state == kExpectFrameSize) {

        if(buffer->readableBytes() < 2){
            //LOG_WARN << "not enough size for judge protocol type" ;
            return;
        }

        cthrift_protocol_type type = CthriftUniformRequest::GetProtocolType(reinterpret_cast<uint8_t *>((
                const_cast<char *>(buffer->peek()))));


        switch(type){
            case CTHRIFT_HESSIAN_PROTOCOL:{
                CLOG_STR_WARN("OnMsg from HESSIAN drop connect" );
                conn->shutdown();
                return;
            }
            case CTHRIFT_UNIFORM_PROTOCOL:{

                if(buffer->readableBytes() < 8){
                    CLOG_STR_WARN("not enough size for protocol uniform head&&length&&totallength, wait for more");
                    return;
                }

                int32_t length = CthriftUniformRequest::GetTotallength(reinterpret_cast<uint8_t *>((
                        const_cast<char *>(buffer->peek()))));

                if (length <= buffer->readableBytes()) {
                    sp_context_worker->i32_want_size = length ;
                    sp_context_worker->enum_state = kExpectFrame;
                } else {
                    CLOG_STR_WARN("not enough size for protocol uniform total length, wait for more");
                    return;
                }
                break;
            }
            case CTHRIFT_THRIFT_PROTOCOL:{
                if (sizeof(int32_t) <= buffer->readableBytes()) {
                    sp_context_worker->i32_want_size =
                            static_cast<uint32_t>(buffer->readInt32());
                    sp_context_worker->enum_state = kExpectFrame;
                } else {
                    CLOG_STR_WARN("not enough size for protocol thrift total length, wait for more");
                    return;
                }
                break;
            }
            default:
                CLOG_STR_WARN("Error type protocol :" << type);
                return;

        }
    } else if (sp_context_worker->enum_state == kExpectFrame) {

      if (buffer->readableBytes()
          >= static_cast<size_t>(sp_context_worker->i32_want_size)) {
        //sp_context_worker->t_last_recv_time_ = time(0);
        HandleMsg(conn, sp_context_worker, buffer);

      } else {
        CLOG_STR_DEBUG("body len " << buffer->readableBytes() << " < want len "
                  << sp_context_worker->i32_want_size << " continue wait");
        break;
      }
    }
  }
}

void
CthriftClientWorker::OnWriteComplete(const muduo::net::TcpConnectionPtr &conn) {
  CLOG_STR_DEBUG(conn->localAddress().toIpPort() << " -> "
            << conn->peerAddress().toIpPort() << " OnWriteComplete");

  if (CTHRIFT_UNLIKELY((conn->getContext()).empty())) {
    CLOG_STR_ERROR("address: " << (conn->peerAddress()).toIpPort() << " "
        "context empty");    //NOT clear here
    return;
  }

  Context4WorkerSharedPtr conn_info;
  try {
    conn_info = boost::any_cast<Context4WorkerSharedPtr>(conn->getContext());
  } catch (boost::bad_any_cast e) {
    CLOG_STR_ERROR("bad_any_cast:" << e.what());
    return;
  }

  conn_info->b_highwater = false;

  if (CTHRIFT_UNLIKELY(1 == atomic_avaliable_conn_num_.incrementAndGet())) {
    muduo::MutexLockGuard lock(mutexlock_avaliable_conn_ready_);
    cond_avaliable_conn_ready_.notifyAll();
  }
}

void
CthriftClientWorker::OnHighWaterMark(const muduo::net::TcpConnectionPtr &conn,
                                     size_t len) {
  CLOG_STR_INFO((conn->localAddress()).toIpPort() << " -> "
           << (conn->peerAddress()).toIpPort() << " OnHighWaterMark");

  if (CTHRIFT_UNLIKELY((conn->getContext()).empty())) {
    CLOG_STR_ERROR("address: " << (conn->peerAddress()).toIpPort() << " "
        "context empty");    //NOT clear here
    return;
  }

  Context4WorkerSharedPtr conn_info;
  try {
    conn_info =
        boost::any_cast<Context4WorkerSharedPtr>(conn->getContext());
  } catch (boost::bad_any_cast e) {
    CLOG_STR_ERROR("bad_any_cast:" << e.what());
    return;
  }

  conn_info->b_highwater = true;

  if (0 >= atomic_avaliable_conn_num_.decrementAndGet()) {
    atomic_avaliable_conn_num_.getAndSet(0); //adjust for safe

    CLOG_STR_WARN("atomic_avaliable_conn_num_ 0");
  }
}

cthrift_protocol_type CthriftClientWorker::DoPreOperationWhenSend(const string& seq_id, Buffer& buffer, SGService& sgservice,
                                                                  const std::string& traceid, const std::string& spanid){

  if (CthriftSgagent::kStrSgagentAppkey == str_svr_appkey_) {
       //sg_agent do nothing
       return CTHRIFT_THRIFT_PROTOCOL;
  }


  bool flag = false;
  for(map<string, ServiceDetail>::const_iterator it = sgservice.serviceInfo.begin();
      it != sgservice.serviceInfo.end(); it++){
      if( true == it->second.unifiedProto){
        flag = true;
      }
  }

  if(false == flag){
    return CTHRIFT_THRIFT_PROTOCOL;
  }


  Header head;
  head.__set_messageType(MessageType::Normal);

  if(b_auth_){
    std::string hash = CthriftKmsTools::hmacSHA1(str_auth_token_, str_client_appkey_);
    Context context;
    context["auth-appkey"] = str_client_appkey_;
    context["auth-signature"] =  hash;
    head.__set_localContext(context);
  }

  int64_t  sequenceId;
  stringstream ss;
  ss << seq_id;
  ss >> sequenceId;


  RequestInfo requestInfo;
  requestInfo.__set_callType(Cthrift_CallType::Reply);
  requestInfo.__set_sequenceId(sequenceId);
  requestInfo.__set_serviceName(str_serviceName_filter_);
  requestInfo.__set_timeout(i32_timeout_);

  TraceInfo traceInfo;
  traceInfo.__set_clientAppkey(str_client_appkey_);
  traceInfo.__set_clientIp(CthriftSgagent::str_local_ip_);

  if(traceid.empty()){
    RemoteProcessCall rpc(str_svr_appkey_);
    traceInfo.__set_spanId(rpc.getRpcId());
    traceInfo.__set_traceId(rpc.getTraceId());
    CLOG_STR_INFO("Local traceID " << rpc.getTraceId() << " spanID " << rpc.getRpcId());
  }else{
    RemoteProcessCall rpc(traceid, spanid, str_svr_appkey_);
    traceInfo.__set_spanId(rpc.getRpcId());
    traceInfo.__set_traceId(rpc.getTraceId());
    CLOG_STR_INFO("Passthrough Local traceID " << rpc.getTraceId() << " spanID " << rpc.getRpcId());
  }

  head.__set_requestInfo(requestInfo);
  head.__set_traceInfo(traceInfo);


  CthriftUniformRequest request;
  request.PackRequest( buffer, head);

  return CTHRIFT_UNIFORM_PROTOCOL;
}

void CthriftClientWorker::SendTransportReq(SharedContSharedPtr sp_shared) {
  Buffer send_buf;
  if (0 != sp_shared->GetWriteBuf(&send_buf)) {
      CLOG_STR_WARN("task already timeout, before send packet"
                            << " upper_spanid " << sp_shared->str_upper_spanid_
                            << " upper_traceid " << sp_shared->str_upper_traceid_);

      ReportTimeToCat(false,  timeDifference(Timestamp::now(), sp_shared->timestamp_start) * 1000);
      return;
  }

  CLOG_STR_DEBUG("send_buf.size " << send_buf.readableBytes());

  TcpClientWeakPtr wp_tcpcli;
  if (ChooseNextReadyConn(&wp_tcpcli)) {
      CLOG_STR_ERROR("No candidate connection to send packet, async task will be dropped"
                             << " upper_spanid " << sp_shared->str_upper_spanid_
                             << " upper_traceid " << sp_shared->str_upper_traceid_);

    ReportTimeToCat(false,  timeDifference(Timestamp::now(), sp_shared->timestamp_start) * 1000);
    return;
  }

  TcpClientSharedPtr sp_tcpcli
      (wp_tcpcli.lock());  //already check valid in ChooseNextReadyConn


  string str_port;

  try{
    str_port = boost::lexical_cast<std::string>((sp_tcpcli->connection()->peerAddress()).toPort());
  } catch(boost::bad_lexical_cast & e) {
      CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
                                                 << " ip:" << (sp_tcpcli->connection()->peerAddress()).toIp()
                                                 << " port:" << (sp_tcpcli->connection()->peerAddress()).toPort()
                                                 << " upper_spanid " << sp_shared->str_upper_spanid_
                                                 << " upper_traceid " << sp_shared->str_upper_traceid_);

    ReportTimeToCat(false,  timeDifference(Timestamp::now(), sp_shared->timestamp_start) * 1000);
    return;
  }

  UnorderedMapIpPort2ConnInfoSP
      iter_ipport_spconninfo = map_ipport_spconninfo_.find(
      (sp_tcpcli->connection()->peerAddress()).toIp() + ":" + str_port);
  if (CTHRIFT_UNLIKELY(
      iter_ipport_spconninfo == map_ipport_spconninfo_.end())) {
      CLOG_STR_ERROR("Not find ip:"
                             << (sp_tcpcli->connection()->peerAddress()).toIp() << " port:"
                             << str_port << " in map_ipport_spconninfo_"
                             << " upper_spanid " << sp_shared->str_upper_spanid_
                             << " upper_traceid " << sp_shared->str_upper_traceid_);

    ReportTimeToCat(false,  timeDifference(Timestamp::now(), sp_shared->timestamp_start) * 1000);
    return;
  }

  SGService sgservice = iter_ipport_spconninfo->second->GetSgservice();

  if (CTHRIFT_UNLIKELY((sp_tcpcli->connection()->getContext()).empty())) {
      CLOG_STR_ERROR("conn context empty"
                             << " upper_spanid " << sp_shared->str_upper_spanid_
                             << " upper_traceid " << sp_shared->str_upper_traceid_);

    ReportTimeToCat(false,  timeDifference(Timestamp::now(), sp_shared->timestamp_start) * 1000);
    return;
  }

  if (CTHRIFT_UNLIKELY(CheckOverTime(sp_shared->timestamp_start,
                                     static_cast<double>(
                                         sp_shared->i32_timeout_ms) / 1000,
                                     0))) {
      CLOG_STR_WARN("before send, appkey " << str_svr_appkey_ << " id "
                                           << sp_shared->str_id << " timeout return"
                                           << " upper_spanid " << sp_shared->str_upper_spanid_
                                           << " upper_traceid " << sp_shared->str_upper_traceid_);

    ReportTimeToCat(false,  timeDifference(Timestamp::now(), sp_shared->timestamp_start) * 1000);
    return;
  }

  if(CTHRIFT_UNDEFINED_PROTOCOL == DoPreOperationWhenSend(sp_shared->str_id, send_buf, sgservice,
                                                          sp_shared->str_upper_traceid_,
                                                          sp_shared->str_upper_spanid_)){
      CLOG_STR_WARN("before send, DoPreOperationWhenSend  CTHRIFT_UNDEFINED_PROTOCOL"
                            << " upper_spanid " << sp_shared->str_upper_spanid_
                            << " upper_traceid " << sp_shared->str_upper_traceid_);

    ReportTimeToCat(false,  timeDifference(Timestamp::now(), sp_shared->timestamp_start) * 1000);
    return;
  }

  sp_tcpcli->connection()->send(&send_buf);

  //CLIENT_SEND((sp_tcpcli->connection()->peerAddress()).toIpPort(), str_svr_appkey_);

    CLOG_STR_DEBUG("send id " << sp_shared->str_id << " done"
                              << " upper_spanid " << sp_shared->str_upper_spanid_
                              << " upper_traceid " << sp_shared->str_upper_traceid_);

  Context4WorkerSharedPtr sp_context;
  try {
    sp_context =
        boost::any_cast<Context4WorkerSharedPtr>(sp_tcpcli->connection()->getContext());
  } catch (boost::bad_any_cast e) {
    CLOG_STR_ERROR("bad_any_cast:" << e.what());
    return;
  }

  //sp_context->t_last_send_time_ = time(0);

  sp_shared->wp_send_conn = sp_tcpcli->connection();
  sp_shared->timestamp_cliworker_send = Timestamp::now();

  //sp_context->b_occupied = true;

  //(sp_context->queue_send).push(sp_shared->str_id);

  map_id_sharedcontextsp_[sp_shared->str_id] = sp_shared;
}

void CthriftClientWorker::TimewheelKick() {
  cnxt_entry_circul_buf_.push_back(CnxtEntryBucket());
}


//触发式资源分配
void CthriftClientWorker::EnableAsync(const int32_t &i32_timeout_ms) {
  if (!p_async_event_loop_) {
    sp_async_event_thread_ = 
      boost::make_shared<muduo::net::EventLoopThread>(muduo::net::EventLoopThread::ThreadInitCallback(), "cthrift_async_thread");
    p_async_event_loop_ = sp_async_event_thread_->startLoop();
    p_async_event_loop_->setContext(TASK_INIT);
  }

  //Init Garbage Collection timewheel
  cnxt_entry_circul_buf_.resize(kI8TimeWheelNum);
  double timeout = i32_timeout_ms > 1000 ? static_cast<double >(i32_timeout_ms)/1000.0 : 1.0;
  p_event_loop_->runEvery(timeout, boost::bind(&CthriftClientWorker::TimewheelKick, this));
}

void CthriftClientWorker::AsyncCallback(const uint32_t& size,
                                        uint8_t* recv_buf,
                                        SharedContSharedPtr sp_shared) {
  double d_left_secs = 0.0;
  if (CheckOverTime(sp_shared->timestamp_start,
                    static_cast<double>(sp_shared->i32_timeout_ms)
                    / 1000, &d_left_secs)){
    CLOG_STR_WARN("async wait appkey " << str_svr_appkey_ << " id "
                                 << sp_shared->str_id
                                 << " already " << sp_shared->i32_timeout_ms
                                 << " ms for readbuf, timeout"
                                 << " upper_spanid " << sp_shared->str_upper_spanid_
                                 << " upper_traceid " << sp_shared->str_upper_traceid_);
    ReportTimeToCat(false,  timeDifference(Timestamp::now(), sp_shared->timestamp_start) * 1000);
    p_async_event_loop_->setContext(TASK_TIMEOUT);
  } else {
    p_async_event_loop_->setContext(TASK_SUCCESS);
    ReportTimeToCat(true,  timeDifference(Timestamp::now(), sp_shared->timestamp_start) * 1000);
  }
  //避免手动释放内存；内存统一由TMemoryBuffer对象管理
  sp_shared->ResetRecvBuf(recv_buf, size);
  //回调用户业务逻辑
  try {
    sp_shared->cob_();
  } catch (...) {
    CLOG_STR_ERROR("Catch exception in async callback"
                           << " upper_spanid " << sp_shared->str_upper_spanid_
                           << " upper_traceid " << sp_shared->str_upper_traceid_);
  }
}

void CthriftClientWorker::AsyncBadCallback(SharedContSharedPtr sp_shared) {
  p_async_event_loop_->setContext(TASK_TOO_MANY);
  sp_shared->cob_();
}

void CthriftClientWorker::AsyncSendReq(SharedContSharedPtr sp_shared) {
  //发送时检测任务是否已经超时，如果超时不进行网络传输直接调用callback，反馈超时
  double d_left_secs = 0.0;
  if (CheckOverTime(sp_shared->timestamp_start,
                    static_cast<double>(sp_shared->i32_timeout_ms)
                    / 1000, &d_left_secs)) {
    CLOG_STR_WARN("async task already timeout, before send packet"
                          << " upper_spanid " << sp_shared->str_upper_spanid_
                          << " upper_traceid " << sp_shared->str_upper_traceid_);

    uint32_t buf_size = 0;
    uint8_t* recv_buf = NULL;
    p_async_event_loop_->runInLoop(boost::bind(&CthriftClientWorker::AsyncCallback,
                                               this, buf_size, recv_buf, sp_shared));
    return;
  }

  Buffer send_buf;
  if (0 != sp_shared->GetAsyncWriteBuf(&send_buf)) {
    return;
  }

  CLOG_STR_DEBUG("send_buf.size " << send_buf.readableBytes());

  TcpClientWeakPtr wp_tcpcli;
  if (ChooseNextReadyConn(&wp_tcpcli)) {
    CLOG_STR_ERROR("No candidate connection to send packet, async task will be dropped"
                           << " upper_spanid " << sp_shared->str_upper_spanid_
                           << " upper_traceid " << sp_shared->str_upper_traceid_);

    ReportTimeToCat(false,  timeDifference(Timestamp::now(), sp_shared->timestamp_start) * 1000);
    return;
  }

  TcpClientSharedPtr sp_tcpcli
          (wp_tcpcli.lock());  //already check valid in ChooseNextReadyConn


  string str_port;

  try{
    str_port = boost::lexical_cast<std::string>((sp_tcpcli->connection()->peerAddress()).toPort());
  } catch(boost::bad_lexical_cast & e) {
    CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
                                               << " ip:" << (sp_tcpcli->connection()->peerAddress()).toIp()
                                               << " port:" << (sp_tcpcli->connection()->peerAddress()).toPort()
                           << " upper_spanid " << sp_shared->str_upper_spanid_
                           << " upper_traceid " << sp_shared->str_upper_traceid_);

    ReportTimeToCat(false,  timeDifference(Timestamp::now(), sp_shared->timestamp_start) * 1000);
    return;
  }

  UnorderedMapIpPort2ConnInfoSP
          iter_ipport_spconninfo = map_ipport_spconninfo_.find(
          (sp_tcpcli->connection()->peerAddress()).toIp() + ":" + str_port);
  if (CTHRIFT_UNLIKELY(
          iter_ipport_spconninfo == map_ipport_spconninfo_.end())) {
    CLOG_STR_ERROR("Not find ip:"
                           << (sp_tcpcli->connection()->peerAddress()).toIp() << " port:"
                           << str_port << " in map_ipport_spconninfo_"
                           << " upper_spanid " << sp_shared->str_upper_spanid_
                           << " upper_traceid " << sp_shared->str_upper_traceid_);
    ReportTimeToCat(false,  timeDifference(Timestamp::now(), sp_shared->timestamp_start) * 1000);
    return;
  }

  SGService sgservice = iter_ipport_spconninfo->second->GetSgservice();
  if (CTHRIFT_UNLIKELY((sp_tcpcli->connection()->getContext()).empty())) {
    CLOG_STR_ERROR("conn context empty"
                           << " upper_spanid " << sp_shared->str_upper_spanid_
                           << " upper_traceid " << sp_shared->str_upper_traceid_);
    ReportTimeToCat(false,  timeDifference(Timestamp::now(), sp_shared->timestamp_start) * 1000);
    return;
  }

  if (CTHRIFT_UNLIKELY(CheckOverTime(sp_shared->timestamp_start,
                                     static_cast<double>(
                                             sp_shared->i32_timeout_ms) / 1000,
                                     0))) {
    CLOG_STR_WARN("before send, appkey " << str_svr_appkey_ << " id "
                                         << sp_shared->str_id << " timeout return"
                          << " upper_spanid " << sp_shared->str_upper_spanid_
                          << " upper_traceid " << sp_shared->str_upper_traceid_);
    ReportTimeToCat(false,  timeDifference(Timestamp::now(), sp_shared->timestamp_start) * 1000);
    return;
  }

  if(CTHRIFT_UNDEFINED_PROTOCOL == DoPreOperationWhenSend(sp_shared->str_id, send_buf, sgservice,
                                                          sp_shared->str_upper_traceid_,
                                                          sp_shared->str_upper_spanid_)){
     CLOG_STR_WARN("before send, DoPreOperationWhenSend  CTHRIFT_UNDEFINED_PROTOCOL"
                           << " upper_spanid " << sp_shared->str_upper_spanid_
                           << " upper_traceid " << sp_shared->str_upper_traceid_);

     ReportTimeToCat(false,  timeDifference(Timestamp::now(), sp_shared->timestamp_start) * 1000);
     return;
  }

  sp_tcpcli->connection()->send(&send_buf);

  //CLIENT_SEND((sp_tcpcli->connection()->peerAddress()).toIpPort(), str_svr_appkey_);

  CLOG_STR_DEBUG("send id " << sp_shared->str_id << " done"
                         << " upper_spanid " << sp_shared->str_upper_spanid_
                         << " upper_traceid " << sp_shared->str_upper_traceid_
                         << (sp_tcpcli->connection()->peerAddress()).toIp()
                         << " port:" << str_port);

  Context4WorkerSharedPtr sp_context;
  try {
    sp_context =
            boost::any_cast<Context4WorkerSharedPtr>(sp_tcpcli->connection()->getContext());
  } catch (boost::bad_any_cast e) {
    CLOG_STR_ERROR("bad_any_cast:" << e.what());
    return;
  }

  //sp_context->t_last_send_time_ = time(0);

  sp_shared->wp_send_conn = sp_tcpcli->connection();
  sp_shared->timestamp_cliworker_send = Timestamp::now();

  //sp_context->b_occupied = true;

  //(sp_context->queue_send).push(sp_shared->str_id);

  map_id_sharedcontextsp_[sp_shared->str_id] = sp_shared;
  cnxt_entry_circul_buf_.back().insert(boost::make_shared<CnxtEntry>(sp_shared, this));
}

void CthriftClientWorker::ReportTimeToCat(const bool& success, const double& time){

  if( ! CTHRIFT_UNLIKELY(CthriftSgagent::b_is_open_cat_) ){
    CLOG_STR_DEBUG("CthriftSgagent::b_is_open_cat_: false" );
    return;
  }

    CatTransaction *trans = newTransaction(string("OctoClient").c_str(), str_svr_appkey_.c_str());
    trans->setDurationInMillis(trans, time); //自己计算打点时间并填入
    trans->setStatus((CatMessage *) trans, success ? CAT_SUCCESS : CAT_ERROR); // 失败状态则置为CAT_ERROR
    trans->setComplete((CatMessage *) trans);

}