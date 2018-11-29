package com.poshly.products.util

import com.poshly.products.util.auth.Authorizer
import spray.routing.{AuthorizationFailedRejection, Rejection, RequestContext}

trait AutorizerMock extends Authorizer {

  def authenticated: Boolean

  override protected def check(ctx: RequestContext): Either[Rejection, Option[String]] = {
    if(authenticated)
      Right(Some("userId"))
    else
      Left(AuthorizationFailedRejection)
  }

  override protected def userIdentifierFromSession(ctx: RequestContext) = {
    if(authenticated)
      Some("userId")
    else
      None
  }

}
