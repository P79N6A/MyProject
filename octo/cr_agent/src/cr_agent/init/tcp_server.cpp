////
//// Created by smartlife on 2017/8/19.
////
#include "cr_interface.h"
#include "../../plugin/plugindef.h"
#include "../../util/cr_common.h"
#include "../server/cr_service.h"
#include "../server/cr_server.h"
#include "../server/cr_server_factory.h"
#include "../../comm/inc_comm.h"
#include <sys/types.h>

enum State {
	kExpectFrameSize,
	kExpectFrame
};
typedef boost::weak_ptr <muduo::net::TcpConnection> TcpConnWeakPtr;
struct ConnContext {
	public:
		enum State enum_state;
		int32_t i32_want_size;
		time_t t_conn;
		time_t t_last_active;
		ConnEntryWeakPtr wp_conn_entry;

		ConnContext(void)
			: enum_state(kExpectFrameSize), i32_want_size(0),
			t_last_active(0) {}
};

typedef boost::shared_ptr <ConnContext> ConnContextSharedPtr;



void TcpServer::SetLogServer(){

	log4cplus::PropertyConfigurator::doConfigure(LOG4CPLUS_TEXT(
				"/opt/meituan/apps/cr_agent/log4cplus.conf"));
	return;

}
int TcpServer::SetServerPara(){

	if(NULL!= CCInterface::GetCCInstance()){
		CCInterface::GetCCInstance()->GetConfigIns()->LoadConf();
		m_ListenPort=CCInterface::GetCCInstance()->GetConfigIns()->GetListenPort();
		if(m_ListenPort<0){
			LOG_ERROR("m_ListenPort init error.")
				return -1;
		}
	}else{
		return -1;
	}
	return -1;
}
void TcpServer::OnConn(const muduo::net::TcpConnectionPtr &conn){

	LOG_INFO(conn->localAddress().toIpPort() << " -> "
			<< conn->peerAddress().toIpPort() << " is "
			<< (conn->connected() ? "UP" : "DOWN"));

	ConnEntrySharedPtr sp_conn_entry = boost::make_shared<ConnEntry>(conn);
	(LocalSingConnEntryCirculBuf::instance()).back().insert(sp_conn_entry);

	ConnEntryWeakPtr wp_conn_entry(sp_conn_entry);

	ConnContextSharedPtr sp_conn_info = boost::make_shared<ConnContext>();
	sp_conn_info->t_conn = time(0);
	sp_conn_info->wp_conn_entry = wp_conn_entry;

	conn->setContext(sp_conn_info);
}

void TcpServer::OnMsg(const muduo::net::TcpConnectionPtr &conn,
		muduo::net::Buffer *buffer,
		muduo::Timestamp receiveTime){

	const char rcvBuffer[1024]={'0'};
	LOG_INFO("address:" << (conn->peerAddress()).toIpPort());    //NOT clear here

	ConnContextSharedPtr sp_conn_info;
	try{
		sp_conn_info =
			boost::any_cast<ConnContextSharedPtr>(conn->getContext());
	}catch (boost::bad_any_cast e){
		LOG_ERROR( "bad_any_cast:" << e.what());
		return;
	}

	boost::shared_ptr<ServerFactory> serverFactory = boost::make_shared<ServerFactory>();
	if (NULL == serverFactory) {
		LOG_ERROR("==serverFactory is null==");
		return;
	}
	boost::shared_ptr<Server> cTcpServer = serverFactory->CreateServer(CRANE_TCP_SERVER);
	if (NULL == cTcpServer) {
		LOG_ERROR("==cTcpServer is null==");
		return;
	}
	bool more = true;
	const int noUsingFd=0;
	while (more) {
		if (sp_conn_info->enum_state == kExpectFrameSize) {

			if (sizeof(int32_t) <= buffer->readableBytes()) {
				sp_conn_info->i32_want_size =
					static_cast<uint32_t>(buffer->readInt32());
				sp_conn_info->enum_state = kExpectFrame;
			}else {
				more = false;
			}
		} else if (sp_conn_info->enum_state == kExpectFrame) 		
			if (buffer->readableBytes() >=
					static_cast<size_t>(sp_conn_info->i32_want_size)) {

				TcpConnWeakPtr wp_tcp_conn(conn);

				boost::shared_ptr<muduo::net::Buffer> sp_copy_buf =
					boost::make_shared<muduo::net::Buffer>();

				sp_copy_buf->append(reinterpret_cast<uint8_t *>((
								const_cast<char *>(buffer->peek()))),
						static_cast<size_t>
						(sp_conn_info->i32_want_size));
				uint8_t *p_ui8_req_buf = reinterpret_cast<uint8_t *>(const_cast<char *>(sp_copy_buf->peek()));
				const int len=strlen((char*)p_ui8_req_buf);
				//memcpy((char *) rcvBuffer, (char *) p_ui8_req_buf, len);
				cTcpServer->onMessage(noUsingFd,(char*)p_ui8_req_buf);
				buffer->retrieve(static_cast<size_t>(sp_conn_info->i32_want_size));
				sp_conn_info->enum_state = kExpectFrameSize;
			} else{ 
				more = false;
			}
	}
}
