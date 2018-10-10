package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
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
	@Reference
	private CartService cartService;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private HttpServletResponse response;
	
	
	
	@RequestMapping("/findCartList")
	public List<Cart> findCartList() {
		//从cookie中取出购物车列表
		String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
		if(cartListString==null||cartListString.equals("")) {
			//防止json转化报空指针异常
			cartListString="[]";
		}
		List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
		return cartList_cookie;
		
	}
	@RequestMapping("/addGoodsToCartList")
	public Result addGoodsToCartList(Long itemId,Integer num) {
		try {
			List<Cart> cartList = findCartList();//获取购物车列表
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			CookieUtil.setCookie(request, response, "cartList", 
					JSON.toJSONString(cartList), 3600*24, "UTF-8");
			return new Result(true, "添加成功");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Result(false, "添加失败");
		}
	}
}
