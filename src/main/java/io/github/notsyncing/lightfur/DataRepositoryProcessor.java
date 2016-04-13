package io.github.notsyncing.lightfur;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import io.github.notsyncing.lightfur.annotations.DataRepository;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class DataRepositoryProcessor extends AbstractProcessor
{
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
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        Element dataRepoElem = roundEnv.getElementsAnnotatedWith(DataRepository.class).stream()
                .findFirst()
                .orElse(null);

        if (dataRepoElem == null) {
            return false;
        }

        String dataRepoSourceName = dataRepoElem.getSimpleName() + ".java";
        String dataRepoPkgName = dataRepoElem.getEnclosingElement().getSimpleName().toString();
        FileObject dataRepoSource;

        try {
            dataRepoSource = processingEnv.getFiler().getResource(StandardLocation.SOURCE_PATH,
                    dataRepoPkgName, dataRepoSourceName);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        CompilationUnit dataRepoUnit;

        try {
            dataRepoUnit = JavaParser.parse(dataRepoSource.openInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        new DataRepositoryCodeVisitor().visit(dataRepoUnit, null);

        return true;
    }
}
