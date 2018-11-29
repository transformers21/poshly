package com.poshly.products.data



case class OfferUI(offerID: String,
                   offerName : String,
                   brand: String,
                   desc : String,
                   startOn :  String,
                   endOn :  String,
                   imageUrl : String,
                   offerType : String,
                   digitalUrl : Option[String]=None,
                   physicalDeliveryLocationOption : Option[String]=None,
                   availableCount : Option[Int]=None,
                   status : String
                  )

object OfferUI {
  val newStatus = "new"
  val onGoingStatus = "on-going"
  val confirmedStatus = "confirmed"
  val expiredStatus = "expired"
  val outOfStockStatus = "out-of-stock"
  val followUpStatus = "follow-up"
  val inactiveStatus = "inactive"
  val confirmedReadErrorStatus = "Error-ReadingConfirmationOffer"
  val unknownStatus = "unknown"
}


case class OfferUIDynamicData(offerID: String,
                              availableCount : Option[Int]=None,
                              status : String
                             )

case class FollowUpOfferUI(offerID: String,
                           offerName : String,
                           offerType : String,
                           brand: String,
                           imageUrl : String,
                           limited :  Boolean,
                           ctaList : Seq[OfferCallToActionUI],
                           status : String
                            )
case class OfferCallToActionUI(ctaType: String,
                                text:String,
                                url: String)


case class UserAddressUI(addressLine1 : String = "",
                         addressLine2 : String ="",
                         addressLine3 : String = "",
                         city : String = "",
                         state: String ="",
                         country : String ="",
                         postal : String =  "",
                         region : String = ""
                          )

case class UserUI(firstName : String,
                   lastName : String,
                   mobilePhone : String,
                   address : Option[UserAddressUI] )
