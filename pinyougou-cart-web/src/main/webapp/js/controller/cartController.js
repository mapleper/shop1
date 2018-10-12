//购物车控制层
app.controller('cartController',function($scope,cartService){
	//查询购物车列表
	$scope.findCartList=function(){
		cartService.findCartList().success(
				function(response){
					$scope.cartList=response;
					$scope.totalValue=cartService.sum($scope.cartList);//求合计数
				}
		);
	}
	
	//添加商品到购物车
	$scope.addGoodsToCartList=function(itemId,num) {
		cartService.addGoodsToCartList(itemId,num).success(function(response) {
			if(response.success) {
				$scope.findCartList();//添加成功刷新页面
			}else{
				alert(response.message);//弹出错误提示
			}
		});
	}
	
	//获取当前等路人收货地址列表
	$scope.findAddressList=function() {
		cartService.findAddressList().success(function(response) {
			$scope.addressList=response;
			
			//加载默认地址
			for(var i=0;i<$scope.addressList.length;i++) {
				if($scope.addressList[i].isDefault=='1'){
					$scope.address=$scope.addressList[i];
					break;
				}
			}
		});
	}
	
	//选择地址
	$scope.selectAddress=function(address) {
		$scope.address=address;
	}
	
	//判断是否是被选中的地址
	$scope.isSelectedAddress=function(address) {
		if(address==$scope.address) {
			return true;
		}else{
			return false;
		}
	}
});