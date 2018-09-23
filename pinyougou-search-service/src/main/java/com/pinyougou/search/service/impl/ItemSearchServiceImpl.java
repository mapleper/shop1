package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
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
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Override
	public Map<String, Object> search(Map searchMap) {
		Map<String, Object> map =new HashMap<>();
		//添加查询条件
		
		/*Query query=new SimpleQuery("*:*");
		
		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);*/
		//1.根据关键字查询 高亮显示
		map.putAll(searchList(searchMap));
		//2.根据关键字查询商品分类
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);
		//3.查询品牌和规格列表
		if(categoryList.size()>0) {
			//可能存在多个品牌  我们取第一个展示
			map.putAll(searchBrandAndSpecList(categoryList.get(0)));
			System.out.println(searchBrandAndSpecList(categoryList.get(0))); 
			
		}
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
	/**
	 * 查询分类列表
	 * @return
	 */
	private List<String> searchCategoryList(Map searchMap) {
		List<String> list=new ArrayList<>();
		
		Query query=new SimpleQuery();
		//按照关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		//设置分组选项
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(groupOptions);
		//得到分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		//根据列得到分组结果集   因为分组选项可能根据多个field进行分组
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		//得到分组入口页
		Page<GroupEntry<TbItem>> entrieList = groupResult.getGroupEntries();
		//得到分组入口集合
		List<GroupEntry<TbItem>> content = entrieList.getContent();
		
		for (GroupEntry<TbItem> entry : content) {
			//将分组结果的名称封装到返回值中
			list.add(entry.getGroupValue());
		}
		return list;
	}
	/**
	 * 查询品牌和规格列表
	 * @return
	 */
	private Map searchBrandAndSpecList(String categoryName) {
		Map map =new HashMap<>();
		//获取到模板Id
		Long typeId=(Long) redisTemplate.boundHashOps("itemCat").get(categoryName);
		if(typeId!=null) {
			//根据模板ID查询品牌列表  从缓存中
			List brandList=(List) redisTemplate.boundHashOps("brandList").get(typeId);
			//返回值中添加品牌列表
			map.put("brandList", brandList);
			
			//根据模板ID查询规格列表
			List specList=(List) redisTemplate.boundHashOps("specList").get(typeId);
			map.put("specList", specList);
		}
		return map;
	}

}
