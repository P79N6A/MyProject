#include "ipc_message.h"

#include <string.h>

namespace cplugin {
Message::Message() 
  : buffer_(NULL),
    buffer_len_(0) {
}

Message::~Message() {
  if (NULL != buffer_) {
    delete buffer_;
    buffer_ = NULL;
  }
}

Message::Header Message::GetHeader() const {
  return header_;
}

void Message::SetPayload(const char* buf, uint32_t n) {
  buffer_ = new char[n + 8];

  header_.size = n + 8;
  header_.type = 0;
  
  memmove(buffer_, reinterpret_cast<char*>(&(header_.size)), sizeof(header_.size));
  memmove(buffer_ + 4, reinterpret_cast<char*>(&(header_.type)), sizeof(header_.type));
  memmove(buffer_ + 8, buf, n);
  
  header_.payload = buffer_ + 8;
}

bool Message::Parse(const char* buf, uint32_t len) {
  int pos = 0;
  memmove(reinterpret_cast<char*>(&(header_.size)), buf, sizeof(header_.size));
  if (header_.size != len) {
    return false;
  }

  pos += sizeof(header_.size);
  memmove(reinterpret_cast<char*>(&(header_.type)), buf+pos, sizeof(header_.type));
  pos += sizeof(header_.type);

  buffer_ = new char[len];
  memmove(buffer_, buf, len);
  header_.payload = buffer_ + pos; 

  return true;
}

const char* Message::GetPayload() const {
  return header_.payload;
}

uint32_t Message::GetPayloadSize() const {
  return header_.size - 8;
}

const char* Message::GetMessage() const {
  return buffer_;  
}

uint32_t Message::GetMessageSize() const {
  return header_.size;
} 
} // namespace cplugin
