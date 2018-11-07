package com.meituan.control.zookeeper.flwc;

/**
 * User: jinmengzhe
 * Date: 2015-06-17
 */
public class FlwcCmdUtil {
    public static ConfData exeConf(String hostName, int port) {
        String content = FlwcCmd.CONF(hostName, port);
        return FlwcFormater.formatConf(content);
    }

    public static ConsData exeCons(String hostName, int port) {
        String content = FlwcCmd.CONS(hostName, port);
        return FlwcFormater.formatCons(content);
    }

    public static CrstData exeCrst(String hostName, int port) {
        String content = FlwcCmd.CRST(hostName, port);
        return FlwcFormater.formatCrst(content);
    }

    public static DumpData exeDump(String hostName, int port) {
        String content = FlwcCmd.DUMP(hostName, port);
        return FlwcFormater.formatDump(content);
    }

    public static EnviData exeEnvi(String hostName, int port) {
        String content = FlwcCmd.ENVI(hostName, port);
        return FlwcFormater.formatEnvi(content);
    }

    public static RuokData exeRuok(String hostName, int port) {
        String content = FlwcCmd.RUOK(hostName, port);
        return FlwcFormater.formatRuok(content);
    }

    public static SrstData exeSrst(String hostName, int port) {
        String content = FlwcCmd.SRST(hostName, port);
        return FlwcFormater.formatSrst(content);
    }

    public static SrvrData exeSrvr(String hostName, int port) {
        String content = FlwcCmd.SRVR(hostName, port);
        return FlwcFormater.formatSrvr(content);
    }

    public static StatData exeStat(String hostName, int port) {
        String content = FlwcCmd.STAT(hostName, port);
        return FlwcFormater.formatStat(content);
    }

    public static WchcData exeWchc(String hostName, int port) {
        String content = FlwcCmd.WCHC(hostName, port);
        return FlwcFormater.formatWchc(content);
    }

    public static WchsData exeWchs(String hostName, int port) {
        String content = FlwcCmd.WCHS(hostName, port);
        return FlwcFormater.formatWchs(content);
    }

    public static WchpData exeWchp(String hostName, int port) {
        String content = FlwcCmd.WCHP(hostName, port);
        return FlwcFormater.formatWchp(content);
    }

    public static MntrData exeMntr(String hostName, int port) {
        String content = FlwcCmd.MNTR(hostName, port);
        return FlwcFormater.formatMntr(content);
    }



    public static void main(String[] args) throws Exception {
        String zkIp = "192.168.2.225";
        int zkPort = 2181;
        ConfData confData = FlwcCmdUtil.exeConf(zkIp, zkPort);
        System.out.println(confData);
    }
}
