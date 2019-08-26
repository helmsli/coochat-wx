package com.company.cooChatWx.domain;

import com.company.cooChatWx.utils.BaseDomain;
/**
 * 用于申请支付订单ID的应答
 * @author helmsli
 *
 */
public class PayGetOrderIdRspContent extends BaseDomain {
	private String orderid;

	public String getOrderid() {
		return orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}
	
}
