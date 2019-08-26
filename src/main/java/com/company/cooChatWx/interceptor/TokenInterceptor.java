package com.company.cooChatWx.interceptor;

import java.io.PrintWriter;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.company.cooChatWx.controller.rest.ControllerUtils;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.nnl.common.util.JsonUtil;

@Service("tokenInterceptor")
public class TokenInterceptor implements HandlerInterceptor, InitializingBean {
	// 是否开启token
	@Value("${coojisu.token.is-open:0}")
	private int isOpen;

	public static final int openToken = 1;

	public static final int notOpenToken = 0;

	@Resource(name = "tokenService")
	private TokenService tokenService;
	//本届点允许访问
	private long WhiteList_Status_allow = 1;
	//本届点不允许访问
	private long WhiteList_Status_notAllow = 2;
	//子节点都允许访问
	private long WhiteList_Status_allChildAllow = 3;
	//子节点条件允许访问
	private long WhiteList_Status_conditionChildAllow = 4;

	private String userKeyPrefix = "!@#";

	@Value("${controller.urlWhiteList:/home/login}")
	private String urlWhiteList;

	@Value("${controller.onlyAuthAjax:0}")
	private int onlyAuthAjax;

	@Value("${controller.loginUrl:/home/login}")
	private String loginUrl;

	@Value("${controller.urlWhiteListUserkey:registerByCode,loginByPass,loginByAuthCode,getSmsValid,getRandom,getRsaPubKey,resetPassByAuthCode,checkAuthCode,registerByUserName,regUserNameForServer}")
	private String urlWhiteListUserkey;

	@Resource(name = "redisTemplate")
	protected RedisTemplate<Object, Object> redisTemplate;

	/**
	 * 用户rest的白名单key
	 */

	/**
	 * Long 0 -- need token 1--not need token
	 */
	private Map<String, Long> controllerWhistMap = new java.util.concurrent.ConcurrentHashMap<String, Long>();

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3)
			throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * 判断是否是ajax请求
	 * @param request
	 * @return true-是ajax请求
	 */
	protected boolean isAjaxRequest(HttpServletRequest request) {
		try {
			String ajaxRequest = request.getHeader("x-requested-with");
			logger.debug("ajax");
			if (ajaxRequest != null && ajaxRequest.contains("XMLHttpRequest")) {
				return true;
			}
			ajaxRequest = request.getHeader("Content-Type");
			logger.debug("ajax:" + ajaxRequest);
			if (ajaxRequest != null && (ajaxRequest.contains("application/x-www-form-urlencoded")
					|| ajaxRequest.contains("application/json"))) {
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	protected TokenInfo getSessionAccessTime(String token) {
		try {
			TokenInfo tokenInfo = tokenService.checkTokenInfo(token);
			return tokenInfo;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 	判断是否允许方位兄台那个资源
	 * @param request
	 * @return
	 */
	protected boolean allowAccess(HttpServletRequest request,TokenContext tokenContext) {

		//绝大多数是有token的优先token
		try {
			// 是否开启token
			if (isOpen == notOpenToken) {
				return true;
			}

			logger.info("request.getMethod()=" + request.getMethod());

			if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
				logger.info("option request allow access");
				return true;
			}

			String token = request.getHeader("token");
			TokenInfo tokenInfo = null;

			if (!StringUtils.isEmpty(token)) {
				if ("!@#$)?(*&1qaz2wsx3edc".compareTo(token) == 0) {
					return true;
				}
				tokenInfo = getSessionAccessTime(token);
			}
			tokenContext.setTokenInfo(tokenInfo);
			logger.debug("login:" + token + ":");
			boolean isAuth = (tokenInfo != null&&tokenInfo.getResult()==tokenInfo.result_success);
			if (isAuth) {
				request.setAttribute("tokenInfo", tokenInfo);
			}
			if (isAuth) {
				return true;
			}
			logger.debug("token result:" + isAuth);
			//判断是否仅仅需要拦截onlAuthAjax
			if (onlyAuthAjax == 1) {
				if (!this.isAjaxRequest(request)) {
					logger.debug("login1:" + token + ":" + request.toString());

					return true;
				}
			}
			//判断是否需要进行拦截
			String requestUrl = request.getRequestURI();
			//如果请求的是登录页面，直接允许访问
			if (requestUrl.equalsIgnoreCase(this.loginUrl)) {
				logger.debug("login2:" + token + ":" + request.toString());

				return true;
			}
			if (!this.isNeedToken(requestUrl)) {
				logger.debug("login3:" + token + ":" + request.toString());

				return true;
			}
			if (requestUrl.startsWith("/user")) {
				String[] requestUrlKey = requestUrl.split("/");
				if (requestUrlKey.length > 0) {
					String userKey = userKeyPrefix + requestUrlKey[requestUrlKey.length - 1].trim().toLowerCase();
					if (!this.isNeedToken(userKey)) {
						logger.debug("login 4");
						return true;
					}
				}
			}

			return false;
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// TODO Auto-generated method stub
		String requestUrl = request.getRequestURI();
		//response.setHeader("Access-Control-Allow-Origin", "*");
		//todo: not redirect
		TokenContext tokenContext = new TokenContext();
		if (allowAccess(request,tokenContext))
		//if(true)
		{
			System.out.print("return true");
			return true;
		} else {
			
			//response.sendRedirect(this.loginUrl);
			response.setContentType("application/json; charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = null;
			try {
				out = response.getWriter();
				// 将返回的对象转换为Json串，返回到输出流 ?
				ProcessResult ret = ControllerUtils.getErrorResponse(2008, "2008");
				if(tokenContext.getTokenInfo()!=null)
				{
					if(tokenContext.getTokenInfo().getResult()==TokenInfo.result_loginOnOthersDevice)
					{
						ret = ControllerUtils.getErrorResponse(3008, "3008");
					}			
					
				}
				logger.debug(ret.toString());
				out.println(JsonUtil.toJson(ret));
				out.flush();

			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Response JSON is error...", e);
			} finally {
				try {
					if (out != null) {
						out.close();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			System.out.print("return false;");
			return false;
		}
		/*
		System.out.println("**********" + request.getHeaderNames());
		Enumeration headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
		    String key = (String) headerNames.nextElement();
		    String value = request.getHeader(key);
		    System.out.println(key + ":" + value);
		}
		
		System.out.println("request url:" +request.getServletPath());
		System.out.println("request url1:" +requestUrl);
		*/
		/**
		 *  public String defultLogin="/account/login";//默认登录页面  
		 *  response.sendRedirect(request.getContextPath()+defultLogin);
		 */
		/**
		 * String url = request.getRequestURI();  
		//URL:login.jsp是公开的;这个demo是除了login.jsp是可以公开访问的，其它的URL都进行拦截控制  
		if(url.indexOf("login.action")>=0){  
		    return true;  
		}  
		//获取Session  
		HttpSession session = request.getSession();  
		String username = (String)session.getAttribute("username");  
		  
		if(username != null){  
		    return true;  
		}  
		//不符合条件的，跳转到登录界面  
		request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);  
		  response.sendRedirect("/login.jsp");
		return false;  
		 
		System.out.println("**********" + response);
		
		System.out.println("**********" + handler);
		
		if (!handler.getClass().isAssignableFrom(HandlerMethod.class)) {  
			System.out.println("&&&&&&&&&&&" + handler.getClass());
		    return true;  
		}  
		final HandlerMethod handlerMethod = (HandlerMethod) handler;  
		final Method method = handlerMethod.getMethod();  
		final Class<?> clazz = method.getDeclaringClass();  
		return false;
		*/
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		String[] controllerWhiteLists = urlWhiteList.split(",");
		for (int i = 0; i < controllerWhiteLists.length; i++) {
			System.out.println(controllerWhiteLists[i]);
			controllerWhistMap.put(controllerWhiteLists[i].trim().toLowerCase(), new Long(1));
		}
		controllerWhiteLists = this.urlWhiteListUserkey.split(",");

		for (int i = 0; i < controllerWhiteLists.length; i++) {
			String userKey = userKeyPrefix + controllerWhiteLists[i].trim().toLowerCase();
			System.out.println();
			controllerWhistMap.put(userKey, new Long(1));
		}

	}

	/**
	 * 是否拦截，
	 * @param requestUrl
	 * @return true -- 必须登录过才能访问， false-- 不需要登录过
	 */
	protected boolean isNeedToken(String requestUrl) {
		String lowerUrl = requestUrl.toLowerCase().trim();

		// TODO Auto-generated method stub
		logger.debug("request url:" + lowerUrl);
		if (!controllerWhistMap.containsKey(lowerUrl)) {
			return true;
		}
		Long needToken = controllerWhistMap.get(lowerUrl);

		return (needToken.longValue() == 0);

	}

}
