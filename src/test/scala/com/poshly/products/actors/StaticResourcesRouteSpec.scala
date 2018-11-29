package com.poshly.products.actors

import akka.actor.ActorRef
import com.poshly.accounts.client.UserService
import com.poshly.accounts.client.data.User
import com.poshly.core.logging.Loggable
import com.poshly.core.redis.RedisConfiguration
import com.poshly.products._
import com.redis.RedisClientPool
import com.typesafe.config.ConfigFactory
import org.mockito.Matchers._
import org.mockito.Mockito._
import spray.http._
import spray.routing.{AuthorizationFailedRejection, Rejection, RequestContext}

import scala.concurrent.Future

class StaticResourcesRouteSpec extends RouteTest with Loggable {

  trait StaticResourcesServices extends
    AccountUserConfiguration with
    RedisConfiguration with
    BaseConfiguration

  def createMockServices() = {

    val components = mock[StaticResourcesServices]

    val userServiceMock = mock[UserService]

    val user = User.empty.addRole("admin").applyId(Some("userId"))

    when(userServiceMock.signinWithPassword(anyString(), anyString())) thenReturn Future(user)

    when(components.userService) thenReturn userServiceMock

    val redisMock = mock[RedisClientPool]

    when(redisMock.withClient(any())) thenReturn None

    when(components.redisClient) thenReturn redisMock

    val testConfiguration = ConfigFactory.defaultReference()

    //    val testConfiguration = ConfigFactory.empty().
    //      withValue("insights.access.cookie.name", ConfigValueFactory.fromAnyRef("X-Poshly-AccessToken")).
    //      withValue("insights.access.cookie.domain", ConfigValueFactory.fromAnyRef(".poshly.com")).
    //      withValue("insights.access.ttl", ConfigValueFactory.fromAnyRef("24 hours")).
    //      withValue("security.salt", ConfigValueFactory.fromAnyRef("salt")).
    //      withValue("webkit.cors.support", ConfigValueFactory.fromIterable(List("*").asJava))

    when(components.config) thenReturn testConfiguration

    components
  }

  def createProductsWebRoute(services: StaticResourcesServices, authenticated: Boolean = true) = {
    new ProductsWebRoute {

      def actorRefFactory = system

      def auth: ActorRef = ???

      def mail: ActorRef = ???

      def logout: akka.actor.ActorRef = ???

      def products: akka.actor.ActorRef = ???

      def productsMobile: akka.actor.ActorRef = ???

      def userActor: ActorRef = ???

      def userMobileActor: ActorRef = ???

      override protected def check(ctx: RequestContext): Either[Rejection, Option[String]] = {
        if (authenticated) {
          Right(Some("userId"))
        }
        else {
          Left(AuthorizationFailedRejection)
        }
      }

      override protected def userIdentifierFromSession(ctx: RequestContext) = {
        if (authenticated) {
          Some("userId")
        }
        else {
          None
        }
      }

      def components: RedisConfiguration with BaseConfiguration = services
    }
  }

  val authorizedInsightRoute = createProductsWebRoute(createMockServices(), authenticated = true)
  val unauthorizedInsightRoute = createProductsWebRoute(createMockServices(), authenticated = false)

  it should "return 200 for / for passed auth" in {
    Get("/") ~> authorizedInsightRoute.staticResources ~> check {
      status should be(StatusCodes.OK)
    }
  }

  it should "return 200 for index.html for passed auth" in {
    Get("/index.html") ~> authorizedInsightRoute.staticResources ~> check {
      status should be(StatusCodes.OK)
    }
  }

  it should "return 200 status code for index-landing.html" in {
    Get("/index-landing.html") ~> unauthorizedInsightRoute.staticResources ~> check {
      status === StatusCodes.OK
    }
  }
}
