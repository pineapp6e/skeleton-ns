package com.hesine.util;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

public class AttachFileContext {
	
	private static Logger log = Logger.getLogger(AttachFileContext.class
			.getName());
	
	public static final String FILE_SEPARATOR = System
			.getProperty("file.separator");

	/**
	 * 原始附件目录前缀
	 */
	public static final String BASE_PATH = PropertiesUtil.getValue("attachment.prefix");
	
	/**
	 * 缩略附件前缀
	 */
	public static final String BASE_THUMBNAIL_PATH = PropertiesUtil.getValue("attachment.thumbnail.prefix");
	
	/**
	 * 图片服务路径基本前缀
	 */
	public static final String ATTACH_URL_PREFIX = PropertiesUtil.getValue("attachment.url.prefix");

	/**
	 * return the thumbnail attachment path .
	 * 
	 * @param userAccount
	 * @return
	 */
	public static String genRelativePath(String userAccount) {
		StringBuilder sb = new StringBuilder();
		sb.append(userAccount);
		sb.append(FILE_SEPARATOR);
		sb.append(DateUtil.getCurrentDate());
		return sb.toString();
	}
	
	/**
	 * the file path url access.
	 * @param fileName
	 * @param relativePath
	 * @return
	 */
	public static String getURL(String fileName,String relativePath){
		StringBuilder sb = new StringBuilder();
		//sb.append(ATTACH_URL_PREFIX);
		sb.append(FILE_SEPARATOR);
		sb.append(relativePath);
		sb.append(FILE_SEPARATOR);
		sb.append(fileName);
		return sb.toString();
	}

	public static String getAttachContent(String filePath, String fileName, String pathPrefix){
		FileInputStream fis;
		byte[] buffer = null;		
		String inputName = pathPrefix + filePath + fileName;
		log.info("get thumbnail name: "+ inputName);
		String res = null;
		try {
			fis = new FileInputStream(inputName);
			int length = fis.available();
			buffer = new byte[length];
			fis.read(buffer);
			res = new String(buffer, "ISO-8859-1");
			fis.close();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return Utility.base64Encode(res);
	}

	
}
