//package com.poshly.products.actors
//
//import akka.actor.Actor
//import com.poshly.core.data.{ValidationError, ValidationMessage}
//import com.poshly.core.logging.Loggable
//import com.poshly.products.QuestionServiceConfiguration
//import com.poshly.products.util.auth.Authorizer
//import com.poshly.qms.common.data.mapper.QuestionJValueMapper
//import spray.http.{AllOrigins, HttpHeaders}
//
//class QuestionActor(comps: QuestionServiceConfiguration) extends Actor with QuestionRoute with Loggable {
//  def components = comps
//
//  def actorRefFactory = context
//
//  def receive = runRoute(routes)
//}
//
//trait QuestionRoute extends BaseRoute with Authorizer {
//
//  def components: QuestionServiceConfiguration
//
//  val routes =
//    pathPrefix("byId" / Segment) { id =>
//      authorizedRequest { _ =>
//        get {
//          onComplete(components.questions.fetchById(id))(handleTry(_) { result =>
//            complete(QuestionJValueMapper.unapply(result))
//          })
//        }
//      }
//    }
//
//}
