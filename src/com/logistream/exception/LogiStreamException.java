package com.logistream.exception;

/** Base class for every application-specific error (unchecked, so code stays clean). */
public class LogiStreamException extends RuntimeException {
    public LogiStreamException(String message) {
        super(message);
    }

    public LogiStreamException(String message, Throwable cause) {
        super(message, cause);
    }
}
