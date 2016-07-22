package io.github.notsyncing.lightfur.codegen.models;

import io.github.notsyncing.lightfur.codegen.DataRepositoryProcessor;

import javax.annotation.processing.Filer;

public class ProcessorContext
{
    private DataRepositoryProcessor processor;
    private Filer filer;

    public DataRepositoryProcessor getProcessor()
    {
        return processor;
    }

    public void setProcessor(DataRepositoryProcessor processor)
    {
        this.processor = processor;
    }

    public Filer getFiler()
    {
        return filer;
    }

    public void setFiler(Filer filer)
    {
        this.filer = filer;
    }
}
