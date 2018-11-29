angular.module('productsApp').controller('LandingCtrl', [
  '$scope',
  '$document',
  '$location',
  '$window',
  '$http',

  function ($scope, $document, $location, $window, $http) {
    'use strict';

    var loginListening = false;

    $scope.active = 0;
    $scope.myInterval = 3000;
    $scope.forgotPasswordPath = '';
    $scope.signUpFormData = {
      email: '',
      password: ''
    };
    $scope.loginFormData = {
      email: '',
      password: ''
    };

    $scope.testimonials = [
      [
        {
          id: 1,
          testimonialText: 'Unprecedented & deeply engaging',
          testimonialSender: 'People',
          thumbnail: '../assets/images/people.png'
        },
        {
          id: 2,
          testimonialText: 'A rising star startup',
          testimonialSender: 'Refinery29',
          thumbnail: '../assets/images/review-refinery29.png'
        },
        {
          id: 3,
          testimonialText: 'Genius',
          testimonialSender: 'Teen Vogue',
          thumbnail: '../assets/images/reviews-teenvogue.png'
        },
        {
          id: 4,
          testimonialText: 'the Holy Grail of health and beauty',
          testimonialSender: 'INC. Magazine',
          thumbnail: './assets/images/reviews-Inc-Magazine.png'
        },
        {
          id: 5,
          testimonialText: 'Poshly.com is the BEST',
          testimonialSender: 'Self',
          thumbnail: '../assets/images/reviews-self.png'
        },
        {
          id: 6,
          testimonialText: 'Genius',
          testimonialSender: 'Fast company',
          thumbnail: '../assets/images/reviews-fastcompany.png'
        }
      ],
      [
        {
          id: 1,
          testimonialText: 'Unprecedented & deeply engaging',
          testimonialSender: 'People',
          thumbnail: '../assets/images/people.png'
        },
        {
          id: 2,
          testimonialText: 'A rising star startup',
          testimonialSender: 'Refinery29',
          thumbnail: '../assets/images/review-refinery29.png'
        },
        {
          id: 3,
          testimonialText: 'Genius',
          testimonialSender: 'Teen Vogue',
          thumbnail: '../assets/images/reviews-teenvogue.png'
        },
        {
          id: 4,
          testimonialText: 'the Holy Grail of health and beauty',
          testimonialSender: 'INC. Magazine',
          thumbnail: './assets/images/reviews-Inc-Magazine.png'
        },
        {
          id: 5,
          testimonialText: 'Poshly.com is the BEST',
          testimonialSender: 'Self',
          thumbnail: '../assets/images/reviews-self.png'
        },
        {
          id: 6,
          testimonialText: 'Genius',
          testimonialSender: 'Fast company',
          thumbnail: '../assets/images/reviews-fastcompany.png'
        }
      ]
    ];


    // =========================== Binding enter keypress event for login & signup buttons =========================== //

    function bindKeypress (element) {
      $(document).bind('keypress', function(event) {
        if (event.keyCode === 13) {
          angular.element(element).triggerHandler('click');
        }
      });
    }

    $(document).unbind('keypress');

    bindKeypress('#loginBtn');

    $scope.onTabSelect = function(tabName) {
      if (tabName === 'signUpTab') {
        $(document).unbind('keypress');
        bindKeypress('#signUpBtn');
      }

      if (tabName === 'loginTab') {
        $(document).unbind('keypress');
        bindKeypress('#loginBtn');
      }
    };

    // =========================== Login logic =========================== //
    $scope.model = {
      key: '6LdT9CAUAAAAALMG-C3k3FF8lDYNzVI1sw4z9hby'
    };

    $scope.setResponse = function (response) {
      $scope.response = response;
      $scope.signUp();
    };

    $scope.setWidgetId = function (widgetId) {
      $scope.widgetId = widgetId;
    };

    $scope.cbExpiration = function() {
      vcRecaptchaService.reload($scope.widgetId);
      $scope.response = null;
    };

    $scope.signUp = function() {

      var signUpURL = 'http://dev.poshly.com/products/api/v1/authenticate/signup?source=poshly&timestamp='+Date.now()+'&locale='+$window.navigator.language;

      $http({
        url: signUpURL,
        type: 'POST',
        data: $scope.loginFormData,
        xhrFields: { withCredentials: true }
      }).then(function (response) {
        console.log(response);

      }, function (error) {
        console.log(error);
      });
    };

    $scope.loginFormSubmit = function ($event) {
      $event.preventDefault();

      login($scope.loginFormData, $document[0].URL);
    };


    $scope.signUpFormSubmit = function ($event) {
      $event.preventDefault();

      $scope.signUp($scope.signUpFormData);
    };

    if (startsWith($document[0].URL, 'http://local.dev') || startsWith($document[0].URL, 'http://dev')) {
      $scope.forgotPasswordPath = 'http://dev.poshly.com/sign-in/recover';
    }

    $window.login = login;

    function login(data, initialPath, token) {

      var redirectPath = getRedirectUrl(initialPath);
      var url = 'http://dev.poshly.com/products/api/v1/authenticate/signup?source=poshly&timestamp='+Date.now()+'&signature='+token+'&locale='+$window.navigator.language;

      $http({
        url: url,
        type: 'POST',
        data: data,
        xhrFields: { withCredentials: true }
      }).then(function (response) {
        console.log(response);
        redirect(redirectPath)
      }, function (error) {
        console.log(error);
      });
    }

    // =========================== Helper functions =========================== //

    function startsWith(str, word) {
      return str.lastIndexOf(word, 0) === 0;
    }

    function redirect(path){
      window.location.replace(path);
      if ($location.origin !== 'http://local.dev.poshly.com:9000') {
        $location.reload();
      }
    }

    function getRedirectUrl(initialPath) {
      var url = initialPath.split('/#/');

      if (url[1]) {
        if (url[0] === 'http://dev.poshly.com') {
          return '/products/#/' + url[1];
        } else {
          return '/#/' + url[1];
        }
      } else {
        return getDashboardPath(url[0]);
      }
    }

    function getDashboardPath(origin){
      var url = '';
      if (origin === 'http://dev.poshly.com') {
        url = '/products/#/dashboard';
      } else {
        url = '/#/dashboard';
      }
      return url;
    }

  }
]);