package com.pinyougou.seckill.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.Result;
import util.IdWorker;

@RestController
@RequestMapping("/pay")
public class PayController {
	@Reference(timeout=5000)
	private WeixinPayService weixinPayService;
	@Reference(timeout=5000)
	private SeckillOrderService seckillOrderService;
	
	/**
	 * 生成二维码
	 * @return
	 */
	@RequestMapping("/createNative")
	public Map createNative() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		//到 redis 提取秒杀订单
		TbSeckillOrder seckillOrder =seckillOrderService.searchOrderFromRedisByUserId(username);
		//判断秒杀订单存在
		if(seckillOrder!=null){
			long fen= (long)(seckillOrder.getMoney().doubleValue()*100);//金额（分）
			return weixinPayService.createNative(seckillOrder.getId()+"",fen+"");
		}else{
			return new HashMap();
		}
	
	}
	
	@RequestMapping("/queryPayStatus")
	public Result  queryPayStatus(String out_trade_no) {
		//这里需要用户名 是因为需要在缓存中根据这个key查询订单
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
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
				
				//若支付成功，修改订单状态  将订单存入数据库
				//orderService.updateOrderStatus(out_trade_no,map.get("transaction_id"));
				seckillOrderService.saveOrderFromRedisToDb(username,Long.valueOf(out_trade_no), map.get("transaction_id"));
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
