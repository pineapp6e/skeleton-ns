
package com.hesine.hichat.access.dao;

import java.util.List;

import com.hesine.hichat.access.model.NotifyRequest;

public interface NotifyDAO {

    int insertMessageBox(String msgId, String account, NotifyRequest notifyRequest);

    List<String> getDoctorIds(String hospitalId);

    int insertMessageQueue(String account, String msgId);
    
    int getQueueCnt(String account);
}
