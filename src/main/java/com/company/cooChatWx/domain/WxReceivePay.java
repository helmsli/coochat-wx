package com.company.cooChatWx.domain;
/**
 * 用付款码收到的信息提示；
 * @author helmsli
 *
 */
public class WxReceivePay extends ChatMsg{
	private String source;
    private String description;
    private String md5Key;
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getMd5Key() {
		return md5Key;
	}
	public void setMd5Key(String md5Key) {
		this.md5Key = md5Key;
	}
    
}
