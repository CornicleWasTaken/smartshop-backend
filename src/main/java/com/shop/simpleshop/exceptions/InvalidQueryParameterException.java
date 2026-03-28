package com.shop.simpleshop.exceptions;

/**
 * Exception thrown when a query parameter contains an invalid value.
 */
public class InvalidQueryParameterException extends RuntimeException {

    private final String parameterName;
    private final String providedValue;
    private final String expectedFormat;

    public InvalidQueryParameterException(String parameterName, String providedValue, String expectedFormat) {
        super(String.format("Invalid query parameter '%s': provided value '%s' is not a valid %s",
                parameterName, providedValue, expectedFormat));
        this.parameterName = parameterName;
        this.providedValue = providedValue;
        this.expectedFormat = expectedFormat;
    }

    public String getParameterName() {
        return parameterName;
    }

    public String getProvidedValue() {
        return providedValue;
    }

    public String getExpectedFormat() {
        return expectedFormat;
    }
}
