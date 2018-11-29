//package com.poshly.products.actors
//
//import akka.actor.Actor
//import com.poshly.core.logging.Loggable
//import com.poshly.core.redis.RedisConfiguration
//import com.poshly.products.BaseConfiguration
//import com.poshly.products.util.auth.Authorizer
//import com.typesafe.config.ConfigObject
//
//class ConfigurationActor(services: RedisConfiguration with BaseConfiguration) extends Actor with ConfigurationRoute with Loggable {
//
//  def components = services
//
//  def actorRefFactory = context
//
//  def receive = runRoute(routes)
//}
//
//trait ConfigurationRoute extends BaseRoute with Authorizer {
//
//  import org.json4s._
//
//  def components: RedisConfiguration with BaseConfiguration
//
//  private def toJson(configObject: ConfigObject): JObject = toJson(configObject.unwrapped())
//
//  private def toJson(configObject: java.util.Map[String, AnyRef]): JObject = {
//    import scala.collection.JavaConversions._
//
//    JObject {
//      configObject.toList.map {
//        case (label, null) =>
//          JField(label, JNull)
//        case (label, booleanValue: java.lang.Boolean) =>
//          JField(label, JBool(booleanValue))
//        case (label, stringValue: java.lang.String) =>
//          JField(label, JString(stringValue))
//        case (label, doubleValue: java.lang.Double) =>
//          JField(label, JDouble(doubleValue))
//        case (label, numberValue: java.lang.Number) =>
//          JField(label, JInt(BigInt(numberValue.longValue())))
//        case (label, configValue: ConfigObject) =>
//          JField(label, toJson(configValue))
//        case (label, configValue: java.util.Map[String, AnyRef]) =>
//          JField(label, toJson(configValue))
//        case (label, configValue: AnyRef) =>
//          JField(label, JNull)
//      }
//    }
//  }
//
//  val routes =
//    pathPrefix("v1") {
//      pathSuffix("public") {
//        get {
//          complete {
//            import org.json4s.JsonDSL.WithBigDecimal._
//
//            val json: JValue =
//              ("meta" -> ("type" -> "public")) ~~
//                ("data" -> toJson(components.config.getObject("products.public")))
//
//            json
//          }
//        }
//      } ~
//        pathSuffix("private") {
//          authorizedRequest { userId =>
//            get {
//              complete {
//                import org.json4s.JsonDSL.WithBigDecimal._
//
//                val json: JValue =
//                  ("meta" -> ("type" -> "private")) ~~
//                    ("data" -> toJson(components.config.getObject("products.private")))
//
//                json
//              }
//          }
//        }
//      }
//    }
//
//}
