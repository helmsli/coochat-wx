package com.company.cooChatWx.message.service;

import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.company.cooChatWx.controller.rest.ControllerUtils;
import com.company.cooChatWx.domain.ChatMsg;
import com.company.cooChatWx.domain.ClientCreateQrCode;
import com.company.cooChatWx.domain.ConfigReceiveMoneyImUser;
import com.company.cooChatWx.domain.MessageConst;
import com.company.cooChatWx.domain.ReceiveMoneyQrCode;
import com.company.cooChatWx.domain.RestErrorCode;
import com.company.cooChatWx.mapper.ReceiveMoneyQrCodeMapper;
import com.company.cooChatWx.orderService.BaseRedisService;
import com.company.cooChatWx.orderService.OrderClientService;
import com.company.cooChatWx.orderService.UserOrderQueryResult;
import com.company.cooChatWx.security.utils.RSAUtils;
import com.company.cooChatWx.security.utils.SecurityKeyServiceImpl;
import com.company.cooChatWx.utils.Md5Encrypt;
import com.company.cooChatWx.utils.MsgPushWebSocketService;
import com.xinwei.msgpush.domain.msg.UserMessage;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.nnl.common.util.JsonUtil;
import com.xinwei.userOrder.domain.QueryUserOrderRequest;
import com.xinwei.userOrder.domain.UserOrder;

@Service("rMoneyQrCodeService")
public class RMoneyQrCodeService extends BaseRedisService implements InitializingBean {
	protected Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${im.pay.waitPaySeconds:600}")
	private int waitPaySeconds;

	@Value("${chat.wxpayMsg.md5Key:payMsg!$@#}")
	private String wxPayMsgMd5Key;

	@Value("${im.pay.administratorTemplate:00855383001975,1qaz2wsx}")
	private String administratorTemplate;
	
	@Autowired
	private SecurityKeyServiceImpl securityKeyServiceImpl;
	
	@Autowired
	private MsgPushWebSocketService msgPushWebSocketService;

	private Map<String, String> administratorMap = new HashMap<String, String>();

	@Autowired
	protected ReceiveMoneyQrCodeMapper receiveMoneyQrCodeMapper;

	private final String category_receiveMoney_imUserId = "canRImUserId";

	@Autowired
	private OrderClientService orderClientService;
	SimpleDateFormat sdfYyyyMmDdHhMmSs = new SimpleDateFormat("yyyyMMddHHmmss");

	/**
	 * 能够使用的收款码队列key
	 * 
	 * @param type
	 * @param money
	 * @return
	 */
	protected String getCanReceiveKey(int type, String money) {
		return "rMsgQrCan:" + type + ":" + money;
	}

	/**
	 * 已经分配给客户，等待收款的收款码队列
	 * 
	 * @param type
	 * @param money
	 * @return
	 */
	protected String getWaitspReceiveKey(int type, String money) {
		return "rMsgQrWaitRsp:" + type + ":" + money;
	}

	/**
	 * 所有的QrCodekey
	 * 
	 * @param type
	 * @param money
	 * @return
	 */
	protected String getQrCodeHashKey(int type, String money) {
		return "rMsgQrWaitRsp:" + type + ":" + money;
	}

	/**
	 * 保存支付信息到数据库和内存；
	 * 
	 * @param receiveMoneyQrCode
	 */
	public boolean saveRMoneyQrcode(ReceiveMoneyQrCode receiveMoneyQrCode) {

		boolean ret = saveRMoneyQrcodeToDb(receiveMoneyQrCode);
		if (!ret) {
			return ret;
		}
		try {

			String qrCodeHashKey = getQrCodeHashKey(receiveMoneyQrCode.getrType(), receiveMoneyQrCode.getAmount());
			String lsJson = (String) this.redisStringTemplate.opsForHash().get(qrCodeHashKey,
					receiveMoneyQrCode.createRedisKey());
			if (StringUtils.isEmpty(lsJson)) {
				logger.debug("left push :" + receiveMoneyQrCode.getRemark());
				String content = JsonUtil.toJson(receiveMoneyQrCode);
				String key = getCanReceiveKey(receiveMoneyQrCode.getrType(), receiveMoneyQrCode.getAmount());
				this.redisStringTemplate.opsForList().leftPush(key, content);
				this.redisStringTemplate.opsForHash().put(qrCodeHashKey, receiveMoneyQrCode.createRedisKey(),
						String.valueOf(receiveMoneyQrCode.STATUS_normal));
			}
			return true;
		} catch (Exception e) {
			logger.error("", e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 用于客户端来配置用于收款的IMId
	 * 
	 * @param createUserId
	 * @param type
	 * @param imUserId
	 * @return
	 */
	public ProcessResult configClientCreateQrCodeUserId(ConfigReceiveMoneyImUser configReceiveMoneyImUser) {
		String createUserId=configReceiveMoneyImUser.getUserId();
		String type=configReceiveMoneyImUser.getType();
		List<String> imUserId=configReceiveMoneyImUser.getUserList();
		String key = configReceiveMoneyImUser.getUserId() +"," + configReceiveMoneyImUser.getAppId();
		if (!this.administratorMap.containsKey(key)) {
			return ControllerUtils.getErrorResponse(-1, "can not configure,no privilege");
		}
		ProcessResult ret = ControllerUtils.getErrorResponse(-1, "no imUserId");
		for (String userId : imUserId) {
			UserOrder userOrder = new UserOrder();
			userOrder.setCategory(category_receiveMoney_imUserId);
			userOrder.setUserId(type);
			userOrder.setOrderId(userId);
			userOrder.setConstCreateTime();
			userOrder.setOrderData(enMd5String(type + ":" + userId));
			ret = this.orderClientService.saveUserOrder(null, userOrder);
			if (ret.getRetCode() != 0) {
				return ret;
			}
		}
		return ret;
	}

	protected String getTypeClientImUserKey(int type) {
		return "createQrcodeType:" + type;
	}

	/**
	 * 从数据库读取IM的收款的用户ID
	 * @param type
	 * @return
	 */
	protected List<String> getAllUseridFromDb(int iType)
	{
		String type = String.valueOf(iType);
		QueryUserOrderRequest queryUserOrderRequest = new QueryUserOrderRequest();
		queryUserOrderRequest.setCategory(category_receiveMoney_imUserId);
		queryUserOrderRequest.setUserId(type);
		UserOrder userOrderConst = new UserOrder();
		userOrderConst.setConstCreateTime();
		queryUserOrderRequest.setStartCreateTime(userOrderConst.getConstCreateDate());
		Calendar now = Calendar.getInstance();
		now.setTime(userOrderConst.getConstCreateDate());
		now.add(Calendar.SECOND, 1);
		queryUserOrderRequest.setEndCreateTime(now.getTime());
		queryUserOrderRequest.setPageNum(0);
		queryUserOrderRequest.setPageSize(1000000);
		queryUserOrderRequest.setUserId(type);
		ProcessResult result = this.orderClientService.queryUserOrderByPage(null, queryUserOrderRequest);
		if (result.getRetCode() != 0) {
			logger.error("query userOrder error:" + result.toString());
			return null;
		}
		List<String> newlist = new ArrayList<>();
		String jsonStr = JsonUtil.toJson(result.getResponseInfo());
		UserOrderQueryResult userOrderPageInfo = JsonUtil.fromJson(jsonStr, UserOrderQueryResult.class);
		if (!userOrderPageInfo.getList().isEmpty()) {
			List<UserOrder> list = userOrderPageInfo.getList();
			for (UserOrder userOrder : list) {
				String correntMd5Str = this
						.enMd5String(enMd5String(userOrder.getUserId() + ":" + userOrder.getOrderId()));
				if (correntMd5Str.compareToIgnoreCase(userOrder.getOrderData()) == 0) {
					newlist.add(userOrder.getOrderId());
				}
			}
		}
		return newlist;
	}
	
	public ProcessResult askClientCreateQrCode(String money, int type) {
		String key = getTypeClientImUserKey(type);
		String values = this.getStringRedisTemplate().opsForList().leftPop(key);
		ProcessResult ret = ControllerUtils.getSuccessResponse(null);
		if (!StringUtils.isEmpty(values)) {
			ret.setResponseInfo(values);
			return ret;
		}
		List<String> newList = getAllUseridFromDb(type);
		if(newList==null)
		{
			return ControllerUtils.getErrorResponse(-1, "get getAllUseridFromDb error:");
		}
		//
		if(newList.size()>0)
		{
			ret = ControllerUtils.getSuccessResponse(null);
			ret.setResponseInfo(newList.get(0));
			newList.remove(0);
			
		}
		else
		{
			return ControllerUtils.getErrorResponse(-1, "no free im user");
		}
		if(newList.size()>0)
		{	
			try {
				this.getStringRedisTemplate().opsForList().rightPushAll(key, newList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("",e);
			}
		}
		return ret;
	}

	public boolean saveRMoneyQrcodeToDb(ReceiveMoneyQrCode receiveMoneyQrCode) {
		try {
			receiveMoneyQrCodeMapper.insert(receiveMoneyQrCode);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("", e);
		}
		try {
			List<ReceiveMoneyQrCode> list = receiveMoneyQrCodeMapper
					.queryReceiveMoneyByRemark(receiveMoneyQrCode.getRemark());
			if (list != null && list.size() > 0) {
				ReceiveMoneyQrCode receiveMoneyQrCodeDb = list.get(0);
				if (receiveMoneyQrCodeDb.getAmount().compareToIgnoreCase(receiveMoneyQrCode.getAmount()) == 0) {
					return true;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("", e);
		}
		return false;
	}

	public ReceiveMoneyQrCode getRMoneyQrcode(int type, String money, String orderId) {
		ReceiveMoneyQrCode receiveMoneyQrCode = this.selectRMoneyQrcode(type, money,100);
		if (receiveMoneyQrCode == null) {
			//等待
			ProcessResult ret = this.askClientCreateQrCode(money, type);
			if(ret.getRetCode()!=0)
			{
				return null;
			}
			String userId = (String)ret.getResponseInfo();
			ClientCreateQrCode clientCreateQrCode = new ClientCreateQrCode();
			//"remark":"备注","amount":"0.01","currency":"RMB"
			Map<String,String> createParams = new HashMap<String,String>();
			createParams.put("remark", orderId);
			createParams.put("amount", money);
			createParams.put("currency", "RMB");
			
			clientCreateQrCode.setParms(createParams);
			PrivateKey privateKey = securityKeyServiceImpl.getPrivatekey(userId);
			if (privateKey == null) {
				logger.error("can not get privateKey:"+ userId);
				return null;
			}
			String sign = RSAUtils.sign(JsonUtil.toJson(createParams), privateKey);
			clientCreateQrCode.setSign(sign);
			UserMessage userMessage =new UserMessage();
			userMessage.setTargetUid(userId);
			userMessage.setRoutingKey("coochat-im");
			userMessage.setMsgTag("wxQR");			
			userMessage.setMsgBody(JsonUtil.toJson(clientCreateQrCode));
			ret = msgPushWebSocketService.sendUserMsg(userMessage);
			if(ret.getRetCode()!=0)
			{
				logger.error("send pushmsg error:" + ret.toString());
				return null;
			}
			receiveMoneyQrCode = this.selectRMoneyQrcode(type, money,60000);
			if (receiveMoneyQrCode == null) {
				return null;
			}
		}
		
		
		String remark = receiveMoneyQrCode.getRemark();
		String remarkOrder = this.getRemarkOrder(type, remark);

		Map<String, String> maps = new HashMap<String, String>();
		maps.put(MessageConst.context_key_remarkInfo, orderId);
		maps.put(MessageConst.context_key_remarkInfo + sdfYyyyMmDdHhMmSs.format(Calendar.getInstance().getTime()),
				orderId);
		ProcessResult ret = this.orderClientService.putContextData(MessageConst.im_remark_category, remarkOrder, maps);
		if (ret.getRetCode() == 0) {
			return receiveMoneyQrCode;
		}
		return null;
		// 如果remark小于9，怎么处理

	}

	/**
	 * 
	 * @param type
	 * @param money
	 * @param orderId
	 * @return
	 */
	public boolean pushRMoneyQrcodeToNewUser(ReceiveMoneyQrCode receiveMoneyQrCode) {
		return reUserRMoneyQrcode(receiveMoneyQrCode);
		// 如果remark小于9，怎么处理

	}

	public String getRemarkOrder(int type, String remark) {
		if (remark.length() < 10) {
			remark = remark + "A001A0005";
		}
		return type + "_" + remark;
	}

	protected ReceiveMoneyQrCode selectRMoneyQrcode(int type, String money,int timeOutMILLISECONDS) {
		try {
			String key = getCanReceiveKey(type, money);
			String ls = this.redisStringTemplate.opsForList().rightPop(key, 60, TimeUnit.MILLISECONDS);
			if (StringUtils.isEmpty(ls)) {
				return null;
			}
			ReceiveMoneyQrCode receiveMoneyQrCode = JsonUtil.fromJson(ls, ReceiveMoneyQrCode.class);
			Calendar now = Calendar.getInstance();
			now.add(Calendar.SECOND, waitPaySeconds);
			receiveMoneyQrCode.setExpireTime(now.getTime());
			int remainSeconds = (int) (now.getTime().getTime() - System.currentTimeMillis());
			receiveMoneyQrCode.setRemainSeconds(remainSeconds / 1000);
			receiveMoneyQrCode.setStatus(receiveMoneyQrCode.STATUS_waitPay);
			String waitKey = getWaitspReceiveKey(type, money);
			this.redisStringTemplate.opsForHash().put(waitKey, receiveMoneyQrCode.createRedisKey(),
					JsonUtil.toJson(receiveMoneyQrCode));

			String qrCodeKey = this.getQrCodeHashKey(type, money);
			this.redisStringTemplate.opsForHash().put(qrCodeKey, receiveMoneyQrCode.createRedisKey(),
					String.valueOf(receiveMoneyQrCode.STATUS_waitPay));
			return receiveMoneyQrCode;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("", e);
		}
		return null;

	}

	protected boolean reUserRMoneyQrcode(ReceiveMoneyQrCode receiveMoneyQrCode) {
		try {
			int type = receiveMoneyQrCode.getrType();
			String money = receiveMoneyQrCode.getAmount();
			String qrCodeHashKey = this.getQrCodeHashKey(type, money);

			String lsJson = (String) this.redisStringTemplate.opsForHash().get(qrCodeHashKey,
					receiveMoneyQrCode.createRedisKey());
			if (StringUtils.isEmpty(lsJson)
					|| lsJson.compareToIgnoreCase(String.valueOf(ReceiveMoneyQrCode.STATUS_waitPay)) == 0) {
				logger.debug("left push :" + receiveMoneyQrCode.getRemark());
				String content = JsonUtil.toJson(receiveMoneyQrCode);
				String key = getCanReceiveKey(receiveMoneyQrCode.getrType(), receiveMoneyQrCode.getAmount());
				this.redisStringTemplate.opsForList().leftPush(key, content);
				this.redisStringTemplate.opsForHash().put(qrCodeHashKey, receiveMoneyQrCode.createRedisKey(),
						String.valueOf(receiveMoneyQrCode.STATUS_normal));
				String waitKey = getWaitspReceiveKey(type, money);
				this.redisStringTemplate.opsForHash().delete(waitKey, receiveMoneyQrCode.createRedisKey());
			}
			return true;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("", e);
		}
		return false;

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		String[] administrator = administratorTemplate.split(";");
		if (administrator != null && administrator.length > 0) {
			for (int i = 0; i < administrator.length; i++) {
				//格式userid,appid 
				administratorMap.put(administrator[i], administrator[i]);
			}
		}
	}

	protected String enMd5String(String content) {
		StringBuilder str = new StringBuilder();
		str.append(wxPayMsgMd5Key);
		str.append(content);
		return Md5Encrypt.md5(str.toString());
	}
}
