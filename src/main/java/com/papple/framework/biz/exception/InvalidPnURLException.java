package com.papple.framework.biz.exception;

public class InvalidPnURLException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6051101884785760026L;

	/**
     * Creates a new instance.
     */
    public InvalidPnURLException() {
    }

    /**
     * Creates a new instance.
     */
    public InvalidPnURLException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance.
     */
    public InvalidPnURLException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     */
    public InvalidPnURLException(Throwable cause) {
        super(cause);
    }

}
