package com.poshly.products.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import com.poshly.core.logging.Loggable
import com.poshly.core.redis.RedisConfiguration
import com.poshly.products.data.{ErrorCodes, ErrorResult}
import com.poshly.products.util.auth._
import com.poshly.products.{BaseConfiguration, ProductsComponents}
import com.poshly.webkit.CORSSupport
import spray.http.CacheDirectives._
import spray.http.HttpHeaders.`Cache-Control`
import spray.http.{AllOrigins, HttpHeaders}
import spray.routing._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class ProductsWebActor(system: ActorSystem, services: ProductsComponents) extends
  HttpServiceActor with ProductsWebRoute with Loggable {

  val products: ActorRef = system.actorOf(Props(new ProductActor(services)), "product-actor")
  val productsMobile: ActorRef = system.actorOf(Props(new ProductMobileActor(services)), "product-mobile-actor")
  val auth: ActorRef = system.actorOf(Props(new AuthenticationActor(services)), "security-actor")
  val logout: ActorRef = system.actorOf(Props(new LogoutActor(services)), "security-mobile-actor")
  val mail: ActorRef = system.actorOf(Props(new MailActor(services)), "mail-actor")
  val userActor: ActorRef = system.actorOf(Props(new UserActor(services)), "user-actor")
  val userMobileActor: ActorRef = system.actorOf(Props(new UserMobileActor(services)), "user-mobile-actor")

  def components: ProductsComponents = services

  def receive: Receive = runRoute(routes)

}

trait ProductsWebRoute extends HttpService with Loggable with CORSSupport with Authorizer {

  def components: RedisConfiguration with BaseConfiguration

  def auth: ActorRef

  def logout: ActorRef

  def mail: ActorRef

  def products: ActorRef

  def productsMobile: ActorRef

  def userActor: ActorRef

  def userMobileActor: ActorRef

  def noCache: Directive0 = respondWithHeader(`Cache-Control`(`no-cache`, `no-store`, `must-revalidate`))

  def cache100days: Directive0 = respondWithHeader(`Cache-Control`(`max-age`(8640000L)))

  implicit val executor = ExecutionContext.Implicits.global

  val rejectionHandler = RejectionHandler {
    case AuthenticationFailedRejection(_, _) :: _ =>
      complete {
        import com.poshly.products.Json4sProtocol._
        ErrorResult(ErrorCodes.UNAUTHENTICATED_REQUEST, Some("UNAUTHENTICATED_REQUEST"))
      }
    case error =>
      logger.error("Error: " + error.toString())
      complete("Error: " + error.toString())

  }

  protected val entrypoint: Option[String] => Route = {
    case Some(id) =>
      //      logger.debug(s"Authenticated: index.html $id")
      getFromResource("webapp/index.html")

    case None =>
      //      logger.debug(s"Not Authenticated: index-landing.html")
      getFromResource("webapp/index-landing.html")
  }

  val staticResources: Route =
    userid { id =>
      cache100days {
        pathPrefixTest("assets" / "images") {
          unmatchedPath { path =>
            //            logger.debug("path:  " + path)
            getFromResourceDirectory("webapp")
          }
        }
      } ~
        noCache { //ctx =>
          //          logger.debug("ctx.request host")
          //          logger.debug(ctx.request.headers.head.name)
          //          logger.debug(ctx.request.headers.head.value)

          pathSingleSlash(entrypoint(id)) ~
            path("index.html")(entrypoint(id)) ~
            unmatchedPath { path =>
              //              logger.debug(s"unmatched path: $path")
              getFromResourceDirectory("webapp")
            }
        }
    }

  val routes = staticResources ~
    noCache {
      path("token") {
        // This should be in an actor and use handleTry(_), but the tokenCache won't work if we have several instances.
        get {
          parameters("timestamp".as[Long]) { timestamp =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              import com.poshly.products.Json4sProtocol._
              onComplete(newToken(timestamp)) {
                case Success(token) => complete(token)
                case Failure(e: Throwable) => complete(ErrorResult(ErrorCodes.BAD_TIMESTAMP, Option(e.getMessage)))
              }
            }
          }
        }
      } ~
        pathPrefix("api") {
          pathPrefix("v2") {
            handleRejections(rejectionHandler) {
              pathPrefix("authenticate") {
                authenticate(authenticateAccount) { str =>
                  auth ! _
                }
              } ~
                authenticate(authenticateAccountWithToken) { str =>
                  pathPrefix("products") {
                    productsMobile ! _
                  } ~
                    pathPrefix("offers") {
                      productsMobile ! _
                    } ~
                    pathPrefix("mail") {
                      mail ! _
                    } ~
                    path("logout") {
                      logout ! _
                    } ~
                    pathPrefix("user") {
                      userMobileActor ! _
                    }
                }
            }
          } ~
            pathPrefix("v1") {
              pathPrefix("authenticate") {
                auth ! _
              } ~
                pathPrefix("products") {
                  products ! _
                } ~
                pathPrefix("offers") {
                  products ! _
                } ~
                pathPrefix("mail") {
                  mail ! _
                } ~
                pathPrefix("user") {
                  userActor ! _
                }
            }
        }
    }
}
