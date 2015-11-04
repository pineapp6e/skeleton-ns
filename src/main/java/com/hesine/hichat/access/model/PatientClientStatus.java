/**
 * 
 */
package com.hesine.hichat.access.model;

/**
 * @author pineapple
 *
 */
public class PatientClientStatus {
	/**
	 * DB中是否保存有这个用户
	 */
	private boolean hasdUser;
	
	/**
	 * 是否是老版本
	 */
	private boolean isOldVersion;

	public boolean isHasdUser() {
		return hasdUser;
	}

	public void setHasdUser(boolean hasdUser) {
		this.hasdUser = hasdUser;
	}

	public boolean isOldVersion() {
		return isOldVersion;
	}

	public void setOldVersion(boolean isOldVersion) {
		this.isOldVersion = isOldVersion;
	}
	
}
