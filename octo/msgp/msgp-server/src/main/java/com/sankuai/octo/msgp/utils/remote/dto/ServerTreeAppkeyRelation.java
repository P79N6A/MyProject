package com.sankuai.octo.msgp.utils.remote.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by emma on 2017/6/7.
 */
public class ServerTreeAppkeyRelation implements Cloneable {

    private Map<String, Set<String>> bjSvrTreeAppkeyPair;
    private Map<String, String> bjAppkeyOwtPair;

    public ServerTreeAppkeyRelation() {
    }

    public ServerTreeAppkeyRelation(Map<String, Set<String>> bjSvrTreeAppkeyPair,
                                    Map<String, String> bjAppkeyOwtPair) {
        this.bjSvrTreeAppkeyPair = new HashMap<>(bjSvrTreeAppkeyPair);
        this.bjAppkeyOwtPair = new HashMap<>(bjAppkeyOwtPair);
    }

    public ServerTreeAppkeyRelation(ServerTreeAppkeyRelation obj) {
        this.bjSvrTreeAppkeyPair = new HashMap<>(obj.getBjSvrTreeAppkeyPair());
        this.bjAppkeyOwtPair = new HashMap<>(obj.getBjAppkeyOwtPair());
    }

    public Map<String, Set<String>> getBjSvrTreeAppkeyPair() {
        return bjSvrTreeAppkeyPair;
    }

    public Map<String, String> getBjAppkeyOwtPair() {
        return bjAppkeyOwtPair;
    }

    public boolean hasData() {
        return bjSvrTreeAppkeyPair != null && !bjSvrTreeAppkeyPair.isEmpty();
    }

    public void clearData() {
        bjSvrTreeAppkeyPair = null;
        bjAppkeyOwtPair = null;
    }
}
