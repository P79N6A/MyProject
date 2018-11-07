package com.sankuai.meituan.config.model;

import com.sankuai.meituan.config.constant.ConstantGenerator;

public class Operator {
    public Operator(String id, String name, ConstantGenerator.Constant type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    protected String id;

    protected String name;

    protected ConstantGenerator.Constant type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ConstantGenerator.Constant getType() {
        return type;
    }

    public void setType(ConstantGenerator.Constant type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
