package com.company.cooChatWx.controller.rest;

import java.security.PrivateKey;
import java.security.PublicKey;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.company.cooChatWx.security.utils.SecurityKeyServiceImpl;

import com.xinwei.nnl.common.domain.JsonRequest;
import com.xinwei.nnl.common.domain.ProcessResult;

@RestController
@RequestMapping("/userRsaKey")
public class SecurityRsaKeyController {
	@Autowired
	protected SecurityKeyServiceImpl securityKeyServiceImpl;
	
	@PostMapping(value = "/{userId}")
	public  ProcessResult getRsaPubKey(@PathVariable String userId,@RequestBody JsonRequest jsonRequest ) {
		
		try {
			ProcessResult ret= securityKeyServiceImpl.createRsakeys(userId);
			if(ret.getRetCode()==0)
			{
				PublicKey publicKey = (PublicKey)ret.getResponseInfo(); 
				String key = Base64.encodeBase64String(publicKey.getEncoded());
				ret.setResponseInfo(key);
			}
			return ret;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ControllerUtils.getFromResponse(e, -1, null);
		}
		
	}
	
}
