package io.github.notsyncing.lightfur;

import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class DataRepositoryCodeVisitor extends VoidVisitorAdapter
{
    @Override
    public void visit(MethodDeclaration n, Object arg)
    {
        super.visit(n, arg);
    }
}
