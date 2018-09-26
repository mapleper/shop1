package com.pinyougou.solrutil;



import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;

public class dealForSolr {
	public static void main(String[] args) {
		//删除所有数据
		ApplicationContext context=new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
		SolrTemplate solrTemplate=(SolrTemplate) context.getBean("solrTemplate");

		Query query=new SimpleQuery("*:*");
		solrTemplate.delete(query);
		solrTemplate.commit();
	}
}
