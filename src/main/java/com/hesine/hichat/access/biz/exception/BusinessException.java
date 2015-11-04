/**
 * Create at Jan 30, 2013
 */
package com.hesine.hichat.access.biz.exception;

/**
 * @author liyan
 *
 * Business Base Exception
 */
public class BusinessException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3773602954855577710L;

	/**
	 * @param name
	 * @param throwable
	 */
	public BusinessException(String name, Throwable throwable) {
		super(name, throwable);
	}

	/**
	 * @param name
	 */
	public BusinessException(String name) {
		super(name);
	}

	/**
	 * @param throwable
	 */
	public BusinessException(Throwable throwable) {
		super(throwable);
	}

}
