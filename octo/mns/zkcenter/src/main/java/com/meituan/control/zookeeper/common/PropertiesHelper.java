package com.meituan.control.zookeeper.common;

import org.apache.log4j.Logger;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * User: jinmengzhe
 * Date: 2015-05-20
 */
public class PropertiesHelper {
	private final static Logger logger = Logger.getLogger(PropertiesHelper.class);
	static Properties properties = null;
	
	private PropertiesHelper() throws IOException  {
		
	}
	
	public static boolean init(String filePath) {
		boolean result = true;
		try {
			properties = new Properties();
			InputStream stream = new BufferedInputStream(new FileInputStream(filePath));;
			properties.load(stream);
			stream.close();
		} catch(Exception e) {
			result = false;
			logger.fatal("fail to init PropertiesHelper", e);
		}
		return result;
	}
	
	public static String getPropertiesValue(String key) {
		return properties.getProperty(key);
	}
	
	public static void main(String[] args) {
		PropertiesHelper.init(CommonTags.CONF_DIR + "WEB-INF/conf/zk_control.properties");
		String ss = PropertiesHelper.getPropertiesValue("testKey");
		System.out.println(ss);
	}
}
