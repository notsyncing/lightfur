package io.github.notsyncing.lightfur.codegen;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.notsyncing.lightfur.codegen.utils.CodeToSqlBuilder;
import io.github.notsyncing.lightfur.dsl.Query;
import io.github.notsyncing.lightfur.codegen.models.ProcessorContext;
import io.github.notsyncing.lightfur.codegen.utils.CodeVisitorUtils;

import java.util.ArrayList;
import java.util.List;

public class DataRepositoryCodeVisitor extends VoidVisitorAdapter<ProcessorContext>
{
    private List<CodeToSqlBuilder> builders = new ArrayList<>();
    private CodeToSqlBuilder currentBuilder;
    private NameExpr prevQuery;
    private List<String> imports = new ArrayList<>();
    private String packageName;

    public List<CodeToSqlBuilder> getBuilders()
    {
        return builders;
    }

    @Override
    public void visit(ImportDeclaration n, ProcessorContext arg)
    {
        imports.add(n.getName().toString());
        super.visit(n, arg);
    }

    @Override
    public void visit(PackageDeclaration n, ProcessorContext arg)
    {
        packageName = n.getPackageName();
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodCallExpr n, ProcessorContext arg)
    {
        NameExpr currQuery = CodeVisitorUtils.getRootScope(n);

        if ((currQuery == null) || (prevQuery == currQuery)) {
            super.visit(n, arg);
            return;
        }

        if (Query.class.getSimpleName().equals(currQuery.getName())) {
            try {
                currentBuilder = new CodeToSqlBuilder(n, arg, packageName, imports);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            builders.add(currentBuilder);

            prevQuery = currQuery;
        }

        super.visit(n, arg);
    }
}
