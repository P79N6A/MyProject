package com.meituan.service.mobile.mtthrift.server.filter;

import com.dianping.rhino.Rhino;
import com.dianping.rhino.onelimiter.LimitResult;
import com.dianping.rhino.onelimiter.OneLimiter;
import com.meituan.dorado.common.RpcRole;
import com.meituan.dorado.rpc.handler.filter.Filter;
import com.meituan.dorado.rpc.handler.filter.FilterHandler;
import com.meituan.dorado.rpc.meta.RpcInvocation;
import com.meituan.dorado.rpc.meta.RpcResult;
import com.meituan.service.mobile.mtthrift.config.ThriftServerGlobalConfig;
import com.meituan.service.mobile.mtthrift.degrage.ServiceDegradeException;
import com.meituan.service.mobile.mtthrift.util.ClientInfoUtil;
import com.meituan.service.mobile.mtthrift.util.Consts;
import com.meituan.service.mobile.mtthrift.util.MtConfigUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ServerRhinoLimiterFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerRhinoLimiterFilter.class);

    private static OneLimiter limiter;

    static {
        try {
            if (StringUtils.isBlank(MtConfigUtil.getAppName())) {
                LOGGER.error("app.name 为空, rhino限流不会生效，如果没有使用rhino限流功能则可以忽略。"
                        + "app.name的配置参考：http://docs.sankuai.com/doc/arch_dp/cat/integration/java/#22");
            } else {
                limiter = Rhino.newOneLimiter();
            }
        } catch (Throwable e) {
            LOGGER.error("rhino限流初始化失败, rhino限流不会生效，如果没有使用rhino限流功能则可以忽略.", e);
        }
    }


    @Override
    public RpcResult filter(RpcInvocation invocation, FilterHandler nextHandler) throws Throwable {

        if (ThriftServerGlobalConfig.isEnableLimit() && limiter != null) {
            String entrance = Consts.mtraceInfra + "-" + invocation.getServiceInterface().getName() + "." + invocation.getMethod().getName();
            Map<String, String> params = new HashMap<String, String>();
            params.put(LimitKeyType.IP.getName(), ClientInfoUtil.getClientIp());
            params.put(LimitKeyType.APPKEY.getName(), ClientInfoUtil.getClientAppKey());

            LimitResult limitResult = limiter.run(entrance, params);
            if (limitResult.isReject()) {
                // 1.抛异常
                StringBuilder str = new StringBuilder("[Rhino-Server] Request is Degraded，")
                        .append(ClientInfoUtil.getClientIp())
                        .append("|").append(ClientInfoUtil.getClientAppKey())
                        .append("|").append(entrance)
                        .append(",strategy: ").append(limitResult.getStrategyEnum())
                        .append(",code: ").append(limitResult.getCode())
                        .append(",msg: ").append(limitResult.getMsg());
                throw new ServiceDegradeException(str.toString());
                // 2.返回特定值
                // 3.执行配置的限流策略
            }
        }
        RpcResult result = nextHandler.handle(invocation);
        return result;
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public RpcRole getRole() {
        return RpcRole.PROVIDER;
    }

    enum LimitKeyType {
        APPKEY("appkey"),
        IP("ip");

        public String getName() {
            return name;
        }

        String name;

        LimitKeyType(String name) {
            this.name = name;
        }
    }
}
