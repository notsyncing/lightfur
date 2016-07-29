package io.github.notsyncing.lightfur.codegen.generators;

import com.github.javaparser.ast.expr.MethodCallExpr;
import io.github.notsyncing.lightfur.codegen.enums.QueryFuncType;
import io.github.notsyncing.lightfur.codegen.utils.CodeToSqlBuilder;
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder;
import io.github.notsyncing.lightfur.sql.builders.SelectQueryBuilder;
import io.github.notsyncing.lightfur.sql.models.wrappers.LongWrapper;

import java.util.Arrays;

public class CountGenerator extends CodeGenerator
{
    public CountGenerator(CodeToSqlBuilder builder)
    {
        super(builder);
    }

    @Override
    public void generate(MethodCallExpr method)
    {
        if (!(getBuilder().getSqlBuilder() instanceof SelectQueryBuilder)) {
            throw new RuntimeException("count can only be used in SELECT!");
        }

        SelectQueryBuilder b = (SelectQueryBuilder) getBuilder().getSqlBuilder();

        b.setSelectColumns(Arrays.asList(new ExpressionBuilder().raw("COUNT(*)")));
        b.from(getBuilder().getDataModelColumnResult().getTable());

        getBuilder().setGeneratedQueryType(QueryFuncType.FirstValue);
        getBuilder().setDataModelType(LongWrapper.class);
    }
}
