/**
 * 
 */
package com.hesine.hichat.access.model;

import com.hesine.hichat.model.ChatInfo;

/**
 * @author pineapple
 *
 */
public class DoctorChatStatus extends ChatInfo {
	
	private String doctorId;
	public String getDoctorId() {
		return doctorId;
	}

	public void setDoctorId(String doctorId) {
		this.doctorId = doctorId;
	}
}
