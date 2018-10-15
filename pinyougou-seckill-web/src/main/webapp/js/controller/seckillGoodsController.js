//控制层
app.controller('seckillGoodsController' ,function($scope,$location,$interval,seckillGoodsService){
	//读取列表数据绑定到表单中
	$scope.findList=function(){
		seckillGoodsService.findList().success(
				function(response){
					$scope.list=response;
				}
		);
	}
	
	//查询实体
	$scope.findOne=function(){
		seckillGoodsService.findOne($location.search()['id']).success(
				function(response){
					$scope.entity= response;
					//秒杀业务倒计时
					//倒计时总秒数
					allsecond=Math.floor( ( new Date($scope.entity.endTime).getTime()-(new Date().getTime())) /1000 );
					
					time=$interval(function(){
						if(allsecond<=0) {
							 $interval.cancel(time);
							 alert("秒杀服务已结束");
						}else{
							allsecond =allsecond-1;
							$scope.timeString=convertTimeString(allsecond);//转换时间字符串
						}
					},1000);
				}
		);
	}
	
	//转换倒计时秒 格式
	convertTimeString=function(allsecond){
		var days= Math.floor( allsecond/(60*60*24));//天数
		var hours= Math.floor( (allsecond-days*60*60*24)/(60*60) );//小时数
		var minutes= Math.floor( (allsecond -days*60*60*24 - hours*60*60)/60 );//分钟数
		var seconds= allsecond -days*60*60*24 - hours*60*60 -minutes*60; //秒数
		var timeString="";
		if(days>0){
			timeString=days+"天 ";
		}
		return timeString+hours+":"+minutes+":"+seconds;
	}
});