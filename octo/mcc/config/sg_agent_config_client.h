/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

#ifndef __SG_AGNET_CONFIG_CLIENT_H__
#define __SG_AGNET_CONFIG_CLIENT_H__

#include "thrift/protocol/TBinaryProtocol.h"
#include "thrift/transport/TSocket.h"
#include "thrift/transport/TTransportUtils.h"
#include <string>

#include "SGAgent.h"
#include "sgagent_worker_service_types.h"
#include "sgagent_common_types.h"
#include "msgparam.h"

using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;

const int SOCTET_TIMEOUT = 500; // ms
const std::string ONLINE = "10";
const std::string ONLINE2 = "169";
const int CONFIG_RETRYTIMES = 3;

const int SGAGENT_NUM = 4;
const std::string SGAGENT_LIST[SGAGENT_NUM]
    = {"10.4.241.165", "10.4.241.166", "10.4.241.125",
        "10.4.246.240"};

const int PORT = 5266;

class sg_agent_config_client
{
    public:

        /**
         * 初始化连接参数
         */
        int init();

        /**
         * 销毁连接， 与init对应
         * 如果调用方想切换连接， 可以用到此方法
         */
        int destroy();

        int setConfig(const proc_conf_param_t& conf);

        int getConfig(std::string& _return, const proc_conf_param_t& node);

        int updateConfig(ConfigUpdateRequest& request);


        int getFileConfig(file_param_t& _return, const file_param_t& file);

        bool is_online();

        static sg_agent_config_client* getInstance();

    private:
        boost::shared_ptr<TSocket> m_socket;
        boost::shared_ptr<TTransport> m_transport;
        boost::shared_ptr<TProtocol> m_protocal;

        boost::shared_ptr<SGAgentClient> m_client;

        std::vector<std::string> vec_domain;

        static sg_agent_config_client* m_config_client;

        static int m_ip_idx;

        // 是否处于线上状态, 新云主机和ONLINE状态保持一致
        //static bool m_online;
        static bool m_useLocalAgent;

        static pthread_rwlock_t rwlock;

        /**
         * 初始化thrift
         */
        int _init_thrift(std::string ip);
        /**
         * 打开连接
         */
        int _openConnection();

        /**
         * 关闭连接
         */
        int _closeConnection();

        /**
         * 检查连接是否正常
         * 已连接直接返回0； 否则重连， 成功返回0； 失败返回errorCode
         */
        int _checkConnection();

        int _getHost(std::string&);

        bool _useLocalAgent();

        /**
         * 获取线下IP
        */
        int _get_offline_ip(std::string&);
};

#endif
