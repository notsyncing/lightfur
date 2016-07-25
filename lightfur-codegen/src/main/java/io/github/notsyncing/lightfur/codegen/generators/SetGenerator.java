package io.github.notsyncing.lightfur.codegen.generators;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import io.github.notsyncing.lightfur.codegen.CodeToSqlBuilder;
import io.github.notsyncing.lightfur.models.ModelColumnResult;
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder;
import io.github.notsyncing.lightfur.sql.base.SQLPart;
import io.github.notsyncing.lightfur.sql.builders.UpdateQueryBuilder;
import io.github.notsyncing.lightfur.sql.models.ColumnModel;
import jdk.nashorn.internal.ir.ExpressionStatement;

public class SetGenerator extends CodeGenerator
{
    public SetGenerator(CodeToSqlBuilder builder)
    {
        super(builder);
    }

    @Override
    public void generate(MethodCallExpr method)
    {
        CodeToSqlBuilder builder = getBuilder();

        if (!(builder.getSqlBuilder() instanceof UpdateQueryBuilder)) {
            throw new RuntimeException("set must be used in UPDATE clause!");
        }

        ModelColumnResult columns = builder.getDataModelColumnResult();

        UpdateQueryBuilder b = (UpdateQueryBuilder)builder.getSqlBuilder();
        LambdaExpr setter = (LambdaExpr)method.getArgs().get(0);
        ExpressionStmt exp = (ExpressionStmt)setter.getBody();
        AssignExpr setExp = (AssignExpr)exp.getExpression();

        FieldAccessExpr field = (FieldAccessExpr)setExp.getTarget();
        ColumnModel column = resolveColumn(field);

        if (column == null) {
            throw new RuntimeException("Column " + field.getField() + " is not found on model " + builder.getDataModelType());
        }

        Expression value = setExp.getValue();
        SQLPart setValue;

        if (value instanceof StringLiteralExpr) {
            String v = ((StringLiteralExpr) value).getValue();
            setValue = new ExpressionBuilder().literal(v);
        } else if (value instanceof NameExpr) {
            String n = ((NameExpr) value).getName();
            setValue = new ExpressionBuilder().namedParameterReference(n);
        } else {
            throw new RuntimeException("Unsupported set target value: " + value + ", to column " + column);
        }

        b.set(column, setValue);
    }
}
