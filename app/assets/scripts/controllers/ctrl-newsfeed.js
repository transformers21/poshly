angular.module('productsApp').controller('NewsFeedCtrl', [
  '$scope',
  '$rootScope',
  '$location',
  'SearchService',
  '$state',
  '$q',
  '$stateParams',
  '$uibModal',
  '$log',
  function($scope,
           $rootScope,
           $location,
           SearchService,
           $state,
           $q,
           $stateParams,
           $uibModal,
           $log){
    'use strict';
    $scope.brands = [];
    $scope.brandsPopular = [];
    $scope.brandsAll = [];
    $scope.addbrandsMessage = "";
    $scope.defaultProductImageURL = SearchService.defaultProductImageURL;
    $scope.searchText = '';
    $scope.allBrands = [
      {
        newsThumbnail: '../assets/images/newsfeed/Wings.jpg',
        productBrand: 'Clinique',
        newsName: 'Wigs on the Rise',
        newsText: 'With wig brands on social media tapping influencers to rock their faux tresses, interest in wigs continues to be on the rise. The majority of consumers who incorporate wigs into their everyday beauty routine prefer a natural look. However, 44% of consumers who wear wigs as a novelty statement piece prefer a wig that is colorful, playful, and bright!'
      },
      {
        newsThumbnail: '../assets/images/newsfeed/unmasking.jpg',
        newsName: 'They are Real Double The Lip',
        newsText: "Benefit Cosmetics recently launched They're Real! Double The Lip, a hybrid lip product that offers precision, semi-matte color, and 8-hour wear. When it comes to the perks of a lipstick/lip pencil in-one product, 25% of… consumers say it makes their pout more defined, while a whopping 67% find the biggest perk to be convenience. Among those who've already purchased Benefit's newest lippy, this appreciation for it's convenience surges to 77%."
      },
      {
        newsThumbnail: '../assets/images/newsfeed/Packing.jpg',
        newsName: 'Unmasking the Lip Mask Trend',
        newsText: "Lip masks are finding their way into consumers skincare routines, with brands including... Patchology offering their take on the hydrating pout treatments. Interest in lip masks is high, with half of consumers saying they'd like to give the treatment a try. 22% of consumers overall use plumping and hydrating lip masks, and among Patchology users, this number leaps to nearly 60%."
      },
      {
        newsThumbnail: '../assets/images/newsfeed/Hum.jpg',
        newsName: 'Packing for Coachella!',
        newsText: "With the first weekend of Coachella now underway, festival-goers are packing their bags and heading… for the SoCal desert to see performances by Lady Gaga, Radiohead, Kendrick Lamar, and more. When it comes to their must-have beauty item at a music fest, consumers rank sunscreen as the most crucial. Waterproof mascara trails closely behind, followed by oil wipes!"
      },
      {
        newsThumbnail: '../assets/images/newsfeed/Luxury.jpg',
        newsName: 'What’s All The HUM About?',
        newsText: "With the first weekend of Coachella now underway, festival-goers are packing their bags and heading… for the SoCal desert to see performances by Lady Gaga, Radiohead, Kendrick Lamar, and more. When it comes to their must-have beauty item at a music fest, consumers rank sunscreen as the most crucial. Waterproof mascara trails closely behind, followed by oil wipes!"
      },
      {
        newsThumbnail: '../assets/images/newsfeed/Seeking.png',
        newsName: 'The Luxury of Bergamot',
        newsText: "With the first weekend of Coachella now underway, festival-goers are packing their bags and heading… for the SoCal desert to see performances by Lady Gaga, Radiohead, Kendrick Lamar, and more. When it comes to their must-have beauty item at a music fest, consumers rank sunscreen as the most crucial. Waterproof mascara trails closely behind, followed by oil wipes!"
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

    $scope.openNews = function () {
    var modalInstance = $uibModal.open({
      ariaLabelledBy: 'modal-title',
      ariaDescribedBy: 'modal-body',
      templateUrl: 'views/modules/news-modal.html',
      controller: 'ModalInstanceCtrl',
      backdropClass:'modal-backdrop',
      openedClass: 'modal-news',
      resolve: {
        items: function () {
          return $scope.items;
        }
      }
    });
    }

  }
]);
