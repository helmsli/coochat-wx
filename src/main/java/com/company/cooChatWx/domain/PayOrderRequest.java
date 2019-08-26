package com.company.cooChatWx.domain;

import java.util.Date;

import com.company.cooChatWx.utils.BaseDomain;
/**
 * 用于申请支付订单的ID和获取微信支付二维码
 * @author helmsli
 *
 */
public class PayOrderRequest extends BaseDomain {
	private String userId;
	private String appId;
	private int expireSeconds;
	private Date requestTime;
	private String content;//保存payRequestContent内容
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userid) {
		this.userId = userid;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public int getExpireSeconds() {
		return expireSeconds;
	}
	public void setExpireSeconds(int expireSeconds) {
		this.expireSeconds = expireSeconds;
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
	
}
