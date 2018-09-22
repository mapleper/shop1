package com.pinyougou.solrutil;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;

@Component
public class SolrUtil {
	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private SolrTemplate solrTemplate;
	/**
	 * 导入商品sku数据 
	 */
	public void importItemData() {
		TbItemExample example = new TbItemExample();
		Criteria criteria = example.createCriteria();
		//只查出已审核的数据
		criteria.andStatusEqualTo("1");
		
		List<TbItem> list = itemMapper.selectByExample(example);
		
		for (TbItem tbItem : list) {
			Map specMap = JSON.parseObject(tbItem.getSpec());
			tbItem.setSpecMap(specMap);//给带注解的字段赋值
			System.out.println(tbItem.getTitle());
		}
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
		
	}
	public static void main(String[] args) {
		ApplicationContext context=new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
		SolrUtil sorlUtil= (SolrUtil) context.getBean("solrUtil");
		sorlUtil.importItemData();
				
	}

}
