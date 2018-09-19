 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体    修改商品信息回显
	$scope.findOne=function(){
		var id=$location.search()['id'];//获取页面传来的id
		if(id==null) {
			return;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				//富文本信息回显
				editor.html($scope.entity.goodsDesc.introduction);
				//转换图片数据格式  回显图片内容
				$scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
				//扩展属性回显    在这里转换后要注意前面  我们在级联分类列表查询时  扩展属性会  监听模板ID值去查询一次  会将这里得到的值覆盖  我们需要去处理
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);
			}
		);				
	}
	
	//保存 
	$scope.add=function(){				
		$scope.entity.goodsDesc.introduction=editor.html();				
		goodsService.add($scope.entity).success(
			function(response){
				if(response.success){
					alert("保存成功");
					$scope.entity={};
					editor.html('');//清空富文本编辑器
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	//文件上传
	$scope.uploadFile=function() {
		uploadService.uploadFile().success(function(response) {
			if(response.success) {
				$scope.image_entity.url=response.message;//设置文件地址

			}else{
				alert(response.message);
			}
			
			
		});
	}
	//商品图片
	$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};//定义页面实体结构
	//添加图片列表
	 $scope.add_image_entity=function(){
		 $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	 }
	 
	//列表中移除图片
	 $scope.remove_image_entity=function(index){
		 $scope.entity.goodsDesc.itemImages.splice(index,1);
	 }
	 
	 //读取一级分类下拉框
	 $scope.selectItemCat1List=function() {
		 itemCatService.findByParentId(0).success(function(response) {
			 $scope.itemCat1List=response;
		 });
	 }
	 //读取二级分类  用到了监听变量变化
	 $scope.$watch('entity.goods.category1Id',function(newValue,oldValue) {
		 //根据一级分类id的变化，查询出二级分类
		 //如果之前已选择 但前面分类再次改变  需要更新后面级别选项框
		 if(oldValue!=undefined) {
			 $scope.entity.goods.category2Id=-1;
			 //优化模板ID的回显  根据一级分类的选择而清空原来的模板ID
			 $scope.entity.goods.typeTemplateId=null;
		 }
		 //因为刚刷新页面 会触发访问一次   会避免前台异常 去掉newValue为null的情况  
		
		 if(newValue!=undefined) {
			 itemCatService.findByParentId(newValue).success(function(response) {
				 $scope.itemCat2List=response;
			 });
		 }
		 
	 });
	 //读取三级下拉列表
	 $scope.$watch('entity.goods.category2Id',function(newValue,oldValue) {
		 //根据一级分类id的变化，查询出二级分类
		 if(newValue!=undefined) {
			 itemCatService.findByParentId(newValue).success(function(response) {
				 $scope.itemCat3List=response;
			 }); 
		 }
		 
	 });
	 //读取模板ID
	 $scope.$watch('entity.goods.category3Id',function(newValue,oldValue) {
		 //根据一级分类id的变化，查询出二级分类
		
		 if(newValue!=undefined) {
			 itemCatService.findOne(newValue).success(function(response) {
				 $scope.entity.goods.typeTemplateId=response.typeId; //更新模板 ID
			 });
		 }
		  
	 });
	 //根据模板ID显示品牌列表  以及扩展属性   规格列表
	 $scope.$watch('entity.goods.typeTemplateId',function(newValue,oldValue) {
		 
		 if(newValue!=undefined&&newValue!=null) {
			 typeTemplateService.findOne(newValue).success(function(response) {
				 $scope.typeTemplate=response;
				 //将字符串形式传化成JSON对象  以在前台获取出数据
				 $scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds);
				 //读取模板中的扩展属性给商品的扩展属性赋值
				 if($location.search()['id']==null) {
					 //前面页面没有传入商品id  则不是回显  进行下面操作
					 $scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.typeTemplate.customAttributeItems);
				 }
				 
			 });
			 //查询规格列表
			 typeTemplateService.findSpecList(newValue).success(function(response) {
				 $scope.specList=response;
			 });
		 }
		 
		 
		 
	 });
	 
	 //$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};//定义页面实体结构
	 //上面已经初始化商品组合实体类   specificationItems:[]已经有初始值
	 
	 $scope.updateSpecAttribute=function($event,name,value) {
		 var object=$scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,
				 'attributeName',name);
		 
		 if(object!=null) {
			//已经有这种规格名称
			 if($event.target.checked) {
				 //被选上
				 object.attributeValue.push(value);
			 }else{
				 //取消勾选,移除该数据
				 object.attributeValue.splice( object.attributeValue.indexOf(value ) ,1);
				 if(object.attributeValue.length==0) {
					 //若该名称规格已没有被选上的值
					 $scope.entity.goodsDesc.specificationItems.splice(
					$scope.entity.goodsDesc.specificationItems.indexOf(object),1);
				 }
			 }
			 
		 }else{
			 $scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		 }
	 }
	 
	 //创建SKU列表  etity.itemList
	 $scope.createItemList=function() {
		 //先将itemList初始化   每次点击都是从这个初始值重新构建
		 $scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0' }];
		 var items= $scope.entity.goodsDesc.specificationItems;
		 //循环通过选择得到的specificationItems
		 for(var i=0;i<items.length;i++) {
			 $scope.entity.itemList=addColumn( $scope.entity.itemList,items[i].attributeName,items[i].attributeValue );
		 }
		 
		 
	 }
	 
	 //定义一个方法 用于itemList数据行列的添加   list--就是entity.itemList
	 addColumn=function(list,columnName,columnValues) {
		 var newList=[];//定义一个新的集合  
		 for(var i=0;i<list.length;i++) {
			 var oldRow=list[i];
			 for(var j=0;j<columnValues.length;j++) {
				 var newRow=JSON.parse(JSON.stringify( oldRow ));
				 //这里newRow.spec[columnName]这样写时因为  columnName是变量  
				 newRow.spec[columnName]=columnValues[j];
				 newList.push(newRow);
			 }
		 }
		 
		 return newList;
	 }
	 //提供一个商品状态数组  方便在前台显示商品状态
	 $scope.status=['未审核','已审核','审核未通过','关闭'];
	 
	 //提供一个分类名称数组  并初始化  方便前台获取数据
	 $scope.itemCatList=[];
	 $scope.findItemCatList=function() {
		 itemCatService.findAll().success(function(response) {
			 for(var i=0;i<response.length;i++) {
				 $scope.itemCatList[response[i].id]=response[i].name;
			 }
		 });
	 }
	 
	 
	 
	 
	
	
    
});	
