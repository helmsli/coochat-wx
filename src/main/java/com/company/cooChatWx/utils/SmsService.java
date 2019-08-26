package com.company.cooChatWx.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.nnl.common.util.JsonUtil;
@Service("smsService")
public class SmsService implements InitializingBean{
	/**
	 * 下线加入向审核人发送短信
	 */
	public static final int bizType_joinNotify=3;
	/**
	 * 审核成功通知
	 */
	public static final int bizType_approvalSuccessNotify=4;
	/**
	 * 审核失败
	 */
	public static final int bizType_approvalFailNotify=5;
	@Value("${smsService.Url}")  
	private String smsServerUrl;
	@Value("${alidayu.transferKey}")  
	private String transferKey;
	
	@Value("${alidayu.templateCode}")  
	private String templateCodes;
	@Value("${alidayu.appName:cootalk}")  
	private String registerAppname;
	
	@Autowired
	private RestTemplate restTemplate;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private Map<String,String>templateMaps = new ConcurrentHashMap<String,String>();
	public ProcessResult sendSms(String phone,int bizType)
	{
		try {
			String url = this.smsServerUrl+ "/smsService/coojisu/0086";
			SmsInfo smsInfo = new SmsInfo();
			
			smsInfo.setTransId(String.valueOf(System.currentTimeMillis()));
			smsInfo.setDestAppName(registerAppname);
			smsInfo.setSmsTemplateCode(templateMaps.get(String.valueOf(bizType)));
			smsInfo.setCountryCode("");
			smsInfo.setCalledPhoneNumbers(phone);
			Map<String,String> paraMap = new HashMap<String,String>();
		
			smsInfo.setParameters(JsonUtil.toJson(paraMap));
			smsInfo.setCheckCrc(SecuritySmsAlgorithm.getCrcString(transferKey, smsInfo));
			ProcessResult ret = this.restTemplate.postForObject(url, smsInfo, ProcessResult.class);
			logger.debug(ret.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("",e);
			
		}

		return null;
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		String[] templateInfos = this.templateCodes.split(",");
		for(int i =0;i<templateInfos.length;i++)
		{
			String templateInfoString=templateInfos[i];
			String []templateInfo = templateInfoString.split(":");
			this.templateMaps.put(templateInfo[0].toLowerCase(), templateInfo[1].trim());
			
		}
	}
}
