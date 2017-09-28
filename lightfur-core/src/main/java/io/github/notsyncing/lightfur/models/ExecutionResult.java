package io.github.notsyncing.lightfur.models;

public class ExecutionResult {
    private long updated;

    public ExecutionResult(long updated) {
        this.updated = updated;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }
}
