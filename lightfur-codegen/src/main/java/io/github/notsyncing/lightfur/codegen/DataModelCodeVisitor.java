package io.github.notsyncing.lightfur.codegen;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.notsyncing.lightfur.annotations.entity.Column;
import io.github.notsyncing.lightfur.annotations.entity.PrimaryKey;
import io.github.notsyncing.lightfur.annotations.entity.Table;
import io.github.notsyncing.lightfur.models.ModelColumnResult;
import io.github.notsyncing.lightfur.sql.models.ColumnModel;
import io.github.notsyncing.lightfur.sql.models.TableModel;
import io.github.notsyncing.lightfur.codegen.utils.CodeVisitorUtils;

import java.util.List;

public class DataModelCodeVisitor extends VoidVisitorAdapter<ModelColumnResult>
{
    private TableModel table;
    private String modelType;

    @Override
    public void visit(ClassOrInterfaceDeclaration n, ModelColumnResult arg)
    {
        List<AnnotationExpr> annotations = n.getAnnotations();

        if (annotations.size() <= 0) {
            return;
        }

        AnnotationExpr tableAnnotation = annotations.stream()
                .filter(a -> a.getName().getName().equals(Table.class.getSimpleName()))
                .findFirst()
                .orElse(null);

        if (tableAnnotation == null) {
            return;
        }

        TableModel table = new TableModel();
        table.setName(CodeVisitorUtils.getAnnotationParameterStringValue(tableAnnotation, "value"));
        table.setSchema(CodeVisitorUtils.getAnnotationParameterStringValue(tableAnnotation, "schema"));

        this.table = table;
        arg.setTable(table);

        modelType = n.getName();

        super.visit(n, arg);
    }

    @Override
    public void visit(FieldDeclaration n, ModelColumnResult arg)
    {
        if (table == null) {
            return;
        }

        List<AnnotationExpr> annotations = n.getAnnotations();

        if (annotations.size() <= 0) {
            return;
        }

        AnnotationExpr columnAnnotation = annotations.stream()
                .filter(a -> a.getName().getName().equals(Column.class.getSimpleName()))
                .findFirst()
                .orElse(null);

        if (columnAnnotation == null) {
            return;
        }

        ColumnModel column = new ColumnModel();
        column.setTable(table);
        column.setColumn(CodeVisitorUtils.getAnnotationParameterStringValue(columnAnnotation, "value"));
        column.setFieldName(n.getVariables().get(0).getId().getName());
        column.setModelType(modelType);

        AnnotationExpr pkAnno = annotations.stream()
                .filter(a -> a.getName().getName().equals(PrimaryKey.class.getSimpleName()))
                .findFirst()
                .orElse(null);

        if (pkAnno != null) {
            column.setPrimaryKey(true);

            if (pkAnno instanceof NormalAnnotationExpr) {
                ((NormalAnnotationExpr) pkAnno).getPairs().forEach(p -> {
                    if (p.getName().equals("autoIncrement")) {
                        column.setAutoIncrement(((BooleanLiteralExpr)p.getValue()).getValue());
                    }
                });
            }
        }

        arg.getColumns().add(column);

        super.visit(n, arg);
    }
}