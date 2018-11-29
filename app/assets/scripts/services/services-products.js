angular.module('productsApp').factory('SearchService', [
  '$http',
  '$cacheFactory',
  '$rootScope',
  function ($http,
            $cacheFactory,
            $rootScope) {
    'use strict';
    var services = {};
    var cacheKeysFastPartialProduct = [];
    var cacheKeysProductIdsByUserIdListName = [];

    services.getAllProducts = function () {
      return $http.get('api/v1/products/all/', {cache: true});
    };

    // services.getPopularProducts = function () {
    //   return $http.get('api/v1/products/popular/', {cache: true});
    // };

    services.postAnswer = function (data) {
      clearHttpCacheByKeys(cacheKeysFastPartialProduct);
      return $http.post('api/v1/products/saveAnswer', data);
    };

    services.postPersonalizeAnswer = function (data) {
      $rootScope.freshQuestionsInRootScope = false;
      return $http.post('api/v1/products/saveAnswer', data);
    };

    services.postPersonalizeMultipleAnswer = function (data) {
      $rootScope.freshQuestionsInRootScope = false;
      return $http.post('api/v1/products/saveMultipleSelectAnswer', data);
    };

    services.getAllProductLists = function () {
      return [
        {name: 'My Favorite Things', selected: false},
        {name: 'My Wishlist', selected: false},
        {name: 'Items to Try or Sample', selected: false},
        {name: 'Daily Looks', selected: false},
        {name: 'Special Occasions', selected: false}
      ];
    };

    services.getFastPartialProductById = function (productId) {
      var getUrl = 'api/v1/products/getFastPartialProductById/' + productId;
      cacheKeysFastPartialProduct.push(getUrl);
      return $http.get(getUrl, {cache: true});
    };

    services.getSlowPartialProductById = function (productId) {
      return $http.get('api/v1/products/getSlowPartialProductById/' + productId, {cache: true});
    };

    services.upsertUserProductList = function (data) {
      clearHttpCacheByKeys(cacheKeysProductIdsByUserIdListName);
      return $http.post('api/v1/products/upsertUserProductList', data);
    };

    services.getProductIdsByUserIdListName = function (listName) {
      var getUrl = 'api/v1/products/getProductIdsByUserIdListName/' + listName;
      cacheKeysProductIdsByUserIdListName.push(getUrl);
      return $http.get(getUrl, {cache: true});
    };

    services.findPersonalizePoshlyQuestions = function (skip, limit) {
      return $http.get('api/v1/products/findPersonalizePoshlyQuestions?skip=' + skip + '&limit=' + limit);
    };

    services.defaultProductImageURL = "https://afefef6413373886bae4-5b5522445f6669185d82e1c80852d23c.ssl.cf2.rackcdn.com/icon-color-large-optimized.png";


    function clearHttpCacheByKeys(keys) {
      for (var i = 0; i < keys.length; i++) {
        $cacheFactory.get('$http').remove(keys[i]);
      }
    }

    return services;
  }]);
