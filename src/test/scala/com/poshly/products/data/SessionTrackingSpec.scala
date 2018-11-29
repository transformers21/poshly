package com.poshly.products.data

import com.poshly.core.logging.Loggable
import org.scalatest.{MustMatchers, WordSpec}

class SessionTrackingSpec extends WordSpec with MustMatchers with Loggable {

  "Session Tracking" should {
    "simple tests" in {
      val session = SessionTracking("agt=123")
      session.accessGrantingToken mustBe Some("123")
      session.accessGrantingToken("456")
      session.accessGrantingToken mustBe Some("456")
    }

    "bigger tests" in {

      val session = SessionTracking("valid=0&src=poshly&id=IJWF1BZOIEB0WQWS4S0GQE0B&agt=20S0QVIAY2OVXMA1ZCST")
      session.accessGrantingToken mustBe Some("20S0QVIAY2OVXMA1ZCST")
      session.accessGrantingToken("456")
      session.accessGrantingToken mustBe Some("456")
    }
  }

}
