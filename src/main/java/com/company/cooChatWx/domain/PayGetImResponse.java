package com.company.cooChatWx.domain;

import com.company.cooChatWx.utils.BaseDomain;

/**
 * 用于申请Im的备注和ID的支付数据
 * @author helmsli
 *
 */
public class PayGetImResponse extends BaseDomain {
	private String userId;
	private String appId;
	private String sign;
	private PayOrderResponseContent responseContent;
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
	public PayOrderResponseContent getResponseContent() {
		return responseContent;
	}
	public void setResponseContent(PayOrderResponseContent responseContent) {
		this.responseContent = responseContent;
	}
	
}
