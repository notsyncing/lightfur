package io.github.notsyncing.lightfur.codegen.generators;

import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import io.github.notsyncing.lightfur.codegen.utils.CodeToSqlBuilder;
import io.github.notsyncing.lightfur.sql.builders.InsertQueryBuilder;
import io.github.notsyncing.lightfur.sql.builders.SelectQueryBuilder;
import io.github.notsyncing.lightfur.sql.models.ColumnModel;
import io.github.notsyncing.lightfur.sql.models.OrderByColumnInfo;

public class SortedGenerator extends CodeGenerator
{
    public SortedGenerator(CodeToSqlBuilder builder)
    {
        super(builder);
    }

    @Override
    public void generate(MethodCallExpr method)
    {
        if (!(getBuilder().getSqlBuilder() instanceof SelectQueryBuilder)) {
            throw new RuntimeException("sorted can only be used in SELECT!");
        }

        SelectQueryBuilder b = (SelectQueryBuilder) getBuilder().getSqlBuilder();

        LambdaExpr setter = (LambdaExpr) method.getArgs().get(0);
        ExpressionStmt exp = (ExpressionStmt) setter.getBody();
        FieldAccessExpr field = (FieldAccessExpr) exp.getExpression();
        ColumnModel orderColumn = resolveColumn(field);
        BooleanLiteralExpr isDesc = (BooleanLiteralExpr) method.getArgs().get(1);

        if (orderColumn == null) {
            throw new RuntimeException("Column " + field.getField() + " is not found on model " + getBuilder().getDataModelType());
        }

        OrderByColumnInfo info = new OrderByColumnInfo();
        info.setColumn(orderColumn);
        info.setDesc(isDesc.getValue());

        b.orderBy(info);
    }
}
