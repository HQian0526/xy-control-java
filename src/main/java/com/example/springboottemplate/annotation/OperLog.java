package com.example.springboottemplate.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要自动记录操作日志的接口（仅写操作建议使用）。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {
    /** 操作模块，如「用户管理」 */
    String module();

    /**
     * 操作类型：{@link OperTypes#ADD}/{@link OperTypes#DELETE}/{@link OperTypes#UPDATE}/{@link OperTypes#QUERY}
     */
    int type();

    /** 备注（可选） */
    String remark() default "";
}
