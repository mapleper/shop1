package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;

import entity.Result;
import pojogroup.Cart;
import util.CookieUtil;

@RestController
@RequestMapping("/cart")
public class CartController {
	@Reference(timeout=6000)
	private CartService cartService;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private HttpServletResponse response;
	
	
	
	@RequestMapping("/findCartList")
	public List<Cart> findCartList() {
		//获取登录名
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("当前登录人:"+username);
		
		//从cookie中取出购物车列表
		String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
		if(cartListString==null||cartListString.equals("")) {
			//防止json转化报空指针异常
			cartListString="[]";
		}
		List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
		
		//判定用户是否登录
		if(username.equals("anonymousUser")) {//如果未登录
			return cartList_cookie;
		}else {//如果登录了
			//从缓存中获取数据
			List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
			
			if(cartList_cookie.size()>0) {//cookie中存在购物车数据才进行合并
				//合并 
				cartList_redis = cartService.mergeCartList(cartList_redis, cartList_cookie);
				//清除本地cookie购物车数据
				CookieUtil.deleteCookie(request, response, "cartList");
				//将合并后的数据存入redis
				cartService.saveCartListToRedis(username, cartList_redis);
				
			}
			
			return cartList_redis;
		}
	
	}
	@RequestMapping("/addGoodsToCartList")
	//@CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
	public Result addGoodsToCartList(Long itemId,Integer num) {
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");//允许某个域访问
		response.setHeader("Access-Control-Allow-Credentials", "true");//允许使用cookie
		
		//获取登录名
			String username = SecurityContextHolder.getContext().getAuthentication().getName();
			System.out.println("当前登录人:"+username);
		
		try {
			List<Cart> cartList = findCartList();//获取购物车列表
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			
			if(username.equals("anonymousUser")) {//如果未登录  存入cookie
				CookieUtil.setCookie(request, response, "cartList", 
						JSON.toJSONString(cartList), 3600*24, "UTF-8");
				System.out.println("向cookie存入数据...");
			}else {//已登录  存入redis
				System.out.println("向redis存入数据...");
				cartService.saveCartListToRedis(username, cartList);
			}	
			return new Result(true, "添加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "添加失败");
		}
	}
}
