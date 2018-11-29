angular.module('productsApp').controller('ListsItemCtrl', [
  '$scope',
  'SearchService',
  '$log',
  '$state',
  '$rootScope',
  function($scope,
           SearchService,
           $log,
           $state,
           $rootScope){
    'use strict';
     $scope.allFavoriteThings  = [
      { 
        productId: 1,
        thingThumbnail: '../assets/images/newest-products/flowerbomb-cream.png',
        productBrand: 'Clinique'
      },
      {
        productId: 2,
        thingThumbnail: '../assets/images/newest-products/flowerbomb-shower.png',
        productBrand: 'Bobbi Brown'
      },
      {
        productId: 3,
        thingThumbnail: '../assets/images/newest-products/spicebomb-fresh.png',
        productBrand: 'Benefit'
      },
      {
        productId: 4,
        thingThumbnail: '../assets/images/newest-products/flowerbomb-precious.png',
        productBrand: 'Origins'
      }
    ];

    $scope.onOpenClose = function (isOpen) {
      $scope.doPushDown = isOpen;
    };

    //Infinite Scroll Magic
    $scope.infiniteScroll = {};
    $scope.infiniteScroll.numToAdd = 20;
    $scope.infiniteScroll.currentItems = 20;

    $scope.resetInfScroll = function() {
      $scope.infiniteScroll.currentItems = $scope.infiniteScroll.numToAdd;
    };
    $scope.addMoreItems = function(){
      $scope.infiniteScroll.currentItems += $scope.infiniteScroll.numToAdd;
    };
    
  }  
]);




