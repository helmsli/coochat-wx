package com.company.cooChatWx.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.cooChatWx.domain.ChatMsg;
import com.company.cooChatWx.domain.WxReceivePay;
import com.company.cooChatWx.message.service.WxMessageService;
import com.company.cooChatWx.message.service.WxPayService;
import com.xinwei.nnl.common.domain.ProcessResult;

@RestController
@RequestMapping("/wxPay")
public class WxPayController {
	@Autowired
	protected WxPayService wxPayService;
	/**
	 * String allClientPassword = RSAUtils.decrypt(clientPassword, privateKey);
	 * @param wxMessage
	 * @return
	 */
	@PostMapping(value = "/receivePayMsg")
	public  ProcessResult receivePayMsg(@RequestBody WxReceivePay chatMsg) {
		
		try {
			return wxPayService.processReceiveMsg(chatMsg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ControllerUtils.getFromResponse(e, -1, null);
		}
		
	}
	
	@PostMapping(value = "/createReceiveQrCode")
	public  ProcessResult createReceiveQrCode(@RequestBody ChatMsg chatMsg) {
		
		try {
			return wxPayService.processReceiveMoneyQrcodeMsg(chatMsg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ControllerUtils.getFromResponse(e, -1, null);
		}
		
	}
	
	
	
}
