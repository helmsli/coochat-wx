package com.company.cooChatWx.domain;

public class RestErrorCode {
	private static final int Error_start = 4000;
	public static final int Error_privateKeyIsNull =Error_start+1;
	public static final int Error_cheatMsg =Error_start+2;
	public static final int Error_Md5Msg =Error_start+3;
	public static final int Error_NoReceiveQrCode =Error_start+4;
	public static final int Error_requestOrderId =Error_start+5;
	/**
	 * remark not found
	 */
	public static final int Error_invalidRemark =Error_start+6;
	
	
}	
