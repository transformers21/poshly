angular.module('productsApp').directive('fallbackSrc', function () {
  'use strict';
  return {
    link: function postLink(scope, iElement, iAttrs) {
      iElement.bind('error', function () {
        angular.element(this).attr("src", iAttrs.fallbackSrc);
      });
    }
  };
});
