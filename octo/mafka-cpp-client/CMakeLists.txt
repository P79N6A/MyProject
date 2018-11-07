cmake_minimum_required(VERSION 2.6)

INCLUDE_DIRECTORIES(SYSTEM src/)
INCLUDE_DIRECTORIES(SYSTEM /usr/include/)
INCLUDE_DIRECTORIES(SYSTEM /usr/include/thrift)
INCLUDE_DIRECTORIES(SYSTEM src/gen-cpp/)
INCLUDE_DIRECTORIES(SYSTEM dependencies/librdkafka/src/)
INCLUDE_DIRECTORIES(SYSTEM ${RAPIDJSON_INCLUDE_DIR})


SET(kafka_src 
   dependencies/librdkafka/src/rdkafka.c
   dependencies/librdkafka/src/rdkafka_broker.c
   dependencies/librdkafka/src/rdkafka_msg.c
   dependencies/librdkafka/src/rdkafka_topic.c
   dependencies/librdkafka/src/rdkafka_conf.c
   dependencies/librdkafka/src/rdkafka_timer.c
   dependencies/librdkafka/src/rdkafka_offset.c
   dependencies/librdkafka/src/rdkafka_transport.c
   dependencies/librdkafka/src/rdkafka_buf.c
   dependencies/librdkafka/src/rdkafka_queue.c
   dependencies/librdkafka/src/rdkafka_op.c
   dependencies/librdkafka/src/rdkafka_request.c
   dependencies/librdkafka/src/rdkafka_cgrp.c
   dependencies/librdkafka/src/rdkafka_pattern.c
   dependencies/librdkafka/src/rdkafka_partition.c
   dependencies/librdkafka/src/rdkafka_subscription.c
   dependencies/librdkafka/src/rdkafka_assignor.c
   dependencies/librdkafka/src/rdkafka_range_assignor.c
   dependencies/librdkafka/src/rdkafka_roundrobin_assignor.c
   dependencies/librdkafka/src/rdkafka_feature.c
   dependencies/librdkafka/src/rdcrc32.c
   dependencies/librdkafka/src/rdaddr.c
   dependencies/librdkafka/src/rdrand.c
   dependencies/librdkafka/src/rdlist.c
   dependencies/librdkafka/src/tinycthread.c
   dependencies/librdkafka/src/rdlog.c
   dependencies/librdkafka/src/rdstring.c
   dependencies/librdkafka/src/rdkafka_event.c
   dependencies/librdkafka/src/rdkafka_metadata.c
   dependencies/librdkafka/src/rdregex.c
   dependencies/librdkafka/src/snappy.c
   dependencies/librdkafka/src/rdgz.c
	
	)

file(GLOB_RECURSE sources   src/castle/gen-cpp/*.cpp  src/*.cpp  ${kafka_src})



ADD_LIBRARY(mafka_client STATIC ${sources})
ADD_LIBRARY(mafka_client_dynamic SHARED ${sources})

SET_TARGET_PROPERTIES(mafka_client PROPERTIES OUTPUT_NAME "mafka_client")

