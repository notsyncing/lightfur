package io.github.notsyncing.lightfur.codegen;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.squareup.javapoet.*;
import io.github.notsyncing.lightfur.DataSession;
import io.github.notsyncing.lightfur.annotations.DataRepository;
import io.github.notsyncing.lightfur.annotations.GeneratedDataContext;
import io.github.notsyncing.lightfur.codegen.models.ProcessorContext;
import io.github.notsyncing.lightfur.sql.builders.SelectQueryBuilder;
import io.github.notsyncing.lightfur.sql.builders.UpdateQueryBuilder;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.*;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class DataRepositoryProcessor extends AbstractProcessor
{
    private List<FileObject> testFiles = new ArrayList<>();
    private String testResultContent;
    private String testGeneratedClassFullName;

    public void addTestFile(FileObject testFile)
    {
        testFiles.add(testFile);
    }

    public FileObject getFile(String fullTypeName)
    {
        if (testFiles.size() > 0) {
            String filename = fullTypeName.replaceAll("\\.", "/") + ".java";

            return testFiles.stream()
                    .filter(f -> f.getName().equals(filename))
                    .findFirst()
                    .orElse(null);
        } else {
            String filename = fullTypeName.substring(fullTypeName.lastIndexOf(".") + 1) + ".java";
            String pkgName = fullTypeName.substring(0, fullTypeName.lastIndexOf("."));

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Opening file " + filename + " for " + pkgName);

            String sourcePath = processingEnv.getOptions().get("lightfur.source_path");

            if ((sourcePath == null) || (sourcePath.isEmpty())) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "You must specify lightfur.source_path option!");
                return null;
            }

            return new SourceFileObject(Paths.get(sourcePath, pkgName.replaceAll("\\.", "/"), filename));
        }
    }

    public String getTestResultContent()
    {
        return testResultContent;
    }

    public String getTestGeneratedClassFullName()
    {
        return testGeneratedClassFullName;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes()
    {
        Set<String> list = new HashSet<>();
        list.add(DataRepository.class.getCanonicalName());

        return list;
    }

    @Override
    public SourceVersion getSupportedSourceVersion()
    {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedOptions()
    {
        Set<String> opts = new HashSet<>();
        opts.add("lightfur.source_path");

        return opts;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        if (roundEnv.processingOver()) {
            return true;
        }

        Element dataRepoElem = roundEnv.getElementsAnnotatedWith(DataRepository.class).stream()
                .findFirst()
                .orElse(null);

        if (dataRepoElem == null) {
            return false;
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Running for " + dataRepoElem.asType().toString());

        String dataRepoTypeName = dataRepoElem.asType().toString();
        String dataRepoPkgName = dataRepoTypeName.substring(0, dataRepoTypeName.lastIndexOf("."));
        FileObject dataRepoSource = getFile(dataRepoTypeName);

        if (dataRepoSource == null) {
            return false;
        }

        CompilationUnit dataRepoUnit;

        try {
            dataRepoUnit = JavaParser.parse(dataRepoSource.openInputStream());
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to parse source file: " + e.getMessage());

            e.printStackTrace();
            return false;
        }

        ProcessorContext context = new ProcessorContext();
        context.setProcessor(this);

        DataRepositoryCodeVisitor visitor = new DataRepositoryCodeVisitor();
        visitor.visit(dataRepoUnit, context);

        for (CodeToSqlBuilder b : visitor.getBuilders()) {
            String queryContextTypeName = b.getDataContextType().getSimpleName() + "_" + b.getQueryContextTag();

            String sql = b.build();

            ClassName dataModelTypeName = ClassName.bestGuess(b.getDataModelType());

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

            if (b.getSqlBuilder() instanceof SelectQueryBuilder) {
                ParameterizedTypeName returnType = ParameterizedTypeName.get(ClassName.get(List.class),
                        dataModelTypeName.withoutAnnotations());

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

                TypeSpec completedBlock = TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(ParameterizedTypeName.get(ClassName.get(Function.class), returnType,
                                returnType))
                        .addMethod(completedFunc)
                        .build();

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

                TypeSpec failedBlock = TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(ParameterizedTypeName.get(ClassName.get(Function.class),
                                ClassName.get(Throwable.class), returnType))
                        .addMethod(failedFunc)
                        .build();

                mb.returns(ParameterizedTypeName.get(ClassName.get(CompletableFuture.class), returnType))
                        .addStatement("return db.queryList($T.class, getSql(), parameters).thenApply($L).exceptionally($L)",
                                dataModelTypeName, completedBlock, failedBlock);
            } else if (b.getSqlBuilder() instanceof UpdateQueryBuilder) {
                MethodSpec completedFunc = MethodSpec.methodBuilder("apply")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(TypeName.OBJECT, "r")
                        .returns(TypeName.OBJECT)
                        .beginControlFlow("if (!finalInDb)")
                        .addStatement("finalDb.end()")
                        .endControlFlow()
                        .addStatement("return r")
                        .build();

                TypeSpec completedBlock = TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(ParameterizedTypeName.get(ClassName.get(Function.class), TypeName.OBJECT,
                                TypeName.OBJECT))
                        .addMethod(completedFunc)
                        .build();

                MethodSpec failedFunc = MethodSpec.methodBuilder("apply")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Throwable.class, "ex")
                        .returns(TypeName.OBJECT)
                        .addStatement("ex.printStackTrace()")
                        .beginControlFlow("if (!finalInDb)")
                        .addStatement("finalDb.end()")
                        .endControlFlow()
                        .addStatement("return null")
                        .build();

                TypeSpec failedBlock = TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(ParameterizedTypeName.get(ClassName.get(Function.class),
                                ClassName.get(Throwable.class), TypeName.OBJECT))
                        .addMethod(failedFunc)
                        .build();

                mb.returns(ParameterizedTypeName.get(ClassName.get(CompletableFuture.class), TypeName.OBJECT))
                        .addStatement("return db.executeWithReturning(getSql(), parameters).thenApply($L).exceptionally($L)",
                                completedBlock, failedBlock);
            } else {

            }

            testGeneratedClassFullName = dataRepoPkgName + "." + queryContextTypeName;

            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("super($T.class, $S, $S)", dataModelTypeName, b.getQueryContextTag(), sql)
                    .build();

            TypeSpec t = TypeSpec.classBuilder(queryContextTypeName)
                    .addAnnotation(GeneratedDataContext.class)
                    .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                    .superclass(ParameterizedTypeName.get(ClassName.get(b.getDataContextType()), dataModelTypeName))
                    .addMethod(constructor)
                    .addMethod(mb.build())
                    .build();

            JavaFile f = JavaFile.builder(dataRepoPkgName, t)
                    .build();

            String fullFilename = dataRepoPkgName + "." + queryContextTypeName;

            if (testFiles.size() > 0) {
                testResultContent = f.toString();

                System.out.println("------------ BEGIN GENERATED CLASS ------------");
                System.out.print(testResultContent);
                System.out.println("------------ END GENERATED CLASS ------------");
            } else {
                try {
                    JavaFileObject file = processingEnv.getFiler().createSourceFile(fullFilename);

                    try (Writer writer = file.openWriter()) {
                        f.writeTo(writer);
                    }

                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generated " + fullFilename);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        return true;
    }
}
