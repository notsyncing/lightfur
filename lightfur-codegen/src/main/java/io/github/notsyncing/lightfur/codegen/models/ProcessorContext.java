package io.github.notsyncing.lightfur.codegen.models;

import io.github.notsyncing.lightfur.codegen.DataRepositoryProcessor;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;

public class ProcessorContext
{
    private DataRepositoryProcessor processor;
    private Filer filer;
    private Messager messager;

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

    public Messager getMessager()
    {
        return messager;
    }

    public void setMessager(Messager messager)
    {
        this.messager = messager;
    }
}
