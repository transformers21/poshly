angular.module('productsApp').controller('DashboardCtrl', [
  '$scope', '$rootScope', 'SearchService','$state', '$log',
  function($scope, $rootScope, SearchService, $state, $log){
    'use strict';
    $scope.products = [];
    $scope.productsPopular = [];
    $scope.productsAll = [];
    $scope.allProductLists = SearchService.getAllProductLists();
    $scope.addProductsMessage = "";
    $scope.defaultProductImageURL = SearchService.defaultProductImageURL;
    $scope.searchText = '';
    $scope.allProducts = [
      {
        productId: 1,
        productStatus: 'New',
        productPercentage: 95,
        productThumbnail: '../assets/images/newest-products/flowerbomb-cream.png',
        productName: 'Flowerbomb Cream',
        productBrand: 'Viktor&Rolf | 6.4 oz',
        productLeft: '50 Left'
      },
      {
        productId: 2,
        productStatus: 'New',
        productPercentage: 94,
        productThumbnail: '../assets/images/newest-products/flowerbomb-shower.png',
        productName: 'Flowerbomb Shower Gel',
        productBrand: 'Viktor&Rolf | 6.7 oz'
      },
      {
        productId: 3,
        productStatus: 'New',
        productPercentage: 92,
        productThumbnail: '../assets/images/newest-products/spicebomb-fresh.png',
        productName: 'Spicebomb Fresh Eau de Toilette',
        productBrand: 'Viktor&Rolf | 3.04 oz'
      },
      {
        productId: 4,
        productStatus: '',
        productPercentage: 90,
        productThumbnail: '../assets/images/newest-products/flowerbomb-precious.png',
        productName: 'Flowerbomb Precious Oil',
        productBrand: 'Viktor&Rolf | 0.68 oz'
      },
      {
        productId: 5,
        productStatus: '',
        productPercentage: 90,
        productThumbnail: '../assets/images/newest-products/pro-glow.png',
        productName: 'PRO Glow',
        productBrand: 'Tarte'
      },
      {
        productId: 6,
        productStatus: '',
        productPercentage: 89,
        productThumbnail: '../assets/images/newest-products/lust-004.png',
        productName: 'Lust 004',
        productBrand: 'McGrath Labs'
      },
      {
        productId: 7,
        productStatus: '',
        productPercentage: 86,
        productThumbnail: '../assets/images/newest-products/everlasting-obsession.png',
        productName: 'Everlasting Obsession Liquid...',
        productBrand: 'Kat Von D'
      },
      {
        productId: 8,
        productStatus: '',
        productPercentage: 85,
        productThumbnail: '../assets/images/newest-products/multistick.png',
        productName: 'Multistick',
        productBrand: 'Bite Beauty',
        productLeft: '5 Left'
      },
      {
        productId: 9,
        productStatus: '',
        productPercentage: 82,
        productThumbnail: '../assets/images/newest-products/the-water.png',
        productName: 'The Water',
        productBrand: 'Tan-Luxe',
        productLeft: '10 Left'
      },
      {
        productId: 10,
        productStatus: '',
        productPercentage: 80,
        productThumbnail: '../assets/images/newest-products/Naked3-pallete.png',
        productName: 'Naked3 Palette',
        productBrand: 'Urban Decay',
        productLeft: '20 Left'
      }
    ];

    $scope.changeList = function(listName) {
      $scope.chosenList = listName;
      SearchService.getProductIdsByUserIdListName(listName)
        .then(function(response) {
          $scope.productIdsInList = response.data;
          $scope.productsInListWithDups = $scope.products.filter(function( obj ) {
            return $scope.productIdsInList.indexOf(obj.productId) > -1;
          });
          $scope.productsInList = removeDups($scope.productsInListWithDups);
          if ($scope.productsInList.length <= 0) {
            $scope.addProductsMessage = "Choose a product from the search bar and add it to this list!";
          } else {
            $scope.addProductsMessage = "";
          }
        }, function(error) {
          $rootScope.showErrorAndGoToDashboard("Error getting products: ", error);
        });
    };

    if ($rootScope.products === undefined || $rootScope.products.length <= 0) {
      SearchService.getAllProducts()
        .then(function successCallback(response) {
          // var headerNewest = [{header: "Newest products",
          //   productId: "", title: "", brand: "", imageUrl: "", size: "", color: ""}];
          // $scope.products = headerPopular.concat(response[0].data, headerNewest, response[1].data);
          $scope.products = response.data;
          $rootScope.products = $scope.products;
          $scope.changeList($scope.allProductLists[0].name);
        }, function errorCallback(e) {
          $rootScope.showErrorAndGoToDashboard("Error getting all products: ", e);
        });
    } else {
      $scope.products = $rootScope.products;
      $scope.changeList($scope.allProductLists[0].name);
    }

    $scope.goToProduct = function () {
      var productId = $scope.products.selected.productId;
      $scope.products.selected = {};
      if (productId && productId !== "") {
        $state.go('product', {
          productId: productId
        });
      }
    };

    $scope.goToProductById = function (productId) {
      $state.go('product', {
        productId: productId
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

    // from http://stackoverflow.com/questions/9229645/remove-duplicates-from-javascript-array
    function removeDups(a) {
      var seen = {};
      var out = [];
      var len = a.length;
      var j = 0;
      for (var i = 0; i < len; i++) {
        var item = a[i].productId;
        if (seen[item] !== 1) {
          seen[item] = 1;
          out[j++] = a[i];
        }
      }
      return out;
    }


  }
]);
