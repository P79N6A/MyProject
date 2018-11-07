package com.meituan.control.zookeeper.server;

import java.util.Map.Entry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.log4j.Logger;
import io.netty.handler.codec.http.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import com.meituan.control.zookeeper.common.CommonTags;
import com.meituan.control.zookeeper.service.RequestContext;
import com.meituan.control.zookeeper.service.ResonseContext;
import com.meituan.control.zookeeper.service.CommonConstruction;
import com.meituan.control.zookeeper.common.SharedClassHelper;

/**
 * User: jinmengzhe
 * Date: 2015-05-20
 */
public class NettyHttpServerHandler extends SimpleChannelInboundHandler<Object> {
	private final static Logger logger = Logger.getLogger(NettyHttpServerHandler.class);

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// super.exceptionCaught(ctx, cause);
		ctx.close();
	}

	@Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) {
        FullHttpRequest request = (FullHttpRequest) msg;
        // construct RequestContext
        RequestContext context = new RequestContext();
		ResonseContext result = new ResonseContext();
        try {
            CommonConstruction.construnctContext(context, request);
			switch (context.reqType) {
				case CommonTags.ReqTypes.TEST:
					result = SharedClassHelper.zkControlService.testMethod(context);
					break;
				case CommonTags.ReqTypes.HEALTH_CHECK:
					result.resultcode = CommonTags.ErrorCode.OK;
					break;
				case CommonTags.ReqTypes.REPORT_ZK_CLIENT_INFO:
					result = SharedClassHelper.zkControlService.reportZkClientInfo(context);
					break;
				case CommonTags.ReqTypes.GET_CLUSTERS_DEPLOY:
					result = SharedClassHelper.zkControlService.getClustersDeploy(context);
					break;
				case CommonTags.ReqTypes.GET_NODE_INFO:
					result = SharedClassHelper.zkControlService.zkWebGetNodeInfo(context);
					break;
				case CommonTags.ReqTypes.GET_JMX:
					result = SharedClassHelper.zkControlService.getJmxInfo(context);
					break;
				case CommonTags.ReqTypes.GET_FLWC:
					result = SharedClassHelper.zkControlService.getFlwcInfo(context);
					break;
				case CommonTags.ReqTypes.GET_MONITOR:
					result = SharedClassHelper.zkControlService.getMonitor(context);
					break;
				// other requests add below

				default:
					throw new Exception("unsupported reqtype=" + context.reqType);
			}
        } catch (Exception e) {
        	logger.error(e.getMessage(), e);
        	result.resultcode = CommonTags.ErrorCode.ERROR_FAILED;
        }
        // response
        try {
            FullHttpResponse response = null;
            if (result.resultcode == CommonTags.ErrorCode.OK) {
            	if (result.data != null) {
            		String content = result.data.toString();
            		ByteBuf buf = Unpooled.wrappedBuffer(content.getBytes("utf-8"));
            		response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
            	} else {
            		response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            	}
            	response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
            	for (Entry<String, String> headerEntry : result.respHeaders.entrySet()) {
            		response.headers().set(headerEntry.getKey(), headerEntry.getValue());
            	}
            } else {
            	if (result.data != null) {
            		String content = result.data.toString();
            		ByteBuf buf = Unpooled.wrappedBuffer(content.getBytes("utf-8"));
            		response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
            	} else {
            		response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            	}
            	response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
            	for (Entry<String, String> headerEntry : result.respHeaders.entrySet()) {
            		response.headers().set(headerEntry.getKey(), headerEntry.getValue());
            	}
            }

			response.headers().set("Access-Control-Allow-Origin", "*");
            
            ctx.writeAndFlush(response);
        } catch (Exception e) {
        	logger.error("exception happens when write response", e);
        	result.resultcode = CommonTags.ErrorCode.ERROR_RESPONSE_FAILED;
        } finally {
        	// This is a synchronous operation, will seriously impact the QPS
			// here may be changge to async log in the future
        	CommonConstruction.writeStatisticLog(context, result);
        }
    }
}
