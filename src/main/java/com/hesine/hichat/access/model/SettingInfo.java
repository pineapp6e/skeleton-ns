package com.hesine.hichat.access.model;

public class SettingInfo {
	private int type;	
	private int isSelected;
	private String content;
	
	public static final int TYPE_CUSTOM_SETTING = 1;
	public static final int TYPE_CLOSE_CHAT_SETTING = 2;
	public static final int TYPE_GETOFF_WORK_REMINDER = 3;
	public static final int TYPE_WORK_TIME_SETTING = 4;
	public static final int TYPE_WORK_DAY_SETTING = 5;
	
	public static final int SELECTED = 1;
	public static final int NOT_SELECTED = 0;
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getIsSelected() {
		return isSelected;
	}
	public void setIsSelected(int isSelected) {
		this.isSelected = isSelected;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
}
