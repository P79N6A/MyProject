package com.sankuai.mtthrift.testSuite.annotationTest;

import com.facebook.swift.service.ThriftException;
import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;
import org.apache.thrift.TException;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-2-19
 * Time: 下午3:53
 */
@ThriftService
public interface Twitter {

    @ThriftMethod
    public boolean testBool(boolean b) throws TException;

    @ThriftMethod
    public byte testByte(byte b) throws TException;

    @ThriftMethod
    public short testI16(short i) throws TException;

    @ThriftMethod
    public int testI32(int i) throws TException;

    @ThriftMethod
    public long testI64(long i) throws TException;

    @ThriftMethod
    public double testDouble(double d) throws TException;

    @ThriftMethod
    public ByteBuffer testBinary(ByteBuffer b) throws TException;

    @ThriftMethod
    public String testString(String s) throws TException;

    @ThriftMethod
    public List<String> testList(List<String> l) throws TException;

    @ThriftMethod
    public Set<String> testSet(Set<String> s) throws TException;

    @ThriftMethod
    public Map<String,String> testMap(Map<String,String> m) throws TException;

    @ThriftMethod
    public void testVoid() throws TException;

    @ThriftMethod
    public String testReturnNull() throws TException;

    @ThriftMethod
    public TweetSearchResult testStruct(String query) throws TException;

    @ThriftMethod(exception = {@ThriftException(type = TwitterUnavailable.class, id = 1)})
    public String testException(Tweet tweet) throws TwitterUnavailable, TException;

}
