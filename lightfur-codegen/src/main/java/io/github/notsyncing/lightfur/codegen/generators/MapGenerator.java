package io.github.notsyncing.lightfur.codegen.generators;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import io.github.notsyncing.lightfur.codegen.utils.CodeToSqlBuilder;
import io.github.notsyncing.lightfur.models.ModelColumnResult;
import io.github.notsyncing.lightfur.sql.base.SQLPart;
import io.github.notsyncing.lightfur.sql.builders.SelectQueryBuilder;
import io.github.notsyncing.lightfur.sql.models.ColumnModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MapGenerator extends CodeGenerator
{
    public MapGenerator(CodeToSqlBuilder builder)
    {
        super(builder);
    }

    @Override
    public void generate(MethodCallExpr method)
    {
        if (!(getBuilder().getSqlBuilder() instanceof SelectQueryBuilder)) {
            throw new RuntimeException("map can only be used with SELECT!");
        }

        SelectQueryBuilder b = (SelectQueryBuilder) getBuilder().getSqlBuilder();

        if (b.getSelectColumns().size() <= 0) {
            ModelColumnResult r = getBuilder().getDataModelColumnResult();

            b.select((List<SQLPart>)(List<?>)r.getColumns())
                    .from(r.getTable());
        }

        ClassExpr mappedType = (ClassExpr) method.getArgs().get(0);
        String fromType = getBuilder().getDataModelType();
        String toType = mappedType.getType().toString();
        getBuilder().setDataModelType(toType);

        LambdaExpr mapper = (LambdaExpr) method.getArgs().get(1);
        List<ExpressionStmt> statements = new ArrayList<>();

        if (mapper.getBody() instanceof ExpressionStmt) {
            statements.add((ExpressionStmt) mapper.getBody());
        } else if (mapper.getBody() instanceof BlockStmt) {
            ((BlockStmt) mapper.getBody()).getStmts()
                    .forEach(s -> statements.add((ExpressionStmt) s));
        } else {
            throw new RuntimeException("Unsupported statement type " + mapper.getBody());
        }

        List<SQLPart> newSelectColumns = new ArrayList<>();

        for (ExpressionStmt s : statements) {
            if (!(s.getExpression() instanceof AssignExpr)) {
                throw new RuntimeException("Unsupported statement type " + s.getClass());
            }

            AssignExpr expr = (AssignExpr) s.getExpression();
            FieldAccessExpr to = (FieldAccessExpr) expr.getTarget();
            FieldAccessExpr from = (FieldAccessExpr) expr.getValue();

            for (SQLPart c : b.getSelectColumns()) {
                if (!(c instanceof ColumnModel)) {
                    newSelectColumns.add(c);
                    continue;
                }

                ColumnModel column = (ColumnModel)c;

                if ((!column.getFieldName().equals(from.getField())) && (!column.getModelType().equals(fromType))) {
                    continue;
                }

                column.setAlias(resolveColumn(to).getColumn());
                newSelectColumns.add(column);
            }
        }

        b.setSelectColumns(newSelectColumns);
    }
}
