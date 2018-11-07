package com.sankuai.octo.mworth.common.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务价值指标注解
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Worth {
    Project project() default Project.OCTO;

    Model model() default Model.OTHER;

    String function() default "";

    public static enum Project {
        OCTO("OCTO"),
        HULK("HULK");

        private String name;

        Project(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static enum Model {
        MNS("命名服务"),
        MCC("配置管理"),
        DataCenter("数据分析"),
        Monitor("监控报警"),
        ErrorLog("异常监控"),
        GRAPH("服务视图"),
        DOC("服务文档"),
        ROUTE("服务分组"),
        QUOTA("一键截流"),
        AUTH("访问控制"),
        API("接口访问"),
        Report("治理报告"),
        OTHER("其它"),
        Component("组件依赖");


        private String name;

        Model(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static String getNamebyOrdinal(int index) {
            return Model.values()[index].getName();
        }
    }
}