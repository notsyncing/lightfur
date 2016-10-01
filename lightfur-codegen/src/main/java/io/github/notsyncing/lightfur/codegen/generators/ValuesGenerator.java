package io.github.notsyncing.lightfur.codegen.generators;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import io.github.notsyncing.lightfur.codegen.utils.CodeToSqlBuilder;
import io.github.notsyncing.lightfur.models.ModelColumnResult;
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder;
import io.github.notsyncing.lightfur.sql.base.SQLPart;
import io.github.notsyncing.lightfur.sql.builders.InsertQueryBuilder;
import io.github.notsyncing.lightfur.sql.models.ColumnModel;

import java.util.List;

public class ValuesGenerator extends CodeGenerator
{
    public ValuesGenerator(CodeToSqlBuilder builder)
    {
        super(builder);
    }

    @Override
    public void generate(MethodCallExpr method)
    {
        if (!(getBuilder().getSqlBuilder() instanceof InsertQueryBuilder)) {
            throw new RuntimeException("values can only be used in INSERT!");
        }

        InsertQueryBuilder b = (InsertQueryBuilder) getBuilder().getSqlBuilder();

        String dataParamName = ((NameExpr)method.getArgs().get(0)).getName();

        List<ColumnModel> columns = getBuilder().getDataModelColumnResult().getColumns();

        columns.forEach(c -> {
            if (c.isAutoIncrement()) {
                return;
            }

            b.column(c, new ExpressionBuilder().namedParameterReference(dataParamName + "_" + c.getFieldName() + "@" +
                    c.getModelType()));
        });
    }
}