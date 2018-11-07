package com.sankuai.octo.query.web;

/**
 * Created by zava on 15/9/30.
 */
public class FalconData {
    private String x;
    private long y;
    private int ts;

    public FalconData(){

    }
    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public long getY() {
        return y;
    }

    public void setY(long y) {
        this.y = y;
    }

    public int getTs() {
        return ts;
    }

    public void setTs(int ts) {
        this.ts = ts;
    }

    public String toString(){
     return "x:"+x+",y:"+y;
    }
}
