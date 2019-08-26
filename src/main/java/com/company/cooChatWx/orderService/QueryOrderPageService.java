package com.company.cooChatWx.orderService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.nnl.common.util.JsonUtil;
import com.xinwei.userOrder.domain.QueryUserOrderRequest;

@Service("queryOrderPageService")
public class QueryOrderPageService {

	private static final Logger logger = LoggerFactory.getLogger(QueryOrderPageService.class);

	@Value("${order.orderDbUrl}")
	private String orderDbUrl;

	@Autowired
	protected RestTemplate restTemplate;

	/**
	 * 	调用 orderDB 里的分页查询
	 * @param queryRequest
	 * @return
	 */
	public ProcessResult getUserOrderPageInfo(QueryUserOrderRequest queryRequest) {
		if (queryRequest.getPageSize() <= 0) {
			queryRequest.setPageSize(100);
		}
		if (queryRequest.getPageNum() <= 0) {
			queryRequest.setPageNum(1);
		}
		if (queryRequest.getStartCreateTime() == null) {
			queryRequest.setStartCreateTime(getStartCreateTime());
		}
		String restUrl = orderDbUrl + "/" + queryRequest.getCategory() + "/" + queryRequest.getUserId() + "/queryUserOrder";
		logger.debug("getOrderList: post: " + restUrl);
		logger.debug("param=" + queryRequest);
		ProcessResult result = restTemplate.postForObject(restUrl, queryRequest, ProcessResult.class);
		if (result.getRetCode() != 0) {
			logger.error("getOrderList error");
			return result;
		}
		String jsonStr = JsonUtil.toJson(result.getResponseInfo());
		UserOrderQueryResult userOrderPageInfo = JsonUtil.fromJson(jsonStr, UserOrderQueryResult.class);
		result.setResponseInfo(userOrderPageInfo);
		logger.debug("after postForObject ret=" + result);
		return result;
	}

	private Date getStartCreateTime() {
		Date date = null;
		try {
			date = new SimpleDateFormat("yyyyMMdd HH:mm:ss").parse("20170910 00:00:00");
		} catch (ParseException e) {

			e.printStackTrace();
		}
		return date;
	}
}
