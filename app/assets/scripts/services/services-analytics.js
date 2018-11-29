'use strict';

angular.module('productsApp')
	
	.service('Analytics', ['$analytics', '$state', function($analytics, $state) {
		function whereAreWe() {
			var state = $state.current.name,
					label = '';
			
			switch(state) {
				case 'dashboard':
					label = 'Home';
					break;
				case 'browse':
					label = 'Canvas';
					break;
				case 'faq':
					label = 'FAQ';
					break;
				case 'faqHome':
					label = 'FAQ';
					break;
				default:
					break;
			}
			
			return label;
		}
		
		return {
			trackEvent : function(event, category) {
	            event = event || 'Search';
				var label = whereAreWe();
				
	            $analytics.eventTrack(event, { 
	              category: category, label: label
	            }); 
	        }
		};
	
	}]);
