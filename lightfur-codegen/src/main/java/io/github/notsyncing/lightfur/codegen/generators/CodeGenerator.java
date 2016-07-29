package io.github.notsyncing.lightfur.codegen.generators;

import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import io.github.notsyncing.lightfur.codegen.utils.CodeToSqlBuilder;
import io.github.notsyncing.lightfur.sql.models.ColumnModel;

public abstract class CodeGenerator
{
    private CodeToSqlBuilder builder;

    public CodeGenerator(CodeToSqlBuilder builder)
    {
        this.builder = builder;
    }

    protected CodeToSqlBuilder getBuilder()
    {
        return builder;
    }

    public abstract void generate(MethodCallExpr method);

    protected ColumnModel resolveColumn(FieldAccessExpr expr)
    {
        return builder.getDataModelColumnResult().getColumns().stream()
                .filter(c -> c.getColumn().equals(expr.getField()))
                .findFirst()
                .orElse(null);
    }
}
