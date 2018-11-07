package com.sankuai.msgp.errorlog.pojo;

public class ErrorLogFilterCount implements Comparable {
    private int count;
    private StringBuffer message;

    public ErrorLogFilterCount(int count, StringBuffer message) {
        this.count = count;
        this.message = message;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public StringBuffer getMessage() {
        return message;
    }

    public void setMessage(StringBuffer message) {
        this.message = message;
    }

    public int compareTo(Object o) {
        ErrorLogFilterCount errorLogFilterCount = (ErrorLogFilterCount) o;
        int otherCount = errorLogFilterCount.getCount();

        if (count <= otherCount) {
            return -1;
        } else {
            return 1;
        }
    }
}
