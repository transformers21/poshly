package com.poshly.products.data

object SessionTracking {
  def apply(params: String): SessionTracking = new SessionTracking(toMap(params))

  def accessGrantingToken(accessGrantingToken: String): SessionTracking = {
    val tracking = new SessionTracking(Map())
    tracking.accessGrantingToken(accessGrantingToken)
    tracking
  }

  private def toMap(params: String): Map[String, String] = {
    val pairs: Seq[Option[(String, String)]] = params.split("&").toSeq.map { param =>
      param.split("=").toList match {
        case key :: value :: Nil =>
          Some(key -> value)
        case l =>
          None
      }
    }
    pairs.flatten.toMap
  }
}

class SessionTracking(params: Map[String, String]) {

  private val idField = "id"
  private val accessGrantingTokenField = "agt"
  private val sourceField = "src"
  private val validatedField = "valid"

  var _params = params

  def id: Option[String] = params.get(idField)

  def id(idValue: String): Unit =
    _params = _params + (idField -> idValue)

  def accessGrantingToken: Option[String] =
    _params.get(accessGrantingTokenField)

  def accessGrantingToken(accessGrantingTokenValue: String): Unit =
    _params = _params + (accessGrantingTokenField -> accessGrantingTokenValue)

  def source: Option[String] =
    _params.get(sourceField)

  def validated: Boolean =
    _params.get(validatedField).fold(false)(_ == "1")

  def validated(validatedValue: Boolean): Unit =
    _params = _params + (validatedField -> toString(validatedValue))

  override def toString =
    _params.map { case (k, v) => k + "=" + v }.mkString("&")

  protected def toString(bool: Boolean): String = {
    bool match {
      case true => "1"
      case _ => "0"
    }
  }
}
