package io.github.notsyncing.lightfur.codegen.generators;

import com.github.javaparser.ast.expr.MethodCallExpr;
import io.github.notsyncing.lightfur.codegen.QueryContextCodeBuilder;

public abstract class CodeGenerator
{
    public abstract void generate(QueryContextCodeBuilder builder, MethodCallExpr method);
}
