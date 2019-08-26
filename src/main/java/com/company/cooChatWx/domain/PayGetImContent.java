package com.company.cooChatWx.domain;

import java.util.Map;

import com.company.cooChatWx.utils.BaseDomain;

public class PayGetImContent extends BaseDomain {
	public static final int type_weChat = 1;
	public static final int type_zhiFuBao = 2;
	
	private String requestId;
	private String orderId;
	private String currency;
	private String amount;
	private int    type=type_weChat;//zhibubao  wechat
	private String remarks;//备注
	private Map<String,String> other=null;
	private String userId;
	private String appId;
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public Map<String, String> getOther() {
		return other;
	}
	public void setOther(Map<String, String> other) {
		this.other = other;
	}
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
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	
}
