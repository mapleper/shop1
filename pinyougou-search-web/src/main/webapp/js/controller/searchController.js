app.controller('searchController',function($scope,searchService) {
	$scope.search=function() {
		$scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo) ;
		searchService.search($scope.searchMap).success(function(response) {
			$scope.resultMap=response;//搜索返回的结果
			buildPageLabel();//调用  得到标签码
		});
	}
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40};//搜索对象
	//添加搜索项
	$scope.addSearchItem=function(key,value) {
		if(key=='category'||key=='brand'||key=='price') {
			//点击的是品牌或者分类
			$scope.searchMap[key]=value;
		}else{
			//点击的是规格
			$scope.searchMap.spec[key]=value;
		}
		$scope.search();//执行搜索
	}
	//移除复合搜索条件--点击面包屑的X
	$scope.removeSearchItem=function(key) {
		if(key=='category'||key=='brand'||key=='price') {
			$scope.searchMap[key]="";
		}else{
			//否则是规格选项
			delete $scope.searchMap.spec[key];//移除此属性
		}
		$scope.search();//执行搜索
	}
	//构建分页标签
	buildPageLabel=function() {
		$scope.pageLabel=[];//新增分页栏属性
		//优化分页栏页码过多    先定义初始页码和最后页码
		var firstPage=1;//初始化开始页码
		var lastPage=$scope.resultMap.totalPages;//初始化截止页码
		if($scope.resultMap.totalPages>5) {//如果总页数大于5页 显示部分页码
			if($scope.searchMap.pageNo<=3) {//如果当前页小于等于3
				//两个对象间才会形成浅克隆的影响   数字则不会
				lastPage=5;
			}else if($scope.searchMap.pageNo>=$scope.resultMap.totalPages-2) {
				//最后5条记录
				firstPage=$scope.resultMap.totalPages-4;
			}else{
				//中间记录
				firstPage=$scope.searchMap.pageNo-2;
				lastPage=$scope.searchMap.pageNo+2;
			}
				
			
		}
		//循环产生页码标签
		for(var i=firstPage;i<=lastPage;i++){
			$scope.pageLabel.push(i);
		}

	}
	//根据指定页码查询
	$scope.queryByPage=function(pageNo) {
		//页码验证  防止页码溢出
		if(pageNo<1 || pageNo>$scope.resultMap.totalPages) {
			return;
		}
		$scope.searchMap.pageNo=pageNo;
		$scope.search();
	}
	
	//为上一页 下一页按钮定义判断方法
	//判断当前页为第一页
	$scope.isTopPage=function(){
		if($scope.searchMap.pageNo==1){
			return true;
		}else{
			return false;
		}
	}
	//判断当前页是否未最后一页
	$scope.isEndPage=function(){
	if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
			return true;
		}else{
			return false;
		}
	}

	
	
});