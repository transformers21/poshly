package com.poshly.products.actors

import akka.actor.Actor
import com.poshly.accounts.client.data.mapper.{AddressJValueMapper, LocationJValueMapper, UserJValueMapper}
import com.poshly.core.data
import com.poshly.core.logging.Loggable
import com.poshly.products.data.{AddressUI, LocationUI, LocationWithNameAndPhone}
import com.poshly.products.services.UserService
import com.poshly.products.util.auth.Authorizer
import com.poshly.products.{AccountUserConfiguration, GeocodeConfiguration}
import spray.routing.Route

class UserActor(comps: GeocodeConfiguration with AccountUserConfiguration) extends Actor with UserRoute with Loggable {

  def services = new UserService(comps)

  def actorRefFactory = context

  def receive = runRoute(routes)

  def components = comps

}

trait UserRoute extends BaseRoute with Authorizer {
  def services: UserService

  def components: GeocodeConfiguration with AccountUserConfiguration

  val routes: Route =
    authorizedRequest { userId =>
      pathEndOrSingleSlash{
        get {
          // TODO: not tested
          complete(components.userService.fetchById(userId).map(UserJValueMapper.unapply))
        }
      } ~
//      path("locale") {
//        get {
//          complete(services.fetchUserLocale(userId))
//        } ~
//          post {
//            parameters('locale) { locale =>
//              onComplete(services.updateUserLocale(userId, locale))(handleTry(_)(_ =>
//                complete("OK")))
//            }
//          }
//      } ~
        pathPrefix("location") {
          pathEndOrSingleSlash {
            get {
              onComplete(services.getUserLocationWithNameAndPhone(userId))(handleTry(_)(loc =>
                complete(loc)))
            }
          } ~
            pathPrefix("smart") {
              path("autocomplete" / Segment) { prefix =>
                get {
                  complete(services.addressAutocomplete(prefix))
                }
              } ~
                path("verify") {
                  post {
                    entity(as[AddressUI]) { addressUI =>
                      val address: data.Address = AddressUI.toCoreAddress(addressUI)
                      onComplete(services.verify(address))(handleTry(_){location =>
                        complete(LocationUI(location))
                      })
                    }
                  }
                }
            } ~
            path("save") {
              post {
                entity(as[LocationWithNameAndPhone]) { locationWithNameAndPhone =>
                  onComplete(services.save(userId, locationWithNameAndPhone))(handleTry(_) { user =>
                    logger.debug("updated user")
                    logger.debug(user)
                    complete("OK")
                  })
                }
              }
            }
        }
    }
}
