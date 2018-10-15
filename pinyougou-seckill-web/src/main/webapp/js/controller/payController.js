app.controller('payController',function($scope,$location,payService) {
	//本地生成二维码
	$scope.createNative=function() {
		payService.createNative().success(function(response) {
			$scope.money= (response.total_fee/100).toFixed(2) ; //金额
			$scope.out_trade_no= response.out_trade_no;//订单号
			
			//生成二维码
			var qr=new QRious({
				element:document.getElementById('qrious'),
				size:250,
				level:'H',
				value:response.code_url//将返回的地址作为二维码的地址
			});
			
			queryPayStatus(response.out_trade_no);//查询支付状态
			
		});
	}
	
	//查询支付状态
	queryPayStatus=function(out_trade_no){
		payService.queryPayStatus(out_trade_no).success(
				function(response){
					if(response.success){//支付成功
						location.href="paysuccess.html#?money="+$scope.money;
					}else{//支付失败
						if(response.message=='二维码超时') {
							alert("支付时间已超时，订单失效")
							location.href="payTimeOut.html";
							
						}else{
							location.href="payfail.html";
						}
						
					}
				}
		);
	}
	
	//获取金额
	$scope.getMoney=function(){
		return $location.search()['money'];
	}
});