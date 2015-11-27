/**
 * 
 */
package com.papple.ws.test.model;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author pineapple
 *
 */
public class ClientChannelMap {

	private static Logger LOGGER = Logger.getLogger(ClientChannelMap.class);
	private static volatile long lastPrintTime = System.currentTimeMillis();
	private static long periodTime = TimeUnit.MILLISECONDS.convert(60,
			TimeUnit.SECONDS);
	
	public final static String DEFAULT_GROUP = "DEFAULT";

	/**
	 * 按医院分组的channel集合
	 */
	public static volatile Map<String, ChannelGroup> CLIENT_GROUP_MAP = new ConcurrentHashMap<String, ChannelGroup>();

	private static Lock lock = new ReentrantLock();

	public static void add(String groupId,
			Channel socketChannel) {
		if (StringUtils.isNotEmpty(groupId)) {
			if (CLIENT_GROUP_MAP.containsKey(groupId)) {
				CLIENT_GROUP_MAP.get(groupId).add(socketChannel);
			} else {
				lock.lock();
				try {
					if (CLIENT_GROUP_MAP.containsKey(groupId)) {
						CLIENT_GROUP_MAP.get(groupId).add(socketChannel);
					} else {
						ChannelGroup recipients = new DefaultChannelGroup(
								"group-" + groupId,
								GlobalEventExecutor.INSTANCE);
						recipients.add(socketChannel);
						CLIENT_GROUP_MAP.put(groupId, recipients);
					}
				} finally {
					lock.unlock();
				}
			}
		}

	}

	public synchronized static void printStatus() {
		if (System.currentTimeMillis() - lastPrintTime > periodTime) {
			lastPrintTime = System.currentTimeMillis();
			LOGGER.info("online client:" + clientCnt());
		}
	}
	
	public static int clientCnt(){
		int cnt = 0;
		for(Entry<String,ChannelGroup> entry :CLIENT_GROUP_MAP.entrySet()){
			cnt+=entry.getValue().size();
		}
		return cnt;
	}
	
}
