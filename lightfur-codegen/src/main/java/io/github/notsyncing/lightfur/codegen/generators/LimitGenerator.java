package io.github.notsyncing.lightfur.codegen.generators;

import com.github.javaparser.ast.expr.MethodCallExpr;
import io.github.notsyncing.lightfur.codegen.QueryContextCodeBuilder;
import io.github.notsyncing.lightfur.models.ModelColumnResult;
import io.github.notsyncing.lightfur.sql.builders.SelectQueryBuilder;

public class LimitGenerator extends CodeGenerator
{
    @Override
    public void generate(QueryContextCodeBuilder builder, MethodCallExpr method)
    {
        if (!(builder.getSqlBuilder() instanceof SelectQueryBuilder)) {
            throw new RuntimeException("findFirst must be used in SELECT!");
        }

        SelectQueryBuilder b = (SelectQueryBuilder) builder.getSqlBuilder();
        b.limit(1);

        if (b.getSelectColumns().size() <= 0) {
            ModelColumnResult r = builder.getDataModelColumnResult();

            b.select(r.getColumns())
                    .from(r.getTable());
        }
    }
}
