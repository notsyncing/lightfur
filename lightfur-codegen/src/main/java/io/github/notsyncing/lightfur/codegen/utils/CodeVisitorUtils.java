package io.github.notsyncing.lightfur.codegen.utils;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;

public class CodeVisitorUtils
{
    public static NameExpr getRootScope(MethodCallExpr exp)
    {
        Expression e = exp.getScope();

        if (e instanceof MethodCallExpr) {
            return getRootScope((MethodCallExpr) e);
        } else if (e instanceof NameExpr) {
            return (NameExpr)e;
        }

        return null;
    }

    public static String getRootScopeName(MethodCallExpr exp)
    {
        NameExpr e = getRootScope(exp);

        if (e == null) {
            return null;
        }

        return e.getName();
    }

    public static boolean inScope(MethodCallExpr exp, String scopeName)
    {
        Expression e = exp.getScope();

        if (e instanceof MethodCallExpr) {
            if (scopeName.equals(((MethodCallExpr) e).getName())) {
                return true;
            }

            return inScope((MethodCallExpr) e, scopeName);
        } else if (e instanceof NameExpr) {
            return ((NameExpr) e).getName().equals(scopeName);
        }

        return false;
    }

    public static Expression getAnnotationParameter(AnnotationExpr exp, String key)
    {
        if (exp instanceof SingleMemberAnnotationExpr) {
            if ("value".equals(key)) {
                return ((SingleMemberAnnotationExpr) exp).getMemberValue();
            } else {
                return new StringLiteralExpr("");
            }
        } else if (exp instanceof NormalAnnotationExpr) {
            return ((NormalAnnotationExpr) exp).getPairs().stream()
                    .filter(p -> p.getName().equals(key))
                    .map(MemberValuePair::getValue)
                    .findFirst()
                    .orElse(new StringLiteralExpr(""));
        }

        throw new RuntimeException("Unsupported AnnotationExpr type " + exp.getClass());
    }

    public static String getAnnotationParameterStringValue(AnnotationExpr exp, String key)
    {
        Expression e = getAnnotationParameter(exp, key);

        if (e instanceof StringLiteralExpr) {
            return ((StringLiteralExpr) e).getValue();
        }

        throw new RuntimeException("Unsupported AnnotationExpr value type " + e.getClass());
    }
}
