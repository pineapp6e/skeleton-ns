package com.hesine.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

public class PropertiesUtil {
	
	private static Properties config = null;
	
	static {
		InputStream in = PropertiesUtil.class.getClassLoader()
				.getResourceAsStream("application.properties");
		config = new Properties();
		try {
			config.load(in);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getValue(String key) {
		return StringUtils.trim(config.getProperty(key));
	}
	
	public static int getIntValue(String key){
		return NumberUtils.toInt(config.getProperty(key));
	}
	
	public static boolean getBooleanValue(String key){
		return BooleanUtils.toBoolean(config.getProperty(key));
	}
}
