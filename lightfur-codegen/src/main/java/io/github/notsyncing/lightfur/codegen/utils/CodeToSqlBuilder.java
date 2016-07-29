package io.github.notsyncing.lightfur.codegen.utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.Type;
import io.github.notsyncing.lightfur.codegen.annotations.Generator;
import io.github.notsyncing.lightfur.codegen.contexts.DeleteContext;
import io.github.notsyncing.lightfur.codegen.contexts.InsertContext;
import io.github.notsyncing.lightfur.codegen.contexts.QueryContext;
import io.github.notsyncing.lightfur.codegen.contexts.UpdateContext;
import io.github.notsyncing.lightfur.codegen.generators.CodeGenerator;
import io.github.notsyncing.lightfur.dsl.DataContext;
import io.github.notsyncing.lightfur.models.ModelColumnResult;
import io.github.notsyncing.lightfur.codegen.models.ProcessorContext;
import io.github.notsyncing.lightfur.sql.SQLBuilder;
import io.github.notsyncing.lightfur.sql.base.SQLPart;

import javax.tools.Diagnostic;
import javax.tools.FileObject;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class CodeToSqlBuilder
{
    private Class<? extends DataContext> dataContextType;
    private SQLPart sqlBuilder;
    private String dataModelType;
    private MethodCallExpr queryExpr;
    private List<MethodCallExpr> callList = new ArrayList<>();
    private List<String> importClasses;
    private String queryContextTag;
    private ProcessorContext context;
    private ModelColumnResult dataModelColumnResult;
    private List<String> executeParameters = new ArrayList<>();
    private String packageName;

    public CodeToSqlBuilder(MethodCallExpr expr, ProcessorContext context, String packageName,
                            List<String> importClasses) throws IllegalAccessException, InstantiationException, IOException, ParseException, NoSuchMethodException, InvocationTargetException
    {
        this.queryExpr = expr;
        this.packageName = packageName;
        this.importClasses = importClasses;
        this.context = context;

        queryExpressionToCallList();

        Method[] queryMethods = null;

        for (MethodCallExpr m : callList) {
            if ((m.getName().equals("get")) || (m.getName().equals("update")) || (m.getName().equals("add"))
                    || (m.getName().equals("remove"))) {
                dataModelType = resolveDataModelType(m);
                dataModelColumnResult = makeDataModelColumnResult(context);
                queryContextTag = ((StringLiteralExpr) m.getArgs().get(1)).getValue();
            }

            if (m.getName().equals("get")) {
                dataContextType = QueryContext.class;
                queryMethods = QueryContext.class.getMethods();

                sqlBuilder = SQLBuilder.select();
            } else if (m.getName().equals("update")) {
                dataContextType = UpdateContext.class;
                queryMethods = UpdateContext.class.getMethods();

                sqlBuilder = SQLBuilder.update(dataModelColumnResult.getTable());
            } else if (m.getName().equals("add")) {
                dataContextType = InsertContext.class;
                queryMethods = InsertContext.class.getMethods();

                sqlBuilder = SQLBuilder.insert().into(dataModelColumnResult.getTable());
            } else if (m.getName().equals("remove")) {
                dataContextType = DeleteContext.class;
                queryMethods = DeleteContext.class.getMethods();

                sqlBuilder = SQLBuilder.delete().from(dataModelColumnResult.getTable());
            } else {
                if (queryMethods == null) {
                    String msg = "No query methods on DataContext extracted from " + m;
                    context.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
                    throw new RuntimeException(msg);
                }

                Method queryMethod = Stream.of(queryMethods)
                        .filter(qm -> qm.getName().equals(m.getName()))
                        .findFirst()
                        .orElse(null);

                if (queryMethod == null) {
                    String msg = "Method " + m.getName() + " is not found on QueryContext!";
                    context.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
                    throw new RuntimeException(msg);
                }

                Generator g = queryMethod.getAnnotation(Generator.class);

                if (g == null) {
                    String msg = "Method " + m.getName() + " has no generator declared!";
                    context.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
                    throw new RuntimeException(msg);
                }

                CodeGenerator cg = g.value().getConstructor(this.getClass()).newInstance(this);
                cg.generate(m);
            }
        }
    }

    public List<String> getExecuteParameters()
    {
        return executeParameters;
    }

    public void addExecuteParameters(List<String> executeParameters)
    {
        this.executeParameters.addAll(executeParameters);
    }

    private ModelColumnResult makeDataModelColumnResult(ProcessorContext context) throws ParseException, IOException
    {
        FileObject modelFile = context.getProcessor().getFile(dataModelType);
        CompilationUnit modelSource = JavaParser.parse(modelFile.openInputStream());
        return SQLColumnListGenerator.fromDataModel(modelSource);
    }

    private String resolveDataModelType(String modelSimpleType)
    {
        return importClasses.stream()
                .filter(s -> s.endsWith("." + modelSimpleType))
                .findFirst()
                .orElse(packageName + "." + modelSimpleType);
    }

    private String resolveDataModelType(MethodCallExpr m)
    {
        return resolveDataModelType(((ClassExpr) m.getArgs().get(0)).getType().toString());
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

    public void setDataModelType(String simpleType)
    {
        dataModelType = resolveDataModelType(simpleType);
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

    public Class<? extends DataContext> getDataContextType()
    {
        return dataContextType;
    }

    public String build()
    {
        return sqlBuilder.toString();
    }
}
