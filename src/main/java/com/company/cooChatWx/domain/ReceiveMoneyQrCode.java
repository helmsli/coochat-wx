package com.company.cooChatWx.domain;

import java.util.Date;

import com.company.cooChatWx.utils.BaseDomain;
import com.company.cooChatWx.utils.Md5Encrypt;

public class ReceiveMoneyQrCode extends BaseDomain {
	public static final int RType_wechat = 101;
	public static final int RType_zhiFuBao = 102;
	
	public static final int STATUS_normal = 0;
	public static final int STATUS_waitPay = 1;
	
	public static final String Currency_RMB = "RMB";
	
	private String userId;// 生成QRcode的登录用户ID
	private String talker;// 生成Qrcode收款码的IMid
	private String amount;// 金额
	private String currency="RMB";
	private String remark;// 备注
	private String url;
	private int status=STATUS_normal;
	private String sign;
	private int rType;
	private Date expireTime;
	private int  remainSeconds;
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getTalker() {
		return talker;
	}

	public void setTalker(String talker) {
		this.talker = talker;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public int getrType() {
		return rType;
	}

	public void setrType(int rType) {
		this.rType = rType;
	}

	public Date getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}

	public String createSign(String key)
	{
		StringBuilder str = new StringBuilder();
		str.append(key);
		str.append(this.getAmount());
		str.append(this.getRemark());
		str.append(this.getrType());
		str.append(this.getTalker());	
		str.append(this.getCurrency());
		return Md5Encrypt.md5(str.toString()); 
	}
	public String createResponseKey()
	{
		StringBuilder str = new StringBuilder();
		str.append(this.getAmount());
		str.append(this.getRemark());
		str.append(this.getrType());
		str.append(this.getTalker());		
		return str.toString(); 
	}
	public String createRedisKey()
	{
		return this.remark;
	}

	public int getRemainSeconds() {
		return remainSeconds;
	}

	public void setRemainSeconds(int remainSeconds) {
		this.remainSeconds = remainSeconds;
	}
	
}
