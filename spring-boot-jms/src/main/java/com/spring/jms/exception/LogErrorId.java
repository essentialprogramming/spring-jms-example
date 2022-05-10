//======================================================================================================================
// Copyright (c) 2011-2018 BMW Group. All rights reserved.
//======================================================================================================================
package com.spring.jms.exception;

import lombok.Getter;
import lombok.ToString;

/**
 * The class represents log id entries.
 */
@Getter
@ToString
public enum LogErrorId implements ErrorId {

    INTERNAL_ERROR("UNKNOWN1000", "An unknown error occurred while processing the request."),
    INVALID_DATA("INVALID1001", "Invalid data.");


    private final String code;

    private final String description;

    /**
     * Standard constructor.
     *
     * @param code        the error code
     * @param description the error description
     */
    LogErrorId(final String code, final String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Get error code.
     *
     * @return the error code
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Get error description.
     *
     * @return the error description
     */
    public String getDescription() {
        return this.description;
    }
}
