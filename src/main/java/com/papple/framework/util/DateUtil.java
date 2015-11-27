package com.papple.framework.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtil {
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	public static String getCurrentDate(){
		return dateFormat.format(Calendar.getInstance().getTime());
	}
	
	public static void main(String[] args) {
		System.out.println(getCurrentDate());
	}
}
