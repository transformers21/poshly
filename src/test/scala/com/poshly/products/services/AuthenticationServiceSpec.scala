package com.poshly.products.services

import com.poshly.accounts.client.{UserService => AccountsUserService}
import com.poshly.accounts.client.data.User
import com.poshly.core.data.ValidationError
import com.poshly.core.logging.Loggable
import com.poshly.core.redis.RedisConfiguration
import com.poshly.products.data.SessionTracking
import com.poshly.products.{AccountUserConfiguration, UnitTest}
import com.redis.RedisClientPool
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import org.mockito.Matchers._
import org.mockito.Mockito._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._

class AuthenticationServiceSpec extends UnitTest with Loggable {

  trait ServicesMock extends AccountUserConfiguration with RedisConfiguration

  val servicesMock = mock[ServicesMock]

  val userServiceMock = mock[AccountsUserService]

  val redisMock = mock[RedisClientPool]

  when(redisMock.withClient(any())) thenReturn None

  when(servicesMock.redisClient) thenReturn redisMock

  val token = "tokenString"
  val tracking = SessionTracking.accessGrantingToken(token)

  "The Auth Service" should
    "handle happy path" in {

    val user = User.empty.applyRoles(Seq("admin"))

    when(userServiceMock.signinWithPassword(anyString(), anyString())) thenReturn Future(user)
    when(servicesMock.userService) thenReturn userServiceMock
    when(servicesMock.config) thenReturn ConfigFactory.empty().withValue("security.salt", ConfigValueFactory.fromAnyRef("salt"))

    val authServ = new AuthenticationService(servicesMock)

    authServ.authenticateUser("email", "password", tracking) shouldBe a[Future[_]]
  }

//  "The Auth Service" should
//    "fail authorization with exception" in {
//
//    val user = User.empty.applyRoles(Seq("unauthorized"))
//
//    when(userServiceMock.signinWithPassword(anyString(), anyString())) thenReturn Future(user)
//
//    when(servicesMock.userService) thenReturn userServiceMock
//
//    val authServ = new AuthenticationService(servicesMock)
//
//    an[ValidationError] should be thrownBy Await.result(authServ.authenticateUser("email", "password", tracking), 1 second)
//  }
}
