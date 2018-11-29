angular.module('productsApp').directive('usersLikeMeReviews',
  function() {
    'use strict';
    return {
      restrict: 'A',
      templateUrl: 'views/partials/partial-user-reviews.html',
      scope: {
        openEndedLikeMeResponses: '='
      },
      link: function(scope) {
        scope.startIndex = 0;
        scope.morePrevMessages = false;
        scope.moreMessages = false;
        scope.messagesToDisplay = [];

        scope.$watch('openEndedLikeMeResponses',function() {
          updateReviews();
        });

        var updateReviews = function() {
          if (scope.openEndedLikeMeResponses){
            scope.moreMessages = scope.startIndex + 3 < scope.openEndedLikeMeResponses.length;
            scope.morePrevMessages = scope.startIndex > 0;
            scope.messagesToDisplay = scope.openEndedLikeMeResponses.slice(scope.startIndex,scope.startIndex + 3);
          }
        };
        scope.displayPrev3Reviews = function(){
          scope.startIndex -= 3;
          updateReviews();
        };
        scope.displayNext3Reviews = function(){
          scope.startIndex += 3;
          updateReviews();
        };
      }
    };//return
  }
);
