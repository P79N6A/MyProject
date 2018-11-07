package com.meituan.mtrace.query;

/**
 * @author zhangzhitong
 * @created 9/25/15
 */
public class ServItem {
    public ServItem() {
        count = 1;
    }
    public ServItem(int duration, int count) {
        this.count = count;
        this.duration = duration;
    }
    public int duration;
    public int count;

    public String toString() {
        return "ServItem(duration:" + this.duration + ", count:" + this.count + ")";
    }
}
