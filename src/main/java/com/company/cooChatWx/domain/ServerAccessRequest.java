package com.company.cooChatWx.domain;

import com.company.cooChatWx.utils.BaseDomain;

public class ServerAccessRequest extends BaseDomain{
	private String userId;
	private String appId;
	private String notifyUrl;
	private int idType;
	private String idNo;
	private String otherJson;
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getNotifyUrl() {
		return notifyUrl;
	}
	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}
	public int getIdType() {
		return idType;
	}
	public void setIdType(int idType) {
		this.idType = idType;
	}
	public String getIdNo() {
		return idNo;
	}
	public void setIdNo(String idNo) {
		this.idNo = idNo;
	}
	public String getOtherJson() {
		return otherJson;
	}
	public void setOtherJson(String otherJson) {
		this.otherJson = otherJson;
	}
	
	
}
