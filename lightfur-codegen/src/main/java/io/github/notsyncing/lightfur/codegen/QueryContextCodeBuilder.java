package io.github.notsyncing.lightfur.codegen;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import io.github.notsyncing.lightfur.codegen.annotations.Generator;
import io.github.notsyncing.lightfur.codegen.generators.CodeGenerator;
import io.github.notsyncing.lightfur.models.ModelColumnResult;
import io.github.notsyncing.lightfur.codegen.generators.SQLColumnListGenerator;
import io.github.notsyncing.lightfur.codegen.models.ProcessorContext;
import io.github.notsyncing.lightfur.sql.SQLBuilder;
import io.github.notsyncing.lightfur.sql.base.SQLPart;
import io.github.notsyncing.lightfur.codegen.utils.CodeVisitorUtils;

import javax.tools.FileObject;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class QueryContextCodeBuilder
{
    private SQLPart sqlBuilder;
    private String dataModelType;
    private MethodCallExpr queryExpr;
    private List<MethodCallExpr> callList = new ArrayList<>();
    private List<String> importClasses;
    private String queryContextTag;
    private ProcessorContext context;
    private ModelColumnResult dataModelColumnResult;

    public QueryContextCodeBuilder(MethodCallExpr expr, ProcessorContext context, String packageName,
                                   List<String> importClasses) throws IllegalAccessException, InstantiationException, IOException, ParseException
    {
        this.queryExpr = expr;
        this.importClasses = importClasses;
        this.context = context;

        queryExpressionToCallList();

        Method[] queryMethods = QueryContext.class.getMethods();

        for (MethodCallExpr m : callList) {
            if (m.getName().equals("get")) {
                sqlBuilder = SQLBuilder.select();

                String modelSimpleType = ((ClassExpr)m.getArgs().get(0)).getType().toString();
                dataModelType = importClasses.stream()
                        .filter(s -> s.endsWith("." + modelSimpleType))
                        .findFirst()
                        .orElse(packageName + "." + modelSimpleType);

                FileObject modelFile = context.getProcessor().getFile(dataModelType);
                CompilationUnit modelSource = JavaParser.parse(modelFile.openInputStream());
                dataModelColumnResult = SQLColumnListGenerator.fromDataModel(modelSource);

                queryContextTag = ((StringLiteralExpr)m.getArgs().get(1)).getValue();
            } else {
                Method queryMethod = Stream.of(queryMethods)
                        .filter(qm -> qm.getName().equals(m.getName()))
                        .findFirst()
                        .orElse(null);

                if (queryMethod == null) {
                    throw new RuntimeException("Method " + m.getName() + " is not found on QueryContext!");
                }

                Generator g = queryMethod.getAnnotation(Generator.class);

                if (g == null) {
                    continue;
                }

                CodeGenerator cg = g.value().newInstance();
                cg.generate(this, m);
            }
        }
    }

    private void queryExpressionToCallList()
    {
        Expression root = CodeVisitorUtils.getRootScope(queryExpr);
        Expression e = queryExpr;

        while (e != root) {
            MethodCallExpr m = (MethodCallExpr)e;
            e = m.getScope();

            if (CodeVisitorUtils.inScope(m, "execute")) {
                continue;
            }

            callList.add(m);
        }

        Collections.reverse(callList);
    }

    public String getDataModelType()
    {
        return dataModelType;
    }

    public void setDataModelType(String type)
    {
        dataModelType = type;
    }

    public SQLPart getSqlBuilder()
    {
        return sqlBuilder;
    }

    public ModelColumnResult getDataModelColumnResult()
    {
        return dataModelColumnResult;
    }

    public String getQueryContextTag()
    {
        return queryContextTag;
    }

    public String build()
    {
        return sqlBuilder.toString();
    }
}
