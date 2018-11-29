package com.poshly.products.actors

import com.poshly.core.data.{CodedException, ValidationError, ValidationMessage}
import com.poshly.core.logging.Loggable
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._
import org.json4s.{DefaultFormats, Formats}
import spray.httpx.Json4sSupport
import spray.routing._

import scala.util.{Failure, Success, Try}

trait BaseRoute extends HttpService with Loggable with Json4sSupport{

  implicit def executionContext = actorRefFactory.dispatcher

  implicit def json4sFormats: Formats = DefaultFormats

  def handleTry[T](tri: Try[T])(f: T => Route): Route = tri match {
    case Success(results) => f(results)
    case Failure(error) => complete(ErrorResponse(error))
  }

  def completeErrorMissingOrNotValidParams: StandardRoute = {
    complete(ErrorResponse(ValidationError(Seq(
      ValidationMessage("Missing or not valid parameters", Some("MISSING_OR_NOT_VALID_PARAMETERS"))))))
  }
}

object ErrorResponse {

  implicit def json4sFormats: Formats = DefaultFormats

  case class ErrorMessage(message: String, code: Option[String] = None)

  def apply(exception: Throwable): JValue = {
    exception match {
      case validationError: ValidationError =>
        response("ValidationError",
          validationError.messages.map { validationMessage =>
            ErrorMessage(validationMessage.message, validationMessage.code)
          })

      case coded: CodedException =>
        response("CodedException", Seq(ErrorMessage(coded.getMessage(), Some(coded.code))))

      case e =>
        response(e.getClass.getSimpleName, Seq(ErrorMessage(e.getMessage, Some("UNKNOWN"))))
    }
  }

  def response(eType: String = "UnexpectedException", messages: Seq[ErrorMessage]): JValue = {
    "error" -> ("type" -> eType) ~
      ("messages" -> messages.map { message =>
        ("code" -> message.code) ~ ("message" -> message.message)
      })
  }
}


