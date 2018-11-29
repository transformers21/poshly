package com.poshly.products.services

import java.security.MessageDigest
import java.util.concurrent.TimeUnit

import com.poshly.accounts.client.data.User
import com.poshly.core.Strings
import com.poshly.core.data.{ValidationError, ValidationMessage}
import com.poshly.core.helpers.SecurityHelper
import com.poshly.core.logging.Loggable
import com.poshly.core.redis.RedisConfiguration
import com.poshly.products.AccountUserConfiguration
import com.poshly.products.data.{AuthToken, SessionTracking}
import org.apache.commons.codec.binary.Base64
import spray.http.{DateTime, HttpCookie}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

class AuthenticationService(components: AccountUserConfiguration with RedisConfiguration) extends Loggable {

  val salt = components.config.getString("security.salt")

  def authenticateUser(usernameOrEmail: String, password: String, sessionTracking: SessionTracking): Future[AuthToken] = {
    def signIn(userLoginField: String) = {
      components.userService.signinWithPassword(
        userLoginField,
        SecurityHelper.encryptPassword(salt, userLoginField, password)
      ).map { user =>
        cacheUserId(user.id, sessionTracking.accessGrantingToken)
      }
    }

    logger.debug("authenticateUser " + usernameOrEmail)
    if (usernameOrEmail.contains("@") && usernameOrEmail.contains(".")) {
      components.userService.fetchByEmail(usernameOrEmail).flatMap(_.username match {
        case Some(username) => signIn(username)
        case None => signIn(usernameOrEmail)
      })
    } else {
      signIn(usernameOrEmail)
    }
  }

  def signOut(n: String): Future[Long] = {
    logger.debug(s"sign-out: $n")
    Future {
      components.redisClient.withClient { client =>
        client.del(s"sso.session.$n.id").getOrElse(0)
      }
    }
  }
  def signOut(name: Option[String]): Future[Long] = {
    signOut(name.getOrElse(""))
  }

  def signUp(user: User): Future[User] = {
    components.userService.signup(user)
  }

  def isValidEmail(email: String): Boolean = {
    email.contains("@") && email.contains(".")
  }

  def hashPassword(name: String, pwd: String): String = {
    val str = salt + name.replaceAll("[^a-zA-Z0-9_]", "") + pwd + salt
    base64Encode(MessageDigest.getInstance("SHA-256").digest(str.getBytes("UTF-8")))
  }

  private def base64Encode(in: Array[Byte]): String = new String(cleanArray((new Base64).encode(in)))
  private final def cleanArray(in: Array[Byte]): Array[Byte] = in.filter(a => a >= 32 && a <= 127)

  def addCookie(tracking: SessionTracking) =
    HttpCookie(accessGrantingCookieName, content = tracking.toString, domain = domain(), path = Some("/"))

  def accessGrantingCookieName = components.config.getString("products.access.cookie.name")

  def accessGrantingCookieDomain = components.config.getString("products.access.cookie.domain")

  private[services] def domain() = Strings.toOption(components.config.getString("products.access.cookie.domain"))

  private[services] def expiry = DateTime.now + components.config.getDuration("products.access.ttl", TimeUnit.MILLISECONDS)

  private[services] def cacheUserId(userIdOpt: Option[String], tokenOption: Option[String]): AuthToken = {
    if (logger.isDebugEnabled) {
      logger.debug("cacheUser: " + userIdOpt + ", " + tokenOption)
    }
    val tok: String = (tokenOption, userIdOpt) match {
      case (Some(token), Some(userId)) =>
        components.redisClient.withClient { redis =>
          val key = s"sso.session.$token.id"
          redis.setex(key, components.config.getDuration("products.access.ttl", TimeUnit.SECONDS).toInt, userId)
          token
        }
      case _ => ""
    }
    AuthToken(tok)
  }

  private[services] def verifyRole(user: User): User = {
    logger.debug(s"Verifying role: ${user.username.getOrElse("N/A")}")
    user.roles match {
      case role if (role.toSet & Set("researcher", "research-administrator", "admin")).nonEmpty =>
        user
      case _ =>
        throw new ValidationError(Seq(ValidationMessage("Unauthorized role privileges", code = Some("AUTHENTICATION_REQUIRED"))))
    }
  }

}
