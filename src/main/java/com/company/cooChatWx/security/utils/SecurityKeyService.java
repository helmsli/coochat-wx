package com.company.cooChatWx.security.utils;

import java.security.PrivateKey;
import java.security.PublicKey;

import com.xinwei.nnl.common.domain.ProcessResult;

/**
 * 生成相应的公钥和私钥 
 * @author helmsli
 *
 */
public interface SecurityKeyService {
	
	/**
	 * 根据transid和电话号码获取私要
	 * @param transid
	 * @param phone
	 * @return
	 */
	public PrivateKey getPrivatekey(String transid);
	
	/**
	 * 
	 * @param transid
	 * @param phone
	 * @return
	 */
	public PublicKey getPublickey(String transid);
	
	/**
	 * 
	 * @param transid
	 * @param phone
	 * @return
	 */
	public ProcessResult createRsakeys(String transid);
}
