'use strict';

angular.module('productsApp').controller('ModalInstanceCtrl', [
  '$uibModalInstance',
  '$scope',
  'items',
  function ($uibModalInstance, $scope, items)
{
  $scope.items = items;

  $scope.ok = function () {
    $uibModalInstance.close($scope.items);
  };

  $scope.cancel = function () {
    $uibModalInstance.dismiss('cancel');
  };
}]);