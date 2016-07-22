package io.github.notsyncing.lightfur.annotations.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识该类/实体所对应的数据库表名称
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table
{
    /**
     * 该类/实体所对应的数据库中的表名称
     */
    String value();

    /**
     * 该表所属的架构
     */
    String schema() default "";
}
