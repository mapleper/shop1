 //控制层 
app.controller('userController' ,function($scope,$controller   ,userService){	
	
	//注册
	$scope.reg=function() {
		if($scope.entity.password!=$scope.password) {
			 alert("两次输入的密码不一致，请重新输入");
			 return ;
		}
		userService.add($scope.entity).success(function(response) {
			alert(response.message);
		});
	}
});	
