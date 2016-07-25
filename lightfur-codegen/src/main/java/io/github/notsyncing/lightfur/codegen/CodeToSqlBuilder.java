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
import io.github.notsyncing.lightfur.dsl.DataContext;
import io.github.notsyncing.lightfur.models.ModelColumnResult;
import io.github.notsyncing.lightfur.codegen.models.ProcessorContext;
import io.github.notsyncing.lightfur.sql.SQLBuilder;
import io.github.notsyncing.lightfur.sql.base.SQLPart;
import io.github.notsyncing.lightfur.codegen.utils.CodeVisitorUtils;

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

    public CodeToSqlBuilder(MethodCallExpr expr, ProcessorContext context, String packageName,
                            List<String> importClasses) throws IllegalAccessException, InstantiationException, IOException, ParseException, NoSuchMethodException, InvocationTargetException
    {
        this.queryExpr = expr;
        this.importClasses = importClasses;
        this.context = context;

        queryExpressionToCallList();

        Method[] queryMethods = null;

        for (MethodCallExpr m : callList) {
            if ((m.getName().equals("get")) || (m.getName().equals("update")) || (m.getName().equals("insert"))
                    || (m.getName().equals("delete"))) {
                dataModelType = getDataModelType(packageName, importClasses, m);
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
            } else if (m.getName().equals("insert")) {

            } else if (m.getName().equals("delete")) {

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

                CodeGenerator cg = g.value().getConstructor(this.getClass()).newInstance(this);
                cg.generate(m);
            }
        }
    }

    private ModelColumnResult makeDataModelColumnResult(ProcessorContext context) throws ParseException, IOException
    {
        FileObject modelFile = context.getProcessor().getFile(dataModelType);
        CompilationUnit modelSource = JavaParser.parse(modelFile.openInputStream());
        return SQLColumnListGenerator.fromDataModel(modelSource);
    }

    private String getDataModelType(String packageName, List<String> importClasses, MethodCallExpr m)
    {
        String modelSimpleType = ((ClassExpr) m.getArgs().get(0)).getType().toString();
        return importClasses.stream()
                .filter(s -> s.endsWith("." + modelSimpleType))
                .findFirst()
                .orElse(packageName + "." + modelSimpleType);
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

    public Class<? extends DataContext> getDataContextType()
    {
        return dataContextType;
    }

    public String build()
    {
        return sqlBuilder.toString();
    }
}
