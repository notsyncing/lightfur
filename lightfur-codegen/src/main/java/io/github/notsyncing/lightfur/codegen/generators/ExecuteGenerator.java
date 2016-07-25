package io.github.notsyncing.lightfur.codegen.generators;

import com.github.javaparser.ast.expr.MethodCallExpr;
import io.github.notsyncing.lightfur.codegen.utils.CodeToSqlBuilder;
import io.github.notsyncing.lightfur.models.ModelColumnResult;
import io.github.notsyncing.lightfur.sql.base.SQLPart;
import io.github.notsyncing.lightfur.sql.builders.SelectQueryBuilder;
import io.github.notsyncing.lightfur.sql.builders.UpdateQueryBuilder;

public class ExecuteGenerator extends CodeGenerator
{
    public ExecuteGenerator(CodeToSqlBuilder builder)
    {
        super(builder);
    }

    @Override
    public void generate(MethodCallExpr method)
    {
        if (getBuilder().getSqlBuilder() instanceof SelectQueryBuilder) {
            SelectQueryBuilder b = (SelectQueryBuilder) getBuilder().getSqlBuilder();

            if (b.getSelectColumns().size() <= 0) {
                ModelColumnResult r = getBuilder().getDataModelColumnResult();

                b.select(r.getColumns())
                        .from(r.getTable());
            }
        } else if (getBuilder().getSqlBuilder() instanceof UpdateQueryBuilder) {
            UpdateQueryBuilder b = (UpdateQueryBuilder) getBuilder().getSqlBuilder();

            ModelColumnResult r = getBuilder().getDataModelColumnResult();
            SQLPart keyColumn = r.getKeyColumn();

            if (keyColumn != null) {
                b.returning(keyColumn, null);
            }
        }
    }
}
