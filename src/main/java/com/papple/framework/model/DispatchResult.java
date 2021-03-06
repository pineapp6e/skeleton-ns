/**
 * 
 */
package com.papple.framework.model;

import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 
 * @author wanghua
 *
 */
public class DispatchResult<T> {
	/**
	 * is the result is invalid
	 * true: invalid
	 * false: valid
	 */
	private boolean invalid;
	
	/**
	 * validation detail description.
	 */
	private String desc;
	
	private SimpleChannelInboundHandler<?> nextHandler;
	
	private T message;
	
	
	/**
	 * @return the invalid
	 */
	public boolean isInvalid() {
		return invalid;
	}

	/**
	 * @param invalid the invalid to set
	 */
	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	/**
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * @param desc the desc to set
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}


	/**
	 * @return the nextHandler
	 */
	public SimpleChannelInboundHandler<?> getNextHandler() {
		return nextHandler;
	}

	/**
	 * @param nextHandler the nextHandler to set
	 */
	public void setNextHandler(SimpleChannelInboundHandler<?> nextHandler) {
		this.nextHandler = nextHandler;
	}

	/**
	 * @return the message
	 */
	public T getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(T message) {
		this.message = message;
	}

}
