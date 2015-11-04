package com.hesine.hichat.access.bo.impl;

import static com.hesine.hichat.access.common.EnumConstants.NOTIFY_ERROR_ACCOUNTID;
import static com.hesine.hichat.access.common.EnumConstants.NOTIFY_ERROR_INSERTMESSAGE;
import static com.hesine.hichat.access.common.EnumConstants.NOTIFY_ERROR_REQPARAM_EMPTY;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hesine.hichat.access.bo.NotifyBO;
import com.hesine.hichat.access.dao.NotifyDAO;
import com.hesine.hichat.access.model.NotifyRequest;
import com.hesine.hichat.model.ReqParam;

@Component("notifyBO")
public class NotifyBOImpl implements NotifyBO {
    private static Logger logger = Logger.getLogger(NotifyBOImpl.class);

    @Autowired
    private NotifyDAO notifyDAO;

    @Override
    public int notifyProcess(String account, NotifyRequest notifyRequest) {
        if (account == null || account.isEmpty()) {
            return NOTIFY_ERROR_ACCOUNTID;
        }

        //String msgId = MessageUtil.getMessageId(account);
        String msgId="";
        logger.info("notify message id : " + msgId);
        // 插入消息
        int ret = notifyDAO.insertMessageBox(msgId, account, notifyRequest);
        if (ret != 1) {
            return NOTIFY_ERROR_INSERTMESSAGE;
        }

        // 查询医生IDs
        Result result = getDoctorIds(notifyRequest.getReqParam());
        if (result.getRet() != 0) {
            return result.getRet();
        }
        // 插入消息队列
        logger.info("doctor accounts : " + result.getList().toString());
        for (String doctor : result.getList()) {
            notifyDAO.insertMessageQueue(doctor, msgId);
        }
        // 通知其他医生
//        for (String doctor : result.getList()) {
//          //  NotifyClientUtil.notifyBySocket(NotifyClientUtil.buildNotifyData(), doctor);
//        }

        return 0;
    }

    private Result getDoctorIds(ReqParam reqParam) {
        Result result = new Result();
//        List<String> doctorList = new ArrayList<String>();

        result.setRet(NOTIFY_ERROR_REQPARAM_EMPTY);
        return result;
    }

}

class Result {
    private int ret;
    List<String> list;

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

}
