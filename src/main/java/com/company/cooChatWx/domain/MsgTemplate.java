package com.company.cooChatWx.domain;

import com.company.cooChatWx.utils.BaseDomain;

public class MsgTemplate extends BaseDomain {
	
	private String category;
	private int    messageType;
	//消息关键字
	private String msgKeyword;
	//通知地址
	private String notifyUrl;
	private String callMethod;
	//收款金额关键字
	private String receiveMoneyKey="";
	//收款备注关键字
	private String receiveMarkKey="";
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	
	public int getMessageType() {
		return messageType;
	}
	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}
	public String getMsgKeyword() {
		return msgKeyword;
	}
	public void setMsgKeyword(String msgKeyword) {
		this.msgKeyword = msgKeyword;
	}
	public String getNotifyUrl() {
		return notifyUrl;
	}
	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}
	public String getCallMethod() {
		return callMethod;
	}
	public void setCallMethod(String callMethod) {
		this.callMethod = callMethod;
	}
	public String getReceiveMoneyKey() {
		return receiveMoneyKey;
	}
	public void setReceiveMoneyKey(String receiveMoneyKey) {
		this.receiveMoneyKey = receiveMoneyKey;
	}
	public String getReceiveMarkKey() {
		return receiveMarkKey;
	}
	public void setReceiveMarkKey(String receiveMarkKey) {
		this.receiveMarkKey = receiveMarkKey;
	}
	
}
