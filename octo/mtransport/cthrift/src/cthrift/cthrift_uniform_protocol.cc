//
// Created by huixiangbo on 17/7/18.
//
#include "cthrift_uniform_protocol.h"
#include "cthrift_common.h"

using namespace muduo;
using namespace muduo::net;
using namespace apache::thrift::transport;
using apache::thrift::transport::TMemoryBuffer;
using apache::thrift::protocol::TBinaryProtocol;
using namespace cthrift;

const int32_t int32_unihead_total_len_index = 4;
const int32_t int32_unihead_head_content_index = 10;
const int32_t int32_unihead_head_len_index = 8;

// class CthriftUniformRequest
CthriftUniformRequest::CthriftUniformRequest(const int32_t i32_len, const uint8_t *p_u8_buf)
   : service_type_(CTHRIFT_UNDEFINED_PROTOCOL),
     i32_req_len_(i32_len),
     p_u8_req_buf_(p_u8_buf),
     i32_body_length_(-1),
     p_u8_body_(NULL),
     p_header_(NULL){

    service_type_ = CthriftUniformRequest::GetProtocolType(p_u8_req_buf_);
}

CthriftUniformRequest::CthriftUniformRequest()
        : service_type_(CTHRIFT_UNDEFINED_PROTOCOL),
          i32_req_len_(-1),
          p_u8_req_buf_(NULL),
          i32_body_length_(-1),
          p_u8_body_(NULL),
          p_header_(NULL){

    //包头
    p_head_buf_[0] = 0xAB;
    p_head_buf_[1] = 0xBA;
    //版本默认1
    p_head_buf_[2] = (uint8_t)1;
    //协议
    p_head_buf_[3] = (uint8_t)1;

    sp_output_tmemorybuffer_ = boost::shared_ptr <TMemoryBuffer>(new TMemoryBuffer());
}

CthriftUniformRequest::~CthriftUniformRequest(){
    if(p_header_) {
        delete p_header_;
    }
}

bool CthriftUniformRequest::PackRequest(muduo::net::Buffer& buf,
                                        Header& head){


    int32_t  i32_size =  buf.readableBytes();
    uint8_t* p_u8_buf =  (uint8_t*)buf.peek();

    CLOG_STR_DEBUG("origin buf  size: " << i32_size);




    boost::shared_ptr<TMemoryBuffer> buffer(new TMemoryBuffer());
    boost::shared_ptr<TBinaryProtocol> binaryProtcol(new TBinaryProtocol(buffer));

    head.write((binaryProtcol.get()));

    uint8_t *p_buf = 0;
    uint32_t u32_len = 0;
    uint16_t u16_len = 0;
    buffer->getBuffer(&p_buf, &u32_len);
    u16_len = u32_len;



    //need check some
    //total length = header length + 2 + body length
    uint32_t u32_total_net_len = htobe32(u32_len + 2 + i32_size - 4);
    //header length in network byte
    uint32_t u16_net_len = htobe16(u16_len);

    memcpy(&(p_head_buf_[4]), &u32_total_net_len, sizeof(uint32_t));
    memcpy(&(p_head_buf_[8]), &u16_net_len, sizeof(uint16_t));


    sp_output_tmemorybuffer_->resetBuffer();
    sp_output_tmemorybuffer_->write(p_head_buf_, 10);
    sp_output_tmemorybuffer_->write(p_buf, u16_len);
    sp_output_tmemorybuffer_->write(p_u8_buf + 4, i32_size - 4);

    CLOG_STR_DEBUG("head buf  size: " << u16_len);


    uint8_t *p_out_buf_;
    uint32_t p_out_size;

    sp_output_tmemorybuffer_->getBuffer(&p_out_buf_, &p_out_size);
    buf.retrieve(static_cast<size_t>(i32_size));
    buf.append(p_out_buf_ , p_out_size );

    int32_t  i32_pack_size =  buf.readableBytes();
    uint8_t* p_u8_pack_buf =  (uint8_t*)buf.peek();
    CLOG_STR_DEBUG("pack buf  size: " << i32_pack_size);

}

bool CthriftUniformRequest::UnPackRequest(){

    if (!check()){
        return false;
    }

    boost::shared_ptr<TMemoryBuffer> buffer(new TMemoryBuffer());
    boost::shared_ptr<TBinaryProtocol> binaryProtcol(new TBinaryProtocol(buffer));


    if(p_header_){
        delete p_header_;
    }

    p_header_ = new Header();


    buffer->resetBuffer((uint8_t *)(p_u8_req_buf_ + int32_unihead_head_content_index), getHeadLength());
    p_header_->read(binaryProtcol.get());

    getBodyLength();
    getBody();
    return true;
}

int32_t CthriftUniformRequest::readInt32(const uint8_t *p_u8_buf) {
    int32_t be32 = 0;
    memcpy(&be32,  p_u8_buf, sizeof be32);
    return be32toh(be32);
}

int16_t CthriftUniformRequest::readInt16(const uint8_t *p_u8_buf) {
    int16_t be16 = 0;
    memcpy(&be16,  p_u8_buf, sizeof be16);
    return be16toh(be16);
}

int32_t CthriftUniformRequest::getTotalLength() {
    int32_t i32_totoal_length = readInt32(p_u8_req_buf_ + int32_unihead_total_len_index);
    return i32_totoal_length;
}

int32_t CthriftUniformRequest::getHeadLength() {
    int16_t i16_head_length = readInt16(p_u8_req_buf_ + int32_unihead_head_len_index);
    return i16_head_length;
}

bool CthriftUniformRequest::check(){
    return (getTotalLength() == i32_req_len_ - 8)&&(service_type_ == CTHRIFT_UNIFORM_PROTOCOL);
}

void CthriftUniformRequest::getBodyLength() {
    i32_body_length_ = getTotalLength() - 2 - getHeadLength();
}

void CthriftUniformRequest::getBody() {
    p_u8_body_ =  (uint8_t *)p_u8_req_buf_ + 10 + getHeadLength();
}

const int32_t  CthriftUniformRequest::GetTotallength(const uint8_t *p_u8_req_buf)
{
    int32_t i32_totoal_length = readInt32(p_u8_req_buf + int32_unihead_total_len_index);
    return i32_totoal_length + int32_unihead_head_len_index ;
}

const cthrift_protocol_type CthriftUniformRequest::GetProtocolType(const uint8_t *p_u8_req_buf){
    //protocol_type_;
    if(0x57 == p_u8_req_buf[0] && 0x58 == p_u8_req_buf[1]){
        return CTHRIFT_HESSIAN_PROTOCOL;
    }else if(0xAB == p_u8_req_buf[0] && 0xBA == p_u8_req_buf[1]){
        return CTHRIFT_UNIFORM_PROTOCOL;
    }else {
        return CTHRIFT_THRIFT_PROTOCOL;
        //handle mtthrift/original thrift, if length header with 0xAB, 0xBA, length >= 2881093632, with 0x57, 0x58, length >= 1465384960
    }
}

// class CthriftUniformResponse
CthriftUniformResponse::CthriftUniformResponse(const int64_t sequenceId, const int8_t status,const std::string message)
: sequenceId_(sequenceId),
  status_(status),
  message_(message),
  p_out_buf_(0),
  p_out_size(-1),
  sp_output_tmemorybuffer_(boost::shared_ptr <TMemoryBuffer>(new TMemoryBuffer())){

    //包头
    p_head_buf_[0] = 0xAB;
    p_head_buf_[1] = 0xBA;
    //版本默认1
    p_head_buf_[2] = (uint8_t)1;
    //协议
    p_head_buf_[3] = (uint8_t)1;
}


CthriftUniformResponse::~CthriftUniformResponse(){

}

void CthriftUniformResponse::PackResponse(const int32_t i32_size, const uint8_t *p_u8_buf){
    Header head;
    ResponseInfo responseInfo;
    responseInfo.__set_sequenceId(sequenceId_);
    responseInfo.__set_status(status_);
    responseInfo.__set_message(message_);

    head.__set_messageType(MessageType::Normal);
    head.__set_responseInfo(responseInfo);



    boost::shared_ptr<TMemoryBuffer> buffer(new TMemoryBuffer());
    boost::shared_ptr<TBinaryProtocol> binaryProtcol(new TBinaryProtocol(buffer));

    head.write((binaryProtcol.get()));

    uint8_t *p_buf = 0;
    uint32_t u32_len = 0;
    uint16_t u16_len = 0;
    buffer->getBuffer(&p_buf, &u32_len);
    u16_len = u32_len;

    //need check some
    //total length = header length + 2 + body length
    uint32_t u32_total_net_len = htobe32(u32_len + 2 + i32_size);
    //header length in network byte
    uint32_t u16_net_len = htobe16(u16_len);

    memcpy(&(p_head_buf_[4]), &u32_total_net_len, sizeof(uint32_t));
    memcpy(&(p_head_buf_[8]), &u16_net_len, sizeof(uint16_t));

    sp_output_tmemorybuffer_->resetBuffer();
    sp_output_tmemorybuffer_->write(p_head_buf_, 10);
    sp_output_tmemorybuffer_->write(p_buf, u16_len);
    sp_output_tmemorybuffer_->write(p_u8_buf, i32_size);

    sp_output_tmemorybuffer_->getBuffer(&p_out_buf_, &p_out_size);
}

//package scanner response
void CthriftUniformResponse::PackScanner(const HeartbeatInfo& heart_beat, int8_t msg_type){
    Header head;
    ResponseInfo responseInfo;
    responseInfo.__set_sequenceId(sequenceId_);
    responseInfo.__set_status(status_);
    responseInfo.__set_message(message_);

    head.__set_messageType(msg_type);
    head.__set_responseInfo(responseInfo);
    head.__set_heartbeatInfo(heart_beat);

    boost::shared_ptr<TMemoryBuffer> buffer(new TMemoryBuffer());
    boost::shared_ptr<TBinaryProtocol> binaryProtcol(new TBinaryProtocol(buffer));

    head.write((binaryProtcol.get()));

    //response send buf
    uint8_t *p_buf = 0;
    //header length
    uint16_t u16_len = 0;
    uint32_t u32_len = 0;
    buffer->getBuffer(&p_buf, &u32_len);
    u16_len = u32_len;

    //header length in network byte
    uint32_t u16_net_len = htobe16(u16_len);
    //total length = 2 + header length + body length(0)
    uint32_t u32_total_net_len = htobe32(u32_len + 2 + 0);

    memcpy(&(p_head_buf_[4]), &u32_total_net_len, sizeof(uint32_t));
    memcpy(&(p_head_buf_[8]), &u16_net_len, sizeof(uint16_t));

    sp_output_tmemorybuffer_->resetBuffer();
    sp_output_tmemorybuffer_->write(p_head_buf_, 10);
    sp_output_tmemorybuffer_->write(p_buf, u16_len);

    sp_output_tmemorybuffer_->getBuffer(&p_out_buf_, &p_out_size);
}


void CthriftUniformResponse::PackAuthFailed(const TraceInfo& traceInfo,  const int32_t i32_size,  const uint8_t *p_u8_buf){
    Header head;
    ResponseInfo responseInfo;
    responseInfo.__set_sequenceId(sequenceId_);
    responseInfo.__set_status(status_);
    responseInfo.__set_message(message_);

    head.__set_messageType(MessageType::Normal);
    head.__set_responseInfo(responseInfo);
    head.__set_traceInfo(traceInfo);

    boost::shared_ptr<TMemoryBuffer> buffer(new TMemoryBuffer());
    boost::shared_ptr<TBinaryProtocol> binaryProtcol(new TBinaryProtocol(buffer));

    head.write((binaryProtcol.get()));
    
    uint8_t *p_buf = 0;
    uint32_t u32_len = 0;
    uint16_t u16_len = 0;
    buffer->getBuffer(&p_buf, &u32_len);
    u16_len = u32_len;

    //need check some
    //total length = header length + 2 + body length
    uint32_t u32_total_net_len = htobe32(u32_len + 2 + i32_size);
    //header length in network byte
    uint32_t u16_net_len = htobe16(u16_len);

    memcpy(&(p_head_buf_[4]), &u32_total_net_len, sizeof(uint32_t));
    memcpy(&(p_head_buf_[8]), &u16_net_len, sizeof(uint16_t));

    sp_output_tmemorybuffer_->resetBuffer();
    sp_output_tmemorybuffer_->write(p_head_buf_, 10);
    sp_output_tmemorybuffer_->write(p_buf, u16_len);
    sp_output_tmemorybuffer_->write(p_u8_buf, i32_size);

    sp_output_tmemorybuffer_->getBuffer(&p_out_buf_, &p_out_size);
}