package com.company.cooChatWx.controller.rest;

import java.security.PublicKey;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.cooChatWx.domain.PayOrderRequest;
import com.company.cooChatWx.domain.ServerAccessRequest;
import com.company.cooChatWx.message.service.ServiceAccessService;
import com.company.cooChatWx.security.utils.SecurityKeyServiceImpl;
import com.xinwei.nnl.common.domain.JsonRequest;
import com.xinwei.nnl.common.domain.ProcessResult;

@RestController
@RequestMapping("/serverAccess")
public class ServerAccessController {
	@Autowired
	protected SecurityKeyServiceImpl securityKeyServiceImpl;
	
	@Autowired
	protected ServiceAccessService serviceAccessService;
	@PostMapping(value = "/getKey")
	public  ProcessResult getRsaPubKey(@RequestBody ServerAccessRequest serverAccessRequest ) {
		try {
			String userId = serviceAccessService.getPrivateKeyIndex(serverAccessRequest.getUserId(), serverAccessRequest.getAppId()); 
			ProcessResult ret= securityKeyServiceImpl.createRsakeys(userId);
			if(ret.getRetCode()!=0)
			{
				return ret;
			}			
			PublicKey publicKey = (PublicKey)ret.getResponseInfo(); 
			String key = Base64.encodeBase64String(publicKey.getEncoded());
			ret = serviceAccessService.saveServiceRequest(serverAccessRequest);
			ret.setResponseInfo(key);
			return ret;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ControllerUtils.getFromResponse(e, -1, null);
		}
		
	}
	/**
	 * 第三方应用申请支付ID
	 * @param payOrderRequest
	 * @return
	 */
	@PostMapping(value = "/getPayOrderId")
	public  ProcessResult getPayOrderId(@RequestBody PayOrderRequest payOrderRequest ) {
		try {
			
			return serviceAccessService.requestPayTransId(payOrderRequest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ControllerUtils.getFromResponse(e, -1, null);
		}
		
	}
	
	/**
	 * 第三方支付申请收款二维码
	 * @param payOrderRequest
	 * @return
	 */
	@PostMapping(value = "/getPayQrCode")
	public  ProcessResult getPayQrCode(@RequestBody PayOrderRequest payOrderRequest ) {
		try {
			
			return serviceAccessService.getImPayInfo(payOrderRequest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ControllerUtils.getFromResponse(e, -1, null);
		}
		
	}
	
	@PostMapping(value = "{category}/{dbid}/{orderId}/notifyThirdApp")
	public ProcessResult notifyThirdApp(@PathVariable String category, @PathVariable String dbid, @PathVariable String orderId,
			@RequestBody JsonRequest jsonRequest) {
			try {
			
			return serviceAccessService.notifyThirdApp(category, orderId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ControllerUtils.getFromResponse(e, -1, null);
		}
		
	}
	
	@PostMapping(value = "{category}/{dbid}/{orderId}/waitPayTimeout")
	public ProcessResult waitPayTimeout(@PathVariable String category, @PathVariable String dbid, @PathVariable String orderId,
			@RequestBody JsonRequest jsonRequest) {
			try {
			
			return serviceAccessService.waitPayTimeout(category, orderId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ControllerUtils.getFromResponse(e, -1, null);
		}
		
	}
}
