package com.poshly.products.actors

import akka.actor.Actor
import com.poshly.core.logging.Loggable
import com.poshly.core.redis.RedisConfiguration
import com.poshly.products.AccountUserConfiguration
import com.poshly.products.services.AuthenticationService
import spray.http.{AllOrigins, HttpHeaders, StatusCodes}
import spray.httpx.unmarshalling.Deserializer._
import spray.routing.Route

class LogoutActor(_components: AccountUserConfiguration with RedisConfiguration)
  extends Actor with LogoutRoute {

  def service = new AuthenticationService(_components)

  def actorRefFactory = context

  def components = _components

  def receive = runRoute(routes)
}

trait LogoutRoute extends BaseRoute with Loggable {

  def service: AuthenticationService

  def components: AccountUserConfiguration with RedisConfiguration

  val routes: Route = {
    get {
      parameters('token) { token =>
        respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
          service.signOut(token)
          complete(StatusCodes.OK)
        }
      }
    }
  }
}

