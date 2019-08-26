package com.company.cooChatWx.mapper;



import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.company.cooChatWx.domain.ReceiveMoneyQrCode;





public interface ReceiveMoneyQrCodeMapper {
	
	//插入
	int insert(ReceiveMoneyQrCode receiveMoney);
	
	//更新根据remark  talker
	int updateReceiveMonByRemarAndTalk(ReceiveMoneyQrCode receiveMoney);
	
	//更新根据remark  
	int updateReceiveMonByRemark(ReceiveMoneyQrCode receiveMoney);
	
	
	//更新状态根据remark  talker
	int updateStatusMonByRemarTalk(ReceiveMoneyQrCode receiveMoney);
	
	
	//按照amount，Status 查询
	List<ReceiveMoneyQrCode> queryReceiveMoneyByAmoutAndStatus(ReceiveMoneyQrCode receiveMoney);
	
	
	//按照Remark 查询
	List<ReceiveMoneyQrCode> queryReceiveMoneyByRemark(@Param("remark") String remark);

}
