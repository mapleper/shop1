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
});