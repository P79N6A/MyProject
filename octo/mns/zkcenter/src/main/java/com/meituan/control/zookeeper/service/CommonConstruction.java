package com.meituan.control.zookeeper.service;

import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.Map.Entry;

import com.meituan.control.zookeeper.common.CommonTags;
import net.sf.json.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.log4j.Logger;


/**
 * User: jinmengzhe
 * Date: 2015-05-20
 */
public class CommonConstruction {
	private final static Logger logger = Logger.getLogger(CommonConstruction.class);
	
	public static void construnctContext(RequestContext context, FullHttpRequest request) throws Exception {
		if (context == null) {
			context = new RequestContext();
		}
		// health check
		String URI = request.getUri();
		if (URI.endsWith("favicon.ico")) {
			context.reqType = CommonTags.ReqTypes.HEALTH_CHECK;
			return;
		}
		// header
		HttpHeaders httpHeaders = request.headers();
		for (Entry<String, String> httpHeader : httpHeaders) {
			context.headerMap.put(httpHeader.getKey(), httpHeader.getValue());
		}
		// parameter
		int index = URI.lastIndexOf("?");
		if (index > 0) {
			String queryString = URI.substring(index + 1);
			String[] parametersKV = queryString.split("&");
			for (int i = 0; i < parametersKV.length; i++) {
				String[] kv = parametersKV[i].split("=");
				if (kv.length == 2) {
					context.parameterMap.put(kv[0].trim(), kv[1].trim());
				} else {
					context.parameterMap.put(kv[0].trim(), null);
				}
			}
		}
		// body
		if (request.getMethod() == HttpMethod.POST) {
			ByteBuf byteBuf  = request.content();
			if (byteBuf != null && byteBuf.capacity() > 0) {
				ByteBuffer buffer = byteBuf.nioBuffer();
				byte[] content = new byte[buffer.limit()];
				buffer.get(content);
				String requestBody = URLDecoder.decode(new String(content), "utf-8");
				if (requestBody == null || requestBody.trim().isEmpty()) {
					throw new Exception("requestBody is empty!");
				}
				context.data = JSONObject.fromObject(requestBody);
			}
		}
		if (context.parameterMap.containsKey(CommonTags.RequestKey.REQTYPE)) {
			context.reqType = context.parameterMap.get(CommonTags.RequestKey.REQTYPE);
		}

		//printRequestContext(context);
	}

	public static void writeStatisticLog(RequestContext context, ResonseContext result) {
		StringBuilder sb = new StringBuilder("statisticLog:").append("reqtype=" + context.reqType)
															 .append(",resultcode=" + result.resultcode);
		logger.info(sb.toString());
	}

	private static void printRequestContext(RequestContext context) {
		System.out.println("---------------header----------------");
		for (Entry<String, String> entry : context.headerMap.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}

		System.out.println("--------------parameters-------------");
		for (Entry<String, String> entry : context.parameterMap.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}

		System.out.println("--------------body-------------------");
		if (context.data != null) {
			System.out.println(context.data.toString(1));
		}
	}
}
