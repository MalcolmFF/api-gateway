package com.inspur.apigateway.pay.dao;

import com.inspur.apigateway.pay.data.PayLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface PayServiceMapper {

    //支付日志表
    void addPayLog(PayLog payLog);

    void updatePayLog(PayLog payLog);

    List<PayLog> queryPayLog(Map payLog);


    //账户资金表
//	public void addPayAccount(PayAccount act);
//	
//	public void updatePayAccount(PayAccount act);

    //public List<PayAccount> queryPayAccount(Map map);

}
