#include "cthrift_svr.h"



//using apache::thrift::transport;
using namespace std;
using namespace muduo;
using namespace muduo::net;
using namespace cthrift;

//const double CthriftSvr::kDCheckConnIntervalSec = 30.0;
const time_t CthriftSvr::kTMaxCliIdleTimeSec = 5*60;  //5 min stale connect collection
const int8_t CthriftSvr::kI8TimeWheelGridNum = 4;

__thread int32_t CthriftSvr::i32_curr_conn_num_;

__thread boost::shared_ptr<TMemoryBuffer> *
    CthriftSvr::sp_p_input_tmemorybuffer_;
__thread boost::shared_ptr<TMemoryBuffer> *
    CthriftSvr::sp_p_output_tmemorybuffer_;
__thread boost::shared_ptr<std::string> *CthriftSvr::sp_p_str_current_connid_;
__thread boost::shared_ptr<std::string> *CthriftSvr::sp_p_str_current_traceid_;
__thread boost::shared_ptr<std::string> *CthriftSvr::sp_p_str_current_spanid_;
__thread boost::shared_ptr <std::map<std::string, std::string> > *CthriftSvr::sp_p_str_user_tag_map_ ;
__thread boost::shared_ptr <std::map<std::string, std::string> > *CthriftSvr::sp_p_str_current_traceid_tag_map_ ;

__thread boost::shared_ptr<TProtocol> *CthriftSvr::sp_p_input_tprotocol_;
__thread boost::shared_ptr<TProtocol> *CthriftSvr::sp_p_output_tprotocol_;

__thread boost::shared_ptr<TProcessor> *CthriftSvr::sp_p_processor_;

__thread boost::shared_ptr <StrStrMap> *CthriftSvr::sp_p_str_str_appkeyMap_ ;
__thread boost::shared_ptr <StrStrMap> *CthriftSvr::sp_p_str_str_whiteMap_ ;
__thread boost::shared_ptr <StrSMMap> *CthriftSvr::sp_p_str_map_methodMap_ ;
__thread boost::shared_ptr <std::string> *CthriftSvr::sp_p_str_local_token ;

void CthriftSvr::InitStaticThreadLocalMember(void) {
  i32_curr_conn_num_ = 0;

  if (CTHRIFT_LIKELY(
          !sp_p_str_current_connid_ || !(*sp_p_str_current_connid_))) {
    sp_p_str_current_connid_ = new boost::shared_ptr<std::string>
            (boost::make_shared<std::string>());
  }

  if (CTHRIFT_LIKELY(
          !sp_p_str_current_spanid_ || !(*sp_p_str_current_spanid_))) {
    sp_p_str_current_spanid_ = new boost::shared_ptr<std::string>
            (boost::make_shared<std::string>());
  }

  if (CTHRIFT_LIKELY(
          !sp_p_str_current_traceid_ || !(*sp_p_str_current_traceid_))) {
    sp_p_str_current_traceid_ = new boost::shared_ptr<std::string>
            (boost::make_shared<std::string>());
  }

  if (CTHRIFT_LIKELY(
          !sp_p_str_current_traceid_tag_map_ || !(*sp_p_str_current_traceid_tag_map_))) {
    sp_p_str_current_traceid_tag_map_ = new boost::shared_ptr<std::map<std::string, std::string> >
            (boost::make_shared<std::map<std::string, std::string> >());
  }

  if (CTHRIFT_LIKELY(
          !sp_p_str_user_tag_map_ || !(*sp_p_str_user_tag_map_))) {
    sp_p_str_user_tag_map_ = new boost::shared_ptr<std::map<std::string, std::string> >
            (boost::make_shared<std::map<std::string, std::string> >());
  }

  if (CTHRIFT_LIKELY(
      !sp_p_input_tmemorybuffer_ || !(*sp_p_input_tmemorybuffer_))) {
    sp_p_input_tmemorybuffer_ = new boost::shared_ptr<TMemoryBuffer>
        (boost::make_shared<TMemoryBuffer>());
  }

  if (CTHRIFT_LIKELY(
      !sp_p_output_tmemorybuffer_ || !(*sp_p_output_tmemorybuffer_))) {
    sp_p_output_tmemorybuffer_ = new boost::shared_ptr<TMemoryBuffer>
        (boost::make_shared<TMemoryBuffer>());
  }

  if (CTHRIFT_LIKELY(!sp_p_input_tprotocol_ || !(*sp_p_input_tprotocol_))) {
    sp_p_input_tprotocol_ =
        new boost::shared_ptr<TProtocol>(inputProtocolFactory_->getProtocol(*sp_p_input_tmemorybuffer_)); //inputProtocolFactory_ is member of TServer
  }

  if (CTHRIFT_LIKELY(!sp_p_output_tprotocol_ || !(*sp_p_output_tprotocol_))) {
    sp_p_output_tprotocol_ =
        new boost::shared_ptr<TProtocol>(inputProtocolFactory_->getProtocol(*sp_p_output_tmemorybuffer_));
  }

  if (CTHRIFT_LIKELY(!sp_p_processor_ || !(*sp_p_processor_))) {
    sp_p_processor_ =
        new boost::shared_ptr<TProcessor>(getProcessor(*sp_p_input_tprotocol_,
                                                       *sp_p_output_tprotocol_,
                                                       boost::make_shared<
                                                           TNullTransport>()));
  }

  if (CTHRIFT_LIKELY(!sp_p_str_str_appkeyMap_ || !(*sp_p_str_str_appkeyMap_))) {
    sp_p_str_str_appkeyMap_ = new boost::shared_ptr<StrStrMap>
            (boost::make_shared<StrStrMap>());
  }

  if (CTHRIFT_LIKELY(!sp_p_str_str_whiteMap_ || !(*sp_p_str_str_whiteMap_))) {
    sp_p_str_str_whiteMap_ = new boost::shared_ptr<StrStrMap>
            (boost::make_shared<StrStrMap>());
  }

  if (CTHRIFT_LIKELY(!sp_p_str_map_methodMap_ || !(*sp_p_str_map_methodMap_))) {
    sp_p_str_map_methodMap_ = new boost::shared_ptr<StrSMMap>
            (boost::make_shared<StrSMMap>());
  }


  if (CTHRIFT_LIKELY(!sp_p_str_local_token || !(*sp_p_str_local_token))) {
    sp_p_str_local_token = new boost::shared_ptr<std::string>
            (boost::make_shared<std::string>());
  }

}


CthriftSvr::~CthriftSvr(void) {
  if (CTHRIFT_LIKELY(sp_p_input_tmemorybuffer_)) {
    delete sp_p_input_tmemorybuffer_;
  }

  if (CTHRIFT_LIKELY(sp_p_output_tmemorybuffer_)) {
    delete sp_p_output_tmemorybuffer_;
  }

  if (CTHRIFT_LIKELY(sp_p_input_tprotocol_)) {
    delete sp_p_input_tprotocol_;
  }

  if (CTHRIFT_LIKELY(sp_p_output_tprotocol_)) {
    delete sp_p_output_tprotocol_;
  }

  if (CTHRIFT_LIKELY(sp_p_processor_)) {
    delete sp_p_processor_;
  }

  if (CTHRIFT_LIKELY(sp_p_str_current_connid_)) {
      delete sp_p_str_current_connid_;
  }

  if (CTHRIFT_LIKELY(sp_p_str_current_traceid_)) {
    delete sp_p_str_current_traceid_;
  }

  if (CTHRIFT_LIKELY(sp_p_str_current_spanid_)) {
    delete sp_p_str_current_spanid_;
  }

  if (CTHRIFT_LIKELY(sp_p_str_current_traceid_tag_map_)) {
    delete sp_p_str_current_traceid_tag_map_;
  }

  if (CTHRIFT_LIKELY(sp_p_str_user_tag_map_)) {
    delete sp_p_str_user_tag_map_;
  }

  if (CTHRIFT_LIKELY(sp_p_str_str_appkeyMap_)) {
    delete sp_p_str_str_appkeyMap_;
  }

  if (CTHRIFT_LIKELY(sp_p_str_str_whiteMap_)) {
    delete sp_p_str_str_whiteMap_;
  }

  if (CTHRIFT_LIKELY(sp_p_str_map_methodMap_)) {
    delete sp_p_str_map_methodMap_;
  }

  if (CTHRIFT_LIKELY(sp_p_str_local_token)) {
    delete sp_p_str_local_token;
  }
}

void CthriftSvr::Init(void) {
    /*
     * 进程同时使用CthriftCli和CthriftSvr时，catClient只能init一次
     *  CthriftSgagent::cat_appkey_ = str_svr_appkey_;
     *  pthread_once(&CthriftSgagent::cat_once_, &CthriftSgagent::InitCat);
     */

    catClientInit(str_svr_appkey_.c_str());
    sp_server_->setConnectionCallback(boost::bind(&CthriftSvr::OnConn,
                                            this,
                                            _1));
    sp_server_->setMessageCallback(boost::bind(&CthriftSvr::OnMsg,
                                         this,
                                         _1,
                                         _2,
                                         _3));

    sp_server_->setWriteCompleteCallback(boost::bind(&CthriftSvr::OnWriteComplete,
                                               this,
                                               _1));

  muduo::CountDownLatch countdown_connthread_init(i16_conn_thread_num_);
    sp_server_->setThreadNum(i16_conn_thread_num_); //just set, NOT start thread until start()

  //NO InitStaticThreadLocalMember in main thread since NO handle here
    sp_server_->setThreadInitCallback(boost::bind(&CthriftSvr::ConnThreadInit,
                                            this,
                                            &countdown_connthread_init));
    sp_server_->start();

  setInputProtocolFactory(boost::make_shared<CthriftTBinaryProtocolFactory>());
  setOutputProtocolFactory(boost::make_shared<CthriftTBinaryProtocolFactory
  >());

  CthriftSgagent::PackDefaultSgservice(str_svr_appkey_,
                                       CthriftSgagent::str_local_ip_,
                                       u16_svr_port_,
                                       &sg_service_);

  sp_sgagent_client_ =
      boost::make_shared<SGAgentClient>(sp_cthrift_client_->GetCthriftProtocol());   //init and RegSvr should be in same thread

  countdown_connthread_init.wait();
  CLOG_STR_INFO("conn thread init done");

  //init worker thread
  string str_pool_name("cthrift_svr_worker_event_thread_pool");
  EventLoopThread *p_eventloop_thread = 0;
  muduo::CountDownLatch countdown_workerthread_init(i16_worker_thread_num_);

  for (int i = 0; i < i16_worker_thread_num_; i++) {
    char buf[str_pool_name.size() + 32];
    snprintf(buf, sizeof buf, "%s%d", str_pool_name.c_str(), i);

    p_eventloop_thread = new EventLoopThread(boost::bind(
        &CthriftSvr::WorkerThreadInit,
        this,
        &countdown_workerthread_init),
                                             buf); //memory leak， but should use these threads during whole process lifetime, so ignore

    vec_worker_event_loop_.push_back(p_eventloop_thread->startLoop());
  }

  p_eventloop_thread = new EventLoopThread(muduo::net::EventLoopThread::ThreadInitCallback(), "cthrift_svr_schedule");
  p_event_loop_ = p_eventloop_thread->startLoop();

  countdown_workerthread_init.wait();

  CLOG_STR_INFO("worker thread init done");

  event_loop_.runEvery(60, boost::bind(&CthriftSvr::StatMsgNumPerMin, this));

  event_loop_.runEvery(60 * 5,
                       boost::bind(&CthriftSvr::InitWorkerThreadPos,
                                   this));  //every 5 min, clear keep-increment worker thread pos, for performance



  p_event_loop_->runEvery(10, boost::bind(&CthriftSvr::UpdateAuthInfo, this));
  p_event_loop_->runInLoop(boost::bind(&CthriftSvr::UpdateAuthInfo, this));


  /*//test cat, del me
  std::cout << "test cat" << std::endl;

  for (int i = 0; i < 1000; i++) {
    CatTransaction *trans =
        newTransaction(string("OctoService").c_str(), str_svr_appkey_.c_str());
    logMetricForCount(str_svr_appkey_.c_str());
    trans->setStatus(reinterpret_cast<CatMessage *>(trans), CAT_SUCCESS);
    trans->setComplete(reinterpret_cast<CatMessage *>(trans));
  }
  std::cout << "test cat over" << std::endl;*/

}

int8_t CthriftSvr::ArgumentCheck(const string &str_app_key,
                                 const uint16_t &u16_port,
                                 const int32_t &i32_svr_overtime_ms,
                                 const int32_t &i32_max_conn_num,
                                 const int8_t &i8_check_type,   //0: ONLY check
    // str_app_key & port  1: full check
                                 string *p_str_reason) const {
  std::string str_argument("appkey: " + str_app_key);

  std::string str_port;

  try{
    str_port = boost::lexical_cast<std::string>(u16_port);
  } catch(boost::bad_lexical_cast & e) {

    CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
              << "u16_port : " << u16_port);

    return -1;
  }

  str_argument += " port: " + str_port;

  if (1 == i8_check_type) {

    std::string str_svr_overtime_ms;

    try{
      str_svr_overtime_ms = boost::lexical_cast<std::string>(i32_svr_overtime_ms);
    } catch(boost::bad_lexical_cast & e) {

      CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
                << "i32_svr_overtime_ms : " << i32_svr_overtime_ms);

      return -1;
    }

    str_argument += " svr overtime: " + str_svr_overtime_ms;

    std::string str_max_conn_num;

    try{
      str_max_conn_num = boost::lexical_cast<std::string>(i32_max_conn_num);
    } catch(boost::bad_lexical_cast & e) {

      CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
                << "i32_max_conn_num : " << i32_max_conn_num);

      return -1;
    }

    str_argument += " svr max conn num: " + str_max_conn_num;
  }

  CLOG_STR_DEBUG("argument: " << str_argument);

  if (CTHRIFT_UNLIKELY(str_app_key.empty() || 0 == u16_port || (1 ==
      i8_check_type && (0 > i32_svr_overtime_ms   //
      // i32_svr_overtime_ms/i32_max_conn_num can be 0, means NO limit
      || 0 > i32_max_conn_num))
  )) {
    p_str_reason->assign("argument: " + str_argument + ", some or all of "
        "them invalid, please check");
    CLOG_STR_WARN(*p_str_reason);
    return -1;
  }

  return 0;
}

void CthriftSvr::RegSvr(void) {
  try {

   map<string, ServiceDetail>::iterator it = sg_service_.serviceInfo.begin();
    for(;it != sg_service_.serviceInfo.end();it++){
        CLOG_STR_DEBUG("servicename :" <<  it->first);
    }

    /*
    * 注册服务
    * REGISTER_TYPE_RESET,  重置(代表后面的serviceName list就是该应用支持的全量接口);
    * REGISTER_TYPE_ADD，   增加(代表后面的serviceName list是该应用新增的接口);
    * REGISTER_TYPE_DELETE，减少(代表后面的serviceName list是该应用删除的接口)。
    */
    //向MNS注册服务，默认是注册为增加,
    int32_t i32_ret = sp_sgagent_client_->registServicewithCmd(REGISTER_TYPE_ADD, sg_service_);
    if (CTHRIFT_UNLIKELY(i32_ret)) {
      CLOG_STR_ERROR("registService failed: " << i32_ret);
      event_loop_.runAfter(kDRetryIntervalSec,
                           boost::bind(&CthriftSvr::RegSvr, this));
    } else {
      CLOG_STR_INFO("reg svr done");
    }
  } catch (TException &tx) {
    CLOG_STR_ERROR("registService failed: " << tx.what());
    event_loop_.runAfter(kDRetryIntervalSec,
                         boost::bind(&CthriftSvr::RegSvr, this));
  }
}

void CthriftSvr::serve() {
  RegSvr();
  event_loop_.loop();
}


void CthriftSvr::stop() {
  (sp_server_->getLoop())->runInLoop(boost::bind(&EventLoop::quit,
                                                 sp_server_->getLoop()));  //stop data input first

  muduo::CurrentThread::sleepUsec(500 * 1000);  //for safe

  for (int i = 0; i < i16_worker_thread_num_; i++) {
    vec_worker_event_loop_[i]->quit();
  } //finish current work


  p_event_loop_->quit();
  muduo::CurrentThread::sleepUsec(1000 * 1000);  //for safe
}

int CthriftSvr::Drain() {
  int32_t i32_ret = 0;
  sg_service_.__set_status(0);
  try {
    i32_ret = sp_sgagent_client_->unRegistService(sg_service_);
    if (CTHRIFT_UNLIKELY(i32_ret)) {
      CLOG_STR_ERROR("unRegistService failed: " << i32_ret);
    } else {
      CLOG_STR_INFO("unRegistService svr done");
    }
  } catch (TException &tx) {
    CLOG_STR_ERROR("unRegistService failed: " << tx.what());
    i32_ret = -1;
  }
  i8_heartbeat_status_ = 0;
  return i32_ret;
}

//For check time
void
CthriftSvr::OnWriteComplete(const muduo::net::TcpConnectionPtr &conn) {
  CLOG_STR_DEBUG("OnWriteComplete");
}

void
CthriftSvr::OnConn(const TcpConnectionPtr &conn) {
  CLOG_STR_INFO(conn->localAddress().toIpPort() << " -> "
           << conn->peerAddress().toIpPort() << " is "
           << (conn->connected() ? "UP" : "DOWN")
           << " Name:" << conn->name());

  if (conn->connected()) {
    static int32_t i32_max_conn_num_per_thread =
        static_cast<int32_t>(i32_max_conn_num_ /
            kI16CpuNum);
    if (CTHRIFT_UNLIKELY(i32_curr_conn_num_ >= i32_max_conn_num_per_thread)) {
      CLOG_STR_WARN("thread max conn " << i32_max_conn_num_per_thread
               << " reach");
      conn->forceClose();
      return;
    }

    ++i32_curr_conn_num_;

    ConnEntrySharedPtr sp_conn_entry = boost::make_shared<ConnEntry>(conn);
    (LocalSingConnEntryCirculBuf::instance()).back().insert(sp_conn_entry);

    ConnEntryWeakPtr wp_conn_entry(sp_conn_entry);

    ConnContextSharedPtr sp_conn_info = boost::make_shared<ConnContext>();
    //sp_conn_info->t_conn = time(0);
    sp_conn_info->wp_conn_entry = wp_conn_entry;

    conn->setContext(sp_conn_info);
    conn->setTcpNoDelay(true);
  } else if (0 < i32_curr_conn_num_) {
    --i32_curr_conn_num_;
  }
}

void CthriftSvr::Process(const boost::shared_ptr<muduo::net::Buffer> &sp_buf,
                         boost::weak_ptr <ConnContext> wp_conn_context,
                         boost::weak_ptr<TcpConnection> wp_tcp_conn,
                         Timestamp timestamp_from_recv) {
  //Process(static_cast<int32_t>(sp_buf->readableBytes() - sizeof(int64_t)),
  TcpConnectionPtr shared_conn(wp_tcp_conn.lock());
  ConnContextSharedPtr shared_context(wp_conn_context.lock());

  if (!(CTHRIFT_LIKELY(shared_conn && shared_conn->connected()))) {
    CLOG_STR_ERROR("connection broken, discard response pkg ");
    return;
  }

  if (!(CTHRIFT_LIKELY(shared_context ))) {
    CLOG_STR_ERROR("context broken, discard response pkg ");
    return;
  }

  bool &is_auth = shared_context->b_is_auth;

  (*sp_p_str_current_connid_)->assign(shared_conn->name());
  SetUserTagMap("conn id" , (*(*sp_p_str_current_connid_)));
  //snprintf(str_current_connid_, k16ConnIDlength, str_current_connid.c_str());
  CLOG_STR_DEBUG("begin  work process from conn " << *(*sp_p_str_current_connid_));

  //need get which protocol and do matching process

  size_t req_size = sp_buf->readableBytes();
  uint8_t *p_ui8_req_buf = reinterpret_cast<uint8_t *>(const_cast<char *>(sp_buf->peek()));

  //if length is less than 2,this package is not complete
  if(req_size <= 2) {
    CLOG_STR_ERROR("end work process error,  revc not enough " << *(*sp_p_str_current_connid_)
              << "req_size " << req_size);
    return;
  }

  cthrift_protocol_type type = CthriftUniformRequest::GetProtocolType(p_ui8_req_buf);
  switch (type){
    case CTHRIFT_HESSIAN_PROTOCOL:
      ProcessHessian(wp_tcp_conn, timestamp_from_recv);
      break;
    case CTHRIFT_UNIFORM_PROTOCOL:
      ProcessUniform(static_cast<int32_t>(sp_buf->readableBytes()),
                     reinterpret_cast<uint8_t *>(const_cast<char *>
                     (sp_buf->peek())),
                     is_auth,
                     wp_tcp_conn, timestamp_from_recv);
      break;
    case CTHRIFT_THRIFT_PROTOCOL:
      ProcessThrift(static_cast<int32_t>(sp_buf->readableBytes()),
                    reinterpret_cast<uint8_t *>(const_cast<char *>
                    (sp_buf->peek())),
                    wp_tcp_conn, timestamp_from_recv);
      break;
    default:
      CLOG_STR_ERROR("ERROR protocol for " << *(*sp_p_str_current_connid_)
                << " protocol " << type);
      break;
  }

  CLOG_STR_DEBUG( "end  work process from conn:" << (*(*sp_p_str_current_connid_)));

  //memset(str_current_connid_, k16ConnIDlength, 0);
  (*sp_p_str_current_connid_)->clear();
  (*sp_p_str_user_tag_map_)->clear();

  //LOG_DEBUG << "msg id " << sp_buf->peekInt64() << " pass to real process";
}

void CthriftSvr::ProcessThrift(const int32_t &i32_req_size,
                   uint8_t *p_u8_req_buf,
                   boost::weak_ptr <muduo::net::TcpConnection> wp_tcp_conn,
                   muduo::Timestamp timestam){
  //call original thrift process
  RemoteProcessCall rpc(str_svr_appkey_);
  (*(*sp_p_str_current_traceid_tag_map_))["traceID"] = rpc.getTraceId();
  (*sp_p_str_current_traceid_)->assign(rpc.getTraceId());

  (*(*sp_p_str_current_traceid_tag_map_))["spanID"] = rpc.getRpcId();
  (*sp_p_str_current_spanid_)->assign(rpc.getRpcId());

  CLOG_STR_DEBUG(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << "begin  work ProcessThrift from conn " << *(*sp_p_str_current_connid_)
            << " traceId :" << *(*sp_p_str_current_traceid_));

  CthriftUniformRequest request;
  if(b_auth_){
     CLOG_STR_WARN(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << " need auth && origin thrift protocol" );

     if(b_grayRelease_){
         CLOG_STR_INFO(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << "user set grayRelease && just log" );
         Process(request, i32_req_size, p_u8_req_buf, wp_tcp_conn, timestam);
     }else{
         HandleAuthFailed(request, i32_req_size, p_u8_req_buf, wp_tcp_conn, timestam);
     }

    }else{
    Process(request, i32_req_size, p_u8_req_buf, wp_tcp_conn, timestam);
  }

  CLOG_STR_DEBUG(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << "end  work ProcessThrift from conn " << *(*sp_p_str_current_connid_)
          << " traceId :" << *(*sp_p_str_current_traceid_));
  (*sp_p_str_current_traceid_)->clear();
  (*sp_p_str_current_traceid_tag_map_)->clear();
  (*sp_p_str_current_spanid_)->clear();
}

void CthriftSvr::ProcessHessian(boost::weak_ptr <muduo::net::TcpConnection> wp_tcp_conn,
                    muduo::Timestamp timestam){
  //just print some message and return;
  CLOG_STR_WARN("have no support  Hessian protocol" << *(*sp_p_str_current_connid_));
}

void CthriftSvr::ProcessUniform(const int32_t &i32_req_size,
                    const uint8_t *p_u8_req_buf,
                    bool &b_is_auth,
                    boost::weak_ptr <muduo::net::TcpConnection> wp_tcp_conn,
                    muduo::Timestamp timestam) {
  //unpack request and do some process
  CthriftUniformRequest request(i32_req_size, p_u8_req_buf);

  if(!request.UnPackRequest()) {
    CLOG_STR_WARN("unPackRequest ERROR from conn" << *(*sp_p_str_current_connid_));
    return;
  }

  switch (request.GetHeader()->messageType){
      case MessageType::Normal:
      ProcessUniformNormal(request, request.GetBodyLength(), request.GetBody(), b_is_auth, wp_tcp_conn, timestam);
      break;
    case MessageType::NormalHeartbeat:
      ProcessUniformNormalHeartBeat(request, wp_tcp_conn, timestam);
      break;
    case MessageType::ScannerHeartbeat:
      ProcessUniformScannerHeartBeat(request, wp_tcp_conn, timestam);
      break;
    default:
      CLOG_STR_WARN("ERROR messageType "<<  request.GetHeader()->messageType << " from conn" << *(*sp_p_str_current_connid_));
      return;
  }
}

void CthriftSvr::HandleAuthFailed(CthriftUniformRequest& request,
                                         const int32_t &i32_req_size,
                                         uint8_t *p_ui8_req_buf,
                                         boost::weak_ptr<TcpConnection> wp_tcp_conn,
                                         Timestamp timestamp_from_recv){
  bool b_is_open_cat = CthriftSgagent::b_is_open_cat_;
  bool b_is_open_mtrace = CthriftSgagent::b_is_open_mtrace_;

  TcpConnectionPtr sp_tcp_conn(wp_tcp_conn.lock());
  if (CTHRIFT_LIKELY(sp_tcp_conn && sp_tcp_conn->connected())) {
    if(CTHRIFT_LIKELY(b_is_open_mtrace)) {
      if(request.GetProtocolType() == CTHRIFT_UNIFORM_PROTOCOL){
        SERVER_SEND_TRACE_WITH_APPKEY_V2(request.GetHeader()->traceInfo.clientAppkey, (sp_tcp_conn->peerAddress()).toIp(), (sp_tcp_conn->peerAddress()).toPort(), str_svr_appkey_,
                                         request.GetHeader()->traceInfo.traceId, request.GetHeader()->traceInfo.spanId);
      }
      else {
        SERVER_SEND_V2((sp_tcp_conn->peerAddress()).toIpPort(), str_svr_appkey_);
      }
    }
    //CANNOT fetch spanname yet in cthrift
  } else {
    CLOG_STR_ERROR(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << "connection broken, discard response pkg from conn  " << *(*sp_p_str_current_connid_));
    return;
  }

  CatTransaction *trans = NULL;
  if(CTHRIFT_LIKELY(b_is_open_cat)){
    trans = newTransaction(string("OctoService").c_str(), str_svr_appkey_.c_str());

    logEvent("OctoService.thriftType", "idl", CAT_SUCCESS, NULL);
    logEvent("OctoService.clientIp", (sp_tcp_conn->peerAddress()).toIpPort().c_str(), CAT_SUCCESS, NULL);
    logEvent("OctoService.requestSize", GetBufSizeRange(i32_req_size).c_str(), CAT_SUCCESS, NULL);
  }

  int clientStatus = 0; //NOT used by cmtrace, just fill


  if(CTHRIFT_LIKELY(b_is_open_cat)) {
      logEvent("OctoService.handleType", "auth failed", CAT_ERROR, NULL);
      trans->setStatus((CatMessage *) trans, CAT_ERROR);
  }

  (*sp_p_input_tmemorybuffer_)->resetBuffer(p_ui8_req_buf,
                                            i32_req_size,
                                            TMemoryBuffer::COPY);

  (*sp_p_output_tmemorybuffer_)->resetBuffer();
  (*sp_p_output_tmemorybuffer_)->getWritePtr(sizeof(int32_t));
  (*sp_p_output_tmemorybuffer_)->wroteBytes(sizeof(int32_t));

  std::string name;
  TMessageType type;
  int32_t seqid;
  (*sp_p_input_tprotocol_)->readMessageBegin(name, type, seqid);



  ::apache::thrift::TApplicationException x("SecurityException");
  (*sp_p_output_tprotocol_)->writeMessageBegin(name, T_EXCEPTION, seqid);
   x.write((*sp_p_output_tprotocol_).get());
  (*sp_p_output_tprotocol_)->writeMessageEnd();
  (*sp_p_output_tprotocol_)->getTransport()->writeEnd();
  (*sp_p_output_tprotocol_)->getTransport()->flush();


  uint8_t *p_u8_res_buf = 0;
  uint32_t u32_res_size = 0;
  (*sp_p_output_tmemorybuffer_)->getBuffer(&p_u8_res_buf, &u32_res_size);



  if(request.GetProtocolType() != CTHRIFT_UNIFORM_PROTOCOL) {

    int32_t i32_body_size =
            static_cast<int32_t>(htonl(static_cast<uint32_t>(u32_res_size - sizeof
                    (int32_t))));
    memcpy(p_u8_res_buf, &i32_body_size, sizeof(int32_t));

    if (CTHRIFT_LIKELY(sp_tcp_conn && sp_tcp_conn->connected())) {
      sp_tcp_conn->send(p_u8_res_buf, u32_res_size);  //already check when begin
    } else {
      CLOG_STR_WARN(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) <<
                                                                     "connection broken, discard response pkg from conn  " << *(*sp_p_str_current_connid_));
    }
  }else{
    CthriftUniformResponse response(request.GetHeader()->requestInfo.sequenceId, StatusCode::SecurityException, "SecurityException");
    response.PackAuthFailed(request.GetHeader()->traceInfo, u32_res_size - sizeof(int32_t), p_u8_res_buf + sizeof(int32_t));

    if (CTHRIFT_LIKELY(sp_tcp_conn && sp_tcp_conn->connected())) {
      sp_tcp_conn->send(response.GetResponseBuf(), response.GetResponseSize());
    } else {
      CLOG_STR_WARN(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) <<
                                                                     "connection broken, discard response pkg from conn  " << *(*sp_p_str_current_connid_));
    }
  }

  if(CTHRIFT_LIKELY(b_is_open_mtrace)) {
    SERVER_RECV_V2(clientStatus);
  }

  if(CTHRIFT_LIKELY(b_is_open_cat)) {
    trans->setComplete((CatMessage *) trans);
  }

  CLOG_STR_INFO(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << "HandleAuthFailed Done" << " from conn " <<  *(*sp_p_str_current_connid_)
   << "   from Peer:" << (sp_tcp_conn->peerAddress()).toIpPort());
}

void CthriftSvr::ProcessUniformNormal(CthriftUniformRequest& request,
                          const int32_t &i32_req_size,
                          const uint8_t *p_u8_req_buf,
                          bool &b_is_auth,
                          boost::weak_ptr <muduo::net::TcpConnection> wp_tcp_conn,
                          muduo::Timestamp timestam){
    (*(*sp_p_str_current_traceid_tag_map_))["traceID"] = (request.GetHeader())->traceInfo.traceId;
    (*sp_p_str_current_traceid_)->assign((request.GetHeader())->traceInfo.traceId);

    (*(*sp_p_str_current_traceid_tag_map_))["spanID"] = (request.GetHeader())->traceInfo.spanId;
    (*sp_p_str_current_spanid_)->assign((request.GetHeader())->traceInfo.spanId);

    CLOG_STR_INFO(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << "begin  work ProcessUniformNormal from conn " << *(*sp_p_str_current_connid_)
              << " from client appkey" << (request.GetHeader())->traceInfo.clientAppkey);


    do{
      if(b_auth_ && !b_is_auth){
        //need auth
        const Header* header =  request.GetHeader();
        const std::string &appkey = header->traceInfo.clientAppkey;
        const std::string &serviceName = header->requestInfo.serviceName;
        const Context &local_context = header->localContext;

        StrStrMap    &appekyTokenMap = (*(*sp_p_str_str_appkeyMap_));
        StrStrMap    &whiteMap = (*(*sp_p_str_str_whiteMap_));
        StrSMMap     &methodAppkeyTokenMap =  (*(*sp_p_str_map_methodMap_));
        //std::string  &local_token = (*(*sp_p_str_local_token));


        if(whiteMap.find(appkey) != whiteMap.end()){
          //success 白名单内部，成功,跳出,如果是服务粒度的鉴权，则后续无需鉴权
          if(AuthAppkey == auth_size_) {
            CLOG_STR_DEBUG(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << " white list  &&  auth  success  " );
            b_is_auth = true;
          }
          break;
        }

        Context::const_iterator it = local_context.find("auth-signature");
        if(it == local_context.end()){
          //todo 需要鉴权 但是没有token,鉴权失败
          CLOG_STR_WARN(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << " token  empty &&  auth  failed  " );

          if(b_grayRelease_){
              CLOG_STR_INFO(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << " user set grayRelease&& just log ");
              break;
          }else{
              HandleAuthFailed(request, i32_req_size, (uint8_t *)p_u8_req_buf, wp_tcp_conn, timestam);
              return;
          }

        }
        //todo hasc
        const std::string &token = it->second;

        if(AuthAppkey == auth_size_){

          if(!token_.empty()){
            if(CthriftKmsTools::hmacSHA1(token_, appkey) == token){
               CLOG_STR_DEBUG(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << " user set token && appkey auth  success  " );
               b_is_auth = true;
               break;
            }else{
              CLOG_STR_WARN(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << " user set token  && appkey auth  failed  " );

                if(b_grayRelease_){
                    CLOG_STR_INFO(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << " user set grayRelease&& just log ");
                    break;
                }else{
                    HandleAuthFailed(request, i32_req_size, (uint8_t *)p_u8_req_buf, wp_tcp_conn, timestam);
                    return;
                }
            }
          }

          if( (appekyTokenMap.find(appkey) != appekyTokenMap.end()) && (CthriftKmsTools::hmacSHA1(appekyTokenMap[appkey], appkey) == token) ){
             b_is_auth = true;
            CLOG_STR_DEBUG(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << " appkey token  &&  auth  success  " );
             break;
          }else{
             //todo auth failed 服务粒度鉴权,鉴权失败
            CLOG_STR_WARN(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << " appkey token  empty ||  auth  failed  " );

              if(b_grayRelease_){
                  CLOG_STR_INFO(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << " user set grayRelease&& just log ");
                  break;
              }else{
                  HandleAuthFailed(request, i32_req_size, (uint8_t *)p_u8_req_buf, wp_tcp_conn, timestam);
                  return;
              }
          }
        }else{

          if(!token_.empty()){
            if(CthriftKmsTools::hmacSHA1(token_, appkey) == token){
              CLOG_STR_DEBUG(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << " user set token && interface auth  success  " );
              break;
            }else{
              CLOG_STR_WARN(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << " user set token  && interface auth  failed  " );

                if(b_grayRelease_){
                    CLOG_STR_INFO(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << " user set grayRelease&& just log ");
                    break;
                }else{
                    HandleAuthFailed(request, i32_req_size, (uint8_t *)p_u8_req_buf, wp_tcp_conn, timestam);
                    return;
                }
            }
          }

          if(serviceName.empty()){
            if( (appekyTokenMap.find(appkey) != appekyTokenMap.end()) && (CthriftKmsTools::hmacSHA1(appekyTokenMap[appkey], appkey) == token) ){
              b_is_auth = true;
              CLOG_STR_DEBUG(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << "interface empty  try appkey auth&& auth  success  " );
              break;
            }else{
              //todo auth failed 接口粒度鉴权,servicename为空，尝试服务粒度鉴权,鉴权失败
              CLOG_STR_WARN(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << " interface empty  try appkey auth&& auth  failed  " );

                if(b_grayRelease_){
                    CLOG_STR_INFO(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << " user set grayRelease&& just log ");
                    break;
                }else{
                    HandleAuthFailed(request, i32_req_size, (uint8_t *)p_u8_req_buf, wp_tcp_conn, timestam);
                    return;
                }
            }
          }


          StrSMMap::iterator it = methodAppkeyTokenMap.find(serviceName);
          if(it == methodAppkeyTokenMap.end()){
            //todo auth failed 接口粒度鉴权,servicename不为空，servicename没有token,鉴权失败
            CLOG_STR_WARN(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << " interface token empty && interface auth  failed : " << serviceName );

              if(b_grayRelease_){
                  CLOG_STR_INFO(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << " user set grayRelease&& just log : " << serviceName );
                  break;
              }else{
                  HandleAuthFailed(request, i32_req_size, (uint8_t *)p_u8_req_buf, wp_tcp_conn, timestam);
                  return;
              }
          }


          if((it->second.find(appkey) == it->second.end()) || (CthriftKmsTools::hmacSHA1(it->second[appkey], appkey) != token))
          {
            //todo auth failed 接口粒度鉴权,servicename不为空，servicename下面appkey没有token,鉴权失败
            CLOG_STR_WARN(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << " interface auth  failed " );

              if(b_grayRelease_){
                  CLOG_STR_INFO(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << " user set grayRelease&& just log ");
                  break;
              }else{
                  HandleAuthFailed(request, i32_req_size, (uint8_t *)p_u8_req_buf, wp_tcp_conn, timestam);
                  return;
              }
          }

          CLOG_STR_DEBUG(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << " auth  success  " );
          //todo success
        }

      }
    }while(0);

    Process(request, i32_req_size, (uint8_t *)p_u8_req_buf, wp_tcp_conn, timestam);
    CLOG_STR_INFO(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << "end  work ProcessUniformNormal from conn " << *(*sp_p_str_current_connid_)
              << " from client appkey" << (request.GetHeader())->traceInfo.clientAppkey
              << " traceId :" << *(*sp_p_str_current_traceid_));
  (*sp_p_str_current_traceid_)->clear();
  (*sp_p_str_current_traceid_tag_map_)->clear();
  (*sp_p_str_current_spanid_)->clear();
}

void CthriftSvr::ProcessUniformNormalHeartBeat(CthriftUniformRequest& request,
                                   boost::weak_ptr <muduo::net::TcpConnection> wp_tcp_conn,
                                   muduo::Timestamp timestam){
    CLOG_STR_DEBUG("begin work ProcessUniformNormalHeartBeat from conn " << *(*sp_p_str_current_connid_)
              << " from client appkey" << (request.GetHeader())->traceInfo.clientAppkey);

    ProcessHeartbeat(request, wp_tcp_conn, timestam);
    CLOG_STR_DEBUG("end  work ProcessUniformNormalHeartBeat from conn " << *(*sp_p_str_current_connid_)
              << " from client appkey" << (request.GetHeader())->traceInfo.clientAppkey);

}

void CthriftSvr::ProcessUniformScannerHeartBeat( CthriftUniformRequest& request,
                                    boost::weak_ptr <muduo::net::TcpConnection> wp_tcp_conn,
                                    muduo::Timestamp timestam){
    CLOG_STR_DEBUG("begin  work ProcessUniformScannerHeartBeat from conn " << *(*sp_p_str_current_connid_)
              << " from client appkey" << (request.GetHeader())->traceInfo.clientAppkey);

    ProcessHeartbeat(request, wp_tcp_conn, timestam);
    CLOG_STR_DEBUG("end  work ProcessUniformScannerHeartBeat from conn " << *(*sp_p_str_current_connid_)
              << " from client appkey" << (request.GetHeader())->traceInfo.clientAppkey);
}


void CthriftSvr::Process( CthriftUniformRequest& request,
                         const int32_t &i32_req_size,
                         uint8_t *p_ui8_req_buf,
                         boost::weak_ptr<TcpConnection> wp_tcp_conn,
                         Timestamp timestamp_from_recv) {

  bool b_is_open_cat = CthriftSgagent::b_is_open_cat_;
  bool b_is_open_mtrace = CthriftSgagent::b_is_open_mtrace_;

  TcpConnectionPtr sp_tcp_conn(wp_tcp_conn.lock());
  if (CTHRIFT_LIKELY(sp_tcp_conn && sp_tcp_conn->connected())) {
    if(CTHRIFT_LIKELY(b_is_open_mtrace)) {
        if(request.GetProtocolType() == CTHRIFT_UNIFORM_PROTOCOL){
	    SERVER_SEND_TRACE_WITH_APPKEY_V2(request.GetHeader()->traceInfo.clientAppkey, (sp_tcp_conn->peerAddress()).toIp(), (sp_tcp_conn->peerAddress()).toPort(), str_svr_appkey_,
                     request.GetHeader()->traceInfo.traceId, request.GetHeader()->traceInfo.spanId);
        }
        else {
            SERVER_SEND_V2((sp_tcp_conn->peerAddress()).toIpPort(), str_svr_appkey_);
        }
    }
    //CANNOT fetch spanname yet in cthrift
  } else {
    CLOG_STR_ERROR(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << "connection broken, discard response pkg from conn  " << *(*sp_p_str_current_connid_));
    return;
  }

  CatTransaction *trans = NULL;
  if(CTHRIFT_LIKELY(b_is_open_cat)){
    trans = newTransaction(string("OctoService").c_str(), str_svr_appkey_.c_str());

    logEvent("OctoService.thriftType", "idl", CAT_SUCCESS, NULL);
    logEvent("OctoService.clientIp", (sp_tcp_conn->peerAddress()).toIpPort().c_str(), CAT_SUCCESS, NULL);
    logEvent("OctoService.requestSize", GetBufSizeRange(i32_req_size).c_str(), CAT_SUCCESS, NULL);
  }

  int clientStatus = 0; //NOT used by cmtrace, just fill

  if (CTHRIFT_UNLIKELY(0 == i32_req_size
                           || 0 == p_ui8_req_buf)) {
    CLOG_STR_ERROR(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << "i32_req_size " << i32_req_size
              << "  OR p_ui8_req_buf = NULL  from conn: " << *(*sp_p_str_current_connid_));

    if(CTHRIFT_LIKELY(b_is_open_mtrace)) {
      SERVER_RECV_V2(clientStatus);
    }

    return;
  }

  const double
      d_svr_overtime_secs = static_cast<double>(i32_svr_overtime_ms_) / 1000;

  //check if more than svr overtime
  if (i32_svr_overtime_ms_
      && CheckOverTime(timestamp_from_recv,
                       d_svr_overtime_secs, 0)) {
    CLOG_STR_WARN(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << "before business handle, already overtime, maybe queue congest, drop the request"
        << "  from conn  " << *(*sp_p_str_current_connid_));

    if(CTHRIFT_LIKELY(b_is_open_mtrace)) {
      SERVER_RECV_V2(clientStatus);
    }

    if(CTHRIFT_LIKELY(b_is_open_cat)) {
      logEvent("OctoService.handleType", "drop", CAT_ERROR, NULL);
      trans->setStatus((CatMessage *) trans, CAT_ERROR);
      trans->setComplete((CatMessage *) trans);
    }
    return;
  }

  if(CTHRIFT_LIKELY(b_is_open_cat)) {
    logEvent("OctoService.handleType", "accept", CAT_SUCCESS, NULL);
  }

  (*sp_p_input_tmemorybuffer_)->resetBuffer(p_ui8_req_buf,
                                            i32_req_size,
                                            TMemoryBuffer::COPY);

  (*sp_p_output_tmemorybuffer_)->resetBuffer();
  (*sp_p_output_tmemorybuffer_)->getWritePtr(sizeof(int32_t));
  (*sp_p_output_tmemorybuffer_)->wroteBytes(sizeof(int32_t));

  /* CatTransaction *trans =
       newTransaction(string("OctoService").c_str(), str_svr_appkey_.c_str());
   logMetricForCount(str_svr_appkey_.c_str());*/

  Timestamp timestamp_begin_business = Timestamp::now();
  double d_business_time_diff_ms = 0.0;

  CLOG_STR_INFO(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << "Begin business Process from " << *(*sp_p_str_current_connid_));
  try {
    (*sp_p_processor_)->process(*sp_p_input_tprotocol_,
                                *sp_p_output_tprotocol_, 0);
  } catch (exception e) {
    CLOG_STR_ERROR(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << "exception from business process: " << e.what()
              << " from conn " <<  *(*sp_p_str_current_connid_));

    d_business_time_diff_ms =
        timeDifference(Timestamp::now(), timestamp_begin_business) * 1000;
    CLOG_STR_DEBUG(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << "business cost " << d_business_time_diff_ms << "ms"
              << " from conn " <<  *(*sp_p_str_current_connid_));

    if (i32_svr_overtime_ms_
        && CheckOverTime(timestamp_from_recv, d_svr_overtime_secs,
                         0)) {
      CLOG_STR_WARN(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << "after business handle, already overtime "
               << i32_svr_overtime_ms_ << "ms, business cost "
               << d_business_time_diff_ms << " ms, just WARN without drop"
               << " from conn " <<  *(*sp_p_str_current_connid_));
    }

    if(CTHRIFT_LIKELY(b_is_open_mtrace)) {
      SERVER_RECV_V2(clientStatus);
    }


    if(CTHRIFT_LIKELY(b_is_open_cat)) {
      trans->setStatus(reinterpret_cast<CatMessage *>(trans), CAT_ERROR);
      trans->setComplete(reinterpret_cast<CatMessage *>(trans));
    }

    return;
  }
  CLOG_STR_INFO(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << "End business Process from conn  " << *(*sp_p_str_current_connid_));

  d_business_time_diff_ms =
      timeDifference(Timestamp::now(), timestamp_begin_business) * 1000;
  CLOG_STR_DEBUG(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << "business cost " << d_business_time_diff_ms << "ms");

  if (i32_svr_overtime_ms_
      && CheckOverTime(timestamp_from_recv,
                       d_svr_overtime_secs, 0)) {
    CLOG_STR_WARN(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << "after business handle, already overtime "
             << i32_svr_overtime_ms_ << "ms, business cost "
             << d_business_time_diff_ms << " ms, just WARN without drop"
            << " from conn:" << *(*sp_p_str_current_connid_));

    if(CTHRIFT_LIKELY(b_is_open_cat)){
        logEvent("OctoService.business", "timeout", CAT_ERROR, NULL);
    }
  }else{
    if(CTHRIFT_LIKELY(b_is_open_cat)){
        logEvent("OctoService.business", "ontime", CAT_SUCCESS, NULL);
    }
  }

  double total_cost = timeDifference(Timestamp::now(), timestamp_from_recv) * 1000;

  SetUserTagMap("total cost",double2String(total_cost) + "ms");

  CLOG_STR_DEBUG(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << "after business process, total cost "
            << total_cost
            << "ms" << " from conn " <<  *(*sp_p_str_current_connid_));

  /* trans->setStatus(reinterpret_cast<CatMessage *>(trans), CAT_SUCCESS);
   //framework process OK, NOT mean business handle error
   trans->setComplete(reinterpret_cast<CatMessage *>(trans));
 */

  uint8_t *p_u8_res_buf = 0;
  uint32_t u32_res_size = 0;
  (*sp_p_output_tmemorybuffer_)->getBuffer(&p_u8_res_buf, &u32_res_size);

  if (CTHRIFT_UNLIKELY(sizeof(int32_t) >= u32_res_size)) {
    CLOG_STR_ERROR(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << "u32_res_size " << u32_res_size << " NOT enough"
              << " from conn " <<  *(*sp_p_str_current_connid_));

    if(CTHRIFT_LIKELY(b_is_open_mtrace)) {
      SERVER_RECV_V2(clientStatus);
    }

    if(CTHRIFT_LIKELY(b_is_open_cat)) {
      logEvent("OctoService.responseSize", GetBufSizeRange(u32_res_size).c_str(), CAT_ERROR, NULL);
      trans->setStatus(reinterpret_cast<CatMessage *>(trans), CAT_ERROR);
      trans->setComplete(reinterpret_cast<CatMessage *>(trans));
    }

    return;
  }

  CLOG_STR_DEBUG(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) << "u32_res_size " << u32_res_size
             << " from conn " <<  *(*sp_p_str_current_connid_));

  if(request.GetProtocolType() != CTHRIFT_UNIFORM_PROTOCOL) {
      int32_t i32_body_size =
              static_cast<int32_t>(htonl(static_cast<uint32_t>(u32_res_size - sizeof
              (int32_t))));
      memcpy(p_u8_res_buf, &i32_body_size, sizeof(int32_t));

      if (CTHRIFT_LIKELY(sp_tcp_conn && sp_tcp_conn->connected())) {
          sp_tcp_conn->send(p_u8_res_buf, u32_res_size);  //already check when begin
      } else {
          CLOG_STR_WARN(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) <<
        "connection broken, discard response pkg from conn  " << *(*sp_p_str_current_connid_));
      }
  }else{
      CthriftUniformResponse response(request.GetHeader()->requestInfo.sequenceId,0,"");
      response.PackResponse(u32_res_size - sizeof(int32_t), p_u8_res_buf + sizeof(int32_t));

      if (CTHRIFT_LIKELY(sp_tcp_conn && sp_tcp_conn->connected())) {
          sp_tcp_conn->send(response.GetResponseBuf(), response.GetResponseSize());
      } else {
          CLOG_STR_WARN(PUTTAGS((*(*sp_p_str_current_traceid_tag_map_))) <<
        "connection broken, discard response pkg from conn  " << *(*sp_p_str_current_connid_));
      }
  }

  if(CTHRIFT_LIKELY(b_is_open_mtrace)) {
    SERVER_RECV_V2(clientStatus);
  }

  if(CTHRIFT_LIKELY(b_is_open_cat)) {
    logEvent("OctoService.responseSize", GetBufSizeRange(u32_res_size).c_str(), CAT_SUCCESS, NULL);
    trans->setStatus(reinterpret_cast<CatMessage *>(trans), CAT_SUCCESS);
    trans->setComplete(reinterpret_cast<CatMessage *>(trans));
  }

  (*(*sp_p_str_user_tag_map_)).insert((*(*sp_p_str_current_traceid_tag_map_)).begin(), (*(*sp_p_str_current_traceid_tag_map_)).end());

   CLOG_STR_INFO(PUTTAGS((*(*sp_p_str_user_tag_map_))) << "Process Done"  << "   from Peer:" << (sp_tcp_conn->peerAddress()).toIpPort());
}

void CthriftSvr::ProcessHeartbeat(CthriftUniformRequest &request,
                                boost::weak_ptr<muduo::net::TcpConnection> wp_tcp_conn,
                                muduo::Timestamp timestamp_from_recv) {
    bool b_is_open_cat = CthriftSgagent::b_is_open_cat_;
    bool b_is_open_mtrace = CthriftSgagent::b_is_open_mtrace_;

    TcpConnectionPtr sp_tcp_conn(wp_tcp_conn.lock());
    if (!CTHRIFT_LIKELY(sp_tcp_conn && sp_tcp_conn->connected())) {
        CLOG_STR_ERROR("connection broken, discard response pkg from conn  " << *(*sp_p_str_current_connid_));
        return;
    }

    CatTransaction *trans = NULL;
    if(CTHRIFT_LIKELY(b_is_open_cat)){
        trans = newTransaction("Heartbeat", str_svr_appkey_.c_str());
        logEvent("Heartbeat.thriftType", "idl", CAT_SUCCESS, NULL);
        logEvent("Heartbeat.clientIp", (sp_tcp_conn->peerAddress()).toIpPort().c_str(), CAT_SUCCESS, NULL);
    }

    const double
            d_svr_overtime_secs = static_cast<double>(i32_svr_overtime_ms_) / 1000;
    int clientStatus = 0;
    //check if more than svr overtime
    if (i32_svr_overtime_ms_
        && CheckOverTime(timestamp_from_recv,
                         d_svr_overtime_secs, 0)) {
        CLOG_STR_WARN("before heartbeat handle, already overtime, maybe queue congest, drop the request"
                << "  from conn  " << *(*sp_p_str_current_connid_));
        if(CTHRIFT_LIKELY(b_is_open_cat)) {
            logEvent("Heartbeat.handleType", "drop", CAT_ERROR, NULL);
            trans->setStatus((CatMessage *) trans, CAT_ERROR);
            trans->setComplete((CatMessage *) trans);
        }
        return;
    }

    if(CTHRIFT_LIKELY(b_is_open_cat)) {
        logEvent("Heartbeat.handleType", "accept", CAT_SUCCESS, NULL);
    }

    Timestamp timestamp_begin_heartbeat = Timestamp::now();

    CLOG_STR_INFO("Begin heartbeat Process from " << *(*sp_p_str_current_connid_));
    LoadInfo load_info;
    HeartbeatInfo heart_info;
    load_info.__set_averageLoad(0.0);
    load_info.__set_threadNum(i16_worker_thread_num_);
    load_info.__set_queueSize(EventLoop::getEventLoopOfCurrentThread()->queueSize());
    std::map<std::string, double> methodQps;
    methodQps[str_svr_appkey_] = (double)(atom_i64_recv_msg_per_min_.get());
    load_info.__set_methodQpsMap(methodQps);

    heart_info.__set_appkey(str_svr_appkey_);
    heart_info.__set_loadInfo(load_info);
    heart_info.__set_status(i8_heartbeat_status_);
    heart_info.__set_sendTime((Timestamp::now()).microSecondsSinceEpoch());
    CLOG_STR_INFO("End heartbeat Process from conn  " << *(*sp_p_str_current_connid_));

    double d_heartbeat_time_diff_ms =
            timeDifference(Timestamp::now(), timestamp_begin_heartbeat) * 1000;
    CLOG_STR_DEBUG("heartbeat process cost " << d_heartbeat_time_diff_ms << "ms");

    CLOG_STR_DEBUG("after heartbeat process, total cost "
              << timeDifference(Timestamp::now(), timestamp_from_recv) * 1000
              << "ms" << " from conn " <<  *(*sp_p_str_current_connid_));

    CthriftUniformResponse response(request.GetHeader()->requestInfo.sequenceId, 0, "");
    response.PackScanner(heart_info, request.GetHeader()->messageType);

    if (CTHRIFT_LIKELY(sp_tcp_conn && sp_tcp_conn->connected())) {
        sp_tcp_conn->send(response.GetResponseBuf(), response.GetResponseSize());
    } else {
        LOG_ERROR << "connection broken, discard response pkg from conn  " << *(*sp_p_str_current_connid_);
    }

   if(CTHRIFT_LIKELY(b_is_open_cat)) {
    logEvent("Heartbeat.responseSize", GetBufSizeRange(response.GetResponseSize()).c_str(), CAT_SUCCESS, NULL);
    trans->setStatus(reinterpret_cast<CatMessage *>(trans), CAT_SUCCESS);
    trans->setComplete(reinterpret_cast<CatMessage *>(trans));
  }


  CLOG_STR_DEBUG("Heartbeat Process Done from connection " << *(*sp_p_str_current_connid_));
}

void CthriftSvr::OnMsg(const muduo::net::TcpConnectionPtr &conn,
                       muduo::net::Buffer *buffer,
                       Timestamp receiveTime) {
  //*sp_p_str_current_connid_ = conn->name();
  (*sp_p_str_current_connid_)->assign(conn->name());
  //snprintf(str_current_connid_, k16ConnIDlength, str_current_connid.c_str());
  CLOG_STR_DEBUG("OnMsg from conn " << *(*sp_p_str_current_connid_)
            <<  " address  "<< (conn->peerAddress()).toIpPort()
            <<  " Current EventLoop size " << EventLoop::getEventLoopOfCurrentThread()->queueSize());

  if (CTHRIFT_UNLIKELY((conn->getContext()).empty())) {
    CLOG_STR_ERROR("conn  "<< *(*sp_p_str_current_connid_)
              << "address: " << (conn->peerAddress()).toIpPort() <<
        "  context empty");    //NOT clear here
    return;
  }

  ConnContextSharedPtr sp_conn_info;
  try {
    sp_conn_info =
        boost::any_cast<ConnContextSharedPtr>(conn->getContext());
  } catch (boost::bad_any_cast e) {
    CLOG_STR_ERROR("bad_any_cast:" << e.what()
              << " conn: " << *(*sp_p_str_current_connid_));
    return;
  }

  ConnContextWeakPtr wp_conn_info(sp_conn_info);


  //先进行可用连接激活操作，避免正常连接被剔除.
  //放在while循环后，永远无法执行该段逻辑
  sp_conn_info->t_last_active = time(0);

  ConnEntrySharedPtr sp_conn_entry((sp_conn_info->wp_conn_entry).lock());
  if (CTHRIFT_UNLIKELY(!sp_conn_entry)) {
      CLOG_STR_ERROR("sp_conn_entry invalid??");
      return;
  } else {
      (LocalSingConnEntryCirculBuf::instance()).back().insert(sp_conn_entry);
  }

  bool more = true;
  while (more) {
    if (sp_conn_info->enum_state == kExpectFrameSize) {


      if(buffer->readableBytes() < 2){
          //LOG_WARN << "not enough size for judge protocol type" ;
          return;
      }

      cthrift_protocol_type type = CthriftUniformRequest::GetProtocolType(reinterpret_cast<uint8_t *>((
              const_cast<char *>(buffer->peek()))));


      switch(type){
          case CTHRIFT_HESSIAN_PROTOCOL:{
              CLOG_STR_WARN("OnMsg from HESSIAN drop connect:" << *(*sp_p_str_current_connid_));
              conn->forceClose();
              return;
          }
          case CTHRIFT_UNIFORM_PROTOCOL:{

              if(buffer->readableBytes() < 8){
                  CLOG_STR_WARN("not enough size for protocol uniform head&&length&&totallength");
                  return;
              }

              int32_t length = CthriftUniformRequest::GetTotallength(reinterpret_cast<uint8_t *>((
                      const_cast<char *>(buffer->peek()))));

              if (length <= buffer->readableBytes()) {
                  sp_conn_info->i32_want_size = length ;
                  sp_conn_info->enum_state = kExpectFrame;
              } else {
                  more = false;
              }
              break;
          }
          case CTHRIFT_THRIFT_PROTOCOL:{
              if (sizeof(int32_t) <= buffer->readableBytes()) {
                  sp_conn_info->i32_want_size =
                          static_cast<uint32_t>(buffer->readInt32());
                  sp_conn_info->enum_state = kExpectFrame;
              } else {
                  more = false;
              }
              break;
          }
          default:
              CLOG_STR_WARN("Error type protocol :" << type);
              break;

      }

    } else if (sp_conn_info->enum_state == kExpectFrame) {
      if (buffer->readableBytes() >=
          static_cast<size_t>(sp_conn_info->i32_want_size)) {
        //stat increment
        atom_i64_recv_msg_per_min_.increment();

        TcpConnWeakPtr wp_tcp_conn(conn);


        /*if (buffer->readableBytes() ==
            static_cast<size_t>(sp_conn_info->i32_want_size)
            || 0 == i16_worker_thread_num_) {*/


        /* if (0 == i16_worker_thread_num_) {


           LOG_DEBUG
               << "Read water is low OR single thread, do job by conn thread";

           Process(sp_conn_info->i32_want_size,
                   reinterpret_cast<uint8_t *>((
                       const_cast<char *>(buffer->peek()))),
                   wp_tcp_conn,
                   receiveTime);

           *//*} else if (buffer->readableBytes() > static_cast<size_t>
          (sp_conn_info->i32_want_size)) {*//*


        } else {


          LOG_DEBUG << "Read water is high, do job by worker thread";*/

        //use by other thread, so should copy out
        boost::shared_ptr<muduo::net::Buffer> sp_copy_buf =
            boost::make_shared<muduo::net::Buffer>();

        /* //add traceid
         int64_t tmp = atom_i64_worker_thread_pos_.get();
         sp_copy_buf->appendInt64(tmp);*/



        sp_copy_buf->append(reinterpret_cast<uint8_t *>((
                                const_cast<char *>(buffer->peek()))),
                            static_cast<size_t>
                            (sp_conn_info->i32_want_size));

        //pick worker thread, RR
        EventLoop *p_worker_event_loop = 0;
        if (CTHRIFT_LIKELY(1 < i16_worker_thread_num_)) {
          p_worker_event_loop =
              vec_worker_event_loop_[atom_i64_worker_thread_pos_.getAndAdd(1)
                  % i16_worker_thread_num_];
        } else if (CTHRIFT_UNLIKELY(1 == i16_worker_thread_num_)) {
          p_worker_event_loop = vec_worker_event_loop_[0];
        } //round robin choose next worker thread, no mutex for performance

        CLOG_STR_DEBUG("io Thread begin into work Thread  from conn  " << *(*sp_p_str_current_connid_)
                  << "  EventLoop queueSize " << p_worker_event_loop->queueSize());
        p_worker_event_loop->runInLoop(boost::bind(&CthriftSvr::Process,
                                                   this,
                                                   sp_copy_buf,
                                                   wp_conn_info,
                                                   wp_tcp_conn,
                                                   receiveTime));
        CLOG_STR_DEBUG("io Thread end into work Thread  from conn " << *(*sp_p_str_current_connid_));
        /*LOG_DEBUG << "msg id " << tmp << " passed to worker, queue size " << p_worker_event_loop->queueSize();*/

        buffer->retrieve(static_cast<size_t>(sp_conn_info->i32_want_size));
        sp_conn_info->enum_state = kExpectFrameSize;

        //收到一个完整的包后，进行可用连接激活操作，避免正常连接被剔除.
        //放在while循环后，永远无法执行该段逻辑
        sp_conn_info->t_last_active = time(0);

        ConnEntrySharedPtr sp_conn_entry((sp_conn_info->wp_conn_entry).lock());
        if (CTHRIFT_UNLIKELY(!sp_conn_entry)) {
            CLOG_STR_ERROR("sp_conn_entry invalid??");
            return;
        } else {
            (LocalSingConnEntryCirculBuf::instance()).back().insert(sp_conn_entry);
        }

      } else {
        more = false;
      }
    }
  }

}

void CthriftSvr::TimewheelKick(void) {
  (LocalSingConnEntryCirculBuf::instance()).push_back(ConnEntryBucket());
}

void
CthriftSvr::ConnThreadInit(muduo::CountDownLatch *p_countdown_connthread_init) {
  //init mtrace
  SERVER_INIT(str_svr_appkey_, "");   //In case handle in IO thread in future,
  // currently useless

  InitStaticThreadLocalMember();

  //time wheel
  assert(LocalSingConnEntryCirculBuf::pointer() == NULL);
  LocalSingConnEntryCirculBuf::instance();
  assert(LocalSingConnEntryCirculBuf::pointer() != NULL);
  (LocalSingConnEntryCirculBuf::instance()).resize(kI8TimeWheelGridNum);

  double dLoopInter = (0.0 == con_collection_interval_) ? (static_cast<double>(kTMaxCliIdleTimeSec)
      / kI8TimeWheelGridNum) : (con_collection_interval_ / kI8TimeWheelGridNum);
  CLOG_STR_DEBUG("dLoopInter " << dLoopInter);

  EventLoop::getEventLoopOfCurrentThread()->runEvery(dLoopInter,
                       boost::bind(&CthriftSvr::TimewheelKick, this));

  p_countdown_connthread_init->countDown();
}

void
CthriftSvr::WorkerThreadInit(muduo::CountDownLatch *p_countdown_workthread_init) {
  //init mtrace
  SERVER_INIT(str_svr_appkey_, "");

  InitStaticThreadLocalMember();
  p_countdown_workthread_init->countDown();
}


void CthriftSvr::SetConnGCInterval(const double &min) {
  double normal_min = min * 60.0;
  if (normal_min < static_cast<double>(kTMaxCliIdleTimeSec)) {
    normal_min = static_cast<double>(kTMaxCliIdleTimeSec);
  }
  con_collection_interval_ = normal_min;
}

void CthriftSvr::UpdateAuthInfo(){

  if(!b_auth_)  {
      CLOG_STR_DEBUG("UpdateAuthInfo && no auth");
      return;
  }

  if(CthriftSgagent::b_is_open_cat_){
      logEvent("cthrift.server.auth", str_svr_appkey_.c_str() , CAT_SUCCESS, NULL);
  }

  tools.Update();

  const StrStrMap& appkeyMap =  tools.GetAppekyTokenMap();
  const StrStrMap& whiteMap =   tools.GetAppkeyWhitelist();
  const StrSMMap&  methodMap = tools.GetMethodAppkeyTokenMap();
  const std::string&  local =  tools.GetLocalTokenString();



  for(vector<muduo::net::EventLoop *>::iterator it = vec_worker_event_loop_.begin();
      it != vec_worker_event_loop_.end(); ++it){

    (*it)->runInLoop(boost::bind(&CthriftSvr::UpdateAuthInfoForWorkThread,
                     this,
                     appkeyMap,
                     whiteMap,
                     methodMap,
                     local));

  }
}

void CthriftSvr::UpdateAuthInfoForWorkThread(StrStrMap& appkeyMap,  StrStrMap& whiteMap ,
                                               StrSMMap&  methodMap ,  std::string&  local){

  (*(*sp_p_str_str_appkeyMap_)).swap(appkeyMap) ;
  (*(*sp_p_str_str_whiteMap_)).swap(whiteMap) ;
  (*(*sp_p_str_map_methodMap_)).swap(methodMap) ;
  (*(*sp_p_str_local_token)).swap(local);
}


void CthriftSvr::SetUserTagMap(const std::string& key, const std::string& value){
  std::map<std::string, std::string>::iterator it =  (*(*sp_p_str_user_tag_map_)).find(key);
  if (it != (*(*sp_p_str_user_tag_map_)).end()){
    it->second = value;
  }else{
    (*(*sp_p_str_user_tag_map_))[key] = value;
  }
}
