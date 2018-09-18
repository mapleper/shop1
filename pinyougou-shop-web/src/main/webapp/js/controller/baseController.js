app.controller('baseController',function($scope) {
	//分页控件配置currentPage:当前页   totalItems :总记录数  itemsPerPage:每页记录数  perPageOptions :分页选项  onChange:当页码变更后自动触发的方法 
	$scope.paginationConf = {
		currentPage: 1,
		totalItems: 10,
		itemsPerPage: 10,
		perPageOptions: [10, 20, 30, 40, 50],
		onChange: function(){
			$scope.reloadList();
		}
	};
	$scope.reloadList=function() {
		$scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
	}
	$scope.selectIds=[];
	$scope.updateSelection=function($event,id) {
		if($event.target.checked) {
			//如果被选中就被加入到数组
			$scope.selectIds.push(id);
		}else{
			//没有选中就删除其id
			var index=$scope.selectIds.indexOf(id);
			$scope.selectIds.splice(index,1);
		}
	}
	
	$scope.jsonToString=function(jsonString,key) {
		var json=JSON.parse(jsonString);//将json字符串转化为json对象
		var value="";
		for(var i=0;i<json.length;i++) {
			if(i>0) {
				value+=",";
			}
			value+=json[i][key];
		}
		return value;
	}
	
	//定义方法 从一个list集合中根据key查询对象
	$scope.searchObjectByKey=function(list,key,keyValue) {
		for(var i=0;i<list.length;i++) {
			if(list[i][key]==keyValue) {
				return list[i];
			}
		}
		return null;
	}
});