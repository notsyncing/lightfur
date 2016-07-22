package io.github.notsyncing.lightfur.codegen.generators;

import com.github.javaparser.ast.expr.MethodCallExpr;
import io.github.notsyncing.lightfur.codegen.QueryContextCodeBuilder;
import io.github.notsyncing.lightfur.models.ModelColumnResult;
import io.github.notsyncing.lightfur.sql.builders.SelectQueryBuilder;

public class ExecuteGenerator extends CodeGenerator
{
    @Override
    public void generate(QueryContextCodeBuilder builder, MethodCallExpr method)
    {
        if (builder.getSqlBuilder() instanceof SelectQueryBuilder) {
            SelectQueryBuilder b = (SelectQueryBuilder) builder.getSqlBuilder();

            if (b.getSelectColumns().size() <= 0) {
                ModelColumnResult r = builder.getDataModelColumnResult();

                b.select(r.getColumns())
                        .from(r.getTable());
            }
        }
    }
}
