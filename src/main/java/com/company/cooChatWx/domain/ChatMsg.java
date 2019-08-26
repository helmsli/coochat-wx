package com.company.cooChatWx.domain;

import java.util.Date;

import com.company.cooChatWx.utils.BaseDomain;

public class ChatMsg extends BaseDomain {
	
	public static final int MsgType_WeXin = 1;
	public static final int MsgType_ZhiFuBao = 2;
	
	private String userId;
	private int    messageType;
	private Date   requestTime;
	private String content;
	private String sign;
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Date getRequestTime() {
		return requestTime;
	}
	public void setRequestTime(Date requestTime) {
		this.requestTime = requestTime;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getMessageType() {
		return messageType;
	}
	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	
}
