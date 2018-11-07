package com.meituan.control.zookeeper.service;

import java.util.HashMap;
import net.sf.json.JSONObject;

/**
 * User: jinmengzhe
 * Date: 2015-05-20
 */
public class RequestContext {
	public HashMap<String, String> headerMap;
	public HashMap<String, String> parameterMap;
	public String reqType;
	public JSONObject data;
	
	public RequestContext() {
		reqType = "";
		headerMap = new HashMap<String, String>();
		parameterMap = new HashMap<String, String>();
		data = null;
	}
}
