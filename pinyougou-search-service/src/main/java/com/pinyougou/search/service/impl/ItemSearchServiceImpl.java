package com.pinyougou.search.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
@Service(timeout=5000)
public class ItemSearchServiceImpl implements ItemSearchService {
	@Autowired
	private SolrTemplate solrTemplate;
	
	@Override
	public Map<String, Object> search(Map searchMap) {
		Map<String, Object> map =new HashMap<>();
		//添加查询条件
		
		/*Query query=new SimpleQuery("*:*");
		
		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);*/
		
		map.putAll(searchList(searchMap));
		return map;	
	}
	//查询列表
	private Map  searchList(Map searchMap) {
		Map map=new HashMap<>();
		
		//设置高亮选项
		HighlightQuery query=new SimpleHighlightQuery();
		//设置高亮的域
		HighlightOptions highlightOptions=new HighlightOptions().addField("item_title");
		highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮前缀
		highlightOptions.setSimplePostfix("</em>"); //高亮后缀
		query.setHighlightOptions(highlightOptions);
		
		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		//高亮页对象
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		//高亮入口集合(每条记录的高亮入口)
		List<HighlightEntry<TbItem>> highlightList = page.getHighlighted();
		for (HighlightEntry<TbItem> highlightEntry : highlightList) {
			/*//获取高亮列表  (高亮域的个数)
			List<Highlight> highlights = highlightEntry.getHighlights();
			for (Highlight h : highlights) {
				//每个域可能存在多个值
				List<String> list = h.getSnipplets();
				
			}*/
			
			TbItem item = highlightEntry.getEntity();//获取原实体类
			if(highlightEntry.getHighlights().size()>0 && highlightEntry.getHighlights().get(0).getSnipplets().size()>0) {
				//设置高亮的结果
				item.setTitle(highlightEntry.getHighlights().get(0).getSnipplets().get(0));
			}
		}
		map.put("rows", page.getContent());
		return map;	
	} 

}
