'use strict';

angular.module('productsApp').controller('RequestProductCtrl', ['$scope', 'CommunicationsService', 'Analytics', function($scope, CommunicationsService, Analytics) {
	angular.extend($scope, Analytics);
	$scope.invalidMessage = false;
	$scope.message = '';
	$scope.message0 = '';

    $scope.requestFail = false;
    $scope.requestSuccess = false;



	$scope.requestProduct = function() {
		$scope.invalidMessage = false;
		if(!$scope.message0 ) {
			$scope.invalidMessage = true;
			return;
		}
		var message = "Research Objective:\n " + String($scope.message0) ;

		CommunicationsService.requestProductV2(message).then(function(response) {
            $scope.message0=''
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
    	$scope.message0=''

	}

}]);
