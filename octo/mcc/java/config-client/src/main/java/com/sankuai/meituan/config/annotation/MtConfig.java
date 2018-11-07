package com.sankuai.meituan.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MtConfig {
	/**
	 * 与{@link com.sankuai.meituan.config.MtConfigClient#id}属性对应,为client对应的标识
	 * 用于在多个MtConfigClient的实例中找到对应的那个client实例
	 * 修改成用client的别名进行映射,避免无法按环境使用不同配置的问题
	 * @see com.sankuai.meituan.config.MtConfigClient#id
	 */
    String clientId() default "";

    /**
     * 监控的配置项key，比如switch
     */
    String key();

    /**
     * 兼容旧版本， node的路径
     */
    String nodeName() default "";
}
