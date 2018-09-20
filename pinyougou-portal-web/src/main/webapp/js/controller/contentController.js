app.controller('contentController',function($scope,contentService) {
	//定义一个所有广告的集合
	$scope.contentList=[];
	$scope.findByCategoryId=function(categoryId) {
		contentService.findByCategoryId(categoryId).success(function(response) {
			$scope.contentList[categoryId]=response;
		});
	}
});