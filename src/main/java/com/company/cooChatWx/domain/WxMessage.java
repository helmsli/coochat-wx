package com.company.cooChatWx.domain;

import com.company.cooChatWx.utils.BaseDomain;

/***
 * 微信接受的消息和发送消息已知的部分字段信息
 */
public class WxMessage extends BaseDomain{
	public static final int Type_text  = 1;
	public static final int Type_ImageFile  = 2;
	public static final int Type_VoiceFile  = 3;
	public static final int Type_VideoFile  = 4;
	/**
	 * 代理用户Id
	 */
	private String agentUserId;
    private String msgId;//微信消息ID
    private String msgSvrId;//消息服务器id

    private int type;//消息类型（1：文本，3：图片，34：音频，43：视频）
    private int status;//状态( 已读    未读)
    private int isSend;//是否自己发送，1为自己发送，0为收到的消息
    private String createTime;//消息创建时间
    private String talker;//与之聊天的唯一微信ID
    private String content;//消息内容，文本直接存在里面，图片，音频则为空
    private String imgPath;//图片路径，编码后的路径
    private String sign;//签名
    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getMsgSvrId() {
        return msgSvrId;
    }

    public void setMsgSvrId(String msgSvrId) {
        this.msgSvrId = msgSvrId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getIsSend() {
        return isSend;
    }

    public void setIsSend(int isSend) {
        this.isSend = isSend;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getTalker() {
        return talker;
    }

    public void setTalker(String talker) {
        this.talker = talker;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getAgentUserId() {
		return agentUserId;
	}

	public void setAgentUserId(String agentUserId) {
		this.agentUserId = agentUserId;
	}
    
}
