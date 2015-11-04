/**
 * 
 */
package com.hesine.hichat.access.model;

/**
 * @author pineapple
 *
 */
public class ChatStatus {
	private long chatId;
	
	/**
	 * 会话状态
	 */
	private int status;

	public long getChatId() {
		return chatId;
	}

	public void setChatId(long chatId) {
		this.chatId = chatId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public static ChatStatus build(long chatId, int status){
		ChatStatus cs = new ChatStatus();
		cs.chatId = chatId;
		cs.status = status;
		return cs;
	}
}
