package com.sankuai.octo.msgp.model.flowCopy;

import com.sankuai.msgp.common.utils.helper.JsonHelper;

public class MccAPIResponse {
    private String status;
    private String msg;
    private MccNodeData data;
    private boolean success;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public MccNodeData getData() {
        return data;
    }

    public void setData(MccNodeData data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MccNode{");
        sb.append("status='").append(status).append('\'');
        sb.append(", msg='").append(msg).append('\'');
        sb.append(", data=").append(data);
        sb.append(", success=").append(success);
        sb.append('}');
        return sb.toString();
    }

    public static void main(String[] args) {
        String json = "{\n" + "    \"status\": \"success\", \n" + "    \"msg\": null, \n" + "    \"data\": {\n"
                + "        \"spaceName\": \"com.meituan.inf.rpc.benchmark\", \n"
                + "        \"nodeName\": \"com.meituan.inf.rpc.benchmark.prod\", \n" + "        \"data\": [\n"
                + "            {\n"
                + "                \"key\": \"com.meituan.inf.rpc.benchmark.circuitbreaker.mtthrift-rhino-key.errorThresholdCount\", \n"
                + "                \"value\": \"2\", \n" + "                \"comment\": null, \n"
                + "                \"oriValue\": null, \n" + "                \"oriComment\": null\n"
                + "            }, \n" + "            {\n"
                + "                \"key\": \"com.meituan.inf.rpc.benchmark.circuitbreaker.mtthrift-rhino-key.errorThresholdPercentage\", \n"
                + "                \"value\": \"1.0\", \n" + "                \"comment\": null, \n"
                + "                \"oriValue\": null, \n" + "                \"oriComment\": null\n"
                + "            }, \n" + "            {\n"
                + "                \"key\": \"com.meituan.inf.rpc.benchmark.circuitbreaker.mtthrift-rhino-key.isActive\", \n"
                + "                \"value\": \"false\", \n" + "                \"comment\": null, \n"
                + "                \"oriValue\": null, \n" + "                \"oriComment\": null\n"
                + "            }, \n" + "            {\n"
                + "                \"key\": \"com.meituan.inf.rpc.benchmark.circuitbreaker.mtthrift-rhino-key.isDegradeOnException\", \n"
                + "                \"value\": \"false\", \n" + "                \"comment\": null, \n"
                + "                \"oriValue\": null, \n" + "                \"oriComment\": null\n"
                + "            }, \n" + "            {\n"
                + "                \"key\": \"com.meituan.inf.rpc.benchmark.circuitbreaker.mtthrift-rhino-key.isForceOpen\", \n"
                + "                \"value\": \"false\", \n" + "                \"comment\": null, \n"
                + "                \"oriValue\": null, \n" + "                \"oriComment\": null\n"
                + "            }, \n" + "            {\n"
                + "                \"key\": \"com.meituan.inf.rpc.benchmark.circuitbreaker.mtthrift-rhino-key.recoverStrategy\", \n"
                + "                \"value\": \"2\", \n" + "                \"comment\": null, \n"
                + "                \"oriValue\": null, \n" + "                \"oriComment\": null\n"
                + "            }, \n" + "            {\n"
                + "                \"key\": \"com.meituan.inf.rpc.benchmark.circuitbreaker.mtthrift-rhino-key.recoverTimeInSeconds\", \n"
                + "                \"value\": \"10\", \n" + "                \"comment\": null, \n"
                + "                \"oriValue\": null, \n" + "                \"oriComment\": null\n"
                + "            }, \n" + "            {\n"
                + "                \"key\": \"com.meituan.inf.rpc.benchmark.circuitbreaker.mtthrift-rhino-key.requestVolumeThreshold\", \n"
                + "                \"value\": \"20\", \n" + "                \"comment\": null, \n"
                + "                \"oriValue\": null, \n" + "                \"oriComment\": null\n"
                + "            }, \n" + "            {\n"
                + "                \"key\": \"com.meituan.inf.rpc.benchmark.circuitbreaker.mtthrift-rhino-key.rollingStatsTime\", \n"
                + "                \"value\": \"10\", \n" + "                \"comment\": null, \n"
                + "                \"oriValue\": null, \n" + "                \"oriComment\": null\n"
                + "            }, \n" + "            {\n"
                + "                \"key\": \"com.meituan.inf.rpc.benchmark.circuitbreaker.mtthrift-rhino-key.sleepWindowInMilliseconds\", \n"
                + "                \"value\": \"5000\", \n" + "                \"comment\": null, \n"
                + "                \"oriValue\": null, \n" + "                \"oriComment\": null\n"
                + "            }, \n" + "            {\n"
                + "                \"key\": \"com.meituan.inf.rpc.benchmark.circuitbreaker.mtthrift-rhino-key.timeoutInMilliseconds\", \n"
                + "                \"value\": \"0\", \n" + "                \"comment\": null, \n"
                + "                \"oriValue\": null, \n" + "                \"oriComment\": null\n"
                + "            }, \n" + "            {\n"
                + "                \"key\": \"com.meituan.inf.rpc.benchmark.circuitbreaker.mtthrift-rhino-key.triggerStrategy\", \n"
                + "                \"value\": \"1\", \n" + "                \"comment\": null, \n"
                + "                \"oriValue\": null, \n" + "                \"oriComment\": null\n"
                + "            }, \n" + "            {\n"
                + "                \"key\": \"com.meituan.inf.rpc.benchmark.faultInject.octo.sankuai.octo.testMTthrift.Client.172.18.181.249.props\", \n"
                + "                \"value\": \"%7B%22type%22%3A2%2C%22sampleRate%22%3A100.0%2C%22maxDelay%22%3Anull%2C%22exceptionType%22%3A%22java.lang.IllegalArgumentException%22%2C%22startTime%22%3A1508840317964%2C%22endTime%22%3A1508840917964%2C%22dyeEnabled%22%3Afalse%2C%22randomDelay%22%3Anull%7D\", \n"
                + "                \"comment\": null, \n" + "                \"oriValue\": null, \n"
                + "                \"oriComment\": null\n" + "            }, \n" + "            {\n"
                + "                \"key\": \"maxConnection\", \n" + "                \"value\": \"11\", \n"
                + "                \"comment\": null, \n" + "                \"oriValue\": null, \n"
                + "                \"oriComment\": null\n" + "            }, \n" + "            {\n"
                + "                \"key\": \"msgCount\", \n" + "                \"value\": \"1000\", \n"
                + "                \"comment\": \"# 消息数量\", \n" + "                \"oriValue\": null, \n"
                + "                \"oriComment\": null\n" + "            }, \n" + "            {\n"
                + "                \"key\": \"msgLength\", \n" + "                \"value\": \"1000\", \n"
                + "                \"comment\": \"# 消息长度\", \n" + "                \"oriValue\": null, \n"
                + "                \"oriComment\": null\n" + "            }, \n" + "            {\n"
                + "                \"key\": \"msgType\", \n" + "                \"value\": \"string\", \n"
                + "                \"comment\": \"# 消息类型：pojo、string、byte\", \n"
                + "                \"oriValue\": null, \n" + "                \"oriComment\": null\n"
                + "            }, \n" + "            {\n" + "                \"key\": \"mtthrift-server-reportCat\", \n"
                + "                \"value\": \"false\", \n" + "                \"comment\": null, \n"
                + "                \"oriValue\": null, \n" + "                \"oriComment\": null\n"
                + "            }, \n" + "            {\n"
                + "                \"key\": \"octo.provider.flowcopy.brokerUrl\", \n"
                + "                \"value\": \"http://10.72.208.105:8080/api/record/\", \n"
                + "                \"comment\": null, \n" + "                \"oriValue\": null, \n"
                + "                \"oriComment\": null\n" + "            }, \n" + "            {\n"
                + "                \"key\": \"octo.provider.flowcopy.config\", \n"
                + "                \"value\": \"{\\\"taskId\\\":7460347472719568411,\\\"recordConfig\\\":{\\\"serviceName\\\":\\\"com.sankuai.octo.benchmark.thrift.EchoService\\\",\\\"methodNames\\\":[\\\"[sendString]\\\"],\\\"sumCount\\\":1000,\\\"serverIps\\\":[\\\"[172.18.185.152]\\\"],\\\"savePath\\\":\\\"testPath\\\",\\\"tagged\\\":true,\\\"description\\\":\\\"æµ\u008Bè¯\u0095\\\"}}\", \n"
                + "                \"comment\": \"# 【勿手动更改!】流量录制配置\", \n" + "                \"oriValue\": null, \n"
                + "                \"oriComment\": null\n" + "            }, \n" + "            {\n"
                + "                \"key\": \"octo.provider.flowcopy.enable\", \n"
                + "                \"value\": \"true\", \n" + "                \"comment\": \"# 【勿手动更改!】流量录制开关\", \n"
                + "                \"oriValue\": null, \n" + "                \"oriComment\": null\n"
                + "            }, \n" + "            {\n"
                + "                \"key\": \"octo.provider.flowcopy.ipport\", \n"
                + "                \"value\": \"10.72.208.105:8889\", \n" + "                \"comment\": null, \n"
                + "                \"oriValue\": null, \n" + "                \"oriComment\": null\n"
                + "            }, \n" + "            {\n" + "                \"key\": \"rpcType\", \n"
                + "                \"value\": \"mtthrift\", \n"
                + "                \"comment\": \"# rpc类型：mtthrift、pigeon、cthrift、dorado、asyncpigeon、thrift、asyncmtthrift\", \n"
                + "                \"oriValue\": null, \n" + "                \"oriComment\": null\n"
                + "            }, \n" + "            {\n" + "                \"key\": \"run\", \n"
                + "                \"value\": \"on\", \n" + "                \"comment\": \"# on：启动服务\", \n"
                + "                \"oriValue\": null, \n" + "                \"oriComment\": null\n"
                + "            }, \n" + "            {\n" + "                \"key\": \"threadNum\", \n"
                + "                \"value\": \"5\", \n" + "                \"comment\": \"# 单机并发数\", \n"
                + "                \"oriValue\": null, \n" + "                \"oriComment\": null\n"
                + "            }\n" + "        ], \n" + "        \"childrenNodes\": [ ], \n"
                + "        \"version\": 498\n" + "    }, \n" + "    \"success\": true\n" + "}";
        MccAPIResponse node = JsonHelper.toObject(json, MccAPIResponse.class);
        System.out.println(node);
    }
}
