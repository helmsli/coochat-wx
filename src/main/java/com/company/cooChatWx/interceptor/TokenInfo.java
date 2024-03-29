package com.company.cooChatWx.interceptor;

import java.io.Serializable;

public class TokenInfo implements Serializable{
	/**
	 * pcweb登录
	 */
	public static final int loginType_web = 1;
	/**
	 * 移动端登录
	 */
	public static final int loginType_mobile = 2;
	/**
	 * 
	 */
	public static final int loginType_pad = 3;
	
	/**
	 * pc客户端
	 */
	public static final int loginType_pc = 4;
	
	private long createTime;
	private long userId;
	private boolean isExpire;
	/**
	 * 登录的渠道名字，PC/WEB/mobile/pad
	 */
	private int loginType;
	
	public static final int result_success=0;
	public static final int result_expireTime=1;
	public static final int result_loginOnOthersDevice=2;
	private int result=result_expireTime;
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getLoginType() {
		return loginType;
	}
	public void setLoginType(int loginType) {
		this.loginType = loginType;
	}
	@Override
	public String toString() {
		return "TokenInfo [createTime=" + createTime + ", userId=" + userId + ", loginType=" + loginType + "]";
	}
	public boolean isExpire() {
		return isExpire;
	}
	public void setExpire(boolean isExpire) {
		this.isExpire = isExpire;
	}
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	
}
