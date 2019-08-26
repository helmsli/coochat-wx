package com.company.cooChatWx.security.utils;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import com.company.cooChatWx.controller.rest.ControllerUtils;
import com.company.cooChatWx.orderService.OrderClientService;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.nnl.common.util.JsonUtil;
import com.xinwei.userOrder.domain.UserOrder;


@Service("securityKeyService")
public class SecurityKeyServiceImpl implements SecurityKeyService {
	protected Logger logger = LoggerFactory.getLogger(getClass());

	@Resource (name = "redisTemplate")
	protected RedisTemplate<Object, Object> redisTemplate;
	
	@Autowired
	private OrderClientService orderClientService;
	
	private KeyPair rsaKeyPair = RSAUtils.generateKeyPair();
	
	protected final String rsaKey_category="coochatxwRsaKey";
	
	/**
	 * 
	 * @param transid
	 * @param phone
	 * @return
	 */
	protected String getRedisPrivatekey(String transid)
	{
		StringBuilder str = new StringBuilder();
		str.append(transid.substring(0, RSAUtils.Length_ServeridNode));
		str.append("*");
		str.append(RSAUtils.Rsa_private_key);
		str.append("*");
		str.append(transid);
		return str.toString();
		
	}
	/**
	 * 
	 * @param transid
	 * @param phone
	 * @return
	 */
	protected String getRedisPublickey(String transid)
	{
		StringBuilder str = new StringBuilder();
		str.append(transid.substring(0, RSAUtils.Length_ServeridNode));
		str.append("*");
		str.append(RSAUtils.Rsa_public_key);
		str.append("*");
		str.append(transid);
		return str.toString();
		
	}
	
	@Override
	public PrivateKey getPrivatekey(String transid) {
		// TODO Auto-generated method stub
		try {
			String transidkey = getRedisPrivatekey(transid);
			ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();		
			String keyStr = (String)opsForValue.get(transidkey);
			logger.debug("key:"+keyStr);
			if(StringUtils.isEmpty(keyStr))
			{
				logger.error("key is null");
				ProcessResult ret = this.getPrivatekeyFromDb(transid);
				if(ret.getRetCode()!=0)
				{
					logger.error(ret.toString());
					return null;
				}
				PrivateKey privateKey = (PrivateKey)ret.getResponseInfo();
				opsForValue.setIfAbsent(transidkey, RSAUtils.getBase64PrivateKey(privateKey));
				redisTemplate.expire(transidkey, 72, TimeUnit.HOURS);
				return privateKey;
			}
			
			PrivateKey privateKey = RSAUtils.getPrivateKey(keyStr);
			return privateKey;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("",e);
		}
		return null;
	}

	/**
	 * 生成public就是重新创建一组秘钥，避免秘钥重复
	 */
	@Override
	public PublicKey getPublickey(String transid) {
		ProcessResult ret =  this.createRsakeys(transid);
		if(ret.getRetCode()==0)
		{
			PublicKey publicKey = (PublicKey)ret.getResponseInfo();
			return publicKey;
		}
		return null;
	}
	@Override
	public ProcessResult createRsakeys(String transid) {
		// TODO Auto-generated method stub
		
		//生成新的publicKey和PrivateKey
		ProcessResult ret;
		try {
			PublicKey publicKey = null;
			PrivateKey privateKey =null;
			synchronized(this)
			{
				 publicKey = rsaKeyPair.getPublic();
				 privateKey =rsaKeyPair.getPrivate();
			}
			String privateKeyStr = getRedisPrivatekey(transid);
			UserOrder userOrder = new UserOrder();
			userOrder.setCategory(this.rsaKey_category);
			userOrder.setUserId(transid);
			userOrder.setOrderId(transid);
			userOrder.setConstCreateTime();
			userOrder.setOrderData(RSAUtils.getBase64PrivateKey(privateKey));
			ret = this.orderClientService.saveUserOrder(null, userOrder);
			if(ret.getRetCode()!=0)
			{
				return ret;
			}
			ret.setResponseInfo(publicKey);
			redisTemplate.opsForValue().set(privateKeyStr, userOrder.getOrderData(), 72, TimeUnit.HOURS);
			return ret;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("",e);
			return ControllerUtils.getFromResponse(e, -1, null);
		}
		
	}

	/**
	 * 从数据库中获取私钥
	 * @param transid
	 * @return
	 */
	public ProcessResult getPrivatekeyFromDb(String transid) {
		// TODO Auto-generated method stub
		
		//生成新的publicKey和PrivateKey
		ProcessResult ret;
		try {
			PrivateKey privateKey =null;
			UserOrder userOrder = new UserOrder();
			userOrder.setCategory(this.rsaKey_category);
			userOrder.setUserId(transid);
			userOrder.setOrderId(transid);
			userOrder.setConstCreateTime();
			ret = this.orderClientService.queryOneOrder(null, userOrder);
			if(ret.getRetCode()!=0)
			{
				return ret;
			}
			userOrder = (UserOrder)ret.getResponseInfo();
			privateKey = RSAUtils.getPrivateKey(userOrder.getOrderData());
			ret.setResponseInfo(privateKey);
			return ret;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("",e);
			return ControllerUtils.getFromResponse(e, -1, null);
		}
		
	}

}
