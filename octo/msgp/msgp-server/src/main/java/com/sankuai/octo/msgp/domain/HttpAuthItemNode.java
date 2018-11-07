package com.sankuai.octo.msgp.domain;
import java.util.List;
/**
 * @author uu
 * @description
 * @date Created in 11:38 2018/5/2
 * @modified
 */
public class HttpAuthItemNode {
    private int id;
    private String username;
    private String token;
    private List<String> patternList;
    private String owtPattern;
    public HttpAuthItemNode(int id, String username, String token, List<String> patternList, String owt_pattern) {
        this.id = id;
        this.username = username;
        this.token = token;
        this.patternList = patternList;
        this.owtPattern = owt_pattern;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public List<String> getPatternList() {
        return patternList;
    }
    public void setPatternList(List<String> patternList) {
        this.patternList = patternList;
    }
    public String getOwtPattern() {
        return owtPattern;
    }
    public void setOwtPattern(String owt_pattern) {
        this.owtPattern = owt_pattern;
    }
}

