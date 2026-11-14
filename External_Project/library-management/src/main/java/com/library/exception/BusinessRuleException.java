package com.library.exception;

/** Thrown when an operation breaks a library rule (no copies left, duplicate ISBN, ...). */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
