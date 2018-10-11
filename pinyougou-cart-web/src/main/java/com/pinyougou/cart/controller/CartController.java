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
		
		//判定用户是否登录
		if(username.equals("anonymousUser")) {//如果未登录
			//从cookie中取出购物车列表
			String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
			if(cartListString==null||cartListString.equals("")) {
				//防止json转化报空指针异常
				cartListString="[]";
			}
			List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
			return cartList_cookie;
		}else {//如果登录了
			//从缓存中获取数据
			List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
			return cartList_redis;
		}
	
	}
	@RequestMapping("/addGoodsToCartList")
	public Result addGoodsToCartList(Long itemId,Integer num) {
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
