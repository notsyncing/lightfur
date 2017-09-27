package io.github.notsyncing.lightfur.models;

public class ExecutionResult {
    private int updated;

    public ExecutionResult(int updated) {
        this.updated = updated;
    }

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }
}
