/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.;
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

#include <iostream>
#include <boost/lexical_cast.hpp>
#include "sg_agent_config_client.h"
#include "SGAgentErr.h"
#include "mcc_comm.h"
#include "log4cplus.h"

sg_agent_config_client* sg_agent_config_client::m_config_client = NULL;
int sg_agent_config_client::m_ip_idx = -1;
bool sg_agent_config_client::m_useLocalAgent = true; // 默认为线上

pthread_rwlock_t  sg_agent_config_client::rwlock = PTHREAD_RWLOCK_INITIALIZER;

int sg_agent_config_client::init()
{
    int ret = 0;

    srand(time(0));
    std::string ip;
    ret = _getHost(ip);
    LOG_INFO("get ip = " << ip);
    if (0 != ret)
    {
        return ret;
    }

    _init_thrift(ip);
    return 0;
}

int sg_agent_config_client::_init_thrift(std::string ip)
{
    try {
        m_socket = boost::shared_ptr<TSocket>(new TSocket(ip, PORT));
        m_transport = boost::shared_ptr<TFramedTransport>(new TFramedTransport(m_socket));
        m_protocal = boost::shared_ptr<TBinaryProtocol>(new TBinaryProtocol(m_transport));
        m_client = boost::shared_ptr<SGAgentClient>(new SGAgentClient(m_protocal));

        //设置超时400ms
        m_socket->setConnTimeout(400);
        m_socket->setSendTimeout(400);
        m_socket->setRecvTimeout(400);

        m_transport -> open();
    } catch (...) {
        return ERR_FAILEDOPENCONNECTION;
    }
    return 0;
}

int sg_agent_config_client::destroy()
{
    m_config_client = NULL;
    return _closeConnection();
}

int sg_agent_config_client::_openConnection()
{
    try {
        if (m_transport -> isOpen())
        {
            return 0;
        }

        m_transport -> close();

        if (!m_useLocalAgent)
        {
            std::string ip = "";
            m_ip_idx = (m_ip_idx + 1) % SGAGENT_NUM;
            _get_offline_ip(ip);
            _init_thrift(ip);
        }
        m_transport -> open();

        if (!m_transport -> isOpen())
        {
            return ERR_FAILEDOPENCONNECTION;
        }
    } catch (...) {
        return ERR_FAILEDOPENCONNECTION;
    }

    return 0;
}

int sg_agent_config_client::_closeConnection()
{
    try {
        m_transport -> close();
    } catch (...) {
        return ERR_FAILEDCLOSECONNECTION;
    }

    if (m_transport -> isOpen())
    {
        return ERR_FAILEDCLOSECONNECTION;
    }
    return 0;
}

int sg_agent_config_client::_checkConnection()
{
    return _openConnection();
}

int sg_agent_config_client::setConfig(const proc_conf_param_t& conf)
{
    pthread_rwlock_wrlock(&rwlock);
    int ret = 0;

    int retry = 0;
    do {
        try {
            ret = _checkConnection();
            if (0 != ret)
            {
                continue;
            }

            ret = m_client -> setConfig(conf);
        } catch (...) {
            LOG_ERROR("Failed to set Config");
            ret = ERR_FAILEDOPENCONNECTION;
        }
    } while (CONFIG_RETRYTIMES > ++retry && 0 != ret);

    pthread_rwlock_unlock(&rwlock);
    return ret;
}

int sg_agent_config_client::getConfig(std::string& _return,
            const proc_conf_param_t& node)
{
    pthread_rwlock_wrlock(&rwlock);
    int ret = 0;
    int retry = 0;
    do {
        try {
            ret = _checkConnection();
            if (0 != ret)
            {
                continue;
            }

            m_client -> getConfig(_return, node);
        } catch (...) {
            LOG_ERROR("Failed to get Config");
            ret = ERR_FAILEDOPENCONNECTION;
        }
    } while (CONFIG_RETRYTIMES > ++retry && 0 != ret);

    pthread_rwlock_unlock(&rwlock);
    return 0;
}

int sg_agent_config_client::updateConfig(ConfigUpdateRequest& request)
{
    int ret = 0;
    ret = _checkConnection();
    if (0 != ret)
    {
        return ret;
    }

    return m_client -> updateConfig(request);
}

int sg_agent_config_client::getFileConfig(file_param_t& _return,
            const file_param_t& file)
{
    if (NULL == m_client) {
        LOG_ERROR("m_client is NULL");
        return -1;
    }

    pthread_rwlock_wrlock(&rwlock);
    int ret = 0;
    int retry = 0;
    do {
        try {
            ret = _checkConnection();
            if (0 != ret)
            {
                continue;
            }

            m_client -> getFileConfig(_return, file);
        } catch (...) {
            LOG_ERROR("Failed to get fileconfig");
            ret = ERR_FAILEDOPENCONNECTION;
        }
    } while (CONFIG_RETRYTIMES > ++retry && 0 != ret);

    pthread_rwlock_unlock(&rwlock);
    return ret;
}

bool sg_agent_config_client::is_online()
{
    return m_useLocalAgent;
}

int sg_agent_config_client::_getHost(std::string& localIp)
{
    m_useLocalAgent = _useLocalAgent();


    if (m_useLocalAgent) {
        localIp = "127.0.0.1";
        m_useLocalAgent = true;
        LOG_DEBUG("m_useLocalAgent = true");
    }
    else
    {
        _get_offline_ip(localIp);
        m_useLocalAgent = false;
        LOG_DEBUG("m_useLocalAgent = false");
    }
    return 0;
}

bool sg_agent_config_client::_useLocalAgent() {
    char ip[INET_ADDRSTRLEN];
    //getNewCloudIp(ip);
    getIntranet(ip);
    char host[256];
    getHostInfo(host, ip);

    if(strstr(host, ".office.mos") > 0
            || strstr(host, ".corp.sankuai.com") > 0
            || strstr(host, "not found") > 0 ) {
        if(NULL != strstr(ip, "10.")
                && 0 == strcmp(ip, strstr(ip, "10.")) ) {
            return true;
        }
        return false;
    }
    else if(strstr(host, ".sankuai.com") > 0) {
        return true;
    }
    return true;
}

sg_agent_config_client* sg_agent_config_client::getInstance()
{
    pthread_rwlock_wrlock(&rwlock);
    if (NULL == m_config_client)
    {
        m_config_client = new sg_agent_config_client();
        m_config_client -> init();
    }
    pthread_rwlock_unlock(&rwlock);
    return m_config_client;
}

int sg_agent_config_client::_get_offline_ip(std::string& localIP)
{
    if (-1 == m_ip_idx)
    {
        if (4 == vec_domain.size())
        {
            m_ip_idx = (boost::lexical_cast<int>(vec_domain[3]) + 1) % SGAGENT_NUM;
        }
        else {
            m_ip_idx = 0;
        }
    }
    localIP = SGAGENT_LIST[m_ip_idx];
}
