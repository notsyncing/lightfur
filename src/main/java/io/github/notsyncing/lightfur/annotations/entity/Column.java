package io.github.notsyncing.lightfur.annotations.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column
{
    String value();
    String type() default "";
    int length() default 0;
    boolean nullable() default true;
    String defaultValue() default "";
}
