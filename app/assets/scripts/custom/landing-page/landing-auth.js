(function($){
  'use strict';
  var loginListening = false;
  var iniPath = document.URL;

  if (startsWith(iniPath,"http://local.dev") || startsWith(iniPath, "http://dev")) {
    $('#forgot-password').attr("href", "http://dev.poshly.com/sign-in/recover");
  }
  $(document).bind('keypress', function(e) {
    if (e.keyCode === 13) {
      $('#login-button').trigger('click');
      return false;
    }
  });

  var clkBtn = "";
  $('#login-button').click(function() {
    clkBtn = 'login-button';
    $(this).addClass('active');
  });

  $('#signup-button').click(function() {
    clkBtn = 'signup-button';
    $(this).addClass('active');
  });

  // handle form submitting
  $('#login-form').submit(function(event) {
    // Stop form from submitting normally
    event.preventDefault();

    var clickedButton = clkBtn;

    var $form = $(this),
        data = {},
        origin = window.location.origin,
        url = getURL(origin),
        validData = true;
        data['locale'] = navigator.language;

    // remember username
    forgetMeNot();

    //construct the data object and catch the missing data
    $form.find('[name]').each(function() {
      var field = $(this),
        name = field.attr('name'),
        value = field.val();

      //close all error messages
      var id = '#invalid_' + name;
      $(id).hide();

      //find empty fields
      if (!value.length && name !== 'checkbox'){
        $(id).toggle();
        validData = false;
      }

      if (!loginListening){
        field.on('focusout', function() {
          var field = $(this),
            name = field.attr('name'),
            value = field.val();

          //close all error messages
          var id = '#invalid_' + name;
          $(id).hide();

          //find empty fields
          if (!value.length && name !== 'checkbox') {
            $(id).toggle();
            validData = false;
          }
        });
      }

      data[name] = value;
    });

    loginListening = true;
    // if the data is valid, make the ajax call
    if (validData && clickedButton === "login-button"){
      logIntoProducts(url, data, iniPath);
    } else if (validData && clickedButton === "signup-button") {
      signUpUser(data);
    }

    return false;
  });

  // get remembered data
  rememberMe();

  // *******************  Helper Functions *********************//

  function startsWith(str, word) {
    return str.lastIndexOf(word, 0) === 0;
  }

  // Redirect and Refresh
  function redirect(path){
    //location.assign ?
    window.location.replace(path);
    if (window.location.origin !== 'http://local.dev.poshly.com:9000') {
      window.location.reload();
    }
  }

  // get/clear data from local storage
  function rememberMe() {
    $('#username').val(localStorage.userName);
  }

  //get correct URL for auth post
  function getURL(origin){
    var url = '';
    if (origin === 'http://localhost:9000' || origin === 'http://local.dev.poshly.com:9000') {
      url = 'http://dev.poshly.com/products/api/v1/authenticate';
    } else {
      url = 'api/v1/authenticate';
    }
    return url;
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

  function forgetMeNot(){
    localStorage.userName = $('#username').val();
  }

  function logIntoProducts(url, data, initialPath){
    var redirectPath = getRedirectUrl(initialPath);
    $.ajax({
      url: url,
      type: 'POST',
      data: data,
      xhrFields: { withCredentials: true },
      success: function(response) {
        if (response.error){
          $('#invalid_message').show();
          return;
        }
        $('#invalid_message').hide();
        $('body').append(response);

        // redirect
        redirect(redirectPath);
      },

      error: function(error) {
        console.error(error);
        $('#invalid_message').show();
      }
    });
  }

  function signUpUser(data) {
    var signUpURL = "api/v1/authenticate/signup";
    $.ajax({
      url: signUpURL,
      type: 'POST',
      data: data,
      xhrFields: { withCredentials: true },
      success: function(response) {
        if (response.error && response.error.messages && response.error.messages[0] &&
          response.error.messages[0].message) {
          var errorMessage = response.error.messages[0].message;
          $('#check_your_email').hide();
          $('#invalid_email_message').text(errorMessage);
          $('#invalid_email').show();
        } else {
          $('#invalid_email').hide();
          $('#check_your_email').show();
        }
      },

      error: function(error) {
        console.error(error);
        // $('#invalid_message').show();
      }
    });

  }


})(jQuery);
