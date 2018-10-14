package com.pinyougou.pay.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;

import util.HttpClient;
@Service
public class WeixinPayServiceImpl implements WeixinPayService {
	@Value("${appid}")
	private String appid;
	@Value("${partner}")
	private String partner;
	@Value("${partnerkey}")
	private String partnerkey;

	
	
	@Override
	public Map createNative(String out_trade_no, String total_fee) {
		//1.创建参数
		Map<String,String> param=new HashMap();//创建参数
		param.put("appid", appid);//公众号
		param.put("mch_id", partner);//商户号
		param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
		param.put("body", "品优购");//商品描述
		param.put("out_trade_no", out_trade_no);//商户订单号
		param.put("total_fee",total_fee);//总金额（分）
		param.put("spbill_create_ip", "127.0.0.1");//IP
		param.put("notify_url", "http://test.itcast.cn");//回调地址(随便写)
		param.put("trade_type", "NATIVE");//交易类型
				
		try {
			//2.生成要发送的XML
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			System.out.println("生成的参数："+xmlParam);
			//利用HttpClient工具类发送请求  得到微信支付二维码地址
			HttpClient client=new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
			client.setHttps(true);
			client.setXmlParam(xmlParam);
			client.post();
			
			
			//3.获得结果--微信返回的url
			String result = client.getContent();
			System.out.println("微信返回的结果:"+result);
			Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
			
			Map<String, String> map=new HashMap<>();//因为微信返回的结果包含很多信息  我们返回需要的数据给前台
			map.put("code_url", resultMap.get("code_url"));
			map.put("total_fee", total_fee);//总金额
			map.put("out_trade_no",out_trade_no);//订单号
			
			return map;
			
			
		} catch (Exception e) {
			e.printStackTrace();
			
			return new HashMap<>();
		}
	
	}

}
