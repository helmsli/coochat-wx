package com.company.coochat;

import java.util.Calendar;

import org.springframework.web.client.RestTemplate;

import com.company.cooChatWx.domain.ChatMsg;
import com.company.cooChatWx.domain.PayGetImContent;
import com.company.cooChatWx.domain.PayGetOrderIdReqContent;
import com.company.cooChatWx.domain.PayGetOrderIdRspContent;
import com.company.cooChatWx.domain.PayOrderRequest;
import com.company.cooChatWx.domain.PayOrderResponse;
import com.company.cooChatWx.domain.ReceiveMoneyQrCode;
import com.company.cooChatWx.security.utils.RSAUtils;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.nnl.common.util.JsonUtil;

public class TestThirdAppPay {
	protected RestTemplate restTemplate=new RestTemplate();
	private String url = "http://172.18.10.74:9203/";
	private String thirdApppublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApOLGzJItn2Bo8CtV5BFMxACfc6Pu1mZKVeG/2Tb4wBTLF/n31A3v9NR8GF/Jg+dy5CQqkurf0sGehVwgiZ5jqLJeyDc1SLj+Mbow9P+oGHq829Ukp/EEzf/SjcK28oKMVjrPCx2Lk3+mtg3hwHzGDohX5GPdTM27nAxAosrdeHvy1QVAN7eaGrnZx5w717DEWgzoFe/rshTuMUZTnvuMwoUvuyUEc8QoU5sSqQWHNETQjVAtyGRe8bN9lM6aMMvvgnSHhYUc7TGFirYSuEPOtALHzJHoWHZx1JKdkE7vj6oTJxNY3Y9LK7bsJZrHuJUoJX1fe2f4I270BbhabfGkMwIDAQAB";
	  private String imUserPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtcnkXjggqp1fkuJTSmTd+axQiEYsDqsx9ko0qG1zneh4QOdjEDD5C4h5ABvJmif6n3TagdknQTwRS3QxxjjHgFXkBzY4UFRGJY510c1bQsDJLoU+7wtlRnLwIJIXeioPwAmfMOBx4Hb9OTBUvKwRMk67xcMeDvQS7hMcera2oNAFe2aaY/9u/kpAHzDoB7D1reGhuVYsjUecNpg1gKRES5HJQ4T5a9EnNmLdSaa/piXinzvvB+KXGxuOxCj1CZ5os9PbkKNYB2toHj1jVqhnm3SlumZGErMnExpfqM5P94dSZBfpOtp+Qs7SmYaBpRrClRk+wdJyASy7JxbM0jR5wwIDAQAB";
	  private String orderId="";
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TestThirdAppPay testThirdAppPay = new TestThirdAppPay();
		testThirdAppPay.createQrCode();
		
		testThirdAppPay.getThirdAppPayOrder();
		testThirdAppPay.getQrCode();
	}
	
	public void getThirdAppPayOrder()
	{
		PayOrderRequest payOrderRequest = new PayOrderRequest();
		payOrderRequest.setAppId("coojisu");
		payOrderRequest.setUserId("00855383001800");
		PayGetOrderIdReqContent payGetOrderIdReqContent = new PayGetOrderIdReqContent();
		payGetOrderIdReqContent.setUserId(payOrderRequest.getUserId());
		payGetOrderIdReqContent.setAppId(payOrderRequest.getAppId());
		
		String content=RSAUtils.encryptAsString(JsonUtil.toJson(payGetOrderIdReqContent), thirdApppublicKey);
		
		payOrderRequest.setContent(content);
		ProcessResult ret = this.restTemplate.postForObject(url+"serverAccess/getPayOrderId", payOrderRequest, ProcessResult.class);
		System.out.println(ret.toString());
		if(ret.getRetCode()==0)
		{
			String ls = JsonUtil.toJson(ret.getResponseInfo());
			PayOrderResponse payOrderResponse = JsonUtil.fromJson(ls, PayOrderResponse.class);
			
			PayGetOrderIdRspContent payGetOrderIdRspContent = JsonUtil.fromJson( payOrderResponse.getResponseContent(), PayGetOrderIdRspContent.class);
			this.orderId =payGetOrderIdRspContent.getOrderid();
		}
	}

	public void createQrCode()
	{
		ChatMsg chatMsg = new ChatMsg();
		chatMsg.setUserId("00855383001800");
		ReceiveMoneyQrCode receiveMoneyQrCode =new ReceiveMoneyQrCode();
		receiveMoneyQrCode.setAmount("101.00");
		receiveMoneyQrCode.setExpireTime(Calendar.getInstance().getTime());
		receiveMoneyQrCode.setRemark("010325132310102");
		receiveMoneyQrCode.setrType(ReceiveMoneyQrCode.RType_wechat);
		receiveMoneyQrCode.setStatus(receiveMoneyQrCode.STATUS_normal);
		receiveMoneyQrCode.setTalker("00855383001800");
		receiveMoneyQrCode.setUrl("http://test.talker.com/101");
		receiveMoneyQrCode.setUserId(chatMsg.getUserId());
		chatMsg.setContent(RSAUtils.encryptAsString(JsonUtil.toJson(receiveMoneyQrCode), imUserPublicKey));
		ProcessResult ret = this.restTemplate.postForObject(url+"wxPay/createReceiveQrCode", chatMsg, ProcessResult.class);
		System.out.println(ret.toString());
	
	}
	public void getQrCode()
	{
		PayOrderRequest chatMsg = new PayOrderRequest();
		chatMsg.setAppId("coojisu");
		chatMsg.setUserId("00855383001800");
		PayGetImContent receiveMoneyQrCode =new PayGetImContent();
		receiveMoneyQrCode.setAmount("0.02");
		receiveMoneyQrCode.setType(ReceiveMoneyQrCode.RType_wechat);
		//receiveMoneyQrCode.setType(0);
		receiveMoneyQrCode.setUserId(chatMsg.getUserId());
		receiveMoneyQrCode.setAppId(chatMsg.getAppId());
		receiveMoneyQrCode.setCurrency(ReceiveMoneyQrCode.Currency_RMB);
		receiveMoneyQrCode.setOrderId(orderId);
		
		chatMsg.setContent(RSAUtils.encryptAsString(JsonUtil.toJson(receiveMoneyQrCode), this.thirdApppublicKey));
		ProcessResult ret = this.restTemplate.postForObject(url+"serverAccess/getPayQrCode", chatMsg, ProcessResult.class);
		System.out.println(ret.toString());
	
	}
}
