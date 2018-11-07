package com.meituan.control.zookeeper.db;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileInputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import com.mchange.v2.c3p0.DataSources;

/**
 * User: jinmengzhe
 * Date: 2015-07-22
 * Desc:
 * 		1 使用数据库的入口类、调用C3p0PoolSource.init(configFile)后初始化数据库资源
 * 		  初始化以后，C3p0PoolSource提供根据dbName查询DataSource的静态方法
 * 		  客户端直接调用MtDBUtil类的静态方法即可
 * 		2 每个数据库区分master和slave: 分别在dbName后面拼接字符串“Server”或者Slave
 * 		3 作为单例使用 进程启动时init一次就行 简单实现即可
 *
 */
public class C3p0PoolSource {
	private static Logger logger = Logger.getLogger(C3p0PoolSource.class);
	private static Map<String, DataSource> dbName2DataSource = new HashMap<String, DataSource>();
	private static boolean isInit = false;

	public static synchronized void init(String configPath) throws Exception {
		if (isInit) {
			return;
		}
		InputStream inStream = null;
		try {
			if (null == configPath || configPath.isEmpty()) {
				throw new Exception("c3p0 config path is empty");
			} else {
				File file = new File(configPath);
				inStream = new FileInputStream(file);
			}

			SAXBuilder sax = new SAXBuilder();
			Document document = sax.build(inStream);
			Element root = document.getRootElement();
			List<Element> svs = root.getChildren();
			for (Element service : svs) {
				String dbName = "";
				String driver = "";
				String url = "";
				String username = "";
				String password = "";
				boolean isSlave = false;

				Map<String, Object> maps = new HashMap<String, Object>();
				List<Element> elements = service.getChildren();
				// 配置项名称是写死的，参见配置文件
				for (Element e : elements) {
					String name = e.getName();
					String value = e.getValue();
					if ("dbName".equals(name)) {
						dbName = value;
					} else if("isSlave".equals(name)) {
						isSlave = Boolean.parseBoolean(value);
					} else if ("driver".equals(name)) {
						driver = value;
					} else if ("url".equals(name)) {
						url = value;
					} else if ("username".equals(name)) {
						username = value;
					} else if ("password".equals(name)) {
						password = value;
					} else {
						maps.put(name, value);
					}
				}
				if (isSlave) {
					dbName = dbName + "Slave";
				} else {
					dbName = dbName + "Server";
				}
				logger.info(dbName + "-" + driver + "-" + url + "-" + username + "-" + password);
				Class.forName(driver);
				DataSource ds_unpooled = DataSources.unpooledDataSource(url, username, password);
				DataSource ds = DataSources.pooledDataSource(ds_unpooled, maps);
				dbName2DataSource.put(dbName, ds);
			}

			isInit = true;
		} catch (Exception e) {
			logger.fatal("fail to init c3p0PoolSource, configPath=" + configPath, e);
			if (inStream != null) {
				try {
					inStream.close();
				} catch (Exception e1) {
				}
			}
		}
	}

	public static DataSource getDataSource(String dbName) {
		return dbName2DataSource.get(dbName);
	}
}