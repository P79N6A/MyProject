//
// Created by huixiangbo on 17/7/18.
//

#ifndef CTHRIFT_UNIFORM_PROTOCOL_H
#define CTHRIFT_UNIFORM_PROTOCOL_H

#include "cthrift_common.h"


namespace apache {
    namespace thrift {
        namespace transport {
            using namespace cthrift;

            typedef enum cthrift_protocol_type{
                 CTHRIFT_HESSIAN_PROTOCOL,
                 CTHRIFT_UNIFORM_PROTOCOL,
                 CTHRIFT_THRIFT_PROTOCOL,
                 CTHRIFT_UNDEFINED_PROTOCOL
            }cthrift_protocol_type;

            enum uniform_service_type{
                UNIFORM_NORMAL_HEARTBEAT,
                UNIFORM_SCANNER_HEARTBEAT,
                UNIFORM_NORMAL,
                UNIFORM_UNDEFINED
            };

            class CthriftUniformRequest {
            private:
                const int32_t i32_req_len_;
                const uint8_t *p_u8_req_buf_;


                cthrift_protocol_type service_type_;
                int32_t i32_body_length_;
                uint8_t *p_u8_body_;

                Header *p_header_;
                uint8_t p_head_buf_[10];

                boost::shared_ptr <TMemoryBuffer>  sp_output_tmemorybuffer_;

            private:
            static int32_t readInt32(const uint8_t *p_u8_buf);
            static int16_t readInt16(const uint8_t *p_u8_buf);


            int32_t getTotalLength();
            int32_t getHeadLength();

            bool check();


             void getBodyLength();
             void getBody();

            public:
                CthriftUniformRequest(const int32_t i32_req_size,const uint8_t *p_u8_req_buf);
                CthriftUniformRequest();
                ~CthriftUniformRequest();


                bool UnPackRequest();
                bool PackRequest(muduo::net::Buffer& buf, Header& head);

                const int32_t GetBodyLength(){
                    return i32_body_length_;
                }

                const uint8_t* GetBody(){
                    return p_u8_body_;
                }

                const Header* GetHeader() {
                    return p_header_;
                }

                static const cthrift_protocol_type GetProtocolType(const uint8_t *p_u8_req_buf);
                static const int32_t    GetTotallength(const uint8_t *p_u8_req_buf);

                const cthrift_protocol_type GetProtocolType() {
                   return   service_type_;
                }

            };



            class CthriftUniformResponse {
            private:
                uint8_t p_head_buf_[10];
                boost::shared_ptr <TMemoryBuffer>  sp_output_tmemorybuffer_;


                uint8_t *p_out_buf_;
                uint32_t p_out_size;
            private:
                int64_t sequenceId_;
                int8_t status_;
                std::string message_;

            public:
                CthriftUniformResponse(const int64_t sequenceId, const int8_t status, const std::string message);
                ~CthriftUniformResponse();


                void PackResponse(const int32_t i32_size, const uint8_t *p_u8_buf);

                //Package ScannerHeartBeat reponse by hawk
                void PackScanner(const HeartbeatInfo&,  int8_t msg_type);

                void PackAuthFailed(const TraceInfo& traceInfo, const int32_t i32_size, const uint8_t *p_u8_buf);


                const uint8_t* GetResponseBuf(){
                    return p_out_buf_;
                }

                const int32_t GetResponseSize(){
                    return p_out_size;
                }

            };
        }
    }
}


#endif //CTHRIFT_UNIFORM_PROTOCOL_H
