/**
 * 
 */
package com.hesine.hichat.access.model;

import java.sql.Timestamp;

/**
 * 客户端认证表对象
 * 
 * @author pineapple
 *
 */
public class AuthAccount {
	private String account;
	private String pnToken;
	private int connectType;//0 、手机端登录  1、网页端登录
	private String deviceToken;	

	/**
	 * 不同PN类型:HPNS/APNS/GCM…
	 */
	private String pnType;

	/**
	 * 用户状态[0:下线,1:上线]
	 */
	private int userState;

	/**
	 * 用户类型（0-病人端，1-医生端）
	 */
	private int userType;
	
	private String appKey;	

	/**
	 * 终端类型（0-手机，1-网页，2-pc）
	 */
	private int terminalType;

	private Timestamp createTime;
	private Timestamp updateTime;
	
	public String getDeviceToken() {
		return deviceToken;
	}

	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}
	
	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}
	
	public int getConnectType() {
		return connectType;
	}

	public void setConnectType(int connectType) {
		this.connectType = connectType;
	}

	public int getTerminalType() {
		return terminalType;
	}

	public void setTerminalType(int terminalType) {
		this.terminalType = terminalType;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPnToken() {
		return pnToken;
	}

	public void setPnToken(String pnToken) {
		this.pnToken = pnToken;
	}

	public String getPnType() {
		return pnType;
	}

	public void setPnType(String pnType) {
		this.pnType = pnType;
	}

	public int getUserState() {
		return userState;
	}

	public void setUserState(int userState) {
		this.userState = userState;
	}

	public int getUserType() {
		return userType;
	}

	public void setUserType(int userType) {
		this.userType = userType;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

}
