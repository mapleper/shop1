app.controller('searchController',function($scope,searchService) {
	$scope.search=function() {
		searchService.search($scope.searchMap).success(function(response) {
			$scope.resultMap=response;//搜索返回的结果
		});
	}
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':''};//搜索对象
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
});