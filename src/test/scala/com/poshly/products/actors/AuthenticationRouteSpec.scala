package com.poshly.products.actors

import com.poshly.accounts.client.UserService
import com.poshly.accounts.client.data.User
import com.poshly.core.redis.RedisConfiguration
import com.poshly.products.{BaseConfiguration, AccountUserConfiguration}
import com.poshly.products.services.AuthenticationService
import com.redis.RedisClientPool
import com.typesafe.config.{ConfigFactory, Config, ConfigValueFactory}
import org.mockito.Matchers._
import org.mockito.Mockito._
import spray.http.HttpHeaders.{Cookie, `Set-Cookie`}
import spray.http.{HttpHeaders, HttpCookie, FormData, StatusCodes}
import scala.concurrent._

class AuthenticationRouteSpec extends RouteTest {

  trait ServicesMock extends AccountUserConfiguration with RedisConfiguration with BaseConfiguration

  def createMockServices(user: User) = {
    val servicesMock = mock[ServicesMock]

    val userServiceMock = mock[UserService]

    when(userServiceMock.signinWithPassword(anyString(), anyString())) thenReturn Future(user)

    when(servicesMock.userService) thenReturn userServiceMock

    val testConfiguration = ConfigFactory.defaultReference()

    when(servicesMock.config) thenReturn testConfiguration

    val redisMock = mock[RedisClientPool]

    when(redisMock.withClient(any())) thenReturn Some("it's cached")

    when(servicesMock.redisClient) thenReturn redisMock

    servicesMock
  }

  def createAuthRoute(user: User) = {
    new AuthenticationRoute {
      def actorRefFactory = system

      lazy val mockComponents = createMockServices(user)

      def service: AuthenticationService = new AuthenticationService(mockComponents)

      def components: AccountUserConfiguration with RedisConfiguration = mockComponents
    }
  }

  val authorizedUser = User
    .empty
    .applyId(Some("user-id"))
    .applyUsername(Some("usernameValue"))
    .applyEmail(Some("emailValue"))
    .applyPassword("passwordValue")
    .applyFirstName(Some("firstNameValue"))
    .applyLastName(Some("lastNameValue"))
    .applyAvatar(Some("avatarValue"))
    .applyAccount(Some("accountValue"))
    .applyFacebook(Some("facebookValue"))
    .applyTwitter(Some("twitterValue"))
    .applyBlog(Some("blogValue"))
    .applyPintrest(Some("pintrestValue"))
    .applyGoogleplus(Some("googleplusValue"))
    .applyHomePhone(Some("homePhoneValue"))
    .applyMobilePhone(Some("mobilePhoneValue"))
    .applyLocale("localeValue")
    .applyLang("langValue")
    .applyTimezone("timezoneValue")
    .applyGender(Some("genderValue"))
    .applyBirthday(None)
    .applyLocations(Seq())
    .applyRoles(Seq("admin"))
    .applyQuestionCount(1)
    .applyQuizCount(2).applyTemporary(false)
    .applyActive(true)
    .applyCreatedBy("testUserValue")
    .applyUpdatedBy("testUserValue")

  val badAuthRoute = createAuthRoute(User.empty)

  "The AuthenticationRoute" should "return a validation error when missing credentials" in {
    Post("/", FormData(Map("password" -> "password"))) ~> badAuthRoute.routes ~> check {
      status should be(StatusCodes.OK)
      body.asString shouldEqual """{"error":{"type":"ValidationError","messages":[{"code":"MISSING_CREDENTIALS","message":"Missing Credentials"}]}}"""
    }
  }

//  it should "return validation error when unsupported role" in {
//    Post("/", FormData(Map("password" -> "password", "username" -> "username"))) ~> badAuthRoute.routes ~> check {
//      status should be(StatusCodes.OK)
//      body.asString shouldEqual """{"error":{"type":"ValidationError","messages":[{"code":"AUTHENTICATION_REQUIRED","message":"Unauthorized role privileges"}]}}"""
//    }
//  }

//  it should "return a 200 status code and add cookie when requested with correct credentials and role" in {
//    val goodAuth = createAuthRoute(authorizedUser)
//    Post("/", FormData(Map("password" -> "password", "username" -> "username"))) ~> goodAuth.routes ~> check {
//      status should be(StatusCodes.OK)
//      header[`Set-Cookie`].isDefined
//      header[`Set-Cookie`].get.cookie.name shouldEqual "local-pt"
//    }
//  }

  val rawHeader = HttpHeaders.RawHeader(name = "Cookie", value = "local-pt=agt=sameString")

//  it should "return a 200 status code and use same cookie when requested with correct credentials and role and cookie exists" in {
//    val goodAuth = createAuthRoute(authorizedUser)
//    Post("/", FormData(Map("password" -> "password", "username" -> "username"))) ~> addHeader(rawHeader) ~> goodAuth.routes ~> check {
//      status should be(StatusCodes.OK)
//      header[`Set-Cookie`].isDefined
//      header[`Set-Cookie`].get.cookie.name shouldEqual "local-pt"
//      header[`Set-Cookie`].get.cookie.content shouldEqual "agt=sameString"
//    }
//  }
}
