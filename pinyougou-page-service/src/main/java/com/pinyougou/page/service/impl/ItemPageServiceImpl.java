package com.pinyougou.page.service.impl;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;


import freemarker.template.Configuration;
import freemarker.template.Template;

@Service
public class ItemPageServiceImpl implements ItemPageService {
	@Value("${pagedir}")
	private String pagedir;
	@Autowired
	private FreeMarkerConfig freeMarkerConfig;
	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	@Override
	public boolean genItemHtml(Long goodsId) {
		Configuration configuration = freeMarkerConfig.getConfiguration();
		try {
			Template template = configuration.getTemplate("item.ftl");
			Map dataModel=new HashMap<>();
			//1.加载商品表数据
			TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
			dataModel.put("goods", goods);
			//2.加载商品描述表数据
			TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
			dataModel.put("goodsDesc", goodsDesc);
			
			Writer out=new FileWriter(pagedir+goodsId+".html");//指定写到的路径及文件名
			template.process(dataModel, out);
			out.close();
			return true;
			
		} catch (Exception e) {			
			e.printStackTrace();
			return false;
		} 
		
	}

}
