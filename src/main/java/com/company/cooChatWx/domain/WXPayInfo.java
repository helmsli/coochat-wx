
package com.company.cooChatWx.domain;

import org.apache.commons.lang3.StringUtils;

import com.company.cooChatWx.utils.BaseDomain;

public class WXPayInfo extends BaseDomain {
    private String transId;
    private String fromTalkerId;//对方的微信ID
    private String toTalkerId;//接受方的微信ID
    private String amount="";//金额
    private String currency="RMB";
    private String remark="";//备注
    private String createTime;//时间
    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public String getFromTalkerId() {
        return fromTalkerId;
    }

    public void setFromTalkerId(String fromTalkerId) {
        this.fromTalkerId = fromTalkerId;
    }

    public String getToTalkerId() {
        return toTalkerId;
    }

    public void setToTalkerId(String toTalkerId) {
        this.toTalkerId = toTalkerId;
    }

   

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getAmount() {
		if(StringUtils.isEmpty(amount))
		{
			return "";
		}
		return amount.trim();
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
	
	public String createResponseKey()
	{
		StringBuilder str = new StringBuilder();
		str.append("0");
		str.append(this.getAmount());
		str.append(this.getTransId());
		str.append(this.getRemark());
		str.append(this.getFromTalkerId());
		return str.toString();
	}
	
    
}
