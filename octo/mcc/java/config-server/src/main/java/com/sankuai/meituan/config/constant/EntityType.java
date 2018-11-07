package com.sankuai.meituan.config.constant;

public enum  EntityType implements ConstantGenerator.Constant {
    ADD_SPACE(1, "添加配置空间"), DELETE_SPACE(2, "删除配置空间"),
    ADD_CONFIG(11, "添加配置"), DELETE_CONFIG(12, "删除配置"), UPDATE_CONFIG(13, "修改配置"), UPDATE_CONFIG_VERSION(14, "更新版本"),
    CREATE_SPACE_CONFIG(21, "创建配置空间配置"), DELETE_SPACE_CONFIG(22, "删除配置空间配置"), UPDATE_SPACE_CONFIG(23, "修改配置空间配置"),
    FILE_DISTRIBUTE(25, "文件下发"), FILE_UPDATE(26, "文件修改"), FILE_DELETE(27, "文件删除"), FILE_ADD(28, "文件上传")
    ;
    public static final ConstantGenerator<EntityType> GENERATOR = ConstantGenerator.create(EntityType.class);
    private final int index;
    private final String name;

    EntityType(int index, String name) {
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
