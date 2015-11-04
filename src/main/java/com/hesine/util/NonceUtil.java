/**
 * 
 */
package com.hesine.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author wanghua
 *
 */
public class NonceUtil {

	private static final String INIT_KEY = "test@hesine.com";
	private static long LAST_NONCE_TIME;
	/**
	 * default nonce generation interval is one day.
	 */
	private static long NONCE_INTERVAL = 1000*60*60*24;
	private static String DIGEST_NONCE;
	private static MessageDigest md;
	
	/**
	 * use current time, predefined key to generate a nonce.
	 * More than one day will be updated
	 * @return
	 * @throws NoSuchAlgorithmException 
	 * @throws UnsupportedEncodingException
	 */
	public synchronized static String makeNonce() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		if(md == null){
			md = MessageDigest.getInstance("MD5");
		}
		long currentTimeMillis = System.currentTimeMillis();
		if(LAST_NONCE_TIME == 0){
			LAST_NONCE_TIME = currentTimeMillis;
			md.update(INIT_KEY.getBytes("UTF-8"));
			md.update(toByteArray(currentTimeMillis));
			DIGEST_NONCE = convertToHexString(md.digest());
		}else{
			long delay = currentTimeMillis - LAST_NONCE_TIME;
			if(delay >= NONCE_INTERVAL){
				LAST_NONCE_TIME = currentTimeMillis;
				md.reset();
				md.update(INIT_KEY.getBytes("UTF-8"));
				md.update(toByteArray(currentTimeMillis));
				DIGEST_NONCE = convertToHexString(md.digest());
			}
		}
		return DIGEST_NONCE;
	}
	
	public static String currentNonce(){
		return DIGEST_NONCE;
	}
	
	static byte[] toByteArray(long longData){
		byte[] array = new byte[8];
		for(int i=0; i<8; i++){
			array[8-1-i] = (byte)(longData>>(i*8)&0xff);
		}
		return array;
	}
	
	
	static String convertToHexString(byte data[]) {
		StringBuffer strBuffer = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			strBuffer.append(Integer.toHexString((0x000000ff & data[i]) | 0xffffff00).substring(6));
		}
		return strBuffer.toString();
	}

}