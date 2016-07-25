package io.github.notsyncing.lightfur.codegen.generators;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import io.github.notsyncing.lightfur.codegen.utils.CodeToSqlBuilder;
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder;
import io.github.notsyncing.lightfur.sql.builders.SelectQueryBuilder;
import io.github.notsyncing.lightfur.sql.builders.UpdateQueryBuilder;

public class FilterGenerator extends CodeGenerator
{
    public FilterGenerator(CodeToSqlBuilder builder)
    {
        super(builder);
    }

    private ExpressionBuilder javaOpToSqlOp(Enum javaOp, ExpressionBuilder sqlOp)
    {
        if (javaOp instanceof BinaryExpr.Operator) {
            BinaryExpr.Operator o = (BinaryExpr.Operator) javaOp;

            switch (o) {
                case equals:
                    sqlOp.eq();
                    break;
                case greaterEquals:
                    sqlOp.gte();
                    break;
                case greater:
                    sqlOp.gt();
                    break;
                case less:
                    sqlOp.lt();
                    break;
                case lessEquals:
                    sqlOp.lte();
                    break;
                case notEquals:
                    sqlOp.ne();
                    break;
                case and:
                    sqlOp.and();
                    break;
                case or:
                    sqlOp.or();
                    break;
                default:
                    throw new RuntimeException("Unsupported operator " + o);
            }
        }

        return sqlOp;
    }

    private ExpressionBuilder javaExpToSqlExp(Expression javaExp, ExpressionBuilder sqlExp)
    {
        if (sqlExp == null) {
            sqlExp = new ExpressionBuilder();
        }

        if (javaExp instanceof BinaryExpr) {
            BinaryExpr exp = (BinaryExpr) javaExp;

            javaExpToSqlExp(exp.getLeft(), sqlExp);
            javaOpToSqlOp(exp.getOperator(), sqlExp);
            javaExpToSqlExp(exp.getRight(), sqlExp);
        } else if (javaExp instanceof FieldAccessExpr) {
            sqlExp.column(resolveColumn((FieldAccessExpr) javaExp));
        } else if (javaExp instanceof LiteralExpr) {
            if (javaExp instanceof IntegerLiteralExpr) {
                sqlExp.literal(Integer.parseInt(((IntegerLiteralExpr) javaExp).getValue()));
            } else if (javaExp instanceof DoubleLiteralExpr) {
                sqlExp.literal(Double.parseDouble(((DoubleLiteralExpr) javaExp).getValue()));
            } else if (javaExp instanceof BooleanLiteralExpr) {
                sqlExp.literal(((BooleanLiteralExpr) javaExp).getValue());
            } else if (javaExp instanceof LongLiteralExpr) {
                sqlExp.literal(Long.parseLong(((LongLiteralExpr) javaExp).getValue()));
            } else if (javaExp instanceof StringLiteralExpr) {
                sqlExp.literal(((StringLiteralExpr) javaExp).getValue());
            } else {
                throw new RuntimeException("Unsupported literal expression: " + javaExp);
            }
        } else if (javaExp instanceof NameExpr) {
            sqlExp.namedParameterReference(((NameExpr) javaExp).getName());
        } else {
            throw new RuntimeException("Unsupported expression: " + javaExp);
        }

        return sqlExp;
    }

    @Override
    public void generate(MethodCallExpr method)
    {
        if (getBuilder().getSqlBuilder() instanceof SelectQueryBuilder) {

        } else if (getBuilder().getSqlBuilder() instanceof UpdateQueryBuilder) {
            UpdateQueryBuilder b = (UpdateQueryBuilder) getBuilder().getSqlBuilder();
            LambdaExpr cond = (LambdaExpr) method.getArgs().get(0);
            ExpressionStmt exp = (ExpressionStmt)cond.getBody();
            b.where(javaExpToSqlExp(exp.getExpression(), null));
        } else {

        }
    }
}
