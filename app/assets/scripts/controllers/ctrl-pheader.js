/**
 * @ngdoc function
 * @name productsApp.controller:NavigationCtrl
 * @description
 * # NavigationCtrl
 * Controller of the productsApp
 */

angular.module('productsApp')
  .controller('PartialCtrl',
      ['$scope', '$uibModal', '$document', '$http', '$log', 'ENV', '$location',
      function($scope, $uibModal, $document, $http, $log, ENV, $location) {
    'use strict';

    $scope.personalize = function () {
    var modalInstance = $uibModal.open({
      ariaLabelledBy: 'modal-title',
      ariaDescribedBy: 'modal-body',
      templateUrl: 'views/modules/personalize-modal.html',
      controller: 'ModalInstanceCtrl',
      backdropClass:'modal-backdrop',
      openedClass: 'modal-personalize',
      resolve: {
        items: function () {
          return $scope.items;
        }
      }
    });
    }

}]);
