package com.spring.jms.exception;

public interface ErrorId {

    /**
     * Get the error code.
     */
    String getCode();

    /**
     * Get the error description.
     */
    String getDescription();

}
