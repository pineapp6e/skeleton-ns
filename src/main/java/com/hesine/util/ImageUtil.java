package com.hesine.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;

public class ImageUtil {

	private static String ffmpegExe ;
	private static String imageSize = PropertiesUtil.getValue("thumbnail.size");
	static {
		//ffmpegExe = PropertiesUtil.getValue("ffmpeg.path");
		//if(StringUtils.isEmpty(ffmpegExe)){
			ffmpegExe = "ffmpeg";
		//}
	}
	public static void compress(String originalFile, String targetFile){
	        List<String> commend = new ArrayList<String>();
	        commend.add(ffmpegExe);
	        commend.add("-y");
	        commend.add("-i");
	        commend.add(originalFile);
	        commend.add("-s");
	        commend.add(imageSize);
	        commend.add("-f");
	        commend.add("image2");
	        commend.add(targetFile);
	        try {
	            ProcessBuilder builder = new ProcessBuilder();
	            builder.command(commend);
	            builder.start();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	}
	
	
	public static void screenshot(String srcVideoName, String targetFile, String startTime){
		 List<String> commend = new ArrayList<String>();
	        commend.add(ffmpegExe);
	        commend.add("-y");
	        commend.add("-i");
	        commend.add(srcVideoName);
	        commend.add("-ss");
	        commend.add(startTime);
	        commend.add("-vframes");
	        commend.add("1");
	        commend.add("-r");
	        commend.add("1");
	        commend.add("-ac");
	        commend.add("1");
	        commend.add("-ab");
	        commend.add("2");
	        commend.add("-s");
	        commend.add(imageSize);
	        commend.add("-f");
	        commend.add("image2");
	        commend.add(targetFile);
	        try {
	            ProcessBuilder builder = new ProcessBuilder();
	            builder.command(commend);
	            builder.start();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	}
	
	public static void saveFile(String saveFile, String attachStr) throws IOException{
			FileOutputStream fo = new FileOutputStream(saveFile);
			//String data = Utility.base64Decode(attachStr);			
			//fo.write(data.getBytes("ISO-8859-1"));
			fo.write(Base64.decodeBase64(attachStr));
			fo.close();
	}
	
	public static void saveAdminFile(String saveFile, String attachStr) throws IOException{
		FileOutputStream fo = new FileOutputStream(saveFile);
		org.apache.commons.codec.binary.Base64 base64 = new org.apache.commons.codec.binary.Base64();
		byte[] data = base64.decode(attachStr);
		fo.write(data);
		fo.close();
}
	
	public static void main(String[] args) throws InterruptedException {
//		compress("D:\\ffmpeg-20131202-git-e3d7a39-win32-static\\bin\\Water.jpg","D:\\ffmpeg-20131202-git-e3d7a39-win32-static\\bin\\Water1223.jpg");
		for(int i=0;i<10;i++){
			screenshot("/Users/pineapple/Desktop/1fkwOl.mp4","/Users/pineapple/Desktop/"+i+".jpg", String.valueOf(i));
			System.out.println("shot "+i);
			TimeUnit.SECONDS.sleep(1);
		}
	}
	
	
}
