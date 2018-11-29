angular.module('productsApp').controller('ViewInsightsCtrl', [
      '$scope',
      '$uibModal',
      function($scope,$uibModal) {
      'use strict';

      $scope.insightsResults = [
      {
        insightsItem: 'Do you use matte lip color products?'
      },
      {
        insightsItem: 'Do you use matte lip color products?(Bollean)'
      },
      {
        insightsItem: 'In your opinion, what is the main drawback of using a matte lip color product?'
      },
      {
        insightsItem: 'In your opinion, what is the benefit of using a matte lip color product?'
      },
      {
        insightsItem: 'Moisturizing for Ultra HD Matte Lip Color (Revlon)'
      },
      {
        insightsItem: 'Reviews for The Matte Lip Color (Kevyn Aucoin)'
      },
      {
        insightsItem: 'Overall Rating for The Matte Lip Color (Kevyn Aucoin)'
      }
    ];

      $scope.tab = 1;

      $scope.setTab = function(newTab){
        $scope.tab = newTab;
      };

      $scope.isSet = function(tabNum){
        return $scope.tab === tabNum;
      };

      $scope.openInsights = function (size) {
        var modalInstance = $uibModal.open({
        ariaLabelledBy: 'modal-title',
        ariaDescribedBy: 'modal-body',
        templateUrl: 'views/modules/insights-modal.html',
        controller: 'ModalInstanceCtrl',
        backdropClass:'modal-backdrop',
        openedClass: 'modal-insights',
        size: size,
          resolve: {
            items: function () {
              return $scope.items;
            }
          }
        });
      };

      $scope.openFaq = function (size) {
        var modalInstance = $uibModal.open({
        ariaLabelledBy: 'modal-title',
        ariaDescribedBy: 'modal-body',
        templateUrl: 'views/modules/faq-modal.html',
        controller: 'ModalInstanceCtrl',
        backdropClass:'modal-backdrop',
        openedClass: 'modal-faq',
        size: size,
          resolve: {
            items: function () {
              return $scope.items;
            }
          }
        });
      };


      $scope.openResult = function (size) {
        var modalInstance = $uibModal.open({
        ariaLabelledBy: 'modal-title',
        ariaDescribedBy: 'modal-body',
        templateUrl: 'views/partials/result-modal.html',
        controller: 'ModalInstanceCtrl',
        backdropClass:'modal-backdrop',
        openedClass: 'modal-faq',
        size: size,
          resolve: {
            items: function () {
              return $scope.items;
            }
          }
        });
      };

      $scope.cancel = function () {
         $uibModalInstance.dismiss('cancel');
      };
      
    
  }
]);
