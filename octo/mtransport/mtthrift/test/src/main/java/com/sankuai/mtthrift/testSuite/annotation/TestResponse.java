package com.sankuai.mtthrift.testSuite.annotation;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

/**
 * Author: caojiguang@gmail.com
 * Date: 16/10/25
 * Description:
 */
@ThriftStruct
public class TestResponse {
    private int userid;
    private String message;
    private int seqid;

    @ThriftField(value = 1, requiredness = ThriftField.Requiredness.REQUIRED)
    public int getUserid() {
        return userid;
    }

    @ThriftField(value = 1, requiredness = ThriftField.Requiredness.REQUIRED)
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

    @ThriftField(value = 3, requiredness = ThriftField.Requiredness.OPTIONAL)
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
