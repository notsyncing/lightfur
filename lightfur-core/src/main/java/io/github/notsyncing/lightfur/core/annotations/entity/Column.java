package io.github.notsyncing.lightfur.core.annotations.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识该字段对应数据库中的某一列，以及在自动创建数据库时提供该列的一些信息
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column
{
    /**
     * 该字段所对应的数据库中的列名
     */
    String value();

    /**
     * 数据库中该列的类型
     */
    String type() default "";

    /**
     * 数据库中该列的长度，0 表示无长度信息
     */
    int length() default 0;

    /**
     * 数据库中该列是否允许为 NULL
     */
    boolean nullable() default true;

    /**
     * 数据库中该列的默认值，空字符串表示无默认值
     */
    String defaultValue() default "";
}
