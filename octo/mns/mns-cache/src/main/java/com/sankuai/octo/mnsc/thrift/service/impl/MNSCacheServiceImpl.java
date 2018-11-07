package com.sankuai.octo.mnsc.thrift.service.impl;

import com.sankuai.octo.appkey.model.AppkeyDesc;
import com.sankuai.octo.appkey.model.AppkeyDescResponse;
import com.sankuai.octo.mnsc.idl.thrift.model.*;
import com.sankuai.octo.mnsc.idl.thrift.service.MNSCacheService;
import com.sankuai.octo.mnsc.model.Env;
import com.sankuai.octo.mnsc.service.mnscService;
import com.sankuai.octo.sandbox.thrift.model.SandboxConfig;
import com.sankuai.octo.sandbox.thrift.model.SandboxResponse;
import com.sankuai.sgagent.thrift.model.SGService;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.springframework.util.Assert;

import java.util.List;

public class MNSCacheServiceImpl implements MNSCacheService.Iface {
    @Override
    public MNSResponse getMNSCache(String appkey, String version, String env) throws TException {
        if (!Env.isValid(env) || org.apache.commons.lang3.StringUtils.isEmpty(appkey)) {
            MNSResponse ret = new MNSResponse();
            ret.setCode(Constants.ILLEGAL_ARGUMENT);
            return ret;
        }
        return mnscService.getMnsc(appkey, version, env);
    }

    @Override
    public MNSResponse getMNSCache4HLB(String appkey, String version, String env) throws TException {
        if (!Env.isValid(env) || org.apache.commons.lang3.StringUtils.isEmpty(appkey)) {
            MNSResponse ret = new MNSResponse();
            ret.setCode(Constants.ILLEGAL_ARGUMENT);
            return ret;
        }
        return mnscService.getMNSCache4HLB(appkey, version, env);
    }

    @Override
    public MNSBatchResponse getMNSCacheByAppkeys(List<String> appkeys, String protocol) throws TException {
        if (null == appkeys || StringUtils.isEmpty(protocol)) {
            return new MNSBatchResponse().setCode(Constants.ILLEGAL_ARGUMENT);
        }
        return mnscService.getMNSCacheByAppkeys(appkeys, protocol);
    }

    @Override
    public AppKeyListResponse getAppKeyListByBusinessLine(int bizCode, String env) throws TException {
        if (!Env.isValid(env)) {
            AppKeyListResponse errRet = new AppKeyListResponse();
            errRet.setCode(Constants.ILLEGAL_ARGUMENT);
            return errRet;
        }
        return mnscService.getAppKeyListByBusinessLine(bizCode, env, false);
    }

    @Override
    public AppKeyListResponse getCellAppKeysByBusinessLine(int bizCode, String env) throws TException {
        if (!Env.isValid(env)) {
            AppKeyListResponse errRet = new AppKeyListResponse();
            errRet.setCode(Constants.ILLEGAL_ARGUMENT);
            return errRet;
        }
        return mnscService.getAppKeyListByBusinessLine(bizCode, env, true);
    }

    @Override
    public SandboxResponse getSandbox(String appkey, String env) throws TException {
        return null;
    }

    @Override
    public HttpPropertiesResponse getHttpPropertiesByBusinessLine(int bizCode, String env) throws org.apache.thrift.TException {
        if (!Env.isValid(env)) {
            HttpPropertiesResponse errRet = new HttpPropertiesResponse();
            errRet.setCode(Constants.ILLEGAL_ARGUMENT);
            return errRet;
        }
        return mnscService.getHttpPropertiesByBusinessLine(bizCode, env);
    }

    @Override
    public HttpPropertiesResponse getHttpPropertiesByAppkey(String appkey, String env) throws org.apache.thrift.TException {
        Assert.hasText(appkey, "appkey不能为空");
        Assert.hasText(env, "env不能为空");
        return mnscService.getHttpPropertiesByAppkey(appkey, env);
    }

    @Override
    public HttpGroupResponse getGroupsByAppkey(String appkey, String env) {
        Assert.hasText(appkey, "appkey不能为空");
        Assert.hasText(env, "env不能为空");
        return mnscService.getHlbGroupByAppkey(appkey, env);
    }

    @Override
    public AllHttpGroupsResponse getAllGroups(String env) {
        Assert.hasText(env, "env不能为空");
        return mnscService.getAllGroups(env);
    }

    @Override
    public boolean saveSandbox(String s, String s1, String s2) throws TException {
        return false;
    }

    @Override
    public boolean deleteSandbox(String s, String s1) throws TException {
        return false;
    }

    @Override
    public List<SandboxConfig> getSandboxConfig(List<String> list, String s) throws TException {
        return null;
    }

    @Override
    public MNSResponse getProvidersByIP(String ip) throws TException {
        Assert.hasText(ip, "ip不能为空");
        return mnscService.getProvidersByIP(ip);
    }

    @Override
    public boolean delProvider(String appkey, int env, int serverType, String ip, int port) throws TException {
        Assert.hasText(appkey, "ip不能为空");
        Assert.state((env > 0) && (env < 4), "env非法");
        Assert.state(0 == serverType || 1 == serverType, "serverType非法");
        Assert.hasText(ip, "ip不能为空");
        return mnscService.delProvider(appkey, env, serverType, ip, port);
    }

    @Override
    public MNSResponse getMNSCacheWithVersionCheck(MnsRequest mnsRequest) throws TException {
        if (null == mnsRequest || StringUtils.isEmpty(mnsRequest.getAppkey()) || !Env.isValid(mnsRequest.getEnv())) {
            MNSResponse invalidResponse = new MNSResponse();
            invalidResponse.setCode(Constants.ILLEGAL_ARGUMENT);
            return invalidResponse;
        }
        return mnscService.getMnsc(mnsRequest);
    }

    @Override
    public AppkeyDescResponse getDescByAppkey(String appkey) throws TException {
        if (StringUtils.isEmpty(appkey)) {
            AppkeyDescResponse ret = new AppkeyDescResponse();
            ret.setErrCode(400);
            ret.setMsg("appkey invalid");
            return ret;
        }
        return mnscService.getDescInfo(appkey);
    }

    @Override
    public AppKeyListResponse getAppkeyListByIP(String ip) throws TException {
        AppKeyListResponse ret = new AppKeyListResponse();
        if (StringUtils.isEmpty(ip)) {
            ret.setCode(400);
        } else {
            List<String> appkeys = mnscService.getAppkeyListByIP(ip.trim());
            ret.setCode(200).setAppKeyList(appkeys);
        }
        return ret;
    }

    @Override
    public RegisterResponse registerService(SGService registerRequest) throws TException {
        if (null == registerRequest || !Env.isValid(registerRequest.getEnvir())
            || StringUtils.isEmpty(registerRequest.getAppkey())
            || StringUtils.isEmpty(registerRequest.getProtocol())
            || StringUtils.isEmpty(registerRequest.getVersion())) {
            RegisterResponse ret = new RegisterResponse();
            ret.setCode(400);
            return ret;
        }
        return mnscService.registerCheck(registerRequest);
    }
}
