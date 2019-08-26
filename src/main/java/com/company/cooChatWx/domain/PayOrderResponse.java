package com.company.cooChatWx.domain;

import com.company.cooChatWx.utils.BaseDomain;

public class PayOrderResponse extends BaseDomain {
	private String userId;
	private String appId;
	private String sign;
	private String responseContent;
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
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public String getResponseContent() {
		return responseContent;
	}
	public void setResponseContent(String responseContent) {
		this.responseContent = responseContent;
	}
	
}
