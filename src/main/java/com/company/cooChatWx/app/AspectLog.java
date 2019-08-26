/**
 * 
 */
package com.company.cooChatWx.app;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 日志切面类 
 */
//申明是个切面
@Aspect
//申明是个spring管理的bean
@Component
@Order(2)
public class AspectLog {
	private Logger log = LoggerFactory.getLogger(getClass());

	//申明一个切点 里面是 execution表达式 com.company.coursestudent
	@Pointcut("execution(public * com.company.cooChatWx.history.mapper..*.*(..)) ||  execution(public * com.company.cooChatWx.history.service..*.*(..)) ||  execution(public * com.company.cooChatWx.user.service..*.*(..)) ||  execution(public * com.company.cooChatWx.account..*.*(..)) ||  execution(public * com.company.cooChatWx.account.recharge..*.*(..)) ||  execution(public * com.company.cooChatWx.service..*.*(..)) ||  execution(public * com.company.cooChatWx.controller.rest..*.*(..)) ")
	public void mylogPoint() {
	}

	
	@Before(value = "mylogPoint()")
	
	public void methodBefore(JoinPoint joinPoint) {
		if ("queryStatus".compareToIgnoreCase(joinPoint.getSignature().getName()) == 0) {
			log = LoggerFactory.getLogger("com.xinwei.queryStatus");

		} else if ("left".compareToIgnoreCase(joinPoint.getSignature().getName()) == 0) {
			log = LoggerFactory.getLogger("com.xinwei.left");

		} else {
			log = LoggerFactory.getLogger(joinPoint.getTarget().getClass().getName());
		}
		StringBuilder ls = new StringBuilder();
		ls.append("before ");
		ls.append(joinPoint.getTarget().getClass().getName());
		ls.append(".");
		ls.append(joinPoint.getSignature().getName());
		ls.append(" ,args:");
		ls.append(Arrays.toString(joinPoint.getArgs()));
		log.info(ls.toString());
		ls = null;
	}

	/**
	 * 在方法执行完结后打印返回内容
	 * @param joinPoint
	 * @param o
	 */
	@AfterReturning(returning = "o", pointcut = "mylogPoint()")
	public void methodAfterReturing(JoinPoint joinPoint, Object o) {
		
		StringBuilder ls = new StringBuilder();
		ls.append("after ");
		ls.append(joinPoint.getTarget().getClass().getName());
		ls.append(".");
		ls.append(joinPoint.getSignature().getName());
		ls.append(", Response:");
		ls.append(o);
		log.info(ls.toString());
		ls = null;
	}

}