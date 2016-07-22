package io.github.notsyncing.lightfur.annotations.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识该列在数据库中为主键
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrimaryKey
{
    /**
     * 该列在数据库中是否为自增长
     */
    boolean autoIncrement() default false;
}
