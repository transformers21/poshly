package com.poshly.products.data

import com.poshly.core.data.Location

case class LocationWithNameAndPhone(firstName: String,
                                    lastName: String,
                                    phoneNumber: Option[String],
                                    location: LocationUI)

object LocationWithNameAndPhone {
  def apply(firstName: String,
            lastName: String,
            phoneNumber: Option[String],
            location: Location): LocationWithNameAndPhone = LocationWithNameAndPhone(
    firstName,
    lastName,
    phoneNumber,
    LocationUI(location)
  )
}

case class LocationUI(description: String,
                      code: String,
                      coordinate: Option[Coordinate],
                      address: Option[AddressUI],
                      dpv_match_code: Option[String],
                      verification_code: Option[String])

object LocationUI {
  def apply(location: Location): LocationUI = LocationUI(
    location.description,
    location.code,
    location.coordinate.map(Coordinate(_)),
    location.address.map(AddressUI(_)),
    location.dpvMatchCode.map(_.toString),
    location.addressVerification.map(_.toString)
  )
  def toCoreLocation(l: LocationUI): Location = {
    com.poshly.core.data.Location(
      description = l.description,
      code = l.code,
      address = l.address.map(AddressUI.toCoreAddress),
      coordinate = l.coordinate.map(Coordinate.toCoreCoordinate),
      addressVerification = l.verification_code.flatMap(com.poshly.core.data.AddressVerificationCode.valueOf),
      dpvMatchCode = l.dpv_match_code.flatMap(com.poshly.core.data.DpvMatchCode.valueOf)
    )
  }
}

case class Coordinate(lat: Double, long: Double)

object Coordinate {
  def apply(coordinate: com.poshly.core.data.Coordinate): Coordinate = Coordinate(coordinate.lat, coordinate.long)
  def toCoreCoordinate(c: Coordinate): com.poshly.core.data.Coordinate = {
    com.poshly.core.data.Coordinate(
      lat = c.lat,
      long = c.long
    )
  }
}

case class AddressUI(addressLine1: String,
                     addressLine2: Option[String],
                     addressLine3: Option[String],
                     city: String,
                     state: String,
                     country: String,
                     postal: Option[String],
                     region: Option[String])

object AddressUI {
  def apply(address: com.poshly.core.data.Address): AddressUI = AddressUI(
    address.addressLine1,
    address.addressLine2,
    address.addressLine3,
    address.city,
    address.state,
    address.country,
    Option(address.postal),
    address.region)
  def toCoreAddress(a: AddressUI): com.poshly.core.data.Address = {
    com.poshly.core.data.Address(
      a.addressLine1,
      a.addressLine2,
      a.addressLine3,
      a.city,
      a.state,
      a.country,
      a.postal.getOrElse(""),
      a.region
    )
  }
}
