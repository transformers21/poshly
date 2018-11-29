package com.poshly.products.util.auth

import java.util.concurrent.TimeUnit

import com.poshly.core.logging.Loggable
import com.poshly.core.redis.RedisConfiguration
import com.poshly.products.BaseConfiguration
import com.poshly.products.data.SessionTracking
import com.poshly.products.services.SecurityCache
import spray.http.Uri.Path
import spray.routing.AuthenticationFailedRejection.CredentialsRejected
import spray.routing._
import spray.routing.authentication.ContextAuthenticator
import spray.routing.directives.BasicDirectives._
import spray.routing.directives.RouteDirectives._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

trait Authorizer extends Loggable with SecurityCache {

  def components: RedisConfiguration with BaseConfiguration

  def authorizedRequest: Directive1[String] = auth(check)

  def userid: Directive1[Option[String]] = withUser(userIdentifierFromSession)

  private def auth(check: RequestContext ⇒ Either[Rejection, Option[String]]): Directive1[String] = {
    extract(check).flatMap {
      case Right(Some(userId)) =>
        provide(userId)
      case Left(rejection) =>
        reject(rejection)
      case _ =>
        reject()
    }
  }


  def authenticateAccount: ContextAuthenticator[String] = {
    ctx =>
      val q = ctx.request.uri.query
      val sigCheck = Future {
        for {
          timestamp <- q.get("timestamp").map(_.toLong)
          source <- q.get("source")
          signature <- q.get("signature")
        } yield checkSignature(source, timestamp, signature)
      }
      sigCheck.flatMap {
        case None => Future(Left(AuthenticationFailedRejection(CredentialsRejected, ctx.request.headers)))
        case Some(f) => f.map {
          case false => Left(AuthenticationFailedRejection(CredentialsRejected, ctx.request.headers))
          case true => Right("ok")
        }
      }
  }

  def authenticateAccountWithToken: ContextAuthenticator[String] = {
    ctx =>
      val q = ctx.request.uri.query
      val sigCheck = Future {
        for {
          token <- q.get("token")
        } yield {
          Future(existsUserIdByToken(token))
        }
      }

      sigCheck.flatMap {
        case None => Future(Left(AuthenticationFailedRejection(CredentialsRejected, ctx.request.headers)))
        case Some(f) => f.map {
          case false => Left(AuthenticationFailedRejection(CredentialsRejected, ctx.request.headers))
          case true => Right("ok")
        }
      }
  }

  private def existsUserIdByToken(token: String): Boolean = {
    components.redisClient.withClient { redisCli =>
      redisCli.exists(s"sso.session.$token.id")
    }
  }

  def getUserIdByTokenAndRefresh(token: String): Option[String] = {
    components.redisClient.withClient { redisCli =>
      val key = s"sso.session.$token.id"
      val userIdOpt = redisCli.get(key)
      userIdOpt.map(userId =>
        redisCli.setex(key, components.config.getDuration("products.access.ttl", TimeUnit.SECONDS).toInt, userId)
      )
      userIdOpt
    }
  }

  private def withUser(checkpf: RequestContext ⇒ Option[String]): Directive1[Option[String]] =
    extract(checkpf).flatMap(provide)

  private def rejectOrAccept(ctx: RequestContext, userId: Option[String]): Either[Rejection, Option[String]] = {
    ctx.request.uri.path match {
      case Path.SingleSlash => Right(userId)
      case Path("/index.html") => Right(userId)
      case _ =>
        userId match {
          case Some(id) => Right(userId)
          case None => Left(AuthorizationFailedRejection)
        }
    }
  }

  import CookieExtractor._

  private def extractCookieTokenFromHeader(ctx: RequestContext): Option[String] = token(ctx, components.config.getString("products.access.cookie.name"))

  protected def check(ctx: RequestContext): Either[Rejection, Option[String]] = rejectOrAccept(ctx, userIdentifierFromSession(ctx))

  protected def userIdentifierFromSession(ctx: RequestContext): Option[String] = {
    try {
      val idv1 = for {
        token <- extractCookieTokenFromHeader(ctx)
        id <- components.redisClient.withClient(_.get(s"sso.session.$token.id"))
      } yield {
//        logger.info(s"Valid ID: $id")
//        logger.debug("userIdentifierFromSession: " + token)
        id
      }
      idv1
      // TODO: Test this on Monday. Mikel
//      val userId = idv1.orElse(
//        ctx.request.uri.query.get("token").flatMap(token => getUserIdByTokenAndRefresh(token))
//      )
//      userId
    } catch {
      case e: Throwable =>
        logger.error(e.getMessage,e)
        None
    }
  }
}

object CookieExtractor {

  val regex = "\\s+".r

  def token(ctx: RequestContext, cookieName: String): Option[String] =
    session(ctx, cookieName).flatMap(_.accessGrantingToken)

  def session(ctx: RequestContext, cookieName: String): Option[SessionTracking] =
    ctx.request.headers.find(_.name == "Cookie").flatMap { header =>
     regex.replaceAllIn(header.value, "").split(";").toList.find(_.startsWith(cookieName)).flatMap { cookieValue =>
        Try{Some(SessionTracking(cookieValue.replaceFirst(cookieName + "=", "")))}.getOrElse(None)
      }
    }
}
