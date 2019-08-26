package com.company.cooChatWx.domain;

import java.util.HashMap;
import java.util.Map;

import com.company.cooChatWx.utils.BaseDomain;

public class ClientCreateQrCode extends BaseDomain {
	Map<String,String> parms = new HashMap<String,String>();
	private String sign = "";
	public Map<String, String> getParms() {
		return parms;
	}
	public void setParms(Map<String, String> parms) {
		this.parms = parms;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	
}
