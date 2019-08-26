package com.company.cooChatWx.message.service;

import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.company.cooChatWx.controller.rest.ControllerUtils;
import com.company.cooChatWx.domain.ChatMsg;
import com.company.cooChatWx.domain.MessageConst;
import com.company.cooChatWx.domain.MsgTemplate;
import com.company.cooChatWx.domain.RestErrorCode;
import com.company.cooChatWx.domain.WxMessage;
import com.company.cooChatWx.orderService.OrderClientService;
import com.company.cooChatWx.security.utils.RSAUtils;
import com.company.cooChatWx.security.utils.SecurityKeyServiceImpl;
import com.xinwei.nnl.common.domain.JsonRequest;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.nnl.common.util.JsonUtil;
import com.xinwei.orderDb.domain.OrderMainContext;
import com.xinwei.userOrder.domain.UserOrder;
@Service("wxMessageService")
public class WxMessageService implements InitializingBean{
	
	@Value("${chat.receiveMsg.template}")
	private String receiveMsgTemplate;
	
	Map<String,MsgTemplate> msgTemplateMap = new HashMap<String,MsgTemplate>();
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private OrderClientService orderClientService;
	
	@Autowired
	private AsynNotifyService asynNotifyService;
	
	@Autowired
	private SecurityKeyServiceImpl securityKeyServiceImpl;
	public ProcessResult processReceiveMsg(ChatMsg chatMsg)
	{
		
		PrivateKey privateKey= securityKeyServiceImpl.getPrivatekey(chatMsg.getUserId());
		if(privateKey==null)
		{
			return ControllerUtils.getErrorResponse(RestErrorCode.Error_privateKeyIsNull, "private key is null");
		}
		String content = RSAUtils.decrypt(chatMsg.getContent(), privateKey);
		WxMessage wxMessage = JsonUtil.fromJson(content, WxMessage.class);
		if(wxMessage.getAgentUserId().compareToIgnoreCase(chatMsg.getUserId())!=0)
		{
			logger.error("cheat msg userId not equal");
			return ControllerUtils.getErrorResponse(RestErrorCode.Error_cheatMsg, "cheat msg");
		}
		logger.debug(wxMessage.toString());
		for(String key :msgTemplateMap.keySet())
		{
			MsgTemplate msgTemplate = msgTemplateMap.get(key);
		
			if(msgTemplate.getMessageType()!=chatMsg.getMessageType())
			{
				continue;
			}
			if(!wxMessage.getContent().contains(msgTemplate.getMsgKeyword()))
			{
				continue;
			}
	
			OrderMainContext orderMainContext= new OrderMainContext();
			orderMainContext.setCatetory(msgTemplate.getCategory());
			
			Map<String,String>contextDatas = new HashMap<String,String>();
			contextDatas.put("type", String.valueOf(chatMsg.getMessageType()));
			contextDatas.put("receiveTime", String.valueOf(System.currentTimeMillis()));
			contextDatas.put("msg", content);
			orderMainContext.setContextDatas(contextDatas);
			ProcessResult ret = this.orderClientService.createOrder(orderMainContext);
			if(ret.getRetCode()!=0)
			{
				return ret;
			}
			ret = this.orderClientService.startOrder(orderMainContext.getCatetory(), orderMainContext.getOrderId());
			if(ret.getRetCode()!=0)
			{
				return ret;
			}
			asynNotifyService.asyncProcess(msgTemplate.getNotifyUrl(),msgTemplate.getCategory(), orderMainContext.getOrderId(),msgTemplate.getCallMethod());
		}
		return ControllerUtils.getSuccessResponse(null);
	
		
	}
	
	
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		String[] springStr  = StringUtils.split(receiveMsgTemplate, ";");
		for(int i=0;i<springStr.length;i++)
		{
			String[] contents = StringUtils.split(springStr[i],",");
			MsgTemplate msgTemplate = new MsgTemplate();
			msgTemplate.setCategory(contents[0]);
			msgTemplate.setMessageType(Integer.parseInt(contents[1]));
			msgTemplate.setMsgKeyword(contents[2]);
			msgTemplate.setNotifyUrl(contents[3]);
			try {
				msgTemplate.setCallMethod(contents[4]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				msgTemplate.setCallMethod("callMethod");
			}
			msgTemplateMap.put(msgTemplate.getMsgKeyword(), msgTemplate);
		}
	}
}
