package com.poshly.products.data

import com.poshly.core.data.CodedException

case class ResultParameters(active: Option[Boolean], limit: Option[Int])
case class Result[T](count: Long, parameters: ResultParameters, data: List[T])
case class ErrorResult(code: Long, message: Option[String])

object ErrorCodes {
  //todo: decide some conventions about these codes...
  val UNAUTHENTICATED_REQUEST = 403
  val REQUEST_TIMEOUT = 408

  val BAD_TIMESTAMP = 602
}

//object BadTimestampException {
//  def apply() = new CodedException("BAD_TIMESTAMP")
//}
object NotValidAddressException {
  def apply() = new CodedException("NOT_VALID_ADDRESS")
}

object NoHomeAddressException {
  def apply() = new CodedException("NO_HOME_ADDRESS")
}
object NotValidNameException {
  def apply() = new CodedException("NOT_VALID_NAME")
}
