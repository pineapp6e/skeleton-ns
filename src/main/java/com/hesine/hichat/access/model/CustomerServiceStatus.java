/**
 * 
 */
package com.hesine.hichat.access.model;

import io.netty.channel.Channel;

import com.hesine.util.PropertiesUtil;

/**
 * @author pineapple
 * 客服服务状态
 */
public class CustomerServiceStatus implements Comparable<CustomerServiceStatus> {
	/**
	 * 客服当前连接channel
	 */
	private Channel channel;
	
	/**
	 * 客服名称
	 */
	private String csName;
	
	/**
	 * 是否为管理员
	 */
	private int role;
	
	/**
	 * 客服最大接待人数
	 */
	private int maxServiceNumber;
	
	/**
	 * 客服当前接待人数
	 */
	private int currentServiceNumber;

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public String getCsName() {
		return csName;
	}

	public void setCsName(String csName) {
		this.csName = csName;
	}

	public int getMaxServiceNumber() {
		return maxServiceNumber;
	}

	public void setMaxServiceNumber(int maxServiceNumber) {
		this.maxServiceNumber = maxServiceNumber;
	}

	public int getCurrentServiceNumber() {
		return currentServiceNumber;
	}

	public void setCurrentServiceNumber(int currentServiceNumber) {
		this.currentServiceNumber = currentServiceNumber;
	}
	
	public void incrementCurrentServiceNumber(){
		this.currentServiceNumber++;
	}
	
	public void decrementCurrentServiceNumber(){
		this.currentServiceNumber--;
	}
	
	public static CustomerServiceStatus init(String csName, int role, Channel channel){
		CustomerServiceStatus css = new CustomerServiceStatus();
		css.csName = csName;
		css.channel = channel;
		css.role = role;
		int maxNumber = PropertiesUtil.getIntValue("max.cs.service.number");
		css.maxServiceNumber = (maxNumber>0?maxNumber:1);
		return css;
	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}

	@Override
	public int compareTo(CustomerServiceStatus css) {
		return this.currentServiceNumber - css.currentServiceNumber;
	}

	@Override
	public String toString() {
		return "[csName:"+csName+", curNumber:"+currentServiceNumber+", maxNumber:"+maxServiceNumber+"]";
	}
	
	
	
}
