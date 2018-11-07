package com.meituan.control.zookeeper.db;

import org.apache.log4j.Logger;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * User: jinmengzhe
 * Date: 2015-07-22
 * Desc:
 * 		 DBManager类、定义一些基本的数据库操作中间过程。不对外使用，提供给MtDBClient使用。
 *
 */
public class MtDBManager {
	private static Logger logger = Logger.getLogger(MtDBManager.class);

	public static Connection getConnection(String dbName, boolean isSlave) throws Exception {
		DataSource dataSource;
		if (isSlave) {
			dataSource = C3p0PoolSource.getDataSource(dbName + "Slave");
			if (null == dataSource) {
				dataSource = C3p0PoolSource.getDataSource(dbName + "Server");
				if (null == dataSource) {
					logger.fatal("cannot get connection from Server.");
				}
			}
		} else {
			dataSource = C3p0PoolSource.getDataSource(dbName + "Server");
			if (null == dataSource) {
				logger.fatal("cannot get connection from Server.");
			}
		}
		return dataSource.getConnection();
	}

	public static Connection getConnection(String dbName) throws Exception {
		return getConnection(dbName, false);
	}
	
	public static void closeConnection(Connection conn) throws SQLException {
		if (null != conn) {
			conn.close();
		}
	}
	
	public static PreparedStatement getPreparedStatement(Connection conn, String sql) throws SQLException {
		return conn.prepareStatement(sql);
	}
	
	public static void closePreparedStatement(PreparedStatement ps) throws SQLException {
		if (null != ps) {
			ps.close();
		}
	}

	public static ResultSet getResultSet(PreparedStatement ps) throws SQLException {
		return ps.executeQuery();
	}
	
	public static void closeResultSet(ResultSet rs) throws SQLException {
		if (null != rs) {
			rs.close();
		}
	}
}
