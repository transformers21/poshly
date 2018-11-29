package com.poshly.products.actors

import akka.actor.{ActorRef, Props}
import com.poshly.accounts.client.UserService
import com.poshly.accounts.client.data.User
import com.poshly.core.logging.Loggable
import com.poshly.core.mailer.{Mailer, MailerConfiguration}
import com.poshly.core.redis.RedisConfiguration
import com.poshly.pie.client.InsightDefinitionService
import com.poshly.pie.client.data.InsightDefinition
import com.poshly.products._
import com.poshly.products.util.AutorizerMock
import com.poshly.qms.client.ProductService
import com.redis.RedisClientPool
import com.typesafe.config.ConfigFactory
import org.mockito.Matchers._
import org.mockito.Mockito._
import spray.http.{StatusCodes, _}

import scala.concurrent.Future

class ProductsWebRouteSpec extends RouteTest with Loggable {

  trait ServicesMock extends AnswersConfiguration with InsightDefinitionConfiguration with GeocodeConfiguration with
    QuestionServiceConfiguration with ProductConfiguration with AnalyticsConfiguration with AccountUserConfiguration
    with RedisConfiguration with BaseConfiguration with MailerConfiguration with OfferConfiguration

  def createMockServices() = {

    val servicesMock = mock[ServicesMock]

    val insightDefsServiceMock = mock[InsightDefinitionService]

    when(servicesMock.insightDefinitions) thenReturn insightDefsServiceMock

//    val searchServiceMock = mock[InsightDefinitionSearchIndex]
//
//    when(servicesMock.insightDefinitionSearchIndex) thenReturn searchServiceMock
//
    val productServiceMock = mock[ProductService]

    when(servicesMock.products) thenReturn productServiceMock

//    val httpEntityFromService = new Right(HttpEntity(contentType = ContentTypes.`application/json`, string = ""))
//
    when(productServiceMock.findAllBrands()).thenReturn(Future(Seq()))
//
//    val insightsServiceMock = mock[InsightsAnalyticsService]
//
//    when(servicesMock.analytics) thenReturn insightsServiceMock
//
//    when(insightsServiceMock.findCountByGroupings(any[Option[String]], any[Option[String]], anyString(), any[(Long,
//      Long)])) thenReturn Future(AnalyticsGroupCountEnvelope.empty)
//
//    when(servicesMock.insightDefinitionSearchIndex) thenReturn searchServiceMock
//
//    when(searchServiceMock.findBy(any[InsightSearchCriteria])) thenReturn InsightDefinitionSearchWebServiceEnvelope
//    (InsightDefinitionSearchWebServiceHeader(0), List())
//
//    when(insightDefsServiceMock.fetchById(any())) thenReturn Future(InsightDefinition.empty)

    val userServiceMock = mock[UserService]

    val user = User.empty.addRole("admin").applyId(Some("userId"))

    when(userServiceMock.signinWithPassword(anyString(), anyString())) thenReturn Future(user)

    when(servicesMock.userService) thenReturn userServiceMock

    val redisMock = mock[RedisClientPool]

    when(redisMock.withClient(any())) thenReturn None

    when(servicesMock.redisClient) thenReturn redisMock

    val testConfiguration = ConfigFactory.defaultReference()

    //    val testConfiguration = ConfigFactory.empty().
    //      withValue("insights.access.cookie.name", ConfigValueFactory.fromAnyRef("X-Poshly-AccessToken")).
    //      withValue("insights.access.cookie.domain", ConfigValueFactory.fromAnyRef(".poshly.com")).
    //      withValue("insights.access.ttl", ConfigValueFactory.fromAnyRef("24 hours")).
    //      withValue("security.salt", ConfigValueFactory.fromAnyRef("salt")).
    //      withValue("webkit.cors.support", ConfigValueFactory.fromIterable(List("*").asJava))

    when(servicesMock.config) thenReturn testConfiguration

    val mailMock = mock[Mailer]

    when(servicesMock.mailer) thenReturn mailMock

//    val testQDEnvelope = QuestionDataEnvelope(QuestionDataHeader("insightDefId", 1), Seq(QuestionData("quid",
//      "title", None, "text", None, Seq())))
//    val questionDataMock = mock[QuestionDataService]
//
//    when(questionDataMock.findQuestionsDataByInsightDef(any[String])) thenReturn Future(testQDEnvelope)
//
//    when(servicesMock.questionData) thenReturn questionDataMock

    servicesMock
  }

  def createProductsWebRoute(servicesMock: ServicesMock, _authenticated: Boolean = true) = {
    new ProductsWebRoute with AutorizerMock {

      def authenticated: Boolean = _authenticated

      def actorRefFactory = system

      def auth: ActorRef = system.actorOf(Props(new AuthenticationActor(servicesMock)))

      def mail: ActorRef = system.actorOf(Props(new MailActor(servicesMock)))

      def logout: akka.actor.ActorRef = system.actorOf(Props(new LogoutActor(servicesMock)))
      def products: akka.actor.ActorRef = system.actorOf(Props(new ProductActor(servicesMock)))
      def productsMobile: akka.actor.ActorRef = system.actorOf(Props(new ProductMobileActor(servicesMock)))

      def userActor: ActorRef = system.actorOf(Props(new UserActor(servicesMock)))
      def userMobileActor: ActorRef = system.actorOf(Props(new UserMobileActor(servicesMock)))

      def productsConfig = servicesMock.config

      def components: RedisConfiguration with BaseConfiguration = servicesMock
    }
  }

  val authorizeProductRoute = createProductsWebRoute(createMockServices())

  it should "return a 602 status for expired timestamp" in {
    Get("/token?timestamp=1491399902404") ~> authorizeProductRoute.routes ~> check {
      status should be(StatusCodes.OK)
      val userJson = """{"code":602,"message":"Time not within server time"}"""
      logger.debug("body")
      logger.debug(body.asString)
      body.asString shouldEqual userJson
    }
  }

  // TODO: Make this work with mock data (example in Admin > AdministrationWebRouteSpec)
//  it should "return a 200 status for users counts" in {
//    Get("/api/v1/products/getFastPartialProductByIdV3/P0000000000000000033") ~> authorizeProductRoute.routes ~> check {
//      status should be(StatusCodes.OK)
//      val userJson = """{"code":602,"message":"Time not within server time"}"""
//      logger.debug("body")
//      logger.debug(body.asString)
//      body.asString shouldEqual userJson
//    }
//  }

}
