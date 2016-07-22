package io.github.notsyncing.lightfur.codegen.annotations;

import io.github.notsyncing.lightfur.codegen.generators.CodeGenerator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Generator
{
    Class<? extends CodeGenerator> value();
}
