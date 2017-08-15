package io.github.notsyncing.lightfur.versioning;

import io.github.lukehutch.fastclasspathscanner.matchprocessor.FileMatchProcessorWithContext;

@FunctionalInterface
public interface DbUpdateFileCollector {
    void collect(String extensionToMatch, FileMatchProcessorWithContext handler);
}
