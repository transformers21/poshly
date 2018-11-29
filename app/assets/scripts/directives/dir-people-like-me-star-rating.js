angular.module('productsApp').directive('peopleLikeMeStarRating',
  function() {
    'use strict';
    return {
      restrict: 'A',
      template: '<ul class="rating">'
              + ' <li ng-repeat="n in starArray | limitTo:fullStars" class="filled">'
              + '  <i class="fa fa-star"></i>'
              + ' </li>'
              + ' <li class="filled"  ng-show = "halfStar" >'
              + '  <i  class="fa fa-star-half-o"></i>'
              + ' </li>'
              + ' <li ng-repeat="n in starArray | limitTo:emptyStars">'
              + '  <i class="fa fa-star"></i>'
              + ' </li>'
              + '</ul>'
              + '<small ng-show="roundedAvg"><strong>{{roundedAvg}} out of {{max}} stars</strong></small>'
              + '<small class="italic" ng-show="!roundedAvg"><strong>Not enough data</strong></small>',
      scope: {
        ratingSimilarCounts: '=',
        max: '='
      },
      link: function(scope) {
        scope.$watch('ratingSimilarCounts',function() {
          updateStars();
        });
        var updateStars = function() {
          scope.halfStar = false;
          scope.fullStars = 0;
          scope.emptyStars = scope.max;
          scope.roundedAvg = 0;
          scope.totalCount = 0;
          scope.starArray = [];
          for ( var i = 1; i <= scope.max; i++) {
            scope.starArray.push(i);
          }

          var response = scope.ratingSimilarCounts;

          if (response){
            scope.fullStars = Math.floor(scope.ratingSimilarCounts * 1);
            scope.halfStar = scope.ratingSimilarCounts - scope.fullStars >= 0.25 &&
                scope.ratingSimilarCounts - scope.fullStars < 0.75;
            scope.emptyStars = scope.max - scope.fullStars - Number(scope.halfStar);
            scope.roundedAvg = scope.ratingSimilarCounts.toFixed(1);
          }
        };
      }
    };
  }
);
