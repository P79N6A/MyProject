package com.meituan.service.mobile.mtthrift.generic;

import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;
import org.apache.thrift.TException;

import java.util.List;

@ThriftService
public interface GenericService {
    @ThriftMethod
    String $invoke(String methodName, List<String> parameterTypes, List<String> parameters) throws TException;
}
