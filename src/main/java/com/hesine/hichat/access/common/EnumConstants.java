package com.hesine.hichat.access.common;

import com.hesine.util.PropertiesUtil;

public class EnumConstants {

	public static final int ACTION_ID_HEART_BEAT = 999; // heart beat

	/**
	 * content type
	 */
	public final static String PLAIN_CONTENT_TYPE = "text/plain; charset=UTF-8";
	public final static String HTML_CONTENT_TYPE = "text/html; charset=UTF-8";

	/**
	 * chat_user chat status
	 */
	public final static int CHAT_STATUS_OPEN = 0;
	public final static int CHAT_STATUS_CLOSE = 1;
	public final static int CHAT_STATUS_DISPATCH = 2;
	public final static int CHAT_STATUS_TRANSFER = 3;

	/**
	 * notify error id and info
	 */
	public static final int NOTIFY_ERROR_ACCOUNTID = 1;
	public static final int NOTIFY_ERROR_INSERTMESSAGE = 2;
	public static final int NOTIFY_ERROR_REQPARAM_EMPTY = 3;
	public static final int NOTIFY_ERROR_DOCTORS_EMPTY = 4;
	public static final int NOTIFY_ERROR_GETDOCTORS = 5;
	
	/**
	 * all request error id and info 
	 */
	public static final int HICHAT_ERROR_EXCEPTION_SERVER = 1;
	public static final int HICHAT_ERROR_SERVER = 2;
	public static final int HICHAT_ERROR_USER = 3;
	public static final int HICHAT_ERROR_PARAM = 4;

	/**
	 * auth account user type
	 */
	public static final int AUTH_ACCOUNT_USERTYPE_PATIENT = 0;
	public static final int AUTH_ACCOUNT_USERTYPE_DOCTOR = 1;

	/**
	 * auth account terminal type
	 */
	public static final int AUTH_ACCOUNT_TERMINALTYPE_MOBILEPHONE = 0;
	public static final int AUTH_ACCOUNT_TERMINALTYPE_WEB = 1;
	public static final int AUTH_ACCOUNT_TERMINALTYPE_PC = 2;

	/**
	 * message is read or not 0 : not read 1 : have read
	 */
	public static final int MESSAGE_NOT_READ = 0;
	public static final int MESSAGE_HAVE_READ = 1;

	/**
	 * PN TYPE
	 */
	public static final byte HPNS = 0;
	public static final byte APNS = 1;
	public static final byte GCM = 2;

	/**
	 * notify type
	 */
	public static final byte UNICAST = 0;
	public static final byte BROADCAST = 1;
	public static final byte BROAD_APP = 2;
	
	/**
     * default keep time(minute) in PN Server when mobile isn't online.
     */
    public static final int MAX_KEEP_TIME_IN_PN = 60 * 24 * 7;
    
    /**
     * message type 
     * 0: 普通消息
     * 1: 通知消息
     */
    public static final byte MESSAGE_TYPE_SIMPLE = 0;
	public static final byte MESSAGE_TYPE_NOTIFY = 1;
	
	 /**
     * minimum file size to compression.
     */
    public static final int COMPRESSION_STANDARD = PropertiesUtil.getIntValue("compression.standard");
    
    /**
     * 用户上、下线标识
     * 0： 下线
     * 1： 上线
     */
    public static final byte USER_STATE_OFFLINE = 0;
    public static final byte USER_STATE_ONLINE = 1;
    
    /**
     * 用户连接方式
     * 0: 手机端
     * 1: 网页端
     */
    public static final int CONNECT_TYPE_FROM_MOBILE = 0;
    public static final int CONNECT_TYPE_FROM_WEB = 1;
    
    /**
     * 默认取历史消息为30
     */
    public static final int HISTORY_MESSAGES_DEFAULT = 30;
    
    /**
     * 消息已读、未读状态
     * 0： 未读
     * 1： 已读
     */
    public static final byte MSG_UNREAD = 0;
    public static final byte MSG_READED = 1;
    
    /**
     * 角色
     * 0: 用户， 1：服务号， 2：客服， 3：管理员
     */
    public static final byte ROLE_CUSTOMER = 0; 
    public static final byte ROLE_SERVICE_NUMBER = 1;
    public static final byte ROLE_CUSTOMER_SERVICE = 2;
    public static final byte ROLE_SERVICE_ADMIN = 3;
}
