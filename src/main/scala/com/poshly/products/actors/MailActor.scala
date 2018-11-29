package com.poshly.products.actors

import akka.actor.Actor
import com.poshly.accounts.client.data.User
import com.poshly.core.logging.Loggable
import com.poshly.core.mailer.Mailer.{From, PlainMailBodyType, Subject, To}
import com.poshly.core.mailer.MailerConfiguration
import com.poshly.core.redis.RedisConfiguration
import com.poshly.products.data.MailEnvelope
import com.poshly.products.util.auth.Authorizer
import com.poshly.products.{AccountUserConfiguration, BaseConfiguration}
import spray.http.{AllOrigins, HttpHeaders}
import spray.routing.AuthenticationFailedRejection
import spray.routing.AuthenticationFailedRejection.CredentialsRejected

import scala.concurrent.Await
import scala.concurrent.duration._

class MailActor(comps: MailerConfiguration with AccountUserConfiguration with RedisConfiguration with BaseConfiguration) extends Actor with MailRoute with Loggable {

  def components = comps

  def actorRefFactory = context

  def receive = runRoute(routes)
}

trait MailRoute extends BaseRoute with Authorizer {

  def components: MailerConfiguration with AccountUserConfiguration with RedisConfiguration with BaseConfiguration

  val routes =
    pathPrefix("newProductRequest") {
      authorizedRequest { userId =>
        post {
          entity(as[MailEnvelope]) { email =>
            newProductRequest(userId, email)
          }
        }
      }
    } ~
      pathPrefix("newProductRequest") {
        post {
          parameters('token) { (token) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) { rq =>
              entity(as[MailEnvelope]) { email =>
                components.redisClient.withClient(_.get(s"sso.session.$token.id"))
                  .fold(reject(AuthenticationFailedRejection(CredentialsRejected, rq.request.headers)))(userId =>
                    newProductRequest(userId, email))
              }
            }
          }
        }
      } ~
      pathPrefix("getAdvice") {
        authorizedRequest { userId =>
          post {
            entity(as[MailEnvelope]) { email =>
              getAdvice(userId, email)
            }
          }
        }
      } ~
      pathPrefix("getAdvice") {
        post {
          parameters('token) { (token) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) { rq =>
              entity(as[MailEnvelope]) { email =>
                components.redisClient.withClient(_.get(s"sso.session.$token.id"))
                  .fold(reject(AuthenticationFailedRejection(CredentialsRejected, rq.request.headers)))(userId =>
                    getAdvice(userId, email))
              }
            }
          }
        }
      }

  private def getAdvice(userId: String, email: MailEnvelope) = {
    val user = retrieveUserOrNone(userId)
    components.mailer.syncSend(
      From("products@poshly.com", Some("Product Advice Request")),
      Subject(email.data.subject),
      Seq(PlainMailBodyType(email.data.content.text + userEmailText(user)), To("members@poshly.com")): _*)
    complete("Notified")
  }

  private def newProductRequest(userId: String, email: MailEnvelope) = {
    val user = retrieveUserOrNone(userId)
    components.mailer.syncSend(
      From("products@poshly.com", Some("Product Request")),
      Subject(email.data.subject),
      Seq(PlainMailBodyType(email.data.content.text + userEmailText(user)), To("members@poshly.com")): _*)
    complete("Notified")
  }

  def retrieveUserOrNone(userId: String): Option[User] = {
    val uf = components.userService.fetchById(userId).map(Option.apply).recover {
      case t: Throwable =>
        logger.error("Error retrieving user email " + userId)
        None
    }
    Await.result(uf, 3 minutes)
  }

  def userEmailText(user: Option[User]): String = {
    s"""
    \n******
    username: ${user.flatMap(_.username).getOrElse("Unknown User Name")}
    email: ${user.flatMap(_.email).getOrElse("Unknown Email")}
    account: ${user.flatMap(_.account).getOrElse("Unknown Account")}"""
  }
}
