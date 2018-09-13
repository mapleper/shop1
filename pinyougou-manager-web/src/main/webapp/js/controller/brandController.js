app.controller('brandController',function($scope,$controller,brandService){
		$controller('baseController',{$scope:$scope});
	
		$scope.findAll=function() {
			brandService.findAll().success(function(response){
				$scope.list=response;
			});
		}
		
		
		$scope.findPage=function(page,size) {
			brandService.findPage(page,size).success(
					function(response){
						$scope.list=response.rows;//显示当前页数据 	
						$scope.paginationConf.totalItems=response.total;//更新总记录数 
					}		
				);
			
		}
		$scope.save=function() {
			var object=null;
			if($scope.entity.id!=null) {
				object=brandService.update($scope.entity);
			}else{
				object=brandService.add($scope.entity);
			}
			object.success(
				function(response) {
					if(response.success) {
						$scope.reloadList();
					}else{
						//失败
						alert(response.message);
					}
				}		
			);
		}
		
		$scope.findOne=function(id) {
			brandService.findOne(id).success(
				function(response) {
					$scope.entity=response;
				}		
			);
		}
		
		
		$scope.dele=function() {
			if(confirm('你确定要删除吗')) {
				brandService.dele($scope.selectIs).success(
						function(response) {
							if(response.success) {
								$scope.reloadList();
								$scope.selectIds=[];
							}else{
								//失败
								alert(response.message);
								
							}
						}		
					);
			}
		}
		$scope.searchEntity={};
		$scope.search=function(page,size) {
			brandService.search(page,size,$scope.searchEntity).success(
					function(response){
						$scope.list=response.rows;//显示当前页数据 	
						$scope.paginationConf.totalItems=response.total;//更新总记录数 
						
					}		
				);
			
			
		}
		
	});