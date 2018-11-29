package com.poshly.products.actors

import akka.actor.Actor
import com.poshly.accounts.client.data.mapper.{AddressJValueMapper, LocationJValueMapper}
import com.poshly.core.data
import com.poshly.core.logging.Loggable
import com.poshly.products.data.{AddressUI, LocationUI, LocationWithNameAndPhone}
import com.poshly.products.services.UserService
import com.poshly.products.util.auth.Authorizer
import com.poshly.products.{AccountUserConfiguration, GeocodeConfiguration}
import org.json4s.JsonAST.JObject
import spray.http.{AllOrigins, HttpHeaders}
import spray.routing.{AuthenticationFailedRejection, Route}
import spray.routing.AuthenticationFailedRejection.CredentialsRejected

class UserMobileActor(comps: GeocodeConfiguration with AccountUserConfiguration)
  extends Actor with UserMobileRoute with Loggable {

  def services = new UserService(comps)

  def actorRefFactory = context

  def receive = runRoute(routes)

  def components = comps

}

trait UserMobileRoute extends BaseRoute with Authorizer {
  def services: UserService

  def components: GeocodeConfiguration with AccountUserConfiguration

  val routes: Route = pathPrefix("location") {
    pathEndOrSingleSlash {
      get {
        parameters('token) { (token) =>
          respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
            getUserIdByTokenAndRefresh(token) match {
              case Some(userId) =>
                onComplete(services.getUserLocationWithNameAndPhone(userId))(handleTry(_)(loc =>
                  complete(loc)))
              case _ => completeErrorMissingOrNotValidParams
            }
          }
        }
      }
    } ~
      pathPrefix("smart") {
        path("autocomplete" / Segment) { prefix =>
          get {
            parameters('token) { token =>
              respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
                getUserIdByTokenAndRefresh(token) match {
                  case Some(_) => complete(services.addressAutocomplete(prefix))
                  case _ => completeErrorMissingOrNotValidParams
                }
              }
            }
          }
        } ~
          path("verify") {
            post {
              parameters('token) { (token) =>
                respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
                  entity(as[AddressUI]) { addressUI =>
                    (getUserIdByTokenAndRefresh(token), addressUI) match {
                      case (Some(_), addr) =>
                        val address: data.Address = AddressUI.toCoreAddress(addr)
                        onComplete(services.verify(address))(handleTry(_)(location =>
                          complete(LocationUI(location))))
                      case _ => completeErrorMissingOrNotValidParams
                    }
                  }
                }
              }
            }
          }
      } ~
      path("save") {
        post {
          parameters('token) { (token) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              entity(as[LocationWithNameAndPhone]) { locationWithNameAndPhone =>
                getUserIdByTokenAndRefresh(token) match {
                  case Some(userId) =>
                    onComplete(services.save(userId, locationWithNameAndPhone))(handleTry(_)(_ => complete("OK")))
                  case _ => completeErrorMissingOrNotValidParams
                }
              }
            }
          }
        }
      }
  }
}
