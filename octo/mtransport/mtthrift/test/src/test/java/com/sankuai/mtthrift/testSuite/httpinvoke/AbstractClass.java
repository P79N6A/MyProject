package com.sankuai.mtthrift.testSuite.httpinvoke;


public abstract class AbstractClass {

    protected String name;

    public abstract void eat();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
