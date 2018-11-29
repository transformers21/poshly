package com.poshly.products.actors

import akka.actor.Actor
import com.poshly.accounts.client.data.{AttributionsUser, User}
import com.poshly.core.Strings
import com.poshly.core.data.{ValidationError, ValidationMessage}
import com.poshly.core.logging.Loggable
import com.poshly.core.redis.RedisConfiguration
import com.poshly.products.AccountUserConfiguration
import com.poshly.products.data.{AuthToken, SessionTracking}
import com.poshly.products.services.AuthenticationService
import com.poshly.products.util.auth.CookieExtractor._
import org.joda.time.DateTime
import spray.http.StatusCodes
import spray.httpx.unmarshalling.Deserializer._
import spray.routing._

class AuthenticationActor(_components: AccountUserConfiguration with RedisConfiguration)
  extends Actor with AuthenticationRoute {

  def service = new AuthenticationService(_components)

  def actorRefFactory = context

  def receive = runRoute(routes)

  def components = _components
}

trait AuthenticationRoute extends BaseRoute with Loggable {

  def service: AuthenticationService

  def components: AccountUserConfiguration with RedisConfiguration

  def optionalToken: Directive1[Option[String]] = extract(extractToken)

  def extractToken(ctx: RequestContext): Option[String] = token(ctx, service.accessGrantingCookieName)

  def optionalSessionCookie: Directive1[Option[SessionTracking]] = extract(extractSessionCookie)

  def extractSessionCookie(ctx: RequestContext): Option[SessionTracking] = session(ctx, service.accessGrantingCookieName)

  val routes = {
    pathPrefix("logout") {
      get {
        optionalSessionCookie {
          case Some(cookie) =>
            service.signOut(cookie.accessGrantingToken)
            optionalHeaderValueByName("Referer") { refererOption =>
              deleteCookie(service.accessGrantingCookieName, service.accessGrantingCookieDomain, "/") {
                refererOption match {
                  case Some(referer) =>
                    redirect(referer, StatusCodes.TemporaryRedirect)
                  case _ =>
                    redirect(components.config.getString("products.url"), StatusCodes.TemporaryRedirect)
                }
              }
            }
          case _ =>
            optionalHeaderValueByName("Referer") {
              case Some(referer) =>
                redirect(referer, StatusCodes.TemporaryRedirect)
              case _ =>
                redirect(components.config.getString("products.url"), StatusCodes.TemporaryRedirect)
            }
        }
      }
    } ~
      // http://local.dev.poshly.com:30030/api/v1 or v2/authenticate/mobile
      pathPrefix("mobile") {
        post {
          formFields('password.?, 'username.?, 'token.?) { (passwordOption, usernameOption, token) =>
            logger.debug(s"authentication: login $usernameOption")
            (passwordOption, usernameOption) match {
              case (Some(password), Some(username)) =>
                val tracking = token.fold(SessionTracking.accessGrantingToken(Strings.random(20)))(token => SessionTracking.accessGrantingToken(token))
                onComplete(service.authenticateUser(username, password, tracking))(handleTry(_) { (token: AuthToken) =>
                  complete(token)
                })
              case _ =>
                complete(ErrorResponse(ValidationError(Seq(ValidationMessage(message = "Missing Credentials", code = Some("MISSING_CREDENTIALS"))))))
            }
          }
        }
      } ~
      pathPrefix("signup") {
        post {
          formFields('password, 'username, 'locale.?) { (password, username, localeOpt) =>
            logger.debug(s"authentication: signup $username")
            logger.debug(s"authentication: signup $password")
            logger.debug(s"authentication: signup $localeOpt")

            if (service.isValidEmail(username) && password.nonEmpty) {
              val userEmail = username.toLowerCase.trim
              val attributions = AttributionsUser
                .empty
                .applySource("poshly")
                .applyCreatedOn(DateTime.now)
                .applyAccount("A0000000000000000001")
              val computedHash = service.hashPassword(userEmail, password)
              val locale = localeOpt.getOrElse("").replace('-','_')
//              val lang = locale.split("_")(0)
              val user = User
                .empty
                .applyUsername(None)
                .applyEmail(Some(userEmail))
                .applyPassword(computedHash)
                .applyGender(Some("genderOther"))
//                .applyLang(lang)
                .applyLocale(locale)
                .applyTemporary(true)
                .applyAccount(attributions.account)
                .applyAttributions(attributions)

              onComplete(service.signUp(user))(handleTry(_) { _ =>
                complete("OK")
              })

            } else {
              complete(ErrorResponse(ValidationError(Seq(
                ValidationMessage("Please enter a valid email address", Some("NOT_VALID_EMAIL"))))))
            }

          }
        }
      } ~
      post {
        optionalToken { token =>
          formFields('password.?, 'username.?, 'redirect.?) { (passwordOption, usernameOption, redirectOption) =>
            logger.debug(s"authentication: login $usernameOption")
            (passwordOption, usernameOption) match {
              case (Some(password), Some(username)) =>
                val tracking = token.fold(SessionTracking.accessGrantingToken(Strings.random(20)))(token => SessionTracking.accessGrantingToken(token))
                onComplete(service.authenticateUser(username, password, tracking))(handleTry(_) { (token: AuthToken) =>
                  val cookie = service.addCookie(tracking)
                  logger.info("cookie: " + cookie)
                  setCookie(cookie) {
                    redirectOption match {
                      case Some(url) =>
                        redirect(url, StatusCodes.MovedPermanently)
                      case _ =>
                        complete(token)
                    }
                  }
                })

              case _ =>
                complete(ErrorResponse(ValidationError(Seq(
                  ValidationMessage(message = "Missing Credentials", code = Some("MISSING_CREDENTIALS"))))))
            }
          }
        }
      }
  }
}

