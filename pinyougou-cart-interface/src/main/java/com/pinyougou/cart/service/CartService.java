package com.pinyougou.cart.service;

import java.util.List;

import pojogroup.Cart;

public interface CartService {
	
	/**
	 * 添加商品到购物车
	 * @param cartList
	 * @param itemId
	 * @param num
	 * @return
	 */
	public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);
	
	/**
	 * 根据用户名从缓存中查出对应的购物车数据
	 * @param username
	 * @return
	 */
	public List<Cart> findCartListFromRedis(String username);
	
	/**
	 * 将购物车数据存入缓存
	 * @param username
	 * @param cartList
	 */
	public void saveCartListToRedis(String username,List<Cart> cartList);
}
