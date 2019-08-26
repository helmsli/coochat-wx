package com.company.cooChatWx.message.service;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.company.cooChatWx.controller.rest.ControllerUtils;
import com.company.cooChatWx.domain.ConfigReceiveMoneyImUser;
import com.company.cooChatWx.domain.MessageConst;
import com.company.cooChatWx.domain.NotifyResult;
import com.company.cooChatWx.domain.PayOrderRequest;
import com.company.cooChatWx.domain.PayOrderResponse;
import com.company.cooChatWx.domain.PayGetImResponse;
import com.company.cooChatWx.domain.PayGetOrderIdReqContent;

import com.company.cooChatWx.domain.PayGetOrderIdRspContent;
import com.company.cooChatWx.domain.PayGetImContent;
import com.company.cooChatWx.domain.ReceiveMoneyQrCode;
import com.company.cooChatWx.domain.RestErrorCode;
import com.company.cooChatWx.domain.ServerAccessRequest;
import com.company.cooChatWx.domain.WXPayInfo;
import com.company.cooChatWx.domain.WxMessage;
import com.company.cooChatWx.orderService.BaseRedisService;
import com.company.cooChatWx.orderService.OrderClientService;
import com.company.cooChatWx.security.utils.RSAUtils;
import com.company.cooChatWx.security.utils.SecurityKeyServiceImpl;
import com.company.cooChatWx.utils.Md5Encrypt;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.nnl.common.util.JsonUtil;
import com.xinwei.orderDb.domain.OrderMainContext;
import com.xinwei.userOrder.domain.UserOrder;

@Service("serviceAccessService")
public class ServiceAccessService extends BaseRedisService{

	@Value("${chat.serviceAccess.md5Key:payMsg!$@#}")
	private String serviceAccessMd5Key;
	
	@Autowired
	protected RestTemplate restTemplate;
	/**
	 * im支付结果Md5Key
	 */
	@Value("${chat.wxpayMsg.md5Key:payMsg!$@#}")
	private String wxPayMsgMd5Key;
	
	@Autowired
	@Lazy
	protected SecurityKeyServiceImpl securityKeyServiceImpl;
	
	private final String category_serviceAccessApp = "Wx-accessApp";
	
	@Autowired
	private OrderClientService orderClientService;
	
	@Autowired
	private RMoneyQrCodeService rMoneyQrCodeService;
	
	
	protected Logger logger = LoggerFactory.getLogger(getClass());

	
	
	/**
	 * 配置收钱的用户ID
	 * @param payOrderRequest
	 * @return
	 */
	public ProcessResult configReceiveImUser(PayOrderRequest payOrderRequest)
	{
		String userId = payOrderRequest.getUserId();
		String appId = payOrderRequest.getAppId();
		
		String privateKeyIndex = this.getPrivateKeyIndex(userId, appId);
		PrivateKey privateKey = securityKeyServiceImpl.getPrivatekey(privateKeyIndex);
		if(privateKey==null)
		{
			return ControllerUtils.getErrorResponse(RestErrorCode.Error_privateKeyIsNull, "private key is null");
		}
		String content = RSAUtils.decrypt(payOrderRequest.getContent(), privateKey);
		ConfigReceiveMoneyImUser configReceiveMoneyImUser = JsonUtil.fromJson(content, ConfigReceiveMoneyImUser.class);
		if(configReceiveMoneyImUser.getUserId().compareToIgnoreCase(userId)!=0)
		{
			return ControllerUtils.getErrorResponse(-1, "userId not corrent");
		}
		return this.rMoneyQrCodeService.configClientCreateQrCodeUserId(configReceiveMoneyImUser);
	}
	
	/**
	 * 申请支付订单编号
	 * @param payOrderRequest
	 * @return
	 */
	public ProcessResult requestPayTransId(PayOrderRequest payOrderRequest)
	{
		String userId = payOrderRequest.getUserId();
		String appId = payOrderRequest.getAppId();
		
		String privateKeyIndex = this.getPrivateKeyIndex(userId, appId);
		PrivateKey privateKey = securityKeyServiceImpl.getPrivatekey(privateKeyIndex);
		if(privateKey==null)
		{
			return ControllerUtils.getErrorResponse(RestErrorCode.Error_privateKeyIsNull, "private key is null");
		}
		String content = RSAUtils.decrypt(payOrderRequest.getContent(), privateKey);
		PayGetOrderIdReqContent payGetOrderIdReqContent = JsonUtil.fromJson(content, PayGetOrderIdReqContent.class);
		if(payGetOrderIdReqContent.getUserId().compareToIgnoreCase(payOrderRequest.getUserId())!=0)
		{
			logger.error("cheat msg userId not equal");
			return ControllerUtils.getErrorResponse(RestErrorCode.Error_cheatMsg, "cheat msg");
		}
		if(payGetOrderIdReqContent.getAppId().compareToIgnoreCase(payOrderRequest.getAppId())!=0)
		{
			logger.error("cheat msg appId not equal");
			return ControllerUtils.getErrorResponse(RestErrorCode.Error_cheatMsg, "cheat msg");
		}
		//申请订单ID
		String orderId = this.orderClientService.getOrderId(MessageConst.requestPayOrder, payGetOrderIdReqContent.getUserId());
		if(StringUtils.isEmpty(orderId))
		{
			return ControllerUtils.getErrorResponse(RestErrorCode.Error_requestOrderId, "request orderId error");
 		}
		
		OrderMainContext orderMainContext = new OrderMainContext();
		orderMainContext.setCatetory(MessageConst.requestPayOrder);
		orderMainContext.setOwnerKey(payGetOrderIdReqContent.getUserId());
		orderMainContext.setOrderId(orderId);
		payGetOrderIdReqContent.setOrderId(orderId);
		Map<String,String> contextMap = new HashMap<String,String>();
		contextMap.put(MessageConst.context_key_request, JsonUtil.toJson(payGetOrderIdReqContent));
		contextMap.put(MessageConst.context_key_sign, Md5Encrypt.md5(JsonUtil.toJson(payGetOrderIdReqContent), this.serviceAccessMd5Key));
		orderMainContext.setContextDatas(contextMap);
		ProcessResult ret =  this.orderClientService.createOrder(orderMainContext);
		if(ret.getRetCode()==0)
		{
			PayOrderResponse payGetOrderIdResponse = new PayOrderResponse();
			PayGetOrderIdRspContent payGetOrderIdRspContent = new PayGetOrderIdRspContent();
			payGetOrderIdRspContent.setOrderid(orderId);
			payGetOrderIdResponse.setResponseContent(JsonUtil.toJson(payGetOrderIdRspContent));
			payGetOrderIdResponse.setAppId(payOrderRequest.getAppId());
			payGetOrderIdResponse.setUserId(payOrderRequest.getUserId());
			payGetOrderIdResponse.setSign(RSAUtils.sign(payGetOrderIdResponse.getResponseContent(), privateKey));
			ret.setResponseInfo(payGetOrderIdResponse);
		}
		return ret;
	}
	
	/**
	 * 申请支付Im的信息，包括备注和相关信息
	 * @param payOrderRequest
	 * @return
	 */
	public ProcessResult getImPayInfo(PayOrderRequest payOrderRequest)
	{
		String userId = payOrderRequest.getUserId();
		String appId = payOrderRequest.getAppId();
		
		String privateKeyIndex = this.getPrivateKeyIndex(userId, appId);
		PrivateKey privateKey = securityKeyServiceImpl.getPrivatekey(privateKeyIndex);
		if(privateKey==null)
		{
			return ControllerUtils.getErrorResponse(RestErrorCode.Error_privateKeyIsNull, "private key is null");
		}
		String content = RSAUtils.decrypt(payOrderRequest.getContent(), privateKey);
		PayGetImContent payGetImContent = JsonUtil.fromJson(content, PayGetImContent.class);
		if(payGetImContent.getUserId().compareToIgnoreCase(payOrderRequest.getUserId())!=0)
		{
			logger.error("cheat msg userId not equal");
			return ControllerUtils.getErrorResponse(RestErrorCode.Error_cheatMsg, "cheat msg");
		}
		if(payGetImContent.getAppId().compareToIgnoreCase(payOrderRequest.getAppId())!=0)
		{
			logger.error("cheat msg appId not equal");
			return ControllerUtils.getErrorResponse(RestErrorCode.Error_cheatMsg, "cheat msg");
		}
		//申请订单ID
		String orderId =payGetImContent.getOrderId();
		if(StringUtils.isEmpty(orderId))
		{
			return ControllerUtils.getErrorResponse(RestErrorCode.Error_requestOrderId, "client send orderId error");
 		}
		ReceiveMoneyQrCode receiveMoneyQrCode = rMoneyQrCodeService.getRMoneyQrcode(payGetImContent.getType(), payGetImContent.getAmount(),orderId);
		if(receiveMoneyQrCode==null)
		{
			return ControllerUtils.getErrorResponse(RestErrorCode.Error_NoReceiveQrCode, "no Qrcode");
		}
		
		Map<String,String> contextMap = new HashMap<String,String>();
		contextMap.put(MessageConst.context_key_content, JsonUtil.toJson(receiveMoneyQrCode));
		contextMap.put(MessageConst.context_key_sign+MessageConst.context_key_content, Md5Encrypt.md5(JsonUtil.toJson(receiveMoneyQrCode), this.serviceAccessMd5Key));
		contextMap.put(MessageConst.context_key_payResult, "0");
		contextMap.put(MessageConst.context_key_sign+MessageConst.context_key_payResult,
				"0");
		ProcessResult ret =  this.orderClientService.putContextData(MessageConst.requestPayOrder, orderId, contextMap);
		if(ret.getRetCode()!=0)
		{
			return ret;
		}
		ret = this.orderClientService.startOrder(MessageConst.requestPayOrder, orderId);
		if(ret.getRetCode()==0)
		{
			PayOrderResponse payGetOrderIdResponse = new PayOrderResponse();
			payGetOrderIdResponse.setResponseContent(JsonUtil.toJson(receiveMoneyQrCode));
			payGetOrderIdResponse.setAppId(payOrderRequest.getAppId());
			payGetOrderIdResponse.setUserId(payOrderRequest.getUserId());
			payGetOrderIdResponse.setSign(RSAUtils.sign(payGetOrderIdResponse.getResponseContent(), privateKey));
			ret.setResponseInfo(payGetOrderIdResponse);
		}
		return ret;
	}
	
	
	
	/**
	 * 获取私要的关键子
	 * @return
	 */
	public String getPrivateKeyIndex(String userId,String appId)
	{
		return userId+ "_" + appId;
	}
	
	
	public String getServerAccessRequestRedisKey(String userId,String appId)
	{
		return "accessServer:" + userId+ "_" + appId;
	}
	
	public ProcessResult saveServiceRequest(ServerAccessRequest serverAccessRequest)
	{
		
		ProcessResult ret = this.saveServiceRequestToDb(serverAccessRequest);
		if(ret.getRetCode()==0)
		{
			try {
				String key = getServerAccessRequestRedisKey(serverAccessRequest.getUserId(),serverAccessRequest.getAppId());
				this.getStringRedisTemplate().opsForValue().set(key, JsonUtil.toJson(serverAccessRequest),48,TimeUnit.HOURS);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		return ret;
	}
	
	
	
	
	public ServerAccessRequest  getServiceRequest(String userId,String appId)
	{
		String key = getServerAccessRequestRedisKey(userId,appId);
		try {
			String jsonStr = this.getStringRedisTemplate().opsForValue().get(key);	
			if(!StringUtils.isEmpty(jsonStr))
			{
				ServerAccessRequest serverAccessRequest=JsonUtil.fromJson(jsonStr, ServerAccessRequest.class);
				return serverAccessRequest;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ServerAccessRequest serverAccessRequest=null;
		ProcessResult ret = this.getServiceRequestToDb(userId, appId);
		if(ret.getRetCode()==0)
		{
			serverAccessRequest =(ServerAccessRequest)ret.getResponseInfo();
			try {
				this.getStringRedisTemplate().opsForValue().setIfAbsent(key, JsonUtil.toJson(serverAccessRequest));
				this.getStringRedisTemplate().expire(key, 48, TimeUnit.HOURS);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return serverAccessRequest;
	}
	protected ProcessResult saveServiceRequestToDb(ServerAccessRequest serverAccessRequest)
	{
		UserOrder userOrder = new UserOrder();
		userOrder.setCategory(category_serviceAccessApp);
		userOrder.setUserId(serverAccessRequest.getUserId());
		userOrder.setOrderId(serverAccessRequest.getAppId());
		userOrder.setConstCreateTime();
		userOrder.setOrderData(JsonUtil.toJson(serverAccessRequest));
		return this.orderClientService.saveUserOrder(null, userOrder);
	}
	protected ProcessResult getServiceRequestToDb(String userId,String appId)
	{
		UserOrder userOrder = new UserOrder();
		userOrder.setCategory(category_serviceAccessApp);
		userOrder.setUserId(userId);
		userOrder.setOrderId(appId);
		userOrder.setConstCreateTime();
		
		ProcessResult ret = this.orderClientService.queryOneOrder(null, userOrder);
		if(ret.getRetCode()==0)
		{
			userOrder = (UserOrder)ret.getResponseInfo();
			ServerAccessRequest serverAccessRequest = JsonUtil.fromJson(userOrder.getOrderData(), ServerAccessRequest.class);
			ret.setResponseInfo(serverAccessRequest);
		}
		return ret;
	}
	
	
	public ProcessResult waitPayTimeout(String category,String orderId)
	{
		
		List<String> keys = new ArrayList<String>();
		keys.add(MessageConst.context_key_payResult);
		keys.add(MessageConst.context_key_sign+MessageConst.context_key_payResult );
	
			
		keys.add(MessageConst.context_key_content);
		keys.add(MessageConst.context_key_sign+MessageConst.context_key_content);
		ProcessResult ret = this.orderClientService.getContextData(category, orderId, keys);
		if(ret.getRetCode()!=0)
		{
			return ret;
		}
		
		Map<String,String> contextMap = (Map<String,String>)ret.getResponseInfo();
		
		WXPayInfo wXPayInfo = null;
		//获取支付结果
		if(contextMap.containsKey(MessageConst.context_key_payResult))
		{
			String jsonStr =  contextMap.get(MessageConst.context_key_payResult);
			if(jsonStr.length()>10)
			{
				wXPayInfo = JsonUtil.fromJson(jsonStr, WXPayInfo.class);
				String md5Key = Md5Encrypt.md5(jsonStr, this.wxPayMsgMd5Key);
				jsonStr =  contextMap.get(MessageConst.context_key_sign+MessageConst.context_key_payResult);
				if(md5Key.compareToIgnoreCase(jsonStr)!=0)
				{
					wXPayInfo=null;
				}
			}
		}
	
		
		
		ReceiveMoneyQrCode receiveMoneyQrCode = JsonUtil.fromJson(contextMap.get(MessageConst.context_key_content), ReceiveMoneyQrCode.class);
		if(wXPayInfo!=null)
		{
			//这里有个BUG，需要确保回收自己申请的，不能把别人的回收
			boolean isPushToNewUser = this.rMoneyQrCodeService.pushRMoneyQrcodeToNewUser(receiveMoneyQrCode);
			if(isPushToNewUser)
			{
				return ControllerUtils.getSuccessResponse(null);
			}
			
		}
		else if(receiveMoneyQrCode.getExpireTime().before(Calendar.getInstance().getTime()))
		{
			boolean isPushToNewUser = this.rMoneyQrCodeService.pushRMoneyQrcodeToNewUser(receiveMoneyQrCode);
			if(isPushToNewUser)
			{
				 return ControllerUtils.getErrorResponse(-9001, "not timeout");
			}
		}
		else
		{
			return ControllerUtils.getErrorResponse(-1, "not timeout");
		}
		return ControllerUtils.getErrorResponse(-1, "other error");
	}
	
	public ProcessResult notifyThirdApp(String category,String orderId)
	{
		List<String> list = new ArrayList<String>();
		list.add(MessageConst.context_key_payResult);
		list.add(MessageConst.context_key_sign+MessageConst.context_key_payResult);
		list.add(MessageConst.context_key_request);
		list.add(MessageConst.context_key_content);
		ProcessResult ret = this.orderClientService.getContextData(category, orderId, list);
		if(ret.getRetCode()!=0)
		{
			logger.error("get context error:" + ret.toString());
			return ret;
		}
		Map<String,String>retMap = (Map<String,String>)ret.getResponseInfo();
		
		String sign = retMap.get(MessageConst.context_key_sign+MessageConst.context_key_payResult );
		String jsonStr = retMap.get(MessageConst.context_key_payResult);
		if(StringUtils.isEmpty(sign)||StringUtils.isEmpty(jsonStr))
		{
			logger.error("get context error not find:" + ret.toString());
			return ControllerUtils.getErrorResponse(-1, "not found error");
		}
		
		if(sign.compareToIgnoreCase(Md5Encrypt.md5(jsonStr, this.wxPayMsgMd5Key))!=0)
		{
			logger.error("notify md5 error:" + ret.toString());
			return ControllerUtils.getErrorResponse(-1, "md5 error");
		}
		WXPayInfo wXPayInfo = JsonUtil.fromJson(jsonStr, WXPayInfo.class);
		PayGetOrderIdReqContent payGetOrderIdReqContent = JsonUtil.fromJson(retMap.get(MessageConst.context_key_request), PayGetOrderIdReqContent.class);
		//上分，通知结果
		ServerAccessRequest serverAccessRequest =  this.getServiceRequest(payGetOrderIdReqContent.getUserId(), payGetOrderIdReqContent.getAppId());
		
		String url = serverAccessRequest.getNotifyUrl();
		NotifyResult notifyResult = new NotifyResult();
		String privateKeyIndex = this.getPrivateKeyIndex(payGetOrderIdReqContent.getUserId(), payGetOrderIdReqContent.getAppId());
		PrivateKey privateKey = securityKeyServiceImpl.getPrivatekey(privateKeyIndex);
	
		notifyResult.setResponseContent(JsonUtil.toJson(payGetOrderIdReqContent));
		notifyResult.setSign(RSAUtils.sign(notifyResult.getResponseContent(), privateKey));
		return this.restTemplate.postForObject(url, notifyResult, ProcessResult.class);
	}

}
