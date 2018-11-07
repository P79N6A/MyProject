#ifndef __MAFKA_CLIENT_HANDLER_H__
#define __MAFKA_CLIENT_HANDLER_H__

#include "util/SGAgentErr.h"
#include "Producer.h"
#include "util/global_def.h"

namespace sg_agent
{
class MafkaClient
{
 public:
  static MafkaClient* getInstance();

  int SendAsync(const char* content, int length);

 private:

  MafkaClient();
  ~MafkaClient();
  static MafkaClient* mafkaClient_;
  mafka::ProducerPtr producer_;
};
} // namespace

#endif

