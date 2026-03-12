package org.example.service;

public class ExternalOperationResult {

    private final boolean success;
    private final String reference;
    private final String message;

    public ExternalOperationResult(boolean success, String reference, String message) {
        this.success = success;
        this.reference = reference;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getReference() {
        return reference;
    }

    public String getMessage() {
        return message;
    }
}

