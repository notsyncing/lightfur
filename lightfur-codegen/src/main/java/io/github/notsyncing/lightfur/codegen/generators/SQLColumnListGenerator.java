package io.github.notsyncing.lightfur.codegen.generators;

import com.github.javaparser.ast.CompilationUnit;
import io.github.notsyncing.lightfur.codegen.DataModelCodeVisitor;
import io.github.notsyncing.lightfur.models.ModelColumnResult;

public class SQLColumnListGenerator
{
    public static ModelColumnResult fromDataModel(CompilationUnit modelCode)
    {
        ModelColumnResult r = new ModelColumnResult();

        new DataModelCodeVisitor().visit(modelCode, r);

        return r;
    }
}
