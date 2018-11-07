package com.sankuai.octo.aggregator.swift;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

import java.nio.ByteBuffer;

@ThriftStruct
public class SwiftCommonLog {
    @ThriftField(value = 1, requiredness = ThriftField.Requiredness.REQUIRED)
    public int cmd;
    @ThriftField(value = 2, requiredness = ThriftField.Requiredness.REQUIRED)
    public ByteBuffer content;
    @ThriftField(3)
    public String extend;
}
