package com.meituan.control.zookeeper.service;

import java.util.HashMap;
import net.sf.json.JSONObject;

/**
 * User: jinmengzhe
 * Date: 2015-05-20
 */
public class ResonseContext {
	public int resultcode;
	public HashMap<String, String> respHeaders;
	public JSONObject data;
	public long startTime;
	public long endTime;
	
	public ResonseContext() {
		resultcode = -1;
		respHeaders = new HashMap<String, String>();
		data = null;
		startTime = System.currentTimeMillis();
		endTime = startTime;
	}
}
