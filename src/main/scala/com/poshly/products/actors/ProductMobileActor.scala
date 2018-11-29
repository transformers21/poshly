package com.poshly.products.actors

import akka.actor.{Actor, ActorContext}
import com.poshly.core.logging.Loggable
import com.poshly.products._
import com.poshly.products.routes.RoutesUtil
import com.poshly.products.util.auth._
import org.json4s.JsonAST.JObject
import spray.http.{AllOrigins, HttpHeaders}
import spray.routing.Route

class ProductMobileActor(comps: ProductConfiguration with
  OfferConfiguration with
  QuestionServiceConfiguration with
  AnswersConfiguration with
  AnalyticsConfiguration with
  InsightDefinitionConfiguration) extends Actor with ProductMobileRoute with Loggable {
  def components = comps
  def actorRefFactory: ActorContext = context
  def receive: Receive = runRoute(routes)
}

trait ProductMobileRoute extends BaseRoute with Authorizer with RoutesUtil {

  def components: ProductConfiguration with OfferConfiguration with QuestionServiceConfiguration
    with AnswersConfiguration with AnalyticsConfiguration with InsightDefinitionConfiguration

  /* More info: http://docs.poshly.com/display/dev/PAPI+Public+API
   Example call (put in one line): http://localhost:30030/api/v2/products/getRestriction?token=YBZJOXJKCROLXHZFMZJK
   &QID=Q0000000000000000235&source=poshly&timestamp=1480356704023&signature
   =5c03ec7e87ff97635bdf6a7037156f84ae7a6285dc49c8664ce2bf4c0f3f3952
  */
  val routes: Route =
    pathPrefix("all") {
      get {
        parameters('token, 'q.as[String].?, 'skip.as[Int].?, 'limit.as[Int].?, 'b.as[String].?) { (token, q, skip, limit, b) =>
          respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
            getUserIdByTokenAndRefresh(token) match {
              case Some(_) => all(q, skip, limit, b)
              case _ => completeErrorMissingOrNotValidParams
            }
          }
        }
      }
    } ~
      pathPrefix("popular") {
        get {
          parameters('token) { token =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              getUserIdByTokenAndRefresh(token) match {
                case Some(_) => findPopularProducts
                case _ => completeErrorMissingOrNotValidParams
              }
            }
          }
        }
      } ~
      path("findAllBrands") {
        get {
          parameters('token, 'skip.as[Int].?, 'limit.as[Int].?) { (token, skip, limit) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              getUserIdByTokenAndRefresh(token) match {
                case Some(_) => findAllBrands(skip, limit)
                case _ => completeErrorMissingOrNotValidParams
              }
            }
          }
        }
      } ~
      path("findAllBrandsWithLogoURLs") {
        get {
          parameters('token, 'skip.as[Int].?, 'limit.as[Int].?) { (token, skip, limit) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              getUserIdByTokenAndRefresh(token) match {
                case Some(_) => findAllBrandsWithLogoURLs(skip, limit)
                case _ => completeErrorMissingOrNotValidParams
              }
            }
          }
        }
      } ~
      pathPrefix("getProductsByUserIdListName") {
        get {
          parameters('token, 'listName, 'skip.as[Int].?, 'limit.as[Int].?) { (token, listName, skip, limit) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              getUserIdByTokenAndRefresh(token) match {
                case Some(userId) => getProductsByUserIdListName(userId, listName, skip, limit)
                case _ => completeErrorMissingOrNotValidParams
              }
            }
          }
        }
      } ~
      pathPrefix("upsertUserProductList") {
        post {
          parameters('token) { (token) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              entity(as[JObject]) { productListJObj =>
                (getUserIdByTokenAndRefresh(token), productListJObj) match {
                  case (Some(userId), pListJObj) => upsertUserProductList(userId, pListJObj)
                  case _ => completeErrorMissingOrNotValidParams
                }
              }
            }
          }
        }
      } ~
      pathPrefix("deleteProductFromList") {
        post {
          parameters('token) { (token) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              entity(as[JObject]) { prodAndListJObj =>
                (getUserIdByTokenAndRefresh(token), prodAndListJObj) match {
                  case (Some(userId), _) => deleteProductFromList(userId, prodAndListJObj)
                  case _ => completeErrorMissingOrNotValidParams
                }
              }
            }
          }
        }
      } ~
      pathPrefix("findPersonalizePoshlyQuestions") {
        get {
          parameters('token, 'skip.as[Int].?, 'limit.as[Int].?) { (token, skip, limit) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              getUserIdByTokenAndRefresh(token) match {
                case Some(userId) => findPersonalizePoshlyQuestions(userId, skip, limit)
                case _ => completeErrorMissingOrNotValidParams
              }
            }
          }
        }
      } ~
      pathPrefix("findMyAnswersCount") {
        get {
          parameters('token) { (token) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              getUserIdByTokenAndRefresh(token) match {
                case Some(userId) => findMyAnswersCount(userId)
                case _ => completeErrorMissingOrNotValidParams
              }
            }
          }
        }
      } ~
      pathPrefix("saveAnswer") {
        post {
          parameters('token) { (token) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              entity(as[JObject]) { questionJObj =>
                (getUserIdByTokenAndRefresh(token), questionJObj) match {
                  case (Some(userId), qJObj) => saveAnswer(userId, qJObj)
                  case _ => completeErrorMissingOrNotValidParams
                }
              }
            }
          }
        }
      } ~
      pathPrefix("saveMultipleSelectAnswer") {
        post {
          parameters('token) { (token) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              entity(as[JObject]) { questionJObj =>
                (getUserIdByTokenAndRefresh(token), questionJObj) match {
                  case (Some(userId), qJObj) => saveMultipleSelectAnswer(userId, qJObj)
                  case _ => completeErrorMissingOrNotValidParams
                }
              }
            }
          }
        }
      } ~
//      pathPrefix("getCompleteProductById") {
//        get {
//          parameters('token, 'pid) { (token, pid) =>
//            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
//              val userIdOpt = getUserIdByTokenAndRefresh(token)
//              (userIdOpt, pid) match {
//                case (Some(userId), _) => getCompleteProductById(userId, pid)
//                case _ => completeErrorMissingOrNotValidParams
//              }
//            }
//          }
//        }
//      } ~
      path("getFastPartialProductById") {
        get {
          parameters('token, 'pid) { (token, pid) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              val userIdOpt = getUserIdByTokenAndRefresh(token)
              (userIdOpt, pid) match {
                case (Some(userId), _) => getFastPartialProductByIdV1(userId, pid)
                case _ => completeErrorMissingOrNotValidParams
              }
            }
          }
        }
      } ~
      path("getFastPartialProductByIdV2") {
        get {
          parameters('token, 'pid) { (token, pid) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              val userIdOpt = getUserIdByTokenAndRefresh(token)
              (userIdOpt, pid) match {
                case (Some(userId), _) => getFastPartialProductByIdV2(userId, pid)
                case _ => completeErrorMissingOrNotValidParams
              }
            }
          }
        }
      } ~
      path("getFastPartialProductByIdV3") {
        get {
          parameters('token, 'pid) { (token, pid) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              val userIdOpt = getUserIdByTokenAndRefresh(token)
              (userIdOpt, pid) match {
                case (Some(userId), _) => getFastPartialProductByIdV3(userId, pid)
                case _ => completeErrorMissingOrNotValidParams
              }
            }
          }
        }
      } ~
      pathPrefix("getSlowPartialProductById") {
        get {
          parameters('token, 'pid) { (token, pid) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              val userIdOpt = getUserIdByTokenAndRefresh(token)
              (userIdOpt, pid) match {
                case (Some(userId), _) => getSlowPartialProductById(userId, pid)
                case _ => completeErrorMissingOrNotValidParams
              }
            }
          }
        }
      } ~
      pathPrefix("findMatchScore") {
        get {
          parameters('token, 'pid) { (token, pid) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              val userIdOpt = getUserIdByTokenAndRefresh(token)
              (userIdOpt, pid) match {
                case (Some(userId), _) => findMatchScore(userId, pid)
                case _ => completeErrorMissingOrNotValidParams
              }
            }
          }
        }
      } ~
      pathPrefix("getUserEligibleOffers") {
        get {
          parameters('token) { (token) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              getUserIdByTokenAndRefresh(token) match {
                case Some(userId) => getUserEligibleOffers(userId)
                case _ => completeErrorMissingOrNotValidParams
              }
            }
          }
        }
      }~
      pathPrefix("getUserFollowUpOffers") {
        get {
          parameters('token) { (token) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              getUserIdByTokenAndRefresh(token) match {
                case Some(userId) => getUserFollowUpOffers(userId)
                case _ => completeErrorMissingOrNotValidParams
              }
            }
          }
        }
      }~
      pathPrefix("confirmAndGetLatestOfferStatus" ) {
        get {
          parameters('token,'offerId) { (token,offerId) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              getUserIdByTokenAndRefresh(token) match {
                case Some(userId) => confirmOffer(offerId,userId)
                case _ => completeErrorMissingOrNotValidParams
              }
            }
          }
        }
      }~
      pathPrefix("confirmUnlimitedOffer" ) {
        get {
          parameters('token,'offerId) { (token,offerId) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              getUserIdByTokenAndRefresh(token) match {
                case Some(userId) => confirmOfferUnlimited(offerId,userId)
                case _ => completeErrorMissingOrNotValidParams
              }
            }
          }
        }

      }~
      pathPrefix("getOfferDynamicStatus" ) {
        get {
          parameters('token,'offerId) { (token,offerId) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              getUserIdByTokenAndRefresh(token) match {
                case Some(userId) => getOfferDynamicStatus(offerId,userId)
                case _ => completeErrorMissingOrNotValidParams
              }
            }
          }
        }

      }~
      pathPrefix("getUserAddress") {
        get {
          parameters('token) { (token) =>
            respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) {
              getUserIdByTokenAndRefresh(token) match {
                case Some(userId) => getUserAddress(userId)
                case _ => completeErrorMissingOrNotValidParams
              }
            }
          }
        }
      }


}
