package com.sankuai.mtthrift.testSuite.annotationTest;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-2-18
 * Time: 下午4:35
 */
@ThriftStruct
public class Tweet {

    private int userId;
    private String userName;
    private String text;

    private Location loc;
    private TweetType tweetType;
    private int age;

    @ThriftConstructor
    public Tweet(int userId, String userName, String text) {
        this.userId = userId;
        this.userName = userName;
        this.text = text;

        this.tweetType = TweetType.TWEET;
        this.age = Constants.DEFAULT_AGE;
    }

    @ThriftField(1)
    public int getUserId() {
        return userId;
    }

    @ThriftField
    public void setUserId(int userId) {
        this.userId = userId;
    }

    @ThriftField(2)
    public String getUserName() {
        return userName;
    }

    @ThriftField
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @ThriftField(3)
    public String getText() {
        return text;
    }

    @ThriftField
    public void setText(String text) {
        this.text = text;
    }

    @ThriftField(4)
    public Location getLoc() {
        return loc;
    }

    @ThriftField
    public void setLoc(Location loc) {
        this.loc = loc;
    }

    @ThriftField(5)
    public TweetType getTweetType() {
        return tweetType;
    }

    @ThriftField
    public void setTweetType(TweetType tweetType) {
        this.tweetType = tweetType;
    }

    @ThriftField(16)
    public int getAge() {
        return age;
    }

    @ThriftField
    public void setAge(int age) {
        this.age = age;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else {
            if (obj instanceof Tweet) {
                Tweet tweet = (Tweet) obj;
                if (tweet.getUserId() == this.getUserId() && tweet.getUserName() == this.getUserName()
                        && tweet.getText() == this.getText()) {
                    return true;
                }
            }
        }
        return false;
    }
}
