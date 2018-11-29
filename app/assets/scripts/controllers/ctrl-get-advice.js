'use strict';

angular.module('productsApp').controller('GetAdviceCtrl', ['$scope', '$location','CommunicationsService', 'Analytics', function($scope,$location, CommunicationsService, Analytics) {
	angular.extend($scope, Analytics);
	$scope.invalidMessage = false;
	$scope.message = '';
	$scope.message0 = '';
    $scope.requestFail = false;
    $scope.requestSuccess = false;

	$scope.getAdvice = function(productId) {
		$scope.invalidMessage = false;
		if(!$scope.message0 ) {
			$scope.invalidMessage = true;
			return;
		}
		var message = "Product : " + $location.absUrl()+'\n\n'+String($scope.message0) ;

		CommunicationsService.getAdvice(message,$location.path()).then(function(response) {
            $scope.message0='';
			if(response.error) {
				$scope.requestFail = true;
				$scope.requestSuccess = false;
				return;
			}
			$scope.requestFail = false;
			$scope.requestSuccess = true;
		});

	};

	$scope.close =function(){

	 	$scope.invalidMessage = false;
    	$scope.requestFail = false;
    	$scope.requestSuccess = false;
    	$scope.message0='';
	}

}]);
