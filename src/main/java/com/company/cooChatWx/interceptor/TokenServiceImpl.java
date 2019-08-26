package com.company.cooChatWx.interceptor;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import com.xinwei.nnl.common.util.JsonUtil;


@Service("tokenService")
public class TokenServiceImpl implements TokenService{

	@Resource (name = "redisTemplate")
	protected RedisTemplate<Object, Object> redisTemplate;
	public  final String Key_prefix_TokenExpired= "luExpir:";
	
	private int durationSeconds = 72*3600;
	
	 private Logger logger = LoggerFactory.getLogger(getClass());
		public static final String Key_prefix_DeviceType= "luDType:";
		public static final String Key_prefix_Split=":";
	 
	@Value("${token.expireMinutes:14400}")  
	private int tokenExpireMinutes;
	
	@Value("${token.updateCacheMinutes:5}")  
	private int updateCacheMinutes;
	
	

	public String getTokenKey(int loginType,long userid)
	{
		StringBuilder str= new StringBuilder();
		str.append(Key_prefix_DeviceType);
		str.append(userid);
		str.append(Key_prefix_Split);
		str.append(loginType);
		return str.toString();
	}
	/**
	 * 获取用户安全级别的token
	 * @param token
	 * @return
	 */
	public  String getTokenAccessKey(String token)
	{
		StringBuilder str= new StringBuilder();
		str.append(Key_prefix_TokenExpired);
		str.append(token);
		return str.toString();
	}
	@Override
	public boolean setTokenInfo(String token, TokenInfo tokenInfo, int duartionSeconds) {
		// TODO Auto-generated method stub
		try {
			String accessKey  =getTokenAccessKey(token);
			ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();
			//tokenInfo.setCreateTime(System.currentTimeMillis());
			opsForValue.set(accessKey, String.valueOf(System.currentTimeMillis()),duartionSeconds,TimeUnit.SECONDS);
			//this.durationSeconds = duartionSeconds;
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;

	}

	@Override
	public TokenInfo getTokenInfo(String token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TokenInfo checkTokenInfo(String token) {
		// TODO Auto-generated method stub
		String accessKey  =getTokenAccessKey(token);
		TokenInfo tokenInfo=new TokenInfo();
		try {
			ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();
			String createTime = (String)opsForValue.get(accessKey);
			
			{
				try {
					
					if(!StringUtils.isEmpty(createTime))
					{
						long lastAccessTime = Long.parseLong(createTime);
						//设置时间
						//判断是否过期,没有过期直接返回
						if(System.currentTimeMillis() - lastAccessTime<=tokenExpireMinutes*60*1000)
						{
							if(System.currentTimeMillis() - lastAccessTime>updateCacheMinutes*60*1000)
							{
								logger.debug("token need to set");
								setTokenInfo(token,tokenInfo,durationSeconds);
							}
							logger.debug("token is expire:" + tokenInfo.getCreateTime());
							tokenInfo.setResult(tokenInfo.result_success);
							return tokenInfo;
						}
						else
						{
							return tokenInfo;
						}
						
					}
					//
					TokenCacheInfo tokenCacheInfo =null;
					String sessionInfoKey  =this.getTokenInfoKey(token);
					{	
						createTime = (String)opsForValue.get(sessionInfoKey);
						tokenCacheInfo = JsonUtil.fromJson(createTime, TokenCacheInfo.class);
						logger.debug("loginUsertokenCache:" + sessionInfoKey);
						logger.debug("loginUsertokenCache obj:" + JsonUtil.toJson(tokenCacheInfo));
						String ls = getTokenKey(tokenCacheInfo.getLoginType(),tokenCacheInfo.getUserId());
						
						createTime = (String)opsForValue.get(ls);
						logger.debug("loginUserSession:" + ls+":" + createTime);
						LoginUserSession loginUserSession = JsonUtil.fromJson(createTime, LoginUserSession.class);	
						if(loginUserSession.getLoginDeviceId().compareToIgnoreCase(tokenCacheInfo.getDeviceId())!=0)
						{
							tokenInfo.setResult(tokenInfo.result_loginOnOthersDevice);
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			return tokenInfo;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tokenInfo;
	}
	public String getTokenInfoKey(String token)
	{
		StringBuilder str= new StringBuilder();
		str.append("tokenInfoTimeout:");
		str.append(token);
		return str.toString();
	}
	@Override
	public boolean delTokenInfo(String token) {
		// TODO Auto-generated method stub
		return true;
	}

}
