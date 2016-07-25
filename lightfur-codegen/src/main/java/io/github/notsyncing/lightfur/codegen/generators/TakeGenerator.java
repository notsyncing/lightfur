package io.github.notsyncing.lightfur.codegen.generators;

import com.github.javaparser.ast.expr.MethodCallExpr;
import io.github.notsyncing.lightfur.codegen.utils.CodeToSqlBuilder;
import io.github.notsyncing.lightfur.sql.builders.SelectQueryBuilder;

public class TakeGenerator extends CodeGenerator
{
    public TakeGenerator(CodeToSqlBuilder builder)
    {
        super(builder);
    }

    @Override
    public void generate(MethodCallExpr method)
    {
        if (!(getBuilder().getSqlBuilder() instanceof SelectQueryBuilder)) {
            throw new RuntimeException("take must be used in SELECT!");
        }

        SelectQueryBuilder b = (SelectQueryBuilder) getBuilder().getSqlBuilder();
        b.limit(1);
    }
}
