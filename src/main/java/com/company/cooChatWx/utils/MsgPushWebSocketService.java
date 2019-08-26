package com.company.cooChatWx.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.company.cooChatWx.controller.rest.ControllerUtils;
import com.xinwei.msgpush.domain.msg.OperMessage;
import com.xinwei.msgpush.domain.msg.TopicMessage;
import com.xinwei.msgpush.domain.msg.UserMessage;
import com.xinwei.nnl.common.domain.ProcessResult;

@Service("msgPushWebSocketService")
public class MsgPushWebSocketService {

	private  final Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${coojisu.msg-push.url}")
	private String restUrl;

	@Autowired
	private RestTemplate restTemplate;

	/**
	 * 	没有持久化的广播消息
	 * @param topicMessage
	 * @return
	 */
	public ProcessResult sendTopicMsg(TopicMessage topicMessage) {
		String url = restUrl + "/send_tms";
		logger.info("sendTopicMsg url=" + url + ",topicMessage=" + topicMessage);
		ProcessResult result = null;
		try {
			result = restTemplate.postForObject(url, topicMessage, ProcessResult.class);
		} catch (RestClientException e) {
			logger.error("sendTopicMsg error:", e);
			e.printStackTrace();
			result = ControllerUtils.getFromResponse(e, -1, result);
		}
		return result;
	}

	/**
	 * 	有持久化的广播消息
	 * @param operMessage
	 * @return
	 */
	public ProcessResult sendOperMsg(OperMessage operMessage) {
		String url = restUrl + "/send_oms";
		logger.info("sendOperMsg url=" + url + ",operMessage=" + operMessage);
		ProcessResult result = null;
		try {
			result = restTemplate.postForObject(url, operMessage, ProcessResult.class);
		} catch (RestClientException e) {
			logger.error("sendOperMsg error:", e);
			e.printStackTrace();
			result = ControllerUtils.getFromResponse(e, -1, result);
		}
		return result;
	}

	/**
	 *	 发送点对点的用户消息
	 * @param userMessage
	 * @return
	 */
	public ProcessResult sendUserMsg(UserMessage userMessage) {
		String url = restUrl + "/send_ums";
		logger.info("sendUserMsg url=" + url + ",userMessage=" + userMessage);
		ProcessResult result = null;
		try {
			result = restTemplate.postForObject(url, userMessage, ProcessResult.class);
		} catch (RestClientException e) {
			logger.error("sendUserMsg error:", e);
			e.printStackTrace();
			result = ControllerUtils.getFromResponse(e, -1, result);
		}
		return result;
	}
}
