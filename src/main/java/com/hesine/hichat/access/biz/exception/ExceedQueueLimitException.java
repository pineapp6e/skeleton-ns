package com.hesine.hichat.access.biz.exception;

public class ExceedQueueLimitException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8437428739794472174L;

	/**
     * Creates a new instance.
     */
    public ExceedQueueLimitException() {
    }

    /**
     * Creates a new instance.
     */
    public ExceedQueueLimitException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance.
     */
    public ExceedQueueLimitException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     */
    public ExceedQueueLimitException(Throwable cause) {
        super(cause);
    }

}
