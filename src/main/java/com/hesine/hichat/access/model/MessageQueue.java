/**
 * 
 */
package com.hesine.hichat.access.model;

import java.sql.Timestamp;

/**
 * @author pineapple
 *
 */
public class MessageQueue {
	private String account;
	private String messageId;
	private int type;
	private Timestamp createTime;

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

}
