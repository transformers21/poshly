package com.poshly.products

import com.poshly.core.datetime.Implicits._
import com.poshly.qms.SponsorshipType
import org.joda.time.DateTime
import org.json4s._
import spray.httpx.Json4sSupport
import org.json4s.ext.JodaTimeSerializers

object Json4sProtocol extends Json4sSupport {
  implicit def json4sFormats: Formats = DefaultFormats ++ JodaTimeSerializers.all + new SponsorshipTypeSerializer + new PoshlyDateTimeSerializer

  class SponsorshipTypeSerializer extends CustomSerializer[SponsorshipType](format => ({
    case jv: JValue => SponsorshipType.valueOf(jv.toString).get
  }, {
    case t: SponsorshipType => JString(t.name)
  }))

  class PoshlyDateTimeSerializer extends CustomSerializer[DateTime](format => ({
    case jv: JValue => jv.toString.toDateTime
  }, {
    case d: DateTime => JString(d.toJSONString)
  }))
}
