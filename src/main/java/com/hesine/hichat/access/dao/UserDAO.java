package com.hesine.hichat.access.dao;

import com.hesine.hichat.model.AccountInfo;

public interface UserDAO {
	public AccountInfo getAccountInfo(String appKey,String userAccount);
	
	public int updateState(String appKey, String userAccount, String deviceToken, int state);
}
