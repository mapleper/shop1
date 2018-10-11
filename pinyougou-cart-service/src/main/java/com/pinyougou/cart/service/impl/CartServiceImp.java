package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;

import pojogroup.Cart;
@Service
public class CartServiceImp implements CartService {
	
	@Autowired
	private TbItemMapper itemMapper;
	
	@Autowired
	private RedisTemplate redisTemplate;
	@Override
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
		//1.根据商品SKU ID查询SKU商品信息
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		if(item==null) {
			throw new RuntimeException("该商品不存在");
		}
		if(!item.getStatus().equals("1")) {
			throw new RuntimeException("该商品状态无效");
		}
		//2.获取商家ID
		String sellerId = item.getSellerId();
		//3.根据商家ID 在传入的cartList中查询是否存在该商家的购物车列表
		Cart cart = searchCartBySellerId(cartList, sellerId);
		
		if(cart==null) {//4.如果不存在该商家的购物车列表
			//4.1新建cart购物车对象
			cart=new Cart();//因为上面cart为null，直接用
			cart.setSellerId(sellerId);
			cart.setSellerName(item.getSeller());
			TbOrderItem orderItem=createOrderItem(item, num);
			List<TbOrderItem> orderItemList=new ArrayList<>();
			orderItemList.add(orderItem);
			cart.setOrderItemList(orderItemList);
			
			//4.2将新建的cart购物车对象添加到购物车列表
			cartList.add(cart);
			
		}else {//5.如果购物车列表存在该商家的购物车
			//查询购物车明细是否存在当前要添加的商品
			TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
			if(orderItem==null) {//5.1若不存在  新建购物车明细对象
				 orderItem = createOrderItem(item, num);
				 cart.getOrderItemList().add(orderItem);
			}else {//5.2若存在 在原购物车明细上添加数量 更改金额
				orderItem.setNum(orderItem.getNum()+num);
				orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*orderItem.getPrice().doubleValue()));
				//如果执行数量操作后 对应商品数量小于等于0  则移除(安全判定)
				if(orderItem.getNum()<=0) {
					cart.getOrderItemList().remove(orderItem);
				}
				//如果移除后cart中明细数量小于等于0  则将cart移除
				if(cart.getOrderItemList().size()==0) {
					cartList.remove(cart);
				}
			}
			
			
			
		}
		
		return cartList;
	}
	/**
	 * 在购物车列表里根据商家ID查询对应的购物车对象
	 * @param cartList
	 * @param sellerId
	 * @return
	 */
	private Cart searchCartBySellerId(List<Cart> cartList,String sellerId) {
		for (Cart cart : cartList) {
			if(cart.getSellerId().equals(sellerId)) {
				return cart;
			}
		}
		return null;
	}
	/**
	 * 创建购物车明细对象
	 * @param item
	 * @param num
	 * @return
	 */
	private TbOrderItem createOrderItem(TbItem item,Integer num) {
		if(num<=0) {
			throw new RuntimeException("数量非法！");
		}
		TbOrderItem orderItem=new TbOrderItem();
		orderItem.setGoodsId(item.getGoodsId());
		orderItem.setItemId(item.getId());
		orderItem.setNum(num);
		orderItem.setPicPath(item.getImage());
		orderItem.setPrice(item.getPrice());
		orderItem.setSellerId(item.getSellerId());
		orderItem.setTitle(item.getTitle());
		orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num) );
		return orderItem;
	}
	/**
	 * 根据商品SKUID在对应的某个商家 购物车对象的 orderItemList中查询是否有同样的购物车明细对象
	 * @param orderItemList
	 * @param itemId
	 * @return
	 */
	private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId) {
		for (TbOrderItem tbOrderItem : orderItemList) {
			//因为是Long   不能直接==比较 
			if(tbOrderItem.getItemId().longValue()==itemId.longValue()) {
				return tbOrderItem;
			}
		}
		return null;
	}
	/**
	 * 从缓存中获取购物车数据
	 */
	@Override
	public List<Cart> findCartListFromRedis(String username) {
		System.out.println("从redis提取购物车数据..."+username);
		List<Cart> cartList=(List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		if(cartList==null) {
			//非空处理
			cartList=new ArrayList<>();
		}
		
		return cartList;
	}
	/**
	 * 向redis中存入购物车数据
	 */
	@Override
	public void saveCartListToRedis(String username, List<Cart> cartList) {
		System.out.println("向redis中存入购物车数据..."+username);
		
		redisTemplate.boundHashOps("cartList").put(username, cartList);
	}

}
