//package com.poshly.products.actors
//
//import akka.actor.{Props, ActorRef}
//import akka.event.Logging
//import com.poshly.accounts.client.UserService
//import com.poshly.accounts.client.data.User
//import com.poshly.core.logging.Loggable
//import com.poshly.core.mailer.{Mailer, MailerConfiguration}
//import com.poshly.core.redis.RedisConfiguration
//import com.poshly.products._
//import com.poshly.products.data._
//import com.poshly.products.services._
//import com.poshly.products.util.AutorizerMock
//import com.poshly.pie.client.InsightDefinitionService
//import com.poshly.pie.client.data.InsightDefinition
//import com.redis.RedisClientPool
//import com.typesafe.config.{ConfigFactory, Config, ConfigValueFactory}
//import org.mockito.Matchers._
//import org.mockito.Mockito._
//import spray.http.HttpHeaders.`Set-Cookie`
//import spray.http._
//import spray.routing.directives.{LogEntry, DebuggingDirectives, LoggingMagnet}
//import spray.routing.{AuthorizationFailedRejection, Rejection, RequestContext}
//
//import scala.concurrent.Future
//
//class InsightsWebRouteSpec extends RouteTest with Loggable {
//
//  trait ServicesMock extends SearchIndexConfiguration with AnswersConfiguration with InsightDefinitionConfiguration with AnalyticsConfiguration with QuestionDataConfiguration with AccountUserConfiguration with RedisConfiguration with BaseConfiguration with MailerConfiguration
//
//  def createMockServices() = {
//
//    val servicesMock = mock[ServicesMock]
//
//    val insightDefsServiceMock = mock[InsightDefinitionService]
//
//    when(servicesMock.insightDefinitions) thenReturn insightDefsServiceMock
//
//    val searchServiceMock = mock[InsightDefinitionSearchIndex]
//
//    when(servicesMock.insightDefinitionSearchIndex) thenReturn searchServiceMock
//
//    val answerServiceMock = mock[AnswerService]
//
//    when(servicesMock.answers) thenReturn answerServiceMock
//
//    val httpEntityFromService = new Right(HttpEntity(contentType = ContentTypes.`application/json`, string = ""))
//
//    when(answerServiceMock.findAnswersById("1")).thenReturn(Future(httpEntityFromService))
//
//    val insightsServiceMock = mock[InsightsAnalyticsService]
//
//    when(servicesMock.analytics) thenReturn insightsServiceMock
//
//    when(insightsServiceMock.findCountByGroupings(any[Option[String]], any[Option[String]], anyString(),any[(Long,Long)])) thenReturn Future(AnalyticsGroupCountEnvelope.empty)
//
//    when(servicesMock.insightDefinitionSearchIndex) thenReturn searchServiceMock
//
//    when(searchServiceMock.findBy(any[InsightSearchCriteria])) thenReturn InsightDefinitionSearchWebServiceEnvelope(InsightDefinitionSearchWebServiceHeader(0), List())
//
//    when(insightDefsServiceMock.fetchById(any())) thenReturn Future(InsightDefinition.empty)
//
//    val userServiceMock = mock[UserService]
//
//    val user = User.empty.addRole("admin").applyId(Some("userId"))
//
//    when(userServiceMock.signinWithPassword(anyString(), anyString())) thenReturn Future(user)
//
//    when(servicesMock.userService) thenReturn userServiceMock
//
//    val redisMock = mock[RedisClientPool]
//
//    when(redisMock.withClient(any())) thenReturn None
//
//    when(servicesMock.redisClient) thenReturn redisMock
//
//    val testConfiguration = ConfigFactory.defaultReference()
//
//    //    val testConfiguration = ConfigFactory.empty().
//    //      withValue("insights.access.cookie.name", ConfigValueFactory.fromAnyRef("X-Poshly-AccessToken")).
//    //      withValue("insights.access.cookie.domain", ConfigValueFactory.fromAnyRef(".poshly.com")).
//    //      withValue("insights.access.ttl", ConfigValueFactory.fromAnyRef("24 hours")).
//    //      withValue("security.salt", ConfigValueFactory.fromAnyRef("salt")).
//    //      withValue("webkit.cors.support", ConfigValueFactory.fromIterable(List("*").asJava))
//
//    when(servicesMock.config) thenReturn testConfiguration
//
//    val mailMock = mock[Mailer]
//
//    when(servicesMock.mailer) thenReturn mailMock
//
//    val testQDEnvelope = QuestionDataEnvelope(QuestionDataHeader("insightDefId",1),Seq(QuestionData("quid","title",None,"text",None, Seq())))
//    val questionDataMock = mock[QuestionDataService]
//
//    when(questionDataMock.findQuestionsDataByInsightDef(any[String])) thenReturn Future(testQDEnvelope)
//
//    when(servicesMock.questionData) thenReturn questionDataMock
//
//    servicesMock
//  }
//
//  def createInsightsWebRoute(servicesMock: ServicesMock, _authenticated: Boolean = true) = {
//    new ProductsWebRoute with AutorizerMock {
//
//      def authenticated: Boolean = _authenticated
//
//      def actorRefFactory = system
//
//      def answers: ActorRef = system.actorOf(Props(new AnswerActor(servicesMock)))
//
////      val navigationServiceMock = mock[NavigationService]
////      when(navigationServiceMock.loadNavigation()).thenReturn(Future(new Right(HttpEntity(contentType = ContentTypes.`application/json`, string = ""))))
////
////      def navigation: ActorRef = system.actorOf(Props(new NavigationActor {
////        override val navigationService = navigationServiceMock
////      }))
//
//      def analytics: ActorRef = system.actorOf(Props(new AnalyticsGroupingActor(servicesMock)))
//
//      def search: ActorRef = system.actorOf(Props(new InsightsSearchActor(servicesMock)))
//
//      def insightDefs: ActorRef = system.actorOf(Props(new InsightsDefinitionActor(servicesMock)))
//
//      def auth: ActorRef = system.actorOf(Props(new AuthenticationActor(servicesMock)))
//
//      def mail: ActorRef = system.actorOf(Props(new MailActor(servicesMock)))
//
//      def questionData: ActorRef = system.actorOf(Props(new QuestionActor(servicesMock)))
//
//      def configActor: ActorRef = system.actorOf(Props(new ConfigurationActor(servicesMock)))
//
//      def insightsConfig = servicesMock.config
//
//      def metrics: ActorRef = null
//
//      def paint: ActorRef = null
//
//      def components: RedisConfiguration with BaseConfiguration = servicesMock
//    }
//  }
//
//  val authorizedInsightRoute = createInsightsWebRoute(createMockServices())
//
//  "The InsightWebRoute" should "return a 200 status for question count requests" in {
//    Get("/api/data/v1/question/1/count") ~> authorizedInsightRoute.routes ~> check {
//      status should be(StatusCodes.OK)
//    }
//  }
//
//  it should "return a 200 status code for analytics request too" in {
//    Get("/api/data/v1/insight/ID000000000000000A20") ~> authorizedInsightRoute.routes ~> check {
//      status should be(StatusCodes.OK)
//    }
//  }
//
//  it should "return a 200 status code for search request too" in {
//    Get("/api/v1/search?q=wowsers") ~> authorizedInsightRoute.routes ~> check {
//      status should be(StatusCodes.OK)
//    }
//  }
//
//  it should "return a 200 status code for insights def view with erroneous param value which should result in finagle service call" in {
//    Get("/api/v1/insight/definition/ID000000000000000A62?view=notSupported") ~> authorizedInsightRoute.routes ~> check {
//      status should be(StatusCodes.OK)
//    }
//  }
//
//  it should "return a 200 status code for insights def view filterBy" in {
//    Get("/api/v1/insight/definition/ID000000000000000A62?view=filterby") ~> authorizedInsightRoute.routes ~> check {
//      status should be(StatusCodes.OK)
//    }
//  }
//
//  it should "return a 200 status code for insights def without view request param" in {
//    Get("/api/v1/insight/definition/ID000000000000000A62") ~> authorizedInsightRoute.routes ~> check {
//      status should be(StatusCodes.OK)
//    }
//  }
//
//  it should "return a 200 status code for posting an authentication request with username" in {
//    Post("/api/v1/authenticate", FormData(Map("password" -> "password", "username" -> "username"))) ~> authorizedInsightRoute.routes ~> check {
//      status should be(StatusCodes.OK)
//      logger.info("Cookie")
//      logger.info(header[`Set-Cookie`])
//      header[`Set-Cookie`].isDefined
//      header[`Set-Cookie`].get.cookie.name === "X-Poshly-AccessToken"
//    }
//  }
//
//
//  it should "return question data" in {
//    Get("/api/v1/questions/insight/ID1/1")  ~> authorizedInsightRoute.routes  ~> check {
//      status should be(StatusCodes.OK)
//    }
//  }
//
//  //now test with failing authorization
//  val unauthorizedInsightRoute = createInsightsWebRoute(createMockServices(), false)
//
//  it should "reject question count requests for failed auth" in {
//    Get("/api/data/v1/question/1/count") ~> unauthorizedInsightRoute.routes ~> check {
//      rejection shouldEqual AuthorizationFailedRejection
//    }
//  }
//
//  it should "reject analytics request for failed auth" in {
//    Get("/api/data/v1/insight/ID000000000000000A20") ~> unauthorizedInsightRoute.routes ~> check {
//      rejection shouldEqual AuthorizationFailedRejection
//    }
//  }
//
//  it should "still return a 200 status code for search request as not behind authentication" in {
//    Get("/api/v1/search?q=wowsers") ~> unauthorizedInsightRoute.routes ~> check {
//      status should be(StatusCodes.OK)
//    }
//  }
//
//  it should "reject insights def for failed auth" in {
//    Get("/api/v1/insight/definition/ID000000000000000A62?view=notSupported") ~> unauthorizedInsightRoute.routes ~> check {
//      rejection shouldEqual AuthorizationFailedRejection
//    }
//  }
//
//  it should "reject insights def view filterBy for failed auth" in {
//    Get("/api/v1/insight/definition/ID000000000000000A62?view=filterby") ~> unauthorizedInsightRoute.routes ~> check {
//      rejection shouldEqual AuthorizationFailedRejection
//    }
//  }
//
//  it should "reject insights def without view request param for failed auth" in {
//    Get("/api/v1/insight/definition/ID000000000000000A62") ~> unauthorizedInsightRoute.routes ~> check {
//      rejection shouldEqual AuthorizationFailedRejection
//    }
//  }
//
//  it should "should still return a 200 status code for posting an authentication request with username because no auth required" in {
//    Post("/api/v1/authenticate", FormData(Map("password" -> "password", "username" -> "username"))) ~> unauthorizedInsightRoute.routes ~> check {
//      status should be(StatusCodes.OK)
//      logger.info("Cookie")
//      logger.info(header[`Set-Cookie`])
//      header[`Set-Cookie`].isDefined
//      header[`Set-Cookie`].get.cookie.name === "X-Poshly-AccessToken"
//    }
//  }
//}
