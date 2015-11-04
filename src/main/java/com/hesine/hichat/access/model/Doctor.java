/**
 * 
 */
package com.hesine.hichat.access.model;

/**
 * 
 * 医生表对象
 * 
 * @author pineapple
 *
 */
public class Doctor extends AuthAccount {

	/**
	 * 所属医院ID
	 */
	private String hospitalId;

	public String getHospitalId() {
		return hospitalId;
	}

	public void setHospitalId(String hospitalId) {
		this.hospitalId = hospitalId;
	}
}
