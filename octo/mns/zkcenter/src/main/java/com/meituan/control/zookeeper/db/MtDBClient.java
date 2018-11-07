package com.meituan.control.zookeeper.db;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * User: jinmengzhe
 * Date: 2015-07-22
 * Desc:
 * 		 一个封装好的数据库操作客户端、直接根据反射将sql执行的结果输出到数据对象中。
 * 		 支持数据库master-salve的读负载均衡
 *
 */
@SuppressWarnings("rawtypes")
public class MtDBClient {
	private static final int RANGE = 10;	// random [0,RANGE)
	private static final int BOUNDARY = 4;	// spilit to: [0,BOUNDARY],(BOUNDARY,RANGE)

	public static List<Object> executeQuery(Class clazz, String sql, Object[] objs, String dbName) throws Exception {
		List<Object> lists = new ArrayList<Object>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = MtDBManager.getConnection(dbName, useSlave());
			ps = MtDBManager.getPreparedStatement(conn, sql);
			if (null != objs) {
				for (int i = 1; i <= objs.length; i++) {
					ps.setObject(i, objs[i-1]);
				}
			}
			rs = MtDBManager.getResultSet(ps);
			Field[] fields = clazz.getDeclaredFields();
			String[] colNames = getColNames(rs);
			while (rs.next()) {
				Object obj = clazz.newInstance();
				for (int j = 0; j < colNames.length; j++) {
					String sqlColumnName = colNames[j];
					for (int i = 0; i < fields.length; i++) {
						if (fields[i].getName().equalsIgnoreCase(sqlColumnName)) {
							fields[i].setAccessible(true);
							fields[i].set(obj, rs.getObject(j+1));
							fields[i].setAccessible(false);
							break;
						}
					}
				}
				lists.add(obj);
			}
		} catch(Exception ex) {
			throw ex;
		} finally {
			MtDBManager.closeResultSet(rs);
			MtDBManager.closePreparedStatement(ps);
			MtDBManager.closeConnection(conn);
		}	
		
		return lists;
	}

	public static int executeUpdate(String sql, Object[] objs, String dbName) throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		int count = -1;
		
		try {
			conn = MtDBManager.getConnection(dbName);
			ps = MtDBManager.getPreparedStatement(conn, sql);
			if (null != objs) {
				for (int i = 1; i <= objs.length; i++) {
					ps.setObject(i, objs[i-1]);
				}
			}
			count = ps.executeUpdate();
		} catch(Exception ex) {
			throw ex;
		} finally {
			MtDBManager.closePreparedStatement(ps);
			MtDBManager.closeConnection(conn);
		}

		return count;
	}
	
	private static boolean useSlave() {
		// [0-4]:use master  [5-9]:use slave
		return (new Random().nextInt(RANGE) > BOUNDARY);
	}
	
	private static String[] getColNames(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        String[] colNames = new String[columns];
        for (int i = 1; i <= columns; i++) {
            colNames[i - 1] = rsmd.getColumnLabel(i);
        }

        return colNames;
    }
}