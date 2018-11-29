angular.module('productsApp').controller('BrandsCtrl', [
  '$scope',
  '$rootScope',
  '$location',
  'SearchService',
  '$state',
  '$q',
  '$stateParams',
  '$uibModal',
  '$log',
  function($scope, $rootScope, SearchService, $state, $log){
    'use strict';
    $scope.brands = [];
    $scope.brandsPopular = [];
    $scope.brandsAll = [];
    $scope.addbrandsMessage = "";
    $scope.defaultProductImageURL = SearchService.defaultProductImageURL;
    $scope.searchText = '';
    $scope.allBrands = [
      {
        brandThumbnail: '../assets/images/brands/Clinique_logo.png',
        productBrand: 'Clinique'
      },
      {
        brandThumbnail: '../assets/images/brands/Bobbi_Brown_logo.jpg',
        productBrand: 'Bobbi Brown'
      },
      {
        brandThumbnail: '../assets/images/brands/Benefit_logo.jpg',
        productBrand: 'Benefit'
      },
      {
        brandThumbnail: '../assets/images/brands/origins_logo.jpg',
        productBrand: 'Origins'
      },
      {
        brandThumbnail: '../assets/images/brands/NARS_logo.jpg',
        productBrand: 'Nars'
      },
      {
        brandThumbnail: '../assets/images/brands/Tom_Ford_logo.jpg',
        productBrand: 'Tom Ford'
      },
      {
        brandThumbnail: '../assets/images/brands/glamglow_logo.jpg',
        productBrand: 'GlamGlow'
      },
      {
        brandThumbnail: '../assets/images/brands/philo_logo.jpg',
        productBrand: 'Philosophy'
      },
      {
        brandThumbnail: '../assets/images/brands/Gap.jpg',
        productBrand: 'Gap'
      },
      {
        brandThumbnail: '../assets/images/brands/Clinique_logo.png',
        productBrand: 'Clinique'
      },
      {
        brandThumbnail: '../assets/images/brands/Bobbi_Brown_logo.jpg',
        productBrand: 'Bobbi Brown'
      },
      {
        brandThumbnail: '../assets/images/brands/Benefit_logo.jpg',
        productBrand: 'Benefit'
      },
      {
        brandThumbnail: '../assets/images/brands/origins_logo.jpg',
        productBrand: 'Origins'
      },
      {
        brandThumbnail: '../assets/images/brands/NARS_logo.jpg',
        productBrand: 'Nars'
      },
      {
        brandThumbnail: '../assets/images/brands/Tom_Ford_logo.jpg',
        productBrand: 'Tom Ford'
      },
      {
        brandThumbnail: '../assets/images/brands/glamglow_logo.jpg',
        productBrand: 'GlamGlow'
      },
      {
        brandThumbnail: '../assets/images/brands/philo_logo.jpg',
        productBrand: 'Philosophy'
      },
      {
        brandThumbnail: '../assets/images/brands/Gap.jpg',
        productBrand: 'Gap'
      }
    ];

    $scope.changeList = function(listName) {
      $scope.chosenList = listName;
      SearchService.getProductIdsByUserIdListName(listName)
        .then(function(response) {
          $scope.productIdsInList = response.data;
          $scope.brandsInListWithDups = $scope.brands.filter(function( obj ) {
            return $scope.productIdsInList.indexOf(obj.productId) > -1;
          });
          $scope.brandsInList = removeDups($scope.brandsInListWithDups);
          if ($scope.brandsInList.length <= 0) {
            $scope.addbrandsMessage = "Choose a brand from the search bar and add it to this list!";
          } else {
            $scope.addbrandsMessage = "";
          }
        }, function(error) {
          $rootScope.showErrorAndGoToDashboard("Error getting brands: ", error);
        });
    };


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
