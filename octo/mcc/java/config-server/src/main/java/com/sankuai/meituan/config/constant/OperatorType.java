package com.sankuai.meituan.config.constant;

public enum OperatorType implements ConstantGenerator.Constant {
    USER(1, "员工访问"), API(2, "api调用"), MTTHRIFT(3, "thrift调用");
    public static final ConstantGenerator<OperatorType> GENERATOR = ConstantGenerator.create(OperatorType.class);
    private final int index;
    private final String name;

    OperatorType(int index, String name) {
        this.index = index;
        this.name = name;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String getName() {
        return name;
    }
}
