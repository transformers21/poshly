'use strict';

angular.module('productsApp')
  .filter('inArray', function(){
      return function(list, arrayFilter){
          if(arrayFilter){
              return list.filter(function( obj ) {
                return arrayFilter.indexOf(obj.productId) > -1;
              });
          }
      };
  });