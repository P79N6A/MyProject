#ifndef IPC_MESSAGE_H_ 
#define IPC_MESSAGE_H_

#include <stdint.h>
#include <unistd.h>

namespace cplugin {

class Message {
  enum Type {
    STRING,
    INT,
    DOUBLE,
    STRUCT
  };

  struct Header {
    uint32_t size;
    uint32_t type;
    char* payload;
  };

 public:
  Message();
  ~Message();

  Header GetHeader() const;
  void SetPayload(const char* buf, uint32_t n);
  bool Parse(const char* buf, uint32_t len);
  const char* GetPayload() const;
  uint32_t GetPayloadSize() const;
  const char* GetMessage() const;
  uint32_t GetMessageSize() const;
 private:
  Header header_;
  char* buffer_;
  uint32_t buffer_len_;
};

} // namespace cplugin

#endif // IPC_MESSAGE_H_
