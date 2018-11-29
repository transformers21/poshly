angular.module('productsApp').service('CommunicationsService', ['$http', '$q', function ($http, $q) {
  'use strict';
  var services = {};

  services.requestProductV2 = function (message) {
    var deferred = $q.defer();
    var data = JSON.stringify(createEmail(message, 'Requested Product'));

    var request = {
      method: 'POST',
      url: 'api/v1/mail/newProductRequest/',
      withCredentials: true,
      processData: false,
      headers: {
        'Content-Type': 'application/json'
      },
      data: data
    };
    $http(request)
      .then(function successCallback(response) {
        deferred.resolve(response);
      }, function errorCallback() {
        deferred.resolve({error: true});
      });
    return deferred.promise;
  };

  services.getAdvice = function (message, productId) {
    var deferred = $q.defer();
    var data = JSON.stringify(createEmail(message, 'Product Consultation : ' + productId));
    var request = {
      method: 'POST',
      url: 'api/v1/mail/getAdvice/',
      withCredentials: true,
      processData: false,
      headers: {
        'Content-Type': 'application/json'
      },
      data: data
    };
    $http(request)
      .then(function successCallback(response) {
        deferred.resolve(response);
      }, function errorCallback() {
        deferred.resolve({error: true});
      });
    return deferred.promise;
  };

  function createEmail(text, subject) {
    var email = {
      data: {
        subject: subject,
        content: {
          text: text
        }
      }
    };

    return email;
  }

  return services;
}]);
