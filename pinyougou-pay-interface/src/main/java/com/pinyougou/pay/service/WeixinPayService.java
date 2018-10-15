package com.pinyougou.pay.service;

import java.util.Map;

public interface WeixinPayService {
	
	/**
	 * 生成微信支付二维码   携带参数发送请求到微信统一下单接口
	 * @param out_trade_no商户订单号
	 * @param total_fee金额(分)
	 * @return
	 */
	public Map createNative(String out_trade_no,String total_fee);
	
	/**
	 * 查询微信订单支付状态
	 * @param out_trade_no
	 * @return
	 */
	public Map queryPayStatus(String out_trade_no);
	
	/**
	 * 关闭微信支付订单
	 * @param out_trade_no
	 * @return
	 */
	public Map closePay(String out_trade_no);

}
