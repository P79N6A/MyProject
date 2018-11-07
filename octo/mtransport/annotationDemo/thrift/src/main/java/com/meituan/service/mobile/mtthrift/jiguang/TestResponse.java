package com.meituan.service.mobile.mtthrift.jiguang;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

/**
 * Created by jiguang on 15/7/23.
 */
@ThriftStruct
public final class TestResponse {

    private int userid;
    private String message;
    private int seqid;

    public TestResponse() {

    }

    public TestResponse(int userid, String message, int seqid) {
        this.userid = userid;
        this.message = message;
        this.seqid = seqid;

    }

    @ThriftField(1)
    public int getUserid() {
        return userid;
    }
    @ThriftField
    public void setUserid(int userid) {
        this.userid = userid;
    }

    @ThriftField(2)
    public String getMessage() {
        return message;
    }
    @ThriftField
    public void setMessage(String message) {
        this.message = message;
    }

    @ThriftField(3)
    public int getSeqid() {
        return seqid;
    }
    @ThriftField
    public void setSeqid(int seqid) {
        this.seqid = seqid;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return toString().equals(o.toString());
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("TestResponse");
        sb.append("{userid='").append(userid).append('\'');
        sb.append(", message=").append(message);
        sb.append(", seqid=").append(seqid);
        sb.append('}');
        return sb.toString();
    }
}


