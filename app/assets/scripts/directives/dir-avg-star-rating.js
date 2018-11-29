angular.module('productsApp').directive('avgStarRating',
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
        ratingCounts: '=',
        max: '='
      },
      link: function(scope) {
        scope.$watch('ratingCounts',function() {
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

          var response = scope.ratingCounts;

          if (response){
            scope.fullStars = Math.floor(scope.ratingCounts * 1);
            scope.halfStar = scope.ratingCounts - scope.fullStars >= 0.25 &&
            scope.ratingCounts - scope.fullStars < 0.75;
            scope.emptyStars = scope.max - scope.fullStars - Number(scope.halfStar);
            scope.roundedAvg = scope.ratingCounts.toFixed(1);
          }
          // if (response){
          //   for ( var k = 1; k <= scope.max; k++) {
          //     scope.totalCount += response[k];
          //   }
          //   if (scope.totalCount){
          //     var weightedCount = 0;
          //     for ( var j = 1; j <= scope.max; j++) {
          //       weightedCount += response[j] * j;
          //     }
          //     var roundedAvgTens =  Math.round(weightedCount / scope.totalCount * 10);
          //     scope.roundedAvg = roundedAvgTens / 10;
          //     scope.fullStars = Math.floor(scope.roundedAvg);
          //     scope.halfStar = roundedAvgTens % 10 >= 3;
          //     scope.emptyStars = scope.max - scope.fullStars - Number(scope.halfStar);
          //   } else {
          //     scope.fullStars = 0;
          //     scope.emptyStars = scope.max;
          //   }
          // }
        };
      }
    };//return
  }
);
