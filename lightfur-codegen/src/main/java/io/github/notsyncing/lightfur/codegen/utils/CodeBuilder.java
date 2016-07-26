package io.github.notsyncing.lightfur.codegen.utils;

import com.squareup.javapoet.*;
import io.github.notsyncing.lightfur.DataSession;
import io.github.notsyncing.lightfur.annotations.GeneratedDataContext;
import io.github.notsyncing.lightfur.codegen.utils.CodeToSqlBuilder;
import io.github.notsyncing.lightfur.sql.base.ReturningQueryBuilder;
import io.github.notsyncing.lightfur.sql.builders.SelectQueryBuilder;
import io.github.notsyncing.lightfur.sql.builders.UpdateQueryBuilder;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CodeBuilder
{
    private CodeToSqlBuilder builder;
    private String queryContextTypeName;
    private List<Integer> paramIndexList = new ArrayList<>();

    public CodeBuilder(CodeToSqlBuilder builder)
    {
        this.builder = builder;
    }

    public String getQueryContextTypeName()
    {
        return queryContextTypeName;
    }

    private TypeSpec generateFailedBlock(TypeName returnType)
    {
        MethodSpec failedFunc = MethodSpec.methodBuilder("apply")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Throwable.class, "ex")
                .returns(returnType)
                .addStatement("ex.printStackTrace()")
                .beginControlFlow("if (!finalInDb)")
                .addStatement("finalDb.end()")
                .endControlFlow()
                .addStatement("return null")
                .build();

        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(Function.class),
                        ClassName.get(Throwable.class), returnType))
                .addMethod(failedFunc)
                .build();
    }

    private TypeSpec generateCompletedBlock(TypeName returnType)
    {
        MethodSpec completedFunc = MethodSpec.methodBuilder("apply")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(returnType, "r")
                .returns(returnType)
                .beginControlFlow("if (!finalInDb)")
                .addStatement("finalDb.end()")
                .endControlFlow()
                .addStatement("return r")
                .build();

        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(Function.class), returnType, returnType))
                .addMethod(completedFunc)
                .build();
    }

    public TypeSpec build()
    {
        queryContextTypeName = builder.getDataContextType().getSimpleName() + "_" + builder.getQueryContextTag();
        String sql = builder.build();

        sql = processSqlNamedParameters(sql);

        String parameterArgs = paramIndexList.stream()
                .map(i -> "parameters[" + i + "]")
                .collect(Collectors.joining(","));

        if (!parameterArgs.isEmpty()) {
            parameterArgs = ", " + parameterArgs;
        }

        ClassName dataModelTypeName = ClassName.bestGuess(builder.getDataModelType());
        TypeName returnType = null;

        if (builder.getSqlBuilder() instanceof SelectQueryBuilder) {
            returnType = ParameterizedTypeName.get(ClassName.get(List.class),
                    dataModelTypeName.withoutAnnotations());
        } else if (builder.getSqlBuilder() instanceof ReturningQueryBuilder) {
            returnType = TypeName.OBJECT;
        }

        MethodSpec.Builder mb = MethodSpec.methodBuilder("execute")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(DataSession.class, "db")
                .addParameter(Object[].class, "parameters").varargs()
                .addStatement("boolean inDb = true")
                .beginControlFlow("if (db == null)")
                .addStatement("db = new $T()", DataSession.class)
                .addStatement("inDb = false")
                .endControlFlow()
                .addStatement("boolean finalInDb = inDb")
                .addStatement("$T finalDb = db", DataSession.class);

        TypeSpec completedBlock = generateCompletedBlock(returnType);
        TypeSpec failedBlock = generateFailedBlock(returnType);

        if (builder.getSqlBuilder() instanceof SelectQueryBuilder) {
            mb.returns(ParameterizedTypeName.get(ClassName.get(CompletableFuture.class), returnType))
                    .addStatement("return db.queryList($T.class, getSql()" + parameterArgs + ").thenApply($L).exceptionally($L)",
                            dataModelTypeName, completedBlock, failedBlock);
        } else if (builder.getSqlBuilder() instanceof ReturningQueryBuilder) {
            mb.returns(ParameterizedTypeName.get(ClassName.get(CompletableFuture.class), TypeName.OBJECT))
                    .addStatement("return db.executeWithReturning(getSql()" + parameterArgs + ").thenApply($L).exceptionally($L)",
                            completedBlock, failedBlock);
        }

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("super($T.class, $S, $S)", dataModelTypeName, builder.getQueryContextTag(), sql)
                .build();

        return TypeSpec.classBuilder(queryContextTypeName)
                .addAnnotation(GeneratedDataContext.class)
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(ClassName.get(builder.getDataContextType()), dataModelTypeName))
                .addMethod(constructor)
                .addMethod(mb.build())
                .build();
    }

    private String processSqlNamedParameters(String sql)
    {
        List<String> sqlNamedParams = new ArrayList<>();
        Pattern sqlNamedParamPattern = Pattern.compile(":([a-zA-Z_][a-zA-Z0-9_]*)");
        Matcher m = sqlNamedParamPattern.matcher(sql);

        while (m.find()) {
            sqlNamedParams.add(m.group(1));
        }

        if ((sqlNamedParams.size() > 0) && (builder.getExecuteParameters() == null)) {
            throw new RuntimeException("SQL need parameters, but no parameters are specified in execute: SQL " + sql);
        }

        m.reset();

        for (String p : sqlNamedParams) {
            boolean found = false;

            for (int i = 0; i < builder.getExecuteParameters().size(); i++) {
                if (builder.getExecuteParameters().get(i).equals(p)) {
                    found = true;
                    paramIndexList.add(i);
                    break;
                }
            }

            if (!found) {
                throw new RuntimeException("Required parameter " + p + " not found in SQL: " + sql);
            }

            sql = m.replaceFirst("?");
        }

        return sql;
    }
}
