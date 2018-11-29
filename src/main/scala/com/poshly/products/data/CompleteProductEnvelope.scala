package com.poshly.products.data

import com.poshly.qms.client.data.Question
import com.poshly.qms.TResponse

case class CompleteProductEnvelopeV1(productId: Option[String] = None,
                                     title: Option[String] = None,
                                     brand: Option[String] = None,
                                     imageUrl: Option[String] = None,
                                     size: Option[String] = None,
                                     color: Option[String] = None,
                                     description: Option[String] = None,
                                     primaryAffiliateLink: Option[String] = None,
                                     matchScore: Option[Int] = None,
                                     multiRankQuestions: Option[Seq[CompleteQuestionMultirank]] = None,
                                     dropdownQuestions: Option[Seq[CompleteQuestionDropdown]] = None,
                                     openendedQuestions: Option[Seq[CompleteQuestionOpenended]] = None,
                                     lists: Option[Seq[String]] = None,
                                     identicallyNamed: Option[Seq[ProductQmsIdTitleBrand]] = None
                                    )

case class CompleteProductEnvelopeV2(productId: Option[String] = None,
                                     title: Option[String] = None,
                                     brand: Option[String] = None,
                                     imageUrls: Option[Seq[String]] = None,
                                     size: Option[String] = None,
                                     color: Option[String] = None,
                                     description: Option[String] = None,
                                     affiliateLinks: Option[Seq[String]] = None,
                                     matchScore: Option[Int] = None,
                                     multiRankQuestions: Option[Seq[CompleteQuestionMultirank]] = None,
                                     dropdownQuestions: Option[Seq[CompleteQuestionDropdown]] = None,
                                     openendedQuestions: Option[Seq[CompleteQuestionOpenended]] = None,
                                     lists: Option[Seq[String]] = None,
                                     identicallyNamed: Option[Seq[ProductQmsIdTitleBrand]] = None,
                                     price: Option[String] = None,
                                     videoUrls: Option[Seq[String]] = None
                                    )

case class CompleteProductEnvelopeV3(productId: Option[String] = None,
                                     title: Option[String] = None,
                                     brand: Option[String] = None,
                                     imageUrls: Option[Seq[String]] = None,
                                     size: Option[String] = None,
                                     color: Option[String] = None,
                                     description: Option[String] = None,
                                     affiliateLinks: Option[Seq[AffiliateLink]] = None,
                                     matchScore: Option[Int] = None,
                                     multiRankQuestions: Option[Seq[CompleteQuestionMultirank]] = None,
                                     dropdownQuestions: Option[Seq[CompleteQuestionDropdown]] = None,
                                     openendedQuestions: Option[Seq[CompleteQuestionOpenended]] = None,
                                     lists: Option[Seq[String]] = None,
                                     identicallyNamed: Option[Seq[ProductQmsIdTitleBrand]] = None,
                                     price: Option[String] = None,
                                     videoUrls: Option[Seq[String]] = None
                                    )

case class PartialSlowProductEnvelope(productId: Option[String] = None,
                                      matchScore: Option[Int] = None,
                                      multiRankQuestions: Option[Seq[PartialSlowQuestionMultirank]] = None,
                                      openendedQuestions: Option[Seq[PartialSlowQuestionOpenended]] = None)

case class AffiliateLink(url: String,
                         brand: Option[String],
                         price: Option[String],
                         imageUrl: Option[String])

case class CompleteQuestionMultirank(id: String,
                                     text: String,
                                     responses: Seq[TResponse],
                                     myAnswer: Option[String] = None,
                                     everyoneAnswer: Option[Float] = None,
                                     peopleLikeMeAns: Option[Float] = None)

case class PartialSlowQuestionMultirank(id: String,
                                        text: String,
                                        everyoneAnswer: Option[Float] = None,
                                        peopleLikeMeAns: Option[Float] = None)

case class CompleteQuestionDropdown(id: String,
                                    text: String,
                                    responses: Seq[TResponse],
                                    myAnswer: Option[String] = None)

case class CompleteQuestionOpenended(id: String,
                                     text: String,
                                     responses: Seq[TResponse],
                                     myAnswer: Option[String] = None,
                                     everyoneAnswers: Option[List[AnswerTimestamp]] = None,
                                     peopleLikeMeAns: Option[Seq[AnswerTimestamp]] = None,
                                     answerComesFromDB: Boolean)

case class PartialSlowQuestionOpenended(id: String,
                                        text: String,
                                        everyoneAnswers: Option[List[AnswerTimestamp]] = None,
                                        peopleLikeMeAns: Option[Seq[AnswerTimestamp]] = None)

case class CompleteQuestionMyAnswer(question: Question,
                                    myAnswer: Option[String] = None)

case class QIdMyAnswer(qid: String,
                       myAnswer: Option[String] = None)

case class AnswerTimestamp(openText: String, timeStamp: String)

case class ProductQmsIdTitleBrand(productId: Option[String],
                                  title: String,
                                  brand: String,
                                  imageUrl: Option[String],
                                  size: Option[String],
                                  color: Option[String])

object CompleteProductEnvelopeV1 {
  def empty() = CompleteProductEnvelopeV1()
}
object CompleteProductEnvelopeV2 {
  def empty() = CompleteProductEnvelopeV2()
}
object CompleteProductEnvelopeV3 {
  def empty() = CompleteProductEnvelopeV3()
}
