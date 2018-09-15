app.controller('indexController',function($scope,$controller,loginService) {
	
	
	//读取当前登录用户名
	$scope.showLoginName=function() {
		loginService.loginName().success(
				function(response){
				$scope.loginName=response.loginName;
					}
				);
	}
});