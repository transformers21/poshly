package com.poshly.products.actors

import akka.actor.{Actor, ActorContext}
import com.poshly.core.logging.Loggable
import com.poshly.products._
import com.poshly.products.routes.RoutesUtil
import com.poshly.products.util.auth._
import org.json4s.JsonAST.JObject
import spray.routing.Route

class ProductActor(comps: ProductConfiguration with
  OfferConfiguration with
  QuestionServiceConfiguration with
  AnswersConfiguration with
  AnalyticsConfiguration with
  InsightDefinitionConfiguration) extends Actor with ProductRoute with BaseRoute with Loggable {
  def components = comps

  def actorRefFactory: ActorContext = context

  def receive: Receive = runRoute(routes)
}

trait ProductRoute extends BaseRoute with Authorizer with RoutesUtil {

  def components: ProductConfiguration with OfferConfiguration with QuestionServiceConfiguration with AnswersConfiguration with
    AnalyticsConfiguration with InsightDefinitionConfiguration

  val routes: Route =
    authorizedRequest { userId =>
      pathPrefix("all") {
        get {
          parameters('q.as[String].?, 'skip.as[Int].?, 'limit.as[Int].?, 'b.as[String].?) { (q, skip, limit, b) =>
            all(q, skip, limit, b)
          }
        }
      } ~
        pathPrefix("popular") {
          get {
            findPopularProducts
          }
        } ~
        path("findAllBrands") {
          get {
            parameters('skip.as[Int].?, 'limit.as[Int].?) { (skip, limit) =>
              findAllBrands(skip, limit)
            }
          }
        } ~
        path("findAllBrandsWithLogoURLs") {
          get {
            parameters('skip.as[Int].?, 'limit.as[Int].?) { (skip, limit) =>
              findAllBrandsWithLogoURLs(skip, limit)
            }
          }
        } ~
        path("getFastPartialProductById" / Segment) { pid =>
          get {
            getFastPartialProductByIdV1(userId, pid)
          }
        } ~
        path("getFastPartialProductByIdV2" / Segment) { pid =>
          get {
            getFastPartialProductByIdV2(userId, pid)
          }
        } ~
        path("getFastPartialProductByIdV3" / Segment) { pid =>
          get {
            getFastPartialProductByIdV3(userId, pid)
          }
        } ~
        pathPrefix("getSlowPartialProductById" / Segment) { pid =>
          get {
            getSlowPartialProductById(userId, pid)
          }
        } ~
        pathPrefix("findMatchScore" / Segment) { pid =>
          get {
            findMatchScore(userId, pid)
          }
        } ~
        pathPrefix("getProductIdsByUserIdListName" / Segment) { listName =>
          get {
            getProductIdsByUserIdListName(userId, listName)
          }
        } ~
        pathPrefix("getProductsByUserIdListName" / Segment) { listName =>
          get {
            parameters('skip.as[Int].?, 'limit.as[Int].?) { (skip, limit) =>
              getProductsByUserIdListName(userId, listName, skip, limit)
            }
          }
        } ~
        pathPrefix("getProductListByUserIdProductId" / Segment) { productId =>
          get {
            complete(getProductListByUserIdProductId(userId, productId))
          }
        } ~
        pathPrefix("findPersonalizePoshlyQuestions") {
          get {
            parameters('skip.as[Int].?, 'limit.as[Int].?) { (skip, limit) =>
              findPersonalizePoshlyQuestions(userId, skip, limit)
            }
          }
        } ~
        pathPrefix("findMyAnswersCount") {
          get {
            findMyAnswersCount(userId)
          }
        } ~
        pathPrefix("saveAnswer") {
          post {
            entity(as[JObject]) { questionJObj =>
              saveAnswer(userId, questionJObj)
            }
          }
        } ~
        pathPrefix("saveMultipleSelectAnswer") {
          post {
            entity(as[JObject]) { questionJObj =>
              saveMultipleSelectAnswer(userId, questionJObj)
            }
          }
        } ~
        pathPrefix("upsertUserProductList") {
          post {
            entity(as[JObject]) { productListJObj =>
              upsertUserProductList(userId, productListJObj)
            }
          }
        } ~
        pathPrefix("deleteProductFromList") {
          post {
            entity(as[JObject]) { prodAndListJObj =>
              deleteProductFromList(userId, prodAndListJObj)
            }
          }
        } ~
        pathPrefix("getUserEligibleOffers") {
          get {
            getUserEligibleOffers(userId)
          }
        } ~
        pathPrefix("getUserFollowUpOffers") {
          get {
            getUserFollowUpOffers(userId)
          }
        } ~
        pathPrefix("confirmAndGetLatestOfferStatus" / Segment) { offerId =>
          get {
            confirmOffer(offerId, userId)
          }
        } ~
        pathPrefix("confirmUnlimitedOffer" / Segment) { offerId =>
          get {
            confirmOfferUnlimited(offerId, userId)
          }
        } ~
        pathPrefix("getOfferDynamicStatus" / Segment) { offerId =>
          get {
            getOfferDynamicStatus(offerId, userId)
          }
        } ~
        pathPrefix("getUserAddress") {
          get {
            getUserAddress(userId)
          }
        }
    }
}
