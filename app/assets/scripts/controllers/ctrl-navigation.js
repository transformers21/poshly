/**
 * @ngdoc function
 * @name productsApp.controller:NavigationCtrl
 * @description
 * # NavigationCtrl
 * Controller of the productsApp
 */

angular.module('productsApp')
  .controller('NavigationCtrl',
      ['$scope', '$uibModal', '$document', '$http', '$log', 'ENV', '$location',
      function($scope, $uibModal, $document, $http, $log, ENV, $location) {
    'use strict';

    $scope.personalizeCount = 14;
    $scope.isActive = function (viewLocation) {
      return viewLocation === $location.path();
    };

    $scope.redirectHome = function() {
      $http.get(ENV.apiEndpoint + 'v1/authenticate/logout/')
        .then(function successCallback() {
          window.location.reload(true);
        }, function errorCallback(error) {
          $log.error(error);
        });
    };

    $scope.animationsEnabled = true;

    $scope.open = function (size) {
      var modalInstance = $uibModal.open({
        animation: $scope.animationsEnabled,
        ariaLabelledBy: 'modal-title',
        ariaDescribedBy: 'modal-body',
        templateUrl: 'views/landing/login-modal.html',
        controller: 'ModalInstanceCtrl',
        size: size,
        resolve: {
          items: function () {
            return $scope.items;
          }
        }
      });
    };
}]);
