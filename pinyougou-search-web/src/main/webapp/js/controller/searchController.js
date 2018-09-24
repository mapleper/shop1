app.controller('searchController',function($scope,searchService) {
	$scope.search=function() {
		searchService.search($scope.searchMap).success(function(response) {
			$scope.resultMap=response;//搜索返回的结果
		});
	}
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{}};//搜索对象
	//添加搜索项
	$scope.addSearchItem=function(key,value) {
		if(key=='category'||key=='brand') {
			//点击的是品牌或者分类
			$scope.searchMap[key]=value;
		}else{
			//点击的是规格
			$scope.searchMap.spec[key]=value;
		}
	}
});