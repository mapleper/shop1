var app=angular.module('pinyougou',[]);//定义模块

//定义过滤器   用于信任html
app.filter('trustHtml',['$sce',function($sce) {
	return function(data) {
		return $sce.trustAsHtml(data);
	}
}]);