package io.github.notsyncing.lightfur.codegen.models;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.tools.FileObject;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

public class SourceFileObject implements FileObject
{
    private Path file;

    public SourceFileObject(Path file)
    {
        this.file = file;
    }

    @Override
    public URI toUri()
    {
        return file.toUri();
    }

    @Override
    public String getName()
    {
        return file.getFileName().toString();
    }

    @Override
    public InputStream openInputStream() throws IOException
    {
        return Files.newInputStream(file);
    }

    @Override
    public OutputStream openOutputStream() throws IOException
    {
        return Files.newOutputStream(file);
    }

    @Override
    public Reader openReader(boolean ignoreEncodingErrors) throws IOException
    {
        return Files.newBufferedReader(file);
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException
    {
        throw new NotImplementedException();
    }

    @Override
    public Writer openWriter() throws IOException
    {
        return Files.newBufferedWriter(file);
    }

    @Override
    public long getLastModified()
    {
        try {
            return Files.getLastModifiedTime(file).toMillis();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public boolean delete()
    {
        try {
            Files.delete(file);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
