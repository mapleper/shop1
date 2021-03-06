package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
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
		//关键字的空格处理--将空格替换成空字符串
		String keywords=(String) searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));
		
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
		String categoryName=(String) searchMap.get("category");
		if(!"".equals(categoryName)) {
			//如果传入了分类名称  则就是点击了分类
			map.putAll(searchBrandAndSpecList(categoryName));
		}else {
			//若没有就按第一个查询
			if(categoryList.size()>0) {
				//可能存在多个品牌  我们取第一个展示
				map.putAll(searchBrandAndSpecList(categoryList.get(0)));
				System.out.println(searchBrandAndSpecList(categoryList.get(0))); 
				
			}
		}
		return map;	
	}
	//查询列表--数据结果列表
	private Map  searchList(Map searchMap) {
		Map map=new HashMap<>();
		
		//设置高亮选项
		HighlightQuery query=new SimpleHighlightQuery();
		//设置高亮的域
		HighlightOptions highlightOptions=new HighlightOptions().addField("item_title");
		highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮前缀
		highlightOptions.setSimplePostfix("</em>"); //高亮后缀
		query.setHighlightOptions(highlightOptions);
		
		//1.1关键字查询
		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		//1.2按商品分类筛选
		if(!"".equals(searchMap.get("category"))) {
			FilterQuery filterQuery=new SimpleFilterQuery();
			Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		//1.3按品牌过滤
		if(!"".equals(searchMap.get("brand"))) {
			FilterQuery filterQuery=new SimpleFilterQuery();
			Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		//1.4按规格过滤
		//这里好像本身就不为null
		if(searchMap.get("spec")!=null) {
			Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
			for(String key:specMap.keySet()) {		
				Criteria filterCriteria=new Criteria("item_spec_"+key).is(specMap.get(key));
				FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}
		//1.5按价格过滤
		if(!"".equals(searchMap.get("price"))) {
			String priceStr=(String) searchMap.get("price");
			String[] price= priceStr.split("-");//得到的是一个  [x,y]的两个值
			
			if(!price[0].equals("0")) {
				//如果起始值大于0  
				Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(price[0]);
				FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);	
			}
			if(!price[1].equals("*")) {
				//如果区间最大值不为*
				Criteria filterCriteria=new Criteria("item_price").lessThanEqual(price[1]);
				FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
			
			
		}
		//1.6分页查询
		Integer pageNo=(Integer) searchMap.get("pageNo");
		if(pageNo==null) {
			//若没有指定当前页 默认为1
			pageNo=1;
		}
		Integer pageSize=(Integer) searchMap.get("pageSize");//每页记录数
		if(pageSize==null) {
			pageSize=40;
		}
		//设置从第几条记录查询    记录索引
		query.setOffset((pageNo-1)*pageSize);
		//设置每页记录条数
		query.setRows(pageSize);
		
		//1.7排序
		String sortValue=(String) searchMap.get("sort");//得到排序的样式 ASC DESC
		String sortField=(String) searchMap.get("sortField");//得到按照哪个域排序
		
		if(sortValue!=null&&!sortField.equals("")) {
			if(sortValue.equals("ASC")) {
				//若是升序				
				Sort sort=new Sort(Sort.Direction.ASC, "item_"+sortField);
				query.addSort(sort);
			}
			if(sortValue.equals("DESC")) {
				//降序
				Sort sort=new Sort(Sort.Direction.DESC, "item_"+sortField);
				query.addSort(sort);
			}
		}
		
		
		//*********获取高亮结果集
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
		map.put("totalPages", page.getTotalPages());//返回总页数
		map.put("total", page.getTotalElements());//返回总记录数
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
	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
		
	}
	/**
	 * 根据SPU  ID删除索引库中数据
	 */
	@Override
	public void deleteByGoodsIds(List goodsIdList) {				
		Query query=new SimpleQuery();		
		Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
	}

}
