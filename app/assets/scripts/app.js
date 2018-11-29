angular.module('productsApp', [

  //Angular
  'ngAnimate',
  'ngRoute',
  'ngSanitize',
  'ngTouch',

  //3rd Party
  'ui.bootstrap',
  'ui.bootstrap.tpls',
  'ui.router',
  'ui.select',
  'angulartics',
  'angulartics.google.analytics',
  'vcRecaptcha',

  //URL Settings managed by grunt.js build
  'config'

]).config(['$stateProvider', '$urlRouterProvider', '$urlMatcherFactoryProvider', '$locationProvider',
  function ($stateProvider, $urlRouterProvider, $urlMatcherFactoryProvider, $locationProvider) {
    'use strict';
    //Using Ui-router. For more info: https://github.com/angular-ui/ui-router

    $locationProvider.hashPrefix('');

    // // Allow trailing slashes on URLs
    $urlMatcherFactoryProvider.strictMode(false);
    // // Sometimes that doesn't work, so adding this for those cases:
    // $urlRouterProvider.rule(function ($injector, $location) {
    //   var path = $location.url();
    //   // check to see if the path already has a slash where it should be
    //   if (path[path.length - 1] === '/' || path.indexOf('/?') > -1) {
    //     return;
    //   }
    //   if (path.indexOf('?') > -1) {
    //     return path.replace('?', '/?');
    //   }
    //   return path + '/';
    // });

    $stateProvider
      // Post login
      .state('dashboard', {
        url: '/dashboard',
        views: {
          'contentHeader': {
            templateUrl: 'views/partials/partial-header.html',
            controller: 'PartialCtrl'
          },
          'contentBody': {
            templateUrl: 'views/view-dashboard.html',
            controller: 'DashboardCtrl'
          }
        }
      })
      .state('landing', {
          url: '/landing',
          views: {
            'contentBody': {
              templateUrl: 'views/view-landing.html',
              controller: 'LandingCtrl'
            }
        }
      })
      .state('brands', {
          url: '/brands',
          views: {
            'contentHeader': {
              templateUrl: 'views/partials/partial-header.html',
              controller: 'PartialCtrl'
          },
            'contentBody': {
              templateUrl: 'views/view-brands.html',
              controller: 'BrandsCtrl'
          }
        }
      })
      .state('newsfeed', {
        url: '/newsfeed',
        views: {
          'contentHeader': {
            templateUrl: 'views/partials/partial-header.html',
            controller: 'PartialCtrl'
          },
          'contentBody': {
            templateUrl: 'views/view-newsfeed.html',
            controller: 'NewsFeedCtrl'
          }
        }
      })
      .state('product', {
        url: '/product/:productId',
        views: {
          'contentHeader': {
            templateUrl: 'views/partials/partial-header.html',
            controller: 'PartialCtrl'
          },
          'contentSubHeader': {
            templateUrl: 'views/partials/partial-personalize-poshly.html',
            controller: 'personalizeCtrl'
          },
          'contentBody': {
            templateUrl: 'views/view-product.html',
            controller: 'productCtrl'
          }
        }
      })
      .state('brand', {
        url: '/brand/:brandName',
        views: {
          'contentHeader': {
            templateUrl: 'views/partials/partial-header.html',
            controller: 'PartialCtrl'
          },
          'contentBody': {
            templateUrl: 'views/view-brand.html',
            controller: 'BrandItemCtrl'
          }
        }
      })      
      .state('offers', {
        url: '/offers',
        views: {
          'contentHeader': {
            templateUrl: 'views/partials/partial-header.html',
            controller: 'PartialCtrl'
          },
          'contentBody': {
            templateUrl: 'views/view-offers.html',
            controller: 'OfferItemCtrl'
          }
        }
      })
      .state('lists', {
        url: '/lists',
        views: {
          'contentHeader': {
            templateUrl: 'views/partials/partial-header.html',
            controller: 'PartialCtrl'
          },
          'contentBody': {
            templateUrl: 'views/view-lists.html',
            controller: 'ListsItemCtrl'
          }
        }
      })
      .state('settings', {
        url: '/settings',
        views: {
          'contentHeader': {
            templateUrl: 'views/partials/partial-header.html',
            controller: 'PartialCtrl'
          },
          'contentBody': {
            templateUrl: 'views/view-settings.html',
            controller: 'ViewSettingsCtrl'
          }
        }
      })
      .state('insights-login', {
        url: '/insights-login',
        views: {
          'contentBody': {
            templateUrl: 'views/view-insigths.html',
            controller: 'ViewInsightsLoginCtrl'
          }
        }
      })
      .state('innerinsights', {
        url: '/innerinsights',
        views: {
          'contentHeader': {
            templateUrl: 'views/partials/insights-header.html',
            controller: 'ViewInsightsCtrl'
          },
          'contentBody': {
            templateUrl: 'views/view-innerinsights.html',
            controller: 'ViewInsightsCtrl'
          }
        }
      })      
      .state('tables', {
        url: '/tables',
        views: {
          'contentHeader': {
            templateUrl: 'views/partials/insights-header.html',
            controller: 'ViewInsightsCtrl'
          },
          'contentBody': {
            templateUrl: 'views/view-tables.html',
            controller: 'ViewInsightsCtrl'
          }
        }
      });

    $urlRouterProvider.otherwise('/landing');
  }
]).run(['$rootScope', '$log', '$location', function($rootScope, $log, $location){
  'use strict';
  $rootScope.showErrorAndGoToDashboard = function(msg, err) {
    $log.error(msg);
    $log.error(err);
    if (err.status === 403) {
      window.location.reload(true);
    }
  };

  $rootScope.isActive = function (viewLocation) {
    return viewLocation === $location.path();
  };

}]);
