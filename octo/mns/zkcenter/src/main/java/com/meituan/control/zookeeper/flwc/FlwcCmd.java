package com.meituan.control.zookeeper.flwc;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Formatter;

/**
 * User: jinmengzhe
 * Date: 2015-06-12
 * Desc:
 *      该类从远程机器去执行13个四字命令、并得到返回结果的原始content。
 *
 */
public class FlwcCmd {
    // 13 four letter words command of zookeeper
    private static Logger logger = Logger.getLogger(FlwcCmd.class);
    public static final String CONF = "src/main/webapp/WEB-INF/conf";
    public static final String CONS = "cons";
    public static final String CRST = "crst";
    public static final String DUMP = "dump";
    public static final String ENVI = "envi";
    public static final String RUOK = "ruok";
    public static final String SRST = "srst";
    public static final String SRVR = "srvr";
    public static final String STAT = "stat";
    public static final String WCHS = "wchs";
    public static final String WCHC = "wchc";
    public static final String WCHP = "wchp";
    public static final String MNTR = "mntr";

    // 查看zk配置项
    public static String CONF(String hostName, int port) {
        String command = new Formatter().format("echo %s | nc %s %d", CONF, hostName, port).toString();
        return exeFlwcCmd(command);
    }

    // 列出所有connection/session的详细信息
    public static String CONS(String hostName, int port) {
        String command = new Formatter().format("echo %s | nc %s %d", CONS, hostName, port).toString();
        return exeFlwcCmd(command);
    }

    // 重置connection/session的统计信息
    public static String CRST(String hostName, int port) {
        String command = new Formatter().format("echo %s | nc %s %d", CRST, hostName, port).toString();
        return exeFlwcCmd(command);
    }

    // 列出所有session的临时节点信息
    public static String DUMP(String hostName, int port) {
        String command = new Formatter().format("echo %s | nc %s %d", DUMP, hostName, port).toString();
        return exeFlwcCmd(command);
    }

    // 列出所有环境变量
    public static String ENVI(String hostName, int port) {
        String command = new Formatter().format("echo %s | nc %s %d", ENVI, hostName, port).toString();
        return exeFlwcCmd(command);
    }

    // 询问server是否OK--
    // A response of "imok" does not necessarily indicate that the server has joined the quorum,
    // just that the server process is active and bound to the specified client port
    public static String RUOK(String hostName, int port) {
        String command = new Formatter().format("echo %s | nc %s %d", RUOK, hostName, port).toString();
        return exeFlwcCmd(command);
    }

    // 重置Server stats信息
    public static String SRST(String hostName, int port) {
        String command = new Formatter().format("echo %s | nc %s %d", SRST, hostName, port).toString();
        return exeFlwcCmd(command);
    }

    // Lists full details for the server
    public static String SRVR(String hostName, int port) {
        String command = new Formatter().format("echo %s | nc %s %d", SRVR, hostName, port).toString();
        return exeFlwcCmd(command);
    }

    // Lists brief details for the server and connected clients
    public static String STAT(String hostName, int port) {
        String command = new Formatter().format("echo %s | nc %s %d", STAT, hostName, port).toString();
        return exeFlwcCmd(command);
    }

    // Lists brief information on watches for the server
    public static String WCHS(String hostName, int port) {
        String command = new Formatter().format("echo %s | nc %s %d", WCHS, hostName, port).toString();
        return exeFlwcCmd(command);
    }

    // Lists detailed information on watches for the server, by session
    public static String WCHC(String hostName, int port) {
        String command = new Formatter().format("echo %s | nc %s %d", WCHC, hostName, port).toString();
        return exeFlwcCmd(command);
    }

    // Lists detailed information on watches for the server, by path
    public static String WCHP(String hostName, int port) {
        String command = new Formatter().format("echo %s | nc %s %d", WCHP, hostName, port).toString();
        return exeFlwcCmd(command);
    }

    // List of variables that could be used for monitoring the health of the cluster
    public static String MNTR(String hostName, int port) {
        String command = new Formatter().format("echo %s | nc %s %d", MNTR, hostName, port).toString();
        return exeFlwcCmd(command);
    }

    private static String exeFlwcCmd(String flwcString) {
        BufferedReader br = null;
        Process p = null;
        try {
            // NOTE!!!
            String[] cmd = {"/bin/sh", "-c", flwcString};
            p = Runtime.getRuntime().exec(cmd);
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }

            return sb.toString();
        } catch (Exception e) {
            logger.fatal("fail to exeFlwcCmd, flwcString=" + flwcString, e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    //
                }
            }
            if (p != null) {
                try {
                    p.getOutputStream().close();
                    p.getInputStream().close();
                    p.getErrorStream().close();
                    p.destroy();
                } catch (Exception e) {
                    logger.fatal(e);
                }
            }
        }

        return null;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 50000; i++) {
            exeFlwcCmd("echo conf | nc 192.168.2.225 2181");
        }
    }
}
