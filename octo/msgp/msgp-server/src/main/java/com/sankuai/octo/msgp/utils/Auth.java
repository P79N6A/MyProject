package com.sankuai.octo.msgp.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Auth {

    Level level() default Level.ANON;

    public static enum Level {
        ANON(0),
        LOGIN(4),
        READ(8),
        OBSERVER(12),
        ADMIN(16),
        OCTO(20);

        private int value;

        Level(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
        }

    ResponseMode responseMode() default ResponseMode.VIEW;

    public static enum ResponseMode {
        VIEW(0),
        JSON(1);

        private int value;

        ResponseMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

}