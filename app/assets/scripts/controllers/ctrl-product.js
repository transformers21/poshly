angular.module('productsApp').controller('productCtrl', [
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
    $scope.productId = $stateParams.productId;
    $scope.product = {};
    $scope.productSlow = {};
    $scope.productImageUrl = '';
    $scope.productColor = '';
    $scope.uniqueColors = [];
    $scope.uniqueSizes = [];
    // To have questions appear always in the same order:
    $scope.orderItem = "id";
    $scope.maxText = 750;
    $scope.items = SearchService.getAllProductLists();
    $scope.defaultProductImageURL = SearchService.defaultProductImageURL;

    $scope.productItem = {
      percentage: 95,
      images: [
        '../assets/images/newest-products/product-image-1.png',
        '../assets/images/newest-products/product-image-2.png',
        '../assets/images/newest-products/product-image-3.png'
      ],
      name: 'Flowerbomb Cream',
      brand: 'Viktor&Rolf',
      price: '49.99',
      size: '6.4 oz',
      color: 'pink',
      overallRating: 4,
      easeOfApplicationRating: 3,
      description: 'This floral explosion releases a profusion of flowers that has the power to make everything seem more positive. Magically evocative notes immediately awaken your deepest senses, giving you the impression of living life in your own secret garden, away from reality. Sambac Jasmine, Centifolia Rose, Cattleya Orchid, and Ballerina Freesia bloom on a base of Patchouli.',
      feedback: [
        {
          text: 'This is such an amazing product. Everyone absolutely has to try it to know for themselves. I can’t express just how much this product has changed my life.',
          author: 'Mikel'
        },
        {
          text: 'Yr fam normcore jean shorts tacos, tilde salvia authentic hammock next level health goth pitchfork quinoa.' +
          ' Shoreditch polaroid pok pok, cred man bun distillery lyft waistcoat raclette. Narwhal messenger bag hexagon,' +
          ' small batch VHS activated charcoal 3 wolf moon leggings fashion axe. Vegan meggings vaporware fixie kombucha,' +
          ' kale chips aesthetic. Semiotics letterpress cred etsy lumbersexual,' +
          ' shabby chic pop-up plaid meh. Try-hard leggings hexagon wolf selfies echo park +1 synth,' +
          ' poke chia flannel. Lomo knausgaard forage authentic kale chips cronut four loko lo-fi, enamel pin keffiyeh.',
          author: 'Jeff'
        }
      ]
    };

    $scope.storeItems = [
      {
        name: 'Amazon',
        image: '../assets/images/stores/amazon_logo.png'
      },
      {
        name: 'Sephora',
        image: '../assets/images/stores/sephora_logo.png'
      },
      {
        name: 'Ultra',
        image: '../assets/images/stores/ulta_ogo.png'
      }
    ];

    $scope.productItem.price = $scope.productItem.price.split('.');

    $scope.productPersonalize = {
      amount: 14,
      title: 'Personalize poshly',
      description: 'When it comes to makeup products, I\'m best described as a...',
      questions: [
        {type: 'Junkie', description: 'I’m obsessed with buying products and knowing all the trends!'},
        {type: 'Lover', description: 'Beauty is important to me and I try to know the trends, but it’s not a total obsession!'},
        {type: 'Like', description: 'I buy beauty products and browse the trends, but it’s not a necesity for me!'}
      ]
    };

    findFastPartialProductById($scope.productId);
    findSlowPartialProductById($scope.productId);

    function findFastPartialProductById(productId) {
      SearchService.getFastPartialProductById(productId)
        .then(function successCallback(response) {
          var data = response.data;
          if (data.error) {
            $rootScope.showErrorAndGoToDashboard("Error fetching fast product data: ", data.error);
          } else {
            $scope.product = response.data;
            $scope.productImageUrl = $scope.product.imageUrl;
            $scope.productColor = $scope.product.color;
            getProductVariants();
            updateScopeItems($scope.product.lists);
          }
        }, function errorCallback(error) {
          $rootScope.showErrorAndGoToDashboard("Error fetching fast product data: ", error);
        });
    }

    function findSlowPartialProductById(productId) {
      SearchService.getSlowPartialProductById(productId)
        .then(function successCallback(response) {
          var data = response.data;
          if (data.error) {
            $rootScope.showErrorAndGoToDashboard("Error fetching slow product data: ", data.error);
          } else {
            $scope.productSlow = response.data;
          }
        }, function errorCallback(error) {
          $rootScope.showErrorAndGoToDashboard("Error fetching slow product data: ", error);
        });
    }

    function getProductVariants(){
      if ($rootScope.products === undefined || $rootScope.products.length <= 0) {
        SearchService.getAllProducts()
          .then(function successCallback(response) {
            var products = response.data;
            if (products.error) {
              $rootScope.showErrorAndGoToDashboard("Error getting all products: ", products.error);
            } else {
              getColorSizeVariants(products);
            }
          }, function errorCallback(error) {
            $rootScope.showErrorAndGoToDashboard("Error getting all products: ", error);
          });
      } else {
        getColorSizeVariants($rootScope.products);
      }
    }

    function getColorSizeVariants(products) {
      var productVariants = products.filter(function( obj ) {
        return (obj.brand === $scope.product.brand &&
                obj.title === $scope.product.title);
      });
      var colorVariants = {};
      var sizeVariants = {};
      productVariants.forEach(function(prod){
        if (!colorVariants[prod.color]){
          colorVariants[prod.color] = {
            'color': prod.color,
            'defaultProductId': prod.productId,
            'defaultImageURL': prod.imageUrl
          };
        }

        if (prod.color === $scope.product.color && prod.size === $scope.product.size){
          colorVariants[prod.color].defaultProductId = prod.productId;
          colorVariants[prod.color].defaultImageURL = prod.imageUrl ;
        }

        if (!sizeVariants[prod.size]){
          sizeVariants[prod.size] = {'size': prod.size };
        }

        if (prod.color === $scope.product.color){
          sizeVariants[prod.size].currentColorProductId=prod.productId;
        }
      });
      $scope.uniqueColors = Object.keys(colorVariants).map(function(color){
        return colorVariants[color];
      });
      $scope.uniqueSizes = Object.keys(sizeVariants).map(function(size){
        return sizeVariants[size];
      });
    }

    $scope.changeProductInfoOnHover = function (image,color){
      $scope.productImageUrl = image;
      $scope.productColor = color;
    };

    $scope.saveAnswer = function(answer, question) {
      var data = {
        id: question.id,
        restriction: answer
      };
      postAnswer(data, question);
    };

    $scope.saveOpenEndedAnswer = function(question) {
      var data = {
        id: question.id,
        restriction: question.myAnswer.substr(0, $scope.maxText)
      };
      postAnswer(data, question);
    };

    $scope.getResponseTextByRestriction = function(restriction, responses) {
      var response = responses.filter(function (arrayItem) {
        return arrayItem.ontologyRestriction === restriction;
      });
      var responseText = "";
      if (response && response.length > 0 && response[0].text) {
        responseText = response[0].text;
      }
      return responseText;
    };

    $scope.goBack = function() {
      $state.go('dashboard');
    };

    function postAnswer(data, question) {
      SearchService.postAnswer(data)
      .then(function() {
        question.answerComesFromDB = true;
        triggerFadeInOutById(question.id);
        // $log.debug("Answer saved.");
      }, function(error) {
        $rootScope.showErrorAndGoToDashboard("Error saving answer: ", error);
      });
    }

    function triggerFadeInOutById(questionId) {
      var el = document.getElementById(questionId);
      if (el) {
      // IE 9 hack to remove, instead of just el.classList.remove(..)
        el.className = el.className.replace( /(?:^|\s)fade-out(?!\S)/g , '' );
      // Trick: Restart the CSS animation. From https://css-tricks.com/restart-css-animation/
        void el.offsetWidth;
        el.classList.add("fade-out");
      }
    }

    $scope.open = function (size) {
      var modalInstance = $uibModal.open({
        ariaLabelledBy: 'modal-title',
        ariaDescribedBy: 'modal-body-lists',
        windowClass: 'products-lists-modal',
        templateUrl: 'mod-request-product-modal.html',
        controller: 'ModalInstanceCtrl',
      // scope: $scope,
        size: size,
        resolve: {
          items: function () {
            return $scope.items;
          }
        }
      });

      modalInstance.result.then(function (selectedItem) {
      // $scope.selected = selectedItem;
        var chosenItems = [];
        for (var i = 0; i < selectedItem.length; i++) {
          var item = selectedItem[i];
          if (item.selected) {
            chosenItems.push(item.name);
          }
        }
        var data = {
          productId: $scope.productId,
          items: chosenItems
        };
        SearchService.upsertUserProductList(data)
          .then(function() {
            updateScopeItems(chosenItems);
            // console.log(response.data);
            // console.log("Product lists updated.");
          }, function(error) {
            $rootScope.showErrorAndGoToDashboard("Error saving product list: ", error);
          });

      });
    };

    function updateScopeItems(chosenItems) {
      var allItems = $scope.items;
      $scope.items = allItems.map(function(obj) {
        return {name: obj.name, selected: chosenItems.indexOf(obj.name) > -1};
      });
      $scope.inLists = itemsInList($scope.items);
    }

    function itemsInList(itemsToTest) {
      var items = itemsToTest.filter(function( obj ) {
        return obj.selected;
      });
      return items.length > 0;
    }

    $scope.openBuyIt = function (size) {
    var modalInstance = $uibModal.open({
      ariaLabelledBy: 'modal-title',
      ariaDescribedBy: 'modal-body',
      templateUrl: 'views/modules/buy-it-modal.html',
      controller: 'ModalInstanceCtrl',
      backdropClass:'modal-backdrop',
      openedClass: 'modal-buy',
      size: size,
      resolve: {
        items: function () {
          return $scope.items;
        }
      }
    });
    }

    $scope.openList = function (size) {
    var modalInstance = $uibModal.open({
      ariaLabelledBy: 'modal-title',
      ariaDescribedBy: 'modal-body',
      templateUrl: 'views/modules/list-modal.html',
      controller: 'ModalInstanceCtrl',
      backdropClass:'modal-backdrop',
      openedClass: 'modal-buy',
      size: size,
      resolve: {
        items: function () {
          return $scope.items;
        }
      }
    });
    }

  }
]);
