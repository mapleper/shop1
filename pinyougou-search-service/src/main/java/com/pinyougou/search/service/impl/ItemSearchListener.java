package com.pinyougou.search.service.impl;

import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
@Component
public class ItemSearchListener implements MessageListener {
	@Autowired
	private ItemSearchService itemSearchService;
	@Override
	public void onMessage(Message message) {
		System.out.println("监听到消息");
		TextMessage textMessage=(TextMessage)message;
		try {
			String text = textMessage.getText();
			List<TbItem> list = JSON.parseArray(text, TbItem.class);
			//在对每一个SPU商品也要考虑到动态域的赋值
			for(TbItem item:list) {
				System.out.println(item.getId()+" "+item.getTitle());
				Map specMap= JSON.parseObject(item.getSpec());//给solr动态域赋值
				item.setSpecMap(specMap);//给带注解的字段赋值	
			}
			itemSearchService.importList(list);
			System.out.println("成功将数据导入索引库");
			
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
	}

}
