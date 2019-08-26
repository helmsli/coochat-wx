package com.company.cooChatWx.message.service;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.company.cooChatWx.domain.ReceiveMoneyQrCode;
import com.company.cooChatWx.domain.RestErrorCode;
import com.company.cooChatWx.domain.WXPayInfo;
import com.company.cooChatWx.domain.WxMessage;
import com.company.cooChatWx.domain.WxReceivePay;
import com.company.cooChatWx.orderService.OrderClientService;
import com.company.cooChatWx.security.utils.RSAUtils;
import com.company.cooChatWx.security.utils.SecurityKeyServiceImpl;
import com.company.cooChatWx.utils.Md5Encrypt;
import com.xinwei.nnl.common.domain.JsonRequest;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.nnl.common.util.JsonUtil;
import com.xinwei.orderDb.domain.OrderMainContext;
import com.xinwei.userOrder.domain.UserOrder;

@Service("wxPayService")
public class WxPayService implements InitializingBean {

	@Value("${chat.wxpayMsg.template}")
	private String receiveMsgTemplate;

	
	
	
	@Value("${chat.wxpayMsg.md5Key:payMsg!$@#}")
	private String wxPayMsgMd5Key;
	
	

	Map<String, MsgTemplate> msgTemplateMap = new HashMap<String, MsgTemplate>();
	
	

	protected Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private OrderClientService orderClientService;

	@Autowired
	private AsynNotifyService asynNotifyService;

	@Autowired
	private SecurityKeyServiceImpl securityKeyServiceImpl;

	@Autowired
	private RMoneyQrCodeService rMoneyQrCodeService;

	/**
	 * 收到微信收款通知
	 * @param chatMsg
	 * @return
	 */
	public ProcessResult processReceiveMsg(WxReceivePay chatMsg) {
		logger.debug("userId:" + chatMsg.getUserId());
		PrivateKey privateKey = securityKeyServiceImpl.getPrivatekey(chatMsg.getUserId());
		if (privateKey == null) {
			return ControllerUtils.getErrorResponse(RestErrorCode.Error_privateKeyIsNull, "private key is null");
		}
		String content = RSAUtils.decrypt(chatMsg.getContent(), privateKey);
		if (StringUtils.isEmpty(content)) {
			return ControllerUtils.getErrorResponse(RestErrorCode.Error_privateKeyIsNull, "private key is");
		}
		WXPayInfo wXPayInfo = JsonUtil.fromJson(content, WXPayInfo.class);

		StringBuilder strb = new StringBuilder();
		strb.append(content);
		strb.append(chatMsg.getSource());
		strb.append(chatMsg.getDescription());
		String correntMd5 = Md5Encrypt.md5(strb.toString());
		if (correntMd5.compareToIgnoreCase(chatMsg.getMd5Key()) != 0) {
			return ControllerUtils.getErrorResponse(RestErrorCode.Error_Md5Msg, "Md5 error");
		}
		
		
		logger.debug(wXPayInfo.toString());
		logger.debug(chatMsg.toString());
		for (String key : msgTemplateMap.keySet()) {
			MsgTemplate msgTemplate = msgTemplateMap.get(key);

			if (msgTemplate.getMessageType() != chatMsg.getMessageType()) {
				logger.error("template:"+msgTemplate.toString());
				continue;
			}
			logger.debug("receive template:" +msgTemplate.toString());
			
			//微信
			if(StringUtils.isEmpty(wXPayInfo.getAmount()))
			{
				String desc = chatMsg.getDescription();
				String[] descContent = StringUtils.split(desc, "\n");
				for(int i=0;i<descContent.length;i++)
				{
					
					logger.debug("descContent:" + descContent[i]);
					if(!StringUtils.isEmpty(msgTemplate.getReceiveMoneyKey()))
					{
						logger.debug("key:" + msgTemplate.getReceiveMoneyKey());
						if(descContent[i].startsWith(msgTemplate.getReceiveMoneyKey()))
						{
							String amount = descContent[i].substring(msgTemplate.getReceiveMoneyKey().length());
							wXPayInfo.setAmount(amount);
						}
					}
					if(!StringUtils.isEmpty(msgTemplate.getReceiveMarkKey()))
					{
						logger.debug("markkey:" + msgTemplate.getReceiveMarkKey());
						if(descContent[i].startsWith(msgTemplate.getReceiveMarkKey()))
						{
							String remark = descContent[i].substring(msgTemplate.getReceiveMarkKey().length());
							wXPayInfo.setRemark(remark);
						}
					}
					
				}
			}
			logger.debug("final wxPayInfo:" + wXPayInfo.toString());
			String remarkOrderId = this.rMoneyQrCodeService.getRemarkOrder(chatMsg.getMessageType(),
					wXPayInfo.getRemark());
			List<String> keys = new ArrayList();
			keys.add(MessageConst.context_key_remarkInfo);
			ProcessResult ret = this.orderClientService.getContextData(MessageConst.im_remark_category, remarkOrderId,
					keys);
			if (ret.getRetCode() != 0) {
				logger.error("not found context_key_remarkInfo");
				return ret;
			}
			Map<String, String> retMap = (Map<String, String>) ret.getResponseInfo();
			if (!retMap.containsKey(MessageConst.context_key_remarkInfo)) {
				//保存订单到数据库
				UserOrder errorRemarkUserOrder = new UserOrder();
				errorRemarkUserOrder.setCategory("errorRemark");
				errorRemarkUserOrder.setUserId("00000000");
				errorRemarkUserOrder.setOrderId(wXPayInfo.getToTalkerId());
				errorRemarkUserOrder.setOrderData(JsonUtil.toJson(wXPayInfo));
				ret = this.orderClientService.saveUserOrder(null, errorRemarkUserOrder);
				if(ret.getRetCode()==0)
				{
					logger.error("not found remark:" + wXPayInfo.toString());
					return ControllerUtils.getSuccessResponse(null);
				}
				else
				{
					return ControllerUtils.getErrorResponse(RestErrorCode.Error_invalidRemark, "not find :" + MessageConst.context_key_remarkInfo);
				}
			}
			String orderId = retMap.get(MessageConst.context_key_remarkInfo);
			//
			retMap.clear();
			retMap.put(MessageConst.context_key_payResult, JsonUtil.toJson(wXPayInfo));
			retMap.put( MessageConst.context_key_sign+MessageConst.context_key_payResult,
					Md5Encrypt.md5(JsonUtil.toJson(wXPayInfo), this.wxPayMsgMd5Key));
			ret = this.orderClientService.putContextData(MessageConst.requestPayOrder, orderId, retMap);
			asynNotifyService.asyncProcess(msgTemplate.getNotifyUrl(), msgTemplate.getCategory(), orderId,
					msgTemplate.getCallMethod());
			if (ret.getRetCode() == 0) {
				ret.setRetMsg(RSAUtils.sign(wXPayInfo.createResponseKey(), privateKey));
			}
			else
			{
				logger.error("error1:"+ ret.toString());
			}
			return ret;

		}
		return ControllerUtils.getErrorResponse(-1, "not found");

	}
	
	
	/**
	 * 创建Qrcode的接口
	 * 
	 * @param chatMsg
	 * @return
	 */
	public ProcessResult processReceiveMoneyQrcodeMsg(ChatMsg chatMsg) {
		logger.debug("userId:" + chatMsg.getUserId());
		PrivateKey privateKey = securityKeyServiceImpl.getPrivatekey(chatMsg.getUserId());
		if (privateKey == null) {
			return ControllerUtils.getErrorResponse(RestErrorCode.Error_privateKeyIsNull, "private key is null");
		}
		String content = RSAUtils.decrypt(chatMsg.getContent(), privateKey);
		if (StringUtils.isEmpty(content)) {
			return ControllerUtils.getErrorResponse(RestErrorCode.Error_privateKeyIsNull, "private key is");
		}
		ReceiveMoneyQrCode receiveMoneyQrCode = JsonUtil.fromJson(content, ReceiveMoneyQrCode.class);
		receiveMoneyQrCode.setSign(receiveMoneyQrCode.createSign(this.wxPayMsgMd5Key));
		logger.debug(receiveMoneyQrCode.toString());
		logger.debug(chatMsg.toString());
		OrderMainContext orderMainContext = new OrderMainContext();
		orderMainContext.setCatetory(MessageConst.createQrCodeCategory);

		Map<String, String> contextDatas = new HashMap<String, String>();
		contextDatas.put("type", String.valueOf(chatMsg.getMessageType()));
		contextDatas.put("receiveTime", String.valueOf(System.currentTimeMillis()));
		chatMsg.setContent(JsonUtil.toJson(receiveMoneyQrCode));
		contextDatas.put("msg", JsonUtil.toJson(chatMsg));
		contextDatas.put("content", enMd5String(JsonUtil.toJson(chatMsg)));
		orderMainContext.setContextDatas(contextDatas);
		ProcessResult ret = this.orderClientService.createOrder(orderMainContext);
		if (ret.getRetCode() != 0) {
			return ret;
		}
		ret = this.orderClientService.startOrder(orderMainContext.getCatetory(), orderMainContext.getOrderId());
		if (ret.getRetCode() != 0) {
			return ret;
		}
	
		boolean result = rMoneyQrCodeService.saveRMoneyQrcode(receiveMoneyQrCode);
		if(result)
		{
			ret = ControllerUtils.getSuccessResponse(null);
			if (ret.getRetCode() == 0) {
				ret.setRetMsg(RSAUtils.sign(receiveMoneyQrCode.createResponseKey(), privateKey));
			}
		}
		else
		{
			ret = ControllerUtils.getErrorResponse(-1, "save db or cache error");
		}
		return ret;

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		String[] springStr = StringUtils.split(receiveMsgTemplate, ";");
		for (int i = 0; i < springStr.length; i++) {
			String[] contents = StringUtils.split(springStr[i], ",");
			MsgTemplate msgTemplate = new MsgTemplate();
			msgTemplate.setCategory(contents[0]);
			msgTemplate.setMessageType(Integer.parseInt(contents[1]));
			msgTemplate.setMsgKeyword(contents[2]);
			msgTemplate.setNotifyUrl(contents[3]);
			try {
				if(contents.length>=5)
				{
					msgTemplate.setCallMethod(contents[4]);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				msgTemplate.setCallMethod("callMethod");
			}

			try {
				if(contents.length>=6)
				{
					msgTemplate.setReceiveMoneyKey(contents[5]);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				msgTemplate.setReceiveMoneyKey("");
			}
			try {
				if(contents.length>=7)
				{
					msgTemplate.setReceiveMarkKey(contents[6]);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				msgTemplate.setReceiveMarkKey("");
			}
			msgTemplateMap.put(msgTemplate.getMsgKeyword(), msgTemplate);
			logger.error("msgTemplate=" + msgTemplate.toString());
		}
		
		
	}

	protected String enMd5String(String content) {
		StringBuilder str = new StringBuilder();
		str.append(wxPayMsgMd5Key);
		str.append(content);
		return Md5Encrypt.md5(str.toString());
	}
}
