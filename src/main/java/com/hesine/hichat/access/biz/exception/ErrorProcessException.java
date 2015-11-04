package com.hesine.hichat.access.biz.exception;

public class ErrorProcessException extends Exception {

	private static final long serialVersionUID = -2249746024158678099L;

	private String message;

	public ErrorProcessException(String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}

}
