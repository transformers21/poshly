'use strict';

angular.module('productsApp').directive('userReviews',

  function() {
    return {
      restrict : 'A',
      templateUrl : 'views/partials/partial-user-reviews.html',

      scope : {
        openEndedResponses : '='
      },
      link : function(scope, elem, attrs) {
        scope.startIndex=0;
        scope.morePrevMessages=false;
        scope.moreMessages=false;
        scope.messagesToDisplay=[];

        scope.$watch('openEndedResponses',function(oldVal, newVal) {

          updateReviews();
        });

        var updateReviews = function() {

         if(scope.openEndedResponses){
           scope.moreMessages=scope.startIndex+3 < scope.openEndedResponses.length;
           scope.morePrevMessages= scope.startIndex>0;

           scope.messagesToDisplay=scope.openEndedResponses.slice(scope.startIndex,scope.startIndex+3);
         }
        }

        scope.displayPrev3Reviews=function(){
          scope.startIndex -=3;
          updateReviews();
        }
        scope.displayNext3Reviews=function(){
          scope.startIndex +=3;
          updateReviews();
        }
      }
    };//return
   }
);