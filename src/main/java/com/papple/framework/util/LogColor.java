package com.papple.framework.util;

public class LogColor {
	/**
	 * log color 
	 */
	public static final String FMT_RED="\033[31m";
	public static final String FMT_GREEN="\033[32m";
	public static final String FMT_YELLOW="\033[33m";
	public static final String FMT_END="\033[0m";
	
	public static String colorLog(String title, String color, String objJson){
		StringBuilder request = new StringBuilder();
		request.append(title);
		request.append(color);
		request.append(objJson);
		request.append(LogColor.FMT_END);
		return request.toString();
	}
	
	public static String requestLog(String objJson){
		return colorLog("Request : ",LogColor.FMT_GREEN, objJson);
	}
	
	public static String sucessResponseLog(String objJson){
		return colorLog("Successful Response : ",LogColor.FMT_YELLOW, objJson);
	}
	
	public static String failResponseLog(String objJson){
		return colorLog("Failed Response : ",LogColor.FMT_RED, objJson);
	}
}
