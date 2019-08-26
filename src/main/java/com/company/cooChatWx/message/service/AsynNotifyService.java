package com.company.cooChatWx.message.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.xinwei.nnl.common.domain.JsonRequest;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.orderDb.domain.OrderMainContext;
@Service("asynNotifyService")
public class AsynNotifyService {
	OrderMainContext orderMainContext = new OrderMainContext();
	protected Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	protected RestTemplate restTemplate;
	@Async
	public void asyncProcess(String url,String category,String orderId,String callMethod)
	{
		try {
			String dbId = orderMainContext.getDbId(orderId);
			String postUrl = url+ category +"/"+dbId + "/" + orderId + "/" + callMethod;
			JsonRequest jsonRequest = new JsonRequest();
			restTemplate.postForObject(postUrl, jsonRequest, ProcessResult.class);
		} catch (RestClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("",e);
		}
	}
}
