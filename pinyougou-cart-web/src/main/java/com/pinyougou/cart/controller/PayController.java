package com.pinyougou.cart.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;

import entity.Result;
import util.IdWorker;

@RestController
@RequestMapping("/pay")
public class PayController {
	@Reference(timeout=5000)
	private WeixinPayService weixinPayService;
	
	/**
	 * 生成二维码
	 * @return
	 */
	@RequestMapping("/createNative")
	public Map createNative() {
		IdWorker idWorker=new IdWorker();
		
		return weixinPayService.createNative(idWorker.nextId()+"","1");
	}
	
	@RequestMapping("/queryPayStatus")
	public Result  queryPayStatus(String out_trade_no) {
		Result result=null;
		while(true) {
			//调用查询接口
			Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);
			if(map==null) {//支付错误
				result=new Result(false, "支付出错");
				break;
			}
			if(map.get("trade_state").equals("SUCCESS")) {
				result=new Result(true, "支付成功");
				break;
			}
			try {
				Thread.sleep(3000);//让线程休息3秒，否则会一直调用查询接口
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}
