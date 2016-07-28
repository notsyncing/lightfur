package io.github.notsyncing.lightfur.codegen.generators;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import io.github.notsyncing.lightfur.codegen.utils.CodeToSqlBuilder;
import io.github.notsyncing.lightfur.models.ModelColumnResult;
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder;
import io.github.notsyncing.lightfur.sql.base.SQLPart;
import io.github.notsyncing.lightfur.sql.builders.InsertQueryBuilder;
import io.github.notsyncing.lightfur.sql.builders.UpdateQueryBuilder;
import io.github.notsyncing.lightfur.sql.models.ColumnModel;

public class SetGenerator extends CodeGenerator
{
    private ColumnModel setColumn;
    private SQLPart setValue;

    public SetGenerator(CodeToSqlBuilder builder)
    {
        super(builder);
    }

    @Override
    public void generate(MethodCallExpr method)
    {
        CodeToSqlBuilder builder = getBuilder();

        if (!(builder.getSqlBuilder() instanceof UpdateQueryBuilder)) {
            throw new RuntimeException("set can only be used in UPDATE!");
        }

        UpdateQueryBuilder b = (UpdateQueryBuilder) builder.getSqlBuilder();
        extractSetColumnInfo(method, builder);

        b.set(setColumn, setValue);
    }

    private void extractSetColumnInfo(MethodCallExpr method, CodeToSqlBuilder builder)
    {
        LambdaExpr setter = (LambdaExpr) method.getArgs().get(0);
        ExpressionStmt exp = (ExpressionStmt) setter.getBody();
        AssignExpr setExp = (AssignExpr) exp.getExpression();

        FieldAccessExpr field = (FieldAccessExpr) setExp.getTarget();
        setColumn = resolveColumn(field);

        if (setColumn == null) {
            throw new RuntimeException("Column " + field.getField() + " is not found on model " + builder.getDataModelType());
        }

        Expression value = setExp.getValue();

        if (value instanceof StringLiteralExpr) {
            String v = ((StringLiteralExpr) value).getValue();
            setValue = new ExpressionBuilder().literal(v);
        } else if (value instanceof NameExpr) {
            String n = ((NameExpr) value).getName();
            setValue = new ExpressionBuilder().namedParameterReference(n);
        } else {
            throw new RuntimeException("Unsupported set target value: " + value + ", to column " + setColumn);
        }
    }
}
