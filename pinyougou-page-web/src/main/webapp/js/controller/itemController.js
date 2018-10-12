//商品详细页（控制层）
app.controller('itemController',function($scope,$http){
	//数量操作
	$scope.addNum=function(x){
		$scope.num=$scope.num+x;
		if($scope.num<1){
			$scope.num=1;
		}
	}
	
	$scope.specificationItems={};//记录用户选择的规格
	//用户选择规格  影响变量
	$scope.selectSpecification=function(name,value){
		$scope.specificationItems[name]=value;
		
		searchSku();//读取 sku
	}
	//判断某规格选项是否被选中
	$scope.isSelected=function(name,value){
		if($scope.specificationItems[name]==value){
			return true;
		}else{
			return false;
		}
	}
	
	$scope.sku={};//当前选择的SKU
	
	//加载默认的SKU  就是后台数据库中is_default的
	$scope.loadSku=function(){
		$scope.sku=skuList[0];
		//同时页面加载时默认选上  
		$scope.specificationItems= JSON.parse(JSON.stringify($scope.sku.spec)) ;
	}
	
	//匹配两个对象
	matchObject=function(map1,map2){
		for(var key in map1){
			if(map1[key]!=map2[key]){
				return false;
			}
		}
		for(var key in map2){
			if(map2[key]!=map1[key]){
				return false;
			}
		}
		return true;
		
	}
	
	//查询SKU--查询选择的规格是否在skuList中存在 
	searchSku=function(){
		for(var i=0;i<skuList.length;i++){
			if(matchObject(skuList[i].spec ,$scope.specificationItems)){
				$scope.sku=JSON.parse(JSON.stringify(skuList[i])) ;//防止浅克隆
				return;
			}
		}
		$scope.sku={id:0,title:'----这种商品已卖光了----',price:0};//如果没有匹配的
	}
	
	//添加商品到购物车
	$scope.addToCart=function(){
		//alert('skuid:'+$scope.sku.id);
		//跨域异步请求
		$http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='
				+ $scope.sku.id +'&num='+$scope.num,{'withCredentials':true}).success(function(response) {
					if(response.success) {
						location.href='http://localhost:9107/cart.html';//跳转到购物车页面
					}else{
						alert(response.message);
					}
				});
	}
	
});