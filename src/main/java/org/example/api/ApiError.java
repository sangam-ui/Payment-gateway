package org.example.api;

import java.time.Instant;

public class ApiError {

    private final Instant timestamp;
    private final int status;
    private final String code;
    private final String error;
    private final String path;

    public ApiError(Instant timestamp, int status, String code, String error, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.code = code;
        this.error = error;
        this.path = path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getError() {
        return error;
    }

    public String getPath() {
        return path;
    }
}

