app.controller('contentController',function($scope,contentService) {
	//定义一个所有广告的集合
	$scope.contentList=[];
	$scope.findByCategoryId=function(categoryId) {
		contentService.findByCategoryId(categoryId).success(function(response) {
			$scope.contentList[categoryId]=response;
		});
	}
	//首页点击搜索跳转到搜索页的方法
	$scope.search=function() {
		location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
	}
});