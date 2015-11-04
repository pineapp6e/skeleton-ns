/**
 * 
 */
package com.hesine.hichat.access.service;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.access.bo.ChatOperationBO;
import com.hesine.hichat.access.common.EnumConstants;
import com.hesine.hichat.access.dao.UserDAO;
import com.hesine.hichat.access.handler.NotifyClientUtil;
import com.hesine.hichat.access.model.CustomerServiceStatus;
import com.hesine.hichat.model.AccountInfo;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.CsChatSummary;
import com.hesine.hichat.model.CurrentChatSummary;
import com.hesine.hichat.model.DispatchChatInfo;
import com.hesine.hichat.model.response.ChatStatistic;
import com.hesine.hichat.model.response.JoinNoticeMsg;
import com.hesine.util.DataAccessFactory;
import com.hesine.util.MessageUtil;

/**
 * @author pineapple
 *
 */
public class ClientChannelCache {

	private static Logger LOGGER = Logger.getLogger(ClientChannelCache.class);
	private static volatile long lastPrintTime = System.currentTimeMillis();
	private static long periodTime = TimeUnit.MILLISECONDS.convert(60,
			TimeUnit.SECONDS);
	public static String DEFAULT_SERVICE_GROUP_ID = "serviceGroupId";

	/**
	 * 按客户ID、channel 生成的键值对
	 */
	protected static volatile Map<String, CustomerServiceStatus> CLIENTID_CHANNEL_MAP = new ConcurrentHashMap<String, CustomerServiceStatus>();

	/**
	 * 在线客服队列，接待人数最少的客服在最前面
	 */
	protected static volatile Map<String, Queue<CustomerServiceStatus>> SERVICE_QUEUE_MAP = new ConcurrentHashMap<String, Queue<CustomerServiceStatus>>();
	/**
	 * 客服组的channel集合
	 */
	protected static volatile Map<String, ChannelGroup> CUSTOMER_SERVICE_GROUP_MAP = new ConcurrentHashMap<String, ChannelGroup>();

	/**
	 * 待接入会话
	 */
	protected static volatile Map<String, Queue<DispatchChatInfo>> DISPATCH_CHAT_MAP = new ConcurrentHashMap<String, Queue<DispatchChatInfo>>();

	
	/**
	 * 当日会话总人数
	 */
	protected static volatile Map<String, Set<String>> DAY_CHAT_PERSON_NUMBER = new ConcurrentHashMap<String,Set<String>>();
	
	/**
	 * 当日会话数
	 */
	protected static volatile Map<String, Set<String>> DAY_CHAT_NUMBER = new ConcurrentHashMap<String,Set<String>>();
	
	/**
	 * 今日消息数
	 */
	protected static volatile Map<String,Integer> DAY_MESSAGES_NUMBER = new ConcurrentHashMap<String, Integer>();
	
	/**
	 * 客服每日工作统计
	 */
	protected static volatile Map<String, Map<String,CsChatSummary>> DAY_RECEPTION_STATISTIC = new ConcurrentHashMap<String, Map<String, CsChatSummary>>();
	
	private static ChatOperationBO chatOperationBO = (ChatOperationBO) DataAccessFactory.dataHolder().get("chatOperationBO");

	private static UserDAO userDAO = (UserDAO) DataAccessFactory.dataHolder().get("userDAO");
	
	private static Lock lock = new ReentrantLock();

	public static void add(AccountInfo account,
			Channel socketChannel) {
		String serviceGroupId = account.getAppKey();
		String clientId = account.getUserId();
		CustomerServiceStatus css = CustomerServiceStatus.init(clientId,account.getUserType(),
				socketChannel);
		int inChantCnt = chatOperationBO.inchatCnt(clientId);
		css.setCurrentServiceNumber(inChantCnt);
		CLIENTID_CHANNEL_MAP.put(MessageUtil.generateServiceKey(serviceGroupId,clientId), css);
		addQueueMap(serviceGroupId, css, SERVICE_QUEUE_MAP);
		addCsStatistic(serviceGroupId, new CsChatSummary(clientId, inChantCnt));
		if (StringUtils.isNotEmpty(serviceGroupId)) {
			if (CUSTOMER_SERVICE_GROUP_MAP.containsKey(serviceGroupId)) {
				CUSTOMER_SERVICE_GROUP_MAP.get(serviceGroupId).add(
						socketChannel);
			} else {
				lock.lock();
				try {
					if (CUSTOMER_SERVICE_GROUP_MAP.containsKey(serviceGroupId)) {
						CUSTOMER_SERVICE_GROUP_MAP.get(serviceGroupId).add(
								socketChannel);
					} else {
						ChannelGroup recipients = new DefaultChannelGroup(
								"serviceGroup-" + serviceGroupId,
								GlobalEventExecutor.INSTANCE);
						recipients.add(socketChannel);
						CUSTOMER_SERVICE_GROUP_MAP.put(serviceGroupId,
								recipients);
					}
				} finally {
					lock.unlock();
				}
			}
		}

	}

	public static void addWaitChat(String groupId, DispatchChatInfo dci) {
		JoinNoticeMsg notifyMsg = MessageUtil.getJoinNotice(dci, ActionInfo.ACTION_ID_JOIN_NOTICE);
		NotifyClientUtil.notifyGroup(JSON.toJSONString(notifyMsg), groupId, null);
		addQueueMap(groupId, dci, DISPATCH_CHAT_MAP);
	}
	
	
	public static void notifyWait(String appKey, String serviceAccount) {
		Queue<DispatchChatInfo> waitQueue = DISPATCH_CHAT_MAP.get(appKey);
		if(CollectionUtils.isNotEmpty(waitQueue)){
			JoinNoticeMsg notifyMsg = MessageUtil.getJoinNotice(waitQueue, ActionInfo.ACTION_ID_JOIN_NOTICE);
			NotifyClientUtil.notifyBySocket(JSON.toJSONString(notifyMsg),
					MessageUtil.generateServiceKey(appKey, serviceAccount)
					);
		}
	}

	public static void addWaitChat(Map<String, List<DispatchChatInfo>> list) {
		for (Entry<String, List<DispatchChatInfo>> entry : list.entrySet()) {
			if (DISPATCH_CHAT_MAP.containsKey(entry.getKey())) {
				DISPATCH_CHAT_MAP.get(entry.getKey()).addAll(entry.getValue());
			} else {
				Queue<DispatchChatInfo> queue = new PriorityBlockingQueue<DispatchChatInfo>();
				DISPATCH_CHAT_MAP.put(entry.getKey(), queue);
				queue.addAll(entry.getValue());
			}
		}
	}
	
	public static void removeCustomerService(String appKey, String csAccount){
		String mapKey = MessageUtil.generateServiceKey(appKey, csAccount);
		CustomerServiceStatus css = CLIENTID_CHANNEL_MAP.get(mapKey);
		CLIENTID_CHANNEL_MAP.remove(mapKey);
		Queue<CustomerServiceStatus> serviceQueue = SERVICE_QUEUE_MAP.get(appKey);
		if(CollectionUtils.isNotEmpty(serviceQueue)){
			serviceQueue.remove(css);
		}
	}

	private static synchronized <T> void addQueueMap(String groupId, T css,
			Map<String, Queue<T>> map) {
		if (map.containsKey(groupId)) {
			map.get(groupId).offer(css);
		} else {
			Queue<T> queue = new PriorityBlockingQueue<T>();
			queue.offer(css);
			map.put(groupId, queue);
		}
	}

	public static Channel getClient(String key) {
		CustomerServiceStatus css = CLIENTID_CHANNEL_MAP.get(key);
		if (css != null) {
			return css.getChannel();
		}
		return null;
	}

	public static CustomerServiceStatus getCustomerServiceStatus(String clientId) {
		return CLIENTID_CHANNEL_MAP.get(clientId);
	}


	public static ChannelGroup getGroup(String serviceGroupId) {
		return CUSTOMER_SERVICE_GROUP_MAP.get(serviceGroupId);
	}
	
	public static Map<String,ChannelGroup> getCsGroup() {
		return Collections.unmodifiableMap(CUSTOMER_SERVICE_GROUP_MAP);
	}

	public static String currentCustomerServiceQueueStatus(){
		StringBuilder sb = new StringBuilder();
		for(Entry<String,Queue<CustomerServiceStatus>> cq : SERVICE_QUEUE_MAP.entrySet()){
			sb.append("------appKey:"+cq.getKey());
			sb.append("\n");
			for(CustomerServiceStatus css : cq.getValue()){
				sb.append("------");
				sb.append(css);
				sb.append("\n");
			}
			sb.append("\n\n");
		}
		return sb.toString();
	}
	
	
	public synchronized static void printStatus() {
		if (System.currentTimeMillis() - lastPrintTime > periodTime) {
			lastPrintTime = System.currentTimeMillis();
			LOGGER.info("online customer service:"
					+ CLIENTID_CHANNEL_MAP.size());
		}
	}

	public static synchronized CustomerServiceStatus peekMostIdleCustomerService(
			String key) {
		Queue<CustomerServiceStatus> queue = SERVICE_QUEUE_MAP.get(key);
		if (CollectionUtils.isNotEmpty(queue)) {
			return queue.peek();
		}
		return null;
	}

	public static void increaseCss(String key, CustomerServiceStatus css) {
		css.incrementCurrentServiceNumber();
		updateCustomerServiceQueue(key, css);
		Map<String,CsChatSummary> csMap = DAY_RECEPTION_STATISTIC.get(key);
		CsChatSummary ccs = csMap.get(css.getCsName());
		ccs.addUnfinishnum(1);
	}

	public static void decreaseCss(String key, CustomerServiceStatus css) {
		css.decrementCurrentServiceNumber();
		updateCustomerServiceQueue(key, css);
	}

	public static boolean updateCustomerServiceQueue(String key,
			CustomerServiceStatus css) {
		Queue<CustomerServiceStatus> queue = SERVICE_QUEUE_MAP.get(key);
		if (CollectionUtils.isNotEmpty(queue)) {
			queue.remove(css);
			return queue.offer(css);
		}
		return false;
	}

	public static void checkOfflineCs(String appKey, String serviceId,
			Channel channel) {
		String mapKey = MessageUtil.generateServiceKey(appKey, serviceId);
		LOGGER.info("remove customer service channel is " + serviceId+",appKey:"+appKey);
		if (CLIENTID_CHANNEL_MAP.containsKey(mapKey)
				&& CLIENTID_CHANNEL_MAP.get(mapKey).getChannel() == channel) {
			userDAO.updateState(appKey, serviceId, serviceId, EnumConstants.USER_STATE_OFFLINE);
			removeCustomerService(appKey, serviceId);
		}
	}
	
	public static synchronized void addChatPerson(String appKey, String userId){
		Set<String> chatPerson = DAY_CHAT_PERSON_NUMBER.get(appKey);
		if(!CollectionUtils.isEmpty(chatPerson)){
			chatPerson.add(userId);
		}else{
			chatPerson = new HashSet<String>();
			chatPerson.add(userId);
			DAY_CHAT_PERSON_NUMBER.put(appKey, chatPerson);
		}
	}
	
	public static synchronized void addChat(String appKey, String chatId){
		Set<String> chatIdSet = DAY_CHAT_NUMBER.get(appKey);
		if(!CollectionUtils.isEmpty(chatIdSet)){
			chatIdSet.add(chatId);
		}else{
			chatIdSet = new HashSet<String>();
			chatIdSet.add(chatId);
			DAY_CHAT_NUMBER.put(appKey, chatIdSet);
		}
	}
	

	public static void statisticData(){
		for(String serviceId : CUSTOMER_SERVICE_GROUP_MAP.keySet()){
			ChatStatistic cstat = new ChatStatistic();
			cstat.setActionId(ActionInfo.ACTION_ID_CHAT_STATISTIC);
			CurrentChatSummary chatSummary = new CurrentChatSummary();
			
			//当前等待人数
			Queue<DispatchChatInfo> queue = DISPATCH_CHAT_MAP.get(serviceId);
			chatSummary.setWaitPersonNum(collectionSize(queue));

			//今日会话总人数
			Set<String> chatPersonSet = DAY_CHAT_PERSON_NUMBER.get(serviceId);
			chatSummary.setChatPersonTodaySum(collectionSize(chatPersonSet));
			
			//今日会话数
			Set<String> chatSet = DAY_CHAT_NUMBER.get(serviceId);
			chatSummary.setChatTodaySum(collectionSize(chatSet));
			
			Integer cnt = DAY_MESSAGES_NUMBER.get(serviceId);
			//今日消息总数
			chatSummary.setMessageTodaySum(cnt == null?0:cnt);
			
			//在线客服数
			chatSummary.setOnlineCsNum(collectionSize(SERVICE_QUEUE_MAP.get(serviceId)));
			
			cstat.setChatSummary(chatSummary);
			int chatPersonNum = 0;
			Map<String, CsChatSummary> csChatSummaryCollection = DAY_RECEPTION_STATISTIC.get(serviceId);
			if(csChatSummaryCollection != null && csChatSummaryCollection.size()>0){
				cstat.setCsChatSummaryList(csChatSummaryCollection.values());
				for(CsChatSummary ccs : csChatSummaryCollection.values()){
					chatPersonNum+= ccs.getUnfinishNum();
				}
			}
			//当前对话人数
			chatSummary.setChatPersonNum(chatPersonNum);
			//未结束会话
			chatSummary.setUnfinishChatSum(chatPersonNum);
			
			NotifyClientUtil.notifyGroupByRole(JSON.toJSONString(cstat), serviceId, EnumConstants.ROLE_SERVICE_ADMIN);
		}
		
	}
	
	
	public static boolean isAdmin(String key){
		CustomerServiceStatus css = CLIENTID_CHANNEL_MAP.get(key);
		if(css == null){
			return false;
		}
		return css.getRole()==EnumConstants.ROLE_SERVICE_ADMIN;
	}
	
	private static int collectionSize(Collection<?> collection){
		if(CollectionUtils.isNotEmpty(collection)){
			return collection.size();
		}
		return 0;
	}

	public static synchronized void addMessageCnt(String appKey) {
		if(DAY_MESSAGES_NUMBER.containsKey(appKey)){
			DAY_MESSAGES_NUMBER.put(appKey, DAY_MESSAGES_NUMBER.get(appKey)+1);
		}else{
			DAY_MESSAGES_NUMBER.put(appKey,1);
		}
	}

	public static void clostChat(String appKey, Long chatId, String userId) {
		Map<String,CsChatSummary> csMap = DAY_RECEPTION_STATISTIC.get(appKey);
		if(csMap!=null){
			CsChatSummary ccs = csMap.get(userId);
			ccs.getFinishChatSet().add(chatId);
			String customerAccount = chatOperationBO.getCustomerAccount(chatId,userId);
			ccs.getReceptionPersonSet().add(customerAccount);
			ccs.decreasUnfinishUnm();
			LOGGER.info("close chat, update ccs data."+ccs.toString());
		}
	}
	
	public static synchronized void addCsStatistic(String appKey, CsChatSummary ccs) {
		Map<String, CsChatSummary> map = DAY_RECEPTION_STATISTIC.get(appKey);
		if(map==null){
			map = new HashMap<String,CsChatSummary>();
			DAY_RECEPTION_STATISTIC.put(appKey, map);
		}
		if(!map.containsKey(ccs.getUserId())){
			map.put(ccs.getUserId(), ccs);
		}
	}
	
	
	
	
}
