package com.inspur.apigateway.pay.dao;


import com.inspur.apigateway.pay.data.PayAccountCapital;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PayAccountMapper {

	/**
	 * 查询账户余额
	 * @param userId
	 * @return
	 */
	PayAccountCapital getPayAccount(String userId);

	void insertPayAccount(PayAccountCapital payAccount);

	void addPayAccount(PayAccountCapital payAccount);
	void subPayAccount(PayAccountCapital payAccount);
}
