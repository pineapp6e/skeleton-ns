/**
 * Create at Jan 24, 2013
 */
package com.hesine.hichat.access.service.snapshot;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hesine.hichat.access.biz.exception.BusinessException;

/**
 * @author liyan
 * 
 */
public class SnapshotService {

	public String getAccessKey(HttpRequest request) {
		return HttpHeaders.getHost(request, "") + request.getUri();
	}

	public static final int WATCH_KEY_ACCESS_BY_URI = 1;
	public static final int WATCH_KEY_ACCESS_BY_IP = 2;
	public static final int WATCH_KEY_PROCESS_BY_STATUS = 3;
	public static final int WATCH_KEY_Exception_BY_Type = 4;

	private Map<String, SumObjectMap> watchMap = new HashMap<String, SumObjectMap>();

	public SnapshotService() {
		// init watch map
		SumObjectMap map = new SumObjectMap("Access Count Group By Uri");
		watchMap.put("1", map);

		map = new SumObjectMap("Access Count Group By IP");
		watchMap.put("2", map);

		map = new SumObjectMap("Process Count Group By Status");
		watchMap.put("3", map);

		map = new SumObjectMap("Exception Count Group By Type");
		watchMap.put("4", map);
	}

	public void addMessageWatch(MessageWatch watch, List<Exception> exceptions) {
		String key = watch.getRequestUri();
		watchMap.get(String.valueOf(WATCH_KEY_ACCESS_BY_URI)).add(key, 1,
				watch.getAliveTime());

		key = String.valueOf(watch.getResponseCode());
		// only add business log
		if (watch.isBusiness()) {
			watchMap.get(String.valueOf(WATCH_KEY_PROCESS_BY_STATUS)).add(key,
					1, watch.getAliveTime(MessageWatch.STATE_BUSINESS));

			// split count BusinessException
			for (Exception ex : exceptions) {
				key = ex.getClass().getName();
				if (ex instanceof BusinessException) {
					watchMap.get(String.valueOf(WATCH_KEY_Exception_BY_Type))
							.add("[logic error]"+key,
									1,
									watch.getAliveTime(MessageWatch.STATE_BUSINESS));
				} else {
					watchMap.get(String.valueOf(WATCH_KEY_Exception_BY_Type))
							.add(key,
									1,
									watch.getAliveTime(MessageWatch.STATE_BUSINESS));
				}
			}
		}

		key = watch.getRemoteIP();
		watchMap.get(String.valueOf(WATCH_KEY_ACCESS_BY_IP)).add(key, 1,
				watch.getAliveTime());
	}

	public String getWatchInfo(int key, int format) {

		// return all known report
		if (key == 0) {
			StringBuffer sb = new StringBuffer(1000);
			for (int i = 1; i < 5; i++) {
				sb.append(watchMap.get(String.valueOf(i)).toString(format));
				sb.append("\n<br>");
			}

			return sb.toString();
		}

		if (null == watchMap.get(String.valueOf(key))) {
			return "Unknow report key: " + key;
		}

		return watchMap.get(String.valueOf(key)).toString(format);

	}

	DecimalFormat format = new DecimalFormat("#,##0.### ms");
}
