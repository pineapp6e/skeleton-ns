/**
 * 
 */
package com.hesine.util;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author wanghua
 * 
 */
public class HcpsPushUtil {

	private static final Logger LOGGER = Logger.getLogger(HcpsPushUtil.class);

	public static String push(String url, String cpId, String cpPwd,
			Map<String, Object> paramMap) throws IOException {
		PushConfig config = PushConfig.initConfig(url, cpId, cpPwd, paramMap);
		/**
		 * execute post request.
		 */
		String content = HttpClientUtil.postHcps(config);
		LOGGER.info("Response:" + content);
		return content;
	}

}
