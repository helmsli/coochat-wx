package com.company.cooChatWx.domain;

import java.util.List;

import com.company.cooChatWx.utils.BaseDomain;

public class ConfigReceiveMoneyImUser extends BaseDomain {
	private String userId;
	private String appId;
	private String type;
	private List<String> userList;
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<String> getUserList() {
		return userList;
	}
	public void setUserList(List<String> userList) {
		this.userList = userList;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	
}
