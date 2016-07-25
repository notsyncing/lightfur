package io.github.notsyncing.lightfur.codegen.generators;

import com.github.javaparser.ast.expr.MethodCallExpr;
import io.github.notsyncing.lightfur.codegen.QueryContextCodeBuilder;
import io.github.notsyncing.lightfur.models.ModelColumnResult;
import io.github.notsyncing.lightfur.sql.builders.SelectQueryBuilder;

public class TakeGenerator extends CodeGenerator
{
    @Override
    public void generate(QueryContextCodeBuilder builder, MethodCallExpr method)
    {
        if (!(builder.getSqlBuilder() instanceof SelectQueryBuilder)) {
            throw new RuntimeException("take must be used in SELECT!");
        }

        SelectQueryBuilder b = (SelectQueryBuilder) builder.getSqlBuilder();
        b.limit(1);
    }
}
