package io.github.notsyncing.lightfur.codegen;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.squareup.javapoet.*;
import io.github.notsyncing.lightfur.annotations.DataRepository;
import io.github.notsyncing.lightfur.codegen.models.ProcessorContext;
import io.github.notsyncing.lightfur.codegen.models.SourceFileObject;
import io.github.notsyncing.lightfur.codegen.utils.CodeBuilder;
import io.github.notsyncing.lightfur.codegen.utils.CodeToSqlBuilder;

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
            CodeBuilder cb = new CodeBuilder(b);
            TypeSpec t = cb.build();

            testGeneratedClassFullName = dataRepoPkgName + "." + cb.getQueryContextTypeName();
            String fullFilename = dataRepoPkgName + "." + cb.getQueryContextTypeName();

            JavaFile f = JavaFile.builder(dataRepoPkgName, t)
                    .build();

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
