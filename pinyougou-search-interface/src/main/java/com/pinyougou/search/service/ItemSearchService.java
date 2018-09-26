package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {
	
	public Map<String, Object> search(Map searchMap);
	
	//导入数据至索引库
	public void importList(List list);
	//根据SPU  ID删除索引库中对应数据
	public void deleteByGoodsIds(List goodsIdList);
}
