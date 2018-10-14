package com.pinyougou.cart.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;

import entity.Result;
import util.IdWorker;

@RestController
@RequestMapping("/pay")
public class PayController {
	@Reference(timeout=5000)
	private WeixinPayService weixinPayService;
	
	@Reference(timeout=5000)
	private OrderService orderService;
	
	/**
	 * 生成二维码
	 * @return
	 */
	@RequestMapping("/createNative")
	public Map createNative() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		//到 redis 查询支付日志
		TbPayLog payLog = orderService.searchPayLogFromRedis(username);
		if(payLog!=null) {
			return weixinPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
			
		}else {
			return new HashMap<>();
		}
			
	}
	
	@RequestMapping("/queryPayStatus")
	public Result  queryPayStatus(String out_trade_no) {
		Result result=null;
		
		int x=0;
		while(true) {
			//调用查询接口
			Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);
			if(map==null) {//支付错误
				result=new Result(false, "支付出错");
				break;
			}
			if(map.get("trade_state").equals("SUCCESS")) {
				result=new Result(true, "支付成功");
				
				//若支付成功，修改订单状态
				orderService.updateOrderStatus(out_trade_no,map.get("transaction_id"));
				break;
			}
			
			
			try {
				Thread.sleep(3000);//让线程休息3秒，否则会一直调用查询接口
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//为了预防 用户一直未支付  而后端会一直调用查询状态    我们设置二维码失效的方法
			x++;
			if(x>=20) {//1分钟未付款二维码超时
				result=new Result(false, "二维码超时");
				break;
			}
		}
		return result;
	}
}
