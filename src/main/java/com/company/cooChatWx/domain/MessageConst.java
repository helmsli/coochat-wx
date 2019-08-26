package com.company.cooChatWx.domain;

public class MessageConst {
	/**
	 * 微信的接收消息
	 */
	public static final String category_receiveWxMsg = "receiveWxMsg";
	/**
	 * 创建付款码的订单
	 */
	public final static String createQrCodeCategory = "cRmoneyQrcode";
	/**
	 * 根据收款码的remark生成订单；
	 */
	public final static String im_remark_category = "cImPayRemark";
	
	
	/**
	 * 第三方服务申请支付订单号
	 */
	public final static String requestPayOrder = "imPayOrder";
	
	
	
	public final static String context_key_request ="context_key_request";
	public final static String context_key_content ="content";
	public final static String context_key_sign ="md5Key";
	
	public final static String context_key_payResult ="payResult:";
	
	/**
	 * 首款码详细信息
	 */
	public final static String context_key_remarkInfo ="remark:";
	
	

}
