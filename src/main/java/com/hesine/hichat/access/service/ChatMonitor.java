package com.hesine.hichat.access.service;

import static com.hesine.util.PropertiesUtil.getValue;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.access.common.EnumConstants;
import com.hesine.hichat.access.dao.ChatDAO;
import com.hesine.hichat.access.dao.ChatOperationBAO;
import com.hesine.hichat.access.handler.NotifyClientUtil;
import com.hesine.hichat.access.model.CustomerServiceStatus;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.CsChatSummary;
import com.hesine.hichat.model.DispatchChatInfo;
import com.hesine.hichat.model.response.JoinNoticeMsg;
import com.hesine.util.DataAccessFactory;
import com.hesine.util.MessageUtil;

public class ChatMonitor {
	private final static Logger logger = Logger.getLogger(ChatMonitor.class);

	private final static ChatOperationBAO chatOperationDAO = (ChatOperationBAO) DataAccessFactory
			.dataHolder().get("chatOperationDAO");

	private final static ChatDAO chatDAO = (ChatDAO) DataAccessFactory
			.dataHolder().get("chatDAO");

	public final static ExecutorService EXECUTOR = Executors
			.newFixedThreadPool(2);

	/**
	 * init the back service for monitor new chat. should executed when server
	 * started.
	 */

	public static void init() {
		monitor();
		statistic();
	}

	public static void monitor() {
		Map<String, List<DispatchChatInfo>> dispatchChat = chatOperationDAO
				.getDispatchChats();
		ClientChannelCache.addWaitChat(dispatchChat);
		EXECUTOR.execute(new Runnable() {
			@Override
			public void run() {
				try {
					while (!Thread.interrupted()) {
						TimeUnit.MILLISECONDS.sleep(100);
						if (!CollectionUtils
								.isEmpty(ClientChannelCache.DISPATCH_CHAT_MAP)) {
							for (Entry<String, Queue<DispatchChatInfo>> entry : ClientChannelCache.DISPATCH_CHAT_MAP
									.entrySet()) {
								if (!CollectionUtils.isEmpty(entry.getValue())) {
									for (DispatchChatInfo dci : entry
											.getValue()) {
										CustomerServiceStatus css = ClientChannelCache
												.peekMostIdleCustomerService(entry
														.getKey());
										if (css != null
												&& css.getCurrentServiceNumber() < css
														.getMaxServiceNumber()) {
											logger.info("most Idle cs:" + css);
											// 自动转给此客户
											// 会话转成打开状态。。
											chatOperationDAO.updateChatStatus(
													dci.getChatId(),
													EnumConstants.CHAT_STATUS_OPEN);
											chatOperationDAO.createChat(
													css.getCsName(),
													entry.getKey(),
													dci.getChatId(),
													getValue("service.account"),
													EnumConstants.CHAT_STATUS_OPEN);
											ClientChannelCache.increaseCss(
													entry.getKey(), css);
											entry.getValue().remove(dci);

											// 消息队列查出
											// save queue
											chatDAO.addMsgQueue(dci,
													css.getCsName(),
													entry.getKey());
											// 通知对应的客服
											com.hesine.hichat.model.response.Base newMsgNotice = MessageUtil
													.getSimpleResponse(
															0,
															ActionInfo.ACTION_ID_NEW_MSG_NOTICE);
											NotifyClientUtil.notifyBySocket(
													JSON.toJSONString(newMsgNotice),
													MessageUtil
															.generateServiceKey(
																	entry.getKey(),
																	css.getCsName()));

											// 通知其它客服此会话已经分配
											JoinNoticeMsg notifyMsg = MessageUtil
													.getJoinNotice(
															dci,
															ActionInfo.ACTION_ID_CANCEL_JOIN);
											NotifyClientUtil.notifyGroup(JSON
													.toJSONString(notifyMsg),
													entry.getKey(), null);
										} else {
											break;
										}
									}
								}
							}
						}
					}
				} catch (Exception e) {
					logger.error("Thread Name:"
							+ Thread.currentThread().getName() + ", "
							+ e.getMessage());
				} finally {
					run();
				}
			}
		});
	}

	public static void statistic() {
		Map<String, Set<String>> data = chatOperationDAO
				.getChatPersonMapByAppkey(false);
		if (data != null) {
			ClientChannelCache.DAY_CHAT_NUMBER.putAll(data);
		}
		data = chatOperationDAO.getChatPersonMapByAppkey(true);
		if (data != null) {
			ClientChannelCache.DAY_CHAT_PERSON_NUMBER.putAll(data);
		}
		Map<? extends String, ? extends Integer> stat = chatOperationDAO
				.getMessageCntDay();
		if (stat != null) {
			ClientChannelCache.DAY_MESSAGES_NUMBER.putAll(stat);
		}
		Map<? extends String, ? extends Map<String, CsChatSummary>> csStat = chatOperationDAO
				.getCsStatisticDay();
		if (csStat != null) {
			ClientChannelCache.DAY_RECEPTION_STATISTIC.putAll(csStat);
		}
		EXECUTOR.execute(new Runnable() {
			@Override
			public void run() {
				try {
					while (!Thread.interrupted()) {
						ClientChannelCache.statisticData();
						TimeUnit.MILLISECONDS.sleep(10000);
					}
				} catch (Exception e) {
					logger.error("Thread Name:"
							+ Thread.currentThread().getName() + ", "
							+ e.getMessage());
				} finally {
					run();
				}
			}
		});
	}

}