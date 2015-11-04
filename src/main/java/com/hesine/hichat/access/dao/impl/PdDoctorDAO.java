/**
 * 
 */
package com.hesine.hichat.access.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.hesine.hichat.access.dao.DoctorDAO;
import com.hesine.hichat.model.AttachInfo;
import com.hesine.hichat.model.ChatInfo;
import com.hesine.hichat.model.DoctorStatusInfo;
import com.hesine.hichat.model.MessageInfo;
import com.hesine.util.AttachFileContext;

/**
 * @author pineapple
 *
 */
@Component("doctorDAO")
public class PdDoctorDAO extends BaseDAO implements DoctorDAO {

	private static final Logger logger = Logger.getLogger(PdDoctorDAO.class.getName());
	@Override
	public List<DoctorStatusInfo> getDoctorListWithParamMpa(
			final Map<String, Object> paramMap) {
		StringBuilder sb = new StringBuilder();
		
		String lastMsgPrefix = " SELECT"
				+"	b.*,a.*,c.*"
				+" FROM";
		String lastMsgSuffix = " b left outer join tb_msg_box_r a on a.msbo_user_account = b.doctorAccount"
				+" AND a.msbo_chat_id = b.chatId"
				+" AND a.msbo_create_time = b.latestChatTime"
				+" left outer join tb_msg_attach_r c ON a.msbo_id = c.msat_id"
				+" ORDER BY"
				+"	b.doctorAccount,"
				+"	b.chatId";
		
		String selectDoctor = "SELECT DISTINCT"
				+"						usex_user_account as doctorAccount"
				+"					FROM"
				+"						hi_auth.tb_user_extend_r"
				+"					WHERE"
				+ (paramMap.get("hospitalId")!=null?
						 "				usex_group_id = :hospitalId" :
						 "				usex_group_id in (:hospitalIds)");
		String selectChatlist = "	("
				+"		SELECT"
				+"			doctorAccount,"
				+ (paramMap.get("chattotalmsg")!=null?"			count(chatId) AS totalCnt,":"0 AS totalCnt,")
				+ (paramMap.get("chatunread")!=null?"			count(chatId) - sum(IFNULL(msbo_is_read,0)) AS unReadCnt,":"0 AS unReadCnt,")
				+ (paramMap.get("chatlasttime")!=null || paramMap.get("chatlastmsg")!=null?"			max(msbo_create_time) AS latestChatTime,":"0 AS latestChatTime,")
				+"			chatId"
				+"		FROM"
				+"			("
				+"				SELECT"
				+"					chme_user_account AS doctorAccount,"
				+"					chme_chat_id AS chatId"
				+"				FROM"
				+"					hi_chat.tb_chat_user_r"
				+ (paramMap.get("doctorId")!=null?
						" where chme_user_account = :doctorId":
						paramMap.get("doctorIds")!=null?" where chme_user_account in (:doctorIds)":
				 "				RIGHT OUTER JOIN("
				+"					SELECT DISTINCT"
				+"						usex_user_account"
				+"					FROM"
				+"						hi_auth.tb_user_extend_r"
				+"					WHERE"
				+ (paramMap.get("hospitalId")!=null?
						 "				usex_group_id = :hospitalId" :
						 "				usex_group_id in (:hospitalIds)")
				+"				)doctor_list_tmp ON usex_user_account = chme_user_account")
				+"			)tb_chat_doctor"
				+"		LEFT OUTER JOIN tb_msg_box_r ON tb_chat_doctor.chatId = msbo_chat_id"
				+"		AND	msbo_user_account = tb_chat_doctor.doctorAccount"
				+"		AND msbo_type = 0"
				+"		AND date_sub(curdate(), INTERVAL "+paramMap.get("maxDays")+" DAY)<= date(msbo_create_time)"
				+"		GROUP BY"
				+"			tb_chat_doctor.doctorAccount,"
				+"			tb_chat_doctor.chatId"
				+"	)";
		if(paramMap.get("chatlist")!=null){
			if(paramMap.get("chatlastmsg")!=null){
				sb.append(lastMsgPrefix);
				sb.append(selectChatlist);
				sb.append(lastMsgSuffix);
			}else{
				sb.append(selectChatlist);
			}
		}else{
			sb.append(selectDoctor);
		}
		logger.info("doctorList sql:"+sb.toString());
		List<DoctorStatusInfo>  resultList = this.getNamedParameterJdbcTemplate().query(sb.toString(), paramMap, new ResultSetExtractor<List<DoctorStatusInfo>>(){
			@Override
			public List<DoctorStatusInfo> extractData(ResultSet rs)
					throws SQLException, DataAccessException {
				List<DoctorStatusInfo>  resultList = new ArrayList<DoctorStatusInfo>();
				String preDoctorAccount = null;
				DoctorStatusInfo doctorStatus = null; 
				while(rs.next()){
					String currentDoctorAccount = rs.getString("doctorAccount");
					if(!currentDoctorAccount.equals(preDoctorAccount)){
						preDoctorAccount = currentDoctorAccount;
						doctorStatus = new DoctorStatusInfo();
						doctorStatus.setDoctor(currentDoctorAccount);
						resultList.add(doctorStatus);
					}
					if(paramMap.get("chatlist")!=null){
						/**
						 * 创建chatStatusList对象
						 */
						if(doctorStatus.getChatStatusList()==null){
							List<ChatInfo> chatList = new ArrayList<ChatInfo>();
							doctorStatus.setChatStatusList(chatList);
						}
						ChatInfo chatStatus = new ChatInfo();
						doctorStatus.getChatStatusList().add(chatStatus);
						chatStatus.setChatId(rs.getLong("chatId"));
						if(paramMap.get("chatlastmsg")!=null){
							MessageInfo chatLastMsg = new MessageInfo();
	                        chatLastMsg.setMsgId(rs.getString("msbo_id"));
	                        chatLastMsg.setChatId(rs.getLong("msbo_chat_id"));
	                        chatLastMsg.setType(rs.getInt("msbo_type"));
	                        chatLastMsg.setFrom(rs.getString("msbo_from"));
	                        chatLastMsg.setTo(rs.getString("msbo_to"));
	                        chatLastMsg.setSubject(rs.getString("msbo_subject"));
	                        chatLastMsg.setBody(rs.getString("msbo_content"));
	                        chatLastMsg.setTime(rs.getTimestamp("msbo_create_time").getTime());
	                        chatLastMsg.setAttachmentMark(rs.getBoolean("msbo_attach_mark"));
	                        if (chatLastMsg.isAttachmentMark() && StringUtils.isNotEmpty(rs.getString("msat_id"))) {
	                            AttachInfo attachInfo = new AttachInfo();
	                            attachInfo.setType(rs.getInt("msat_type"));
	                            attachInfo.setName(rs.getString("msat_name"));
	                            attachInfo.setSize(rs.getInt("msat_size"));
	                            attachInfo.setUrl(rs.getString("msat_url"));
	                            attachInfo.setMd5(rs.getString("msat_md5"));
	                            attachInfo.setAttachment(rs.getString("msat_content"));
            					String attName = attachInfo.getName();
            					logger.info("initial attach name:" + attName + ", attType:"
            							+ attachInfo.getType());
            					String attUrl = attachInfo.getUrl();
            					attUrl = attUrl
            							.substring(AttachFileContext.ATTACH_URL_PREFIX
            									.length());
            					attUrl = attUrl.substring(0,
            							attUrl.length() - attName.length());
            					if (attachInfo.getType() == AttachInfo.ATTACH_VIDEO) {
            						attName = attName.substring(0, attName.indexOf("."))
            								.concat(".jpg");
            					}
            					String attachContent = AttachFileContext.getAttachContent(
            							attUrl, attName, null);
            					attachInfo.setAttachment(attachContent);
	                            chatLastMsg.setAttachInfo(attachInfo);
	                        }
							chatStatus.setChatLastMsg(chatLastMsg);
						}
						chatStatus.setChatLastTime(rs.getLong("latestChatTime"));
						chatStatus.setChatTotalMsg(rs.getInt("totalCnt"));
						chatStatus.setChatUnread(rs.getInt("unReadCnt"));
					}
				}
				return resultList;
			}
		});
		return resultList;
	}
}
