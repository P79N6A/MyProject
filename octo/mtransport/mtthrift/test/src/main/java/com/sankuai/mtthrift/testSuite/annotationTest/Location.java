package com.sankuai.mtthrift.testSuite.annotationTest;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-2-18
 * Time: 下午4:25
 */

@ThriftStruct
public class Location {

    private double latitude;
    private double longitude;

    @ThriftConstructor
    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @ThriftField(1)
    public double getLatitude() {
        return latitude;
    }

    @ThriftField
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @ThriftField(2)
    public double getLongitude() {
        return longitude;
    }

    @ThriftField
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
