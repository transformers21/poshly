package com.poshly.products.services

import com.poshly.accounts.client.data.User
import com.poshly.accounts.client.data.mapper.LocationJValueMapper
import com.poshly.core.Strings
import com.poshly.core.data._
import com.poshly.core.logging.Loggable
import com.poshly.products.data.{AddressUI => _, _}
import com.poshly.products.{AccountUserConfiguration, GeocodeConfiguration}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class UserService(components: GeocodeConfiguration with AccountUserConfiguration) extends Loggable {

//  def fetchUserLocale(userId: String): Future[String] = {
//    components.userService.fetchById(userId).map(user => user.locale)
//  }
//
//  def updateUserLocale(userId: String, locale: String): Future[Unit] = {
//    components.userService.updateLocale(userId, locale)
//  }

  def addressAutocomplete(prefix: String): Future[Seq[String]] = {
    components.addressService.addressAutocomplete(prefix)
  }

  def getUserLocationWithNameAndPhone(userId: String): Future[LocationWithNameAndPhone] = {
    components.userService.fetchById(userId).map { user =>
      val homeLocation = user.locations.find(loc => loc.code == "Home" || loc.description == "Home")
      homeLocation match {
        case Some(location) =>
          val firstName = user.firstName.getOrElse("")
          val lastName = user.lastName.getOrElse("")
          val phoneNumber = user.mobilePhone
          LocationWithNameAndPhone(firstName, lastName, phoneNumber, location)
        case _ => throw NoHomeAddressException()
      }
    }
  }

  def verify(address: Address): Future[Location] = {
    components.addressService.verifyAddress(Strings.random(3), address).map {
      case va if va.dvpMatchCode != DpvMatchCode.NotConfirmed &&
        va.addressVerification != AddressVerificationCode.Unverified =>
        Location("Home", "Home", va.address, va.coordinate, None, Some(va.addressVerification), Some(va.dvpMatchCode))
      case _ => throw NotValidAddressException()
    }
  }

  def save(userId: String, locationWithNameAndPhone: LocationWithNameAndPhone): Future[User] = {
    val firstName = locationWithNameAndPhone.firstName
    val lastName = locationWithNameAndPhone.lastName
    if (firstName.isEmpty || lastName.isEmpty) {
      throw NotValidNameException()
    }
    val phoneNumber = locationWithNameAndPhone.phoneNumber
    val coreLocation: Location = LocationUI.toCoreLocation(locationWithNameAndPhone.location)
    val updatedUser = components.userService.addLocation(userId, coreLocation).flatMap { user =>
      val modifyUser = if (phoneNumber.isDefined) {
        user.applyMobilePhone(phoneNumber).applyFirstName(Option(firstName)).applyLastName(Option(lastName))
      } else {
        user.applyFirstName(Option(firstName)).applyLastName(Option(lastName))
      }
      components.userService.update(modifyUser)
    }
    updatedUser
  }


}
