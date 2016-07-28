package io.github.notsyncing.lightfur.codegen.generators;

import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import io.github.notsyncing.lightfur.codegen.utils.CodeToSqlBuilder;
import io.github.notsyncing.lightfur.sql.builders.InsertQueryBuilder;
import io.github.notsyncing.lightfur.sql.models.ColumnModel;

public class IgnoreGenerator extends CodeGenerator
{
    public IgnoreGenerator(CodeToSqlBuilder builder)
    {
        super(builder);
    }

    @Override
    public void generate(MethodCallExpr method)
    {
        if (!(getBuilder().getSqlBuilder() instanceof InsertQueryBuilder)) {
            throw new RuntimeException("ignore can only be used with INSERT!");
        }

        InsertQueryBuilder b = (InsertQueryBuilder) getBuilder().getSqlBuilder();

        LambdaExpr setter = (LambdaExpr) method.getArgs().get(0);
        ExpressionStmt exp = (ExpressionStmt) setter.getBody();
        FieldAccessExpr field = (FieldAccessExpr) exp.getExpression();
        ColumnModel setColumn = resolveColumn(field);

        if (setColumn == null) {
            throw new RuntimeException("Column " + field.getField() + " is not found on model " + getBuilder().getDataModelType());
        }

        b.ignore(setColumn.getColumn());
    }
}
