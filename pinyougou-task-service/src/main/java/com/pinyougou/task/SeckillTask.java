package com.pinyougou.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;

@Component
public class SeckillTask {
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	
	
	/**
	 * 每分钟刷新秒杀商品
	 */
	@Scheduled(cron="0/10 * * * * ?")
	public void refreshSeckillGoods(){
		//查询所有的秒杀商品键集合
		List ids = new ArrayList( redisTemplate.boundHashOps("seckillGoods").keys());
		//查询正在秒杀的商品列表
		TbSeckillGoodsExample example=new TbSeckillGoodsExample();
		 Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");//审核通过
		criteria.andStockCountGreaterThan(0);//剩余库存大于 0
		criteria.andStartTimeLessThanOrEqualTo(new Date());//开始时间小于等于当前时间
		criteria.andEndTimeGreaterThan(new Date());//结束时间大于当前时间
		if(ids.size()>0) {//当缓存中没有数据  拿出的key 通过List的构造方法创建的ids不是null 而是[] 会对criteria创建条件产生影响
			criteria.andIdNotIn(ids);//排除缓存中已经有的商品
		}
		
		List<TbSeckillGoods> seckillGoodsList=seckillGoodsMapper.selectByExample(example);
		//装入缓存
		for( TbSeckillGoods seckill:seckillGoodsList ){
			redisTemplate.boundHashOps("seckillGoods").put(seckill.getId(),seckill);
			System.out.println("更新秒杀商品，id:"+seckill.getId());
		}
		System.out.println("将"+seckillGoodsList.size()+"条商品装入缓存");
	}
	/**
	 * 移除过期秒杀商品   --当结束时间已到  秒杀商品还存在库存的  将它从缓存中删除
	 */
	@Scheduled(cron="* * * * * ?")
	public void removeSeckillGoods() {
		System.out.println("执行过期商品清除任务。。"+new Date());
		//从缓存中获取出秒杀商品列表
		List<TbSeckillGoods> seckillGoodsList=redisTemplate.boundHashOps("seckillGoods").values();
		for (TbSeckillGoods seckill : seckillGoodsList) {
			if(seckill.getEndTime().getTime()<new Date().getTime() ) {
				//如果已过期
				seckillGoodsMapper.updateByPrimaryKey(seckill);//向数据库保存记录
				redisTemplate.boundHashOps("seckillGoods").delete(seckill.getId());//移除缓存中商品数据
				System.out.println("商品ID为:"+seckill.getId()+"已过期，从缓存中删除");
			}
		}
	}
}
