package com.poshly.products.routes

import com.poshly.accounts.InsightSearchCriteria
import com.poshly.accounts.client.data.mapper.{ConfirmedOfferUserStatusJValueMapper, ProductListJValueMapper}
import com.poshly.accounts.client.data.{ConfirmedOfferUserStatus, ProductList, UserInsight}
import com.poshly.core.data._
import com.poshly.pie.client.data.Answer
import com.poshly.products._
import com.poshly.products.actors.{BaseRoute, ErrorResponse}
import com.poshly.products.data._
import com.poshly.products.util.auth._
import com.poshly.qms.client.data.{OfferQms, ProductQms, Question}
import com.poshly.qms.common.data.mapper.{ProductQmsIdTitleBrandJValueMapper, QuestionJValueMapper}
import com.poshly.sparkservice.EntityType
import com.poshly.sparkservice.client.data.{GroupingCriteria, PeopleLikeMeTuple}
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.JsonAST
import org.json4s.JsonAST._
import spray.routing.{Route, StandardRoute}

import scala.concurrent.Future

trait RoutesUtil extends BaseRoute with Authorizer with EndRoutesHelper {
  def components: ProductConfiguration with QuestionServiceConfiguration with AnswersConfiguration with
    AnalyticsConfiguration with InsightDefinitionConfiguration with OfferConfiguration

  val SIMILAR_USERS_COUNT_THRESHOLD = 30

  // Local URL: http://local.dev.poshly.com:30030/api/v1/products/all?q=Chloe&skip=0&limit=0
  def all(qOpt: Option[String] = None,
          skipOpt: Option[Int] = None,
          limitOpt: Option[Int] = None,
          brand: Option[String] = None): Route = {
    val sorting: Seq[SortOptions] = Seq(SortOptions(ProductQms.productIdField, SortDirection.Descending))
    val accents: Map[Char, String] = Map(
      'a' -> "[aàáâãäåæ]",
      'c' -> "[cç]",
      'e' -> "[eèéêëæ]",
      'i' -> "[iìíîï]",
      'n' -> "[nñ]",
      'o' -> "[oòóôõöø]",
      's' -> "[sß]",
      'u' -> "[uùúûü]",
      'y' -> "[yÿ]"
    )
    val qRegexOpt = qOpt.map(_.toLowerCase.map(qChar => accents.getOrElse(qChar, qChar.toString)).mkString)
    val bRegexOpt = brand.map(_.toLowerCase.map(bChar => accents.getOrElse(bChar, bChar.toString)).mkString)
    onComplete(components.products.findByQuery(
      qRegexOpt,
      Option(SearchOptions(skipOpt, limitOpt, sorting)),
      bRegexOpt
    ))(handleTry(_) { results =>
      complete(results.map(ProductQmsIdTitleBrandJValueMapper.unapply))
    })
  }

  val findPopularProducts: StandardRoute = {
    val pids = if (components.config.getString("env") == "dev") {
      Seq("P0000000000000000033",
        "P0000000000000000038")
    } else {
      Seq("P00000000000000000A8",
        "P00000000000000000EA",
        "P00000000000000000EB",
        "P000000000000000004F",
        "P00000000000000001C1",
        "P0000000000000000012")
    }
    complete(components.products.fetchByIds(pids).map(_.map(ProductQmsIdTitleBrandJValueMapper.unapply)))
  }

  def findAllBrands(skip: Option[Int], limit: Option[Int]): StandardRoute = {
    complete(components.products.findAllBrands(Option(SearchOptions(skip, limit))))
  }

  def findAllBrandsWithLogoURLs(skip: Option[Int], limit: Option[Int]): Route = {
    onComplete(components.products.findAllBrands(Option(SearchOptions(skip, limit))))(handleTry(_) { brands =>
      complete(mapBrandsToLogoURLs(brands))
    })
  }

  def byId(pid: String): StandardRoute = {
    complete(components.products.fetchById(pid))
  }

  def findMatchScore(uid: String, pid: String): Route = {
    onComplete(components.products.fetchById(pid))(handleTry(_) { productQms =>
      val multiRankSetQIds = transformOptEmptySeqToNone(productQms.multiRank)
      val peopleLikeMeTupleF = getPeopleLikeMeTuple(productQms.keywordsTags.map(_.toSet), multiRankSetQIds, None, uid)
      val finalScore = for {
        peopleLikeMeTuple <- peopleLikeMeTupleF
      } yield {
        val averages = peopleLikeMeTuple.qidAverageList.map(_.average.toFloat)
        val score = Math.round(averages.sum / averages.length * 10 + 50)
        val matchScore =
          if (score < 50) {
            50
          }
          else {
            score
          }
        matchScore.toString
      }
      complete(finalScore)
    })
  }

  /**
    * Gets the data coming from Spark (PeopleLikeMe, Everyone and MatchScore).
    *
    * @param uid The user id
    * @param pid The product id
    * @return A partially complete product envelope without questions metadata and "my answers".
    */
  def getSlowPartialProductById(uid: String, pid: String): Route = {
    onComplete(components.products.fetchById(pid))(handleTry(_) { productQms =>
      val multiRankSetQIds = transformOptEmptySeqToNone(productQms.multiRank)
      val openendedSetQIds = transformOptEmptySeqToNone(productQms.openendeds)
      val peopleLikeMeTupleF = getPeopleLikeMeTuple(
        productQms.keywordsTags.map(_.toSet), multiRankSetQIds, openendedSetQIds, uid)
      val allQIds = multiRankSetQIds.getOrElse(Set()) ++ openendedSetQIds.getOrElse(Set())
      val allQsF = components.questions.findByIds(allQIds.toSeq)
      val mrF = allQsF.map(_.filter(_.id.flatMap(qid => multiRankSetQIds.map(_.contains(qid))).getOrElse(false)))
      val oeF = allQsF.map(_.filter(_.id.flatMap(qid => openendedSetQIds.map(_.contains(qid))).getOrElse(false)))
      val completeProductEnvelope = for {
        peopleLikeMeTuple <- peopleLikeMeTupleF
        mrSeq <- mrF
        oeSeq <- oeF
      } yield {
        val mrEveryoneAnsF = findQIdEveryoneAnswerMultiRank(mrSeq)
        val oeEveryoneAnsF = findQIdEveryoneAnswerOpenEnded(oeSeq)
        for {
          mrEveryoneAns <- mrEveryoneAnsF
          oeEveryoneAns <- oeEveryoneAnsF
        } yield {
          // Combine multiRank data
          val partialMRQuestionsOpt = optionOrNone(mrSeq.flatMap { q =>
            q.id.map { qid =>
              val everyoneAns = mrEveryoneAns.get(qid)
              val peopleLikeMeAns = peopleLikeMeTuple.qidAverageList.find(_.qid == qid).map(_.average.toFloat)
              PartialSlowQuestionMultirank(qid,
                q.text,
                everyoneAns,
                peopleLikeMeAns)
            }
          })
          val matchScore = partialMRQuestionsOpt.map { completeMRQuestions =>
            val sum = completeMRQuestions.flatMap(_.peopleLikeMeAns.map(a => a)).sum
            Math.round(sum / completeMRQuestions.length * 10 + 50)
          } match {
            case Some(score: Int) => Option(score)
            case _ => Option(50)
          }
          // Combine openended data
          val partialOEQuestionsOpt = optionOrNone(oeSeq.flatMap { q =>
            q.id.map { qid =>
              val everyoneAns = oeEveryoneAns.get(qid)
              val peopleLikeMeAns = optionOrNone(
                peopleLikeMeTuple.qidOpenendedList.filter(_.qid == qid).map(x => AnswerTimestamp(x.openended, "")))
              PartialSlowQuestionOpenended(qid,
                q.text,
                everyoneAns,
                peopleLikeMeAns)
            }
          })
          PartialSlowProductEnvelope(
            productQms.productId,
            matchScore,
            partialMRQuestionsOpt,
            partialOEQuestionsOpt
          )
        }
      }
      complete(completeProductEnvelope)
    })
  }

  /**
    * Gets the data coming from MongoDB (question metadata and my answer).
    *
    * @param uid The user id
    * @param pid The product id
    * @return A partially complete product envelope without MatchScore, PeopleLikeMe and Everyone data.
    */
  private def getFastPartialProductLatest(uid: String, pid: String, productQms: ProductQms) = {
    val multiRankSetQIds = transformOptEmptySeqToNone(productQms.multiRank)
    val dropdownSetQIds = transformOptEmptySeqToNone(productQms.dropdowns)
    val openendedSetQIds = transformOptEmptySeqToNone(productQms.openendeds)
    val allQIds = multiRankSetQIds.getOrElse(Set()) ++
      dropdownSetQIds.getOrElse(Set()) ++
      openendedSetQIds.getOrElse(Set())

    val allQsF = components.questions.findByIds(allQIds.toSeq)
    val mrF = allQsF.map(_.filter(_.id.flatMap(qid => multiRankSetQIds.map(_.contains(qid))).getOrElse(false)))
    val ddF = allQsF.map(_.filter(_.id.flatMap(qid => dropdownSetQIds.map(_.contains(qid))).getOrElse(false)))
    val oeF = allQsF.map(_.filter(_.id.flatMap(qid => openendedSetQIds.map(_.contains(qid))).getOrElse(false)))
    val allMyAnswerF = findQIdMyAnswer(uid, allQIds)
    val getProductListByUserIdProductId = components.userService.findProductsListBy(uid, pid).map(_.items)
    val findIdenticallyNamedProducts = components.products.findByTitleBrand(productQms.title, productQms.brand)

    for {
      productList <- getProductListByUserIdProductId
      mrSeq <- mrF
      ddSeq <- ddF
      oeSeq <- oeF
      allMyAnswer <- allMyAnswerF
      identicallyNamedProducts <- findIdenticallyNamedProducts
    } yield {
      // Combine multiRank data
      val completeMRQuestionsOpt: Option[Seq[CompleteQuestionMultirank]] = optionOrNone(mrSeq.flatMap { q =>
        q.id.map { qid =>
          CompleteQuestionMultirank(qid,
            q.text,
            q.underlyingResponses,
            allMyAnswer.get(qid),
            None,
            None)
        }
      })
      // Combine dropdown data
      val completeDDQuestionsOpt: Option[Seq[CompleteQuestionDropdown]] = optionOrNone(ddSeq.flatMap { q =>
        q.id.map { qid =>
          val myAns = allMyAnswer.get(qid)
          CompleteQuestionDropdown(qid,
            q.text,
            q.underlyingResponses,
            myAns)
        }
      })
      // Combine openended data
      val completeOEQuestionsOpt: Option[Seq[CompleteQuestionOpenended]] = optionOrNone(oeSeq.flatMap { q =>
        q.id.map { qid =>
          CompleteQuestionOpenended(qid,
            q.text,
            q.underlyingResponses,
            allMyAnswer.get(qid),
            None,
            None,
            allMyAnswer.get(qid).nonEmpty)
        }
      })
      val homonymousProducts = optionOrNone(identicallyNamedProducts.filter(_.productId.getOrElse(pid) != pid).
        map(p => ProductQmsIdTitleBrand(p.productId, p.title, p.brand, p.imageUrls.map(_.head), p.size, p.color)))
      val affiliateLinks = productQms.affiliateLinks.map(_.map {
        case al if al.brand.isEmpty && al.url.startsWith("http://amzn.to") =>
          AffiliateLink (al.url, Option("Amazon"), al.price, brandToLogoURL(Option("Amazon")))
        case al =>
          AffiliateLink(al.url, al.brand, al.price, brandToLogoURL(al.brand))
      })

      CompleteProductEnvelopeV3(
        productQms.productId,
        Option(productQms.title),
        Option(productQms.brand),
        productQms.imageUrls.map(_.toSeq),
        productQms.size,
        productQms.color,
        productQms.description,
        affiliateLinks,
        None,
        completeMRQuestionsOpt,
        completeDDQuestionsOpt,
        completeOEQuestionsOpt,
        Option(productList),
        homonymousProducts,
        productQms.price,
        productQms.videoUrls.map(_.toSeq)
      )
    }
  }

  def getFastPartialProductByIdV1(uid: String, pid: String): Route = {
    onComplete(components.products.fetchById(pid))(handleTry(_) { productQms =>
      val mappedProduct = getFastPartialProductLatest(uid, pid, productQms).map { product =>
        CompleteProductEnvelopeV1(
          product.productId,
          product.title,
          product.brand,
          product.imageUrls.map(_.head),
          product.size,
          product.color,
          product.description,
          product.affiliateLinks.map(_.headOption.getOrElse(AffiliateLink("", None, None, None)).url),
          None,
          product.multiRankQuestions,
          product.dropdownQuestions,
          product.openendedQuestions,
          product.lists,
          product.identicallyNamed
        )
      }
      complete(mappedProduct)
    })
  }

  def getFastPartialProductByIdV2(uid: String, pid: String): Route = {
    onComplete(components.products.fetchById(pid))(handleTry(_) { productQms =>
      val mappedProduct = getFastPartialProductLatest(uid, pid, productQms).map { product =>
        CompleteProductEnvelopeV2(
          product.productId,
          product.title,
          product.brand,
          productQms.imageUrls.map(_.toSeq),
          product.size,
          product.color,
          product.description,
          productQms.affiliateLinks.map(_.map(a => a.url)),
          None,
          product.multiRankQuestions,
          product.dropdownQuestions,
          product.openendedQuestions,
          product.lists,
          product.identicallyNamed
        )
      }
      complete(mappedProduct)
    })
  }

  def getFastPartialProductByIdV3(uid: String, pid: String): Route = {
    onComplete(components.products.fetchById(pid))(handleTry(_) { productQms =>
      complete(getFastPartialProductLatest(uid, pid, productQms))
    })
  }

  def findQIdMyAnswer(uid: String, qSet: Set[String]): Future[Map[String, String]] = {
    if (qSet.isEmpty) {
      Future(Map[String, String]())
    } else {
      components.userService.findInsightsByMultipleCriteria(
        qSet.map(q => InsightSearchCriteria(userid = Some(uid), question = Option(q))).toSeq,
        BooleanClauseOperator.Any,
        SearchOptions()
      ).map { userInsightSeq =>
        userInsightSeq.groupBy(_.question).flatMap { tuple =>
          tuple._2.sortWith(_.updatedOn.getMillis > _.updatedOn.getMillis).headOption.flatMap(userInsight =>
            tuple._1.flatMap { question =>
              userInsight.restriction.map(ans => (question, ans))
            })
        }
      }
    }
  }

  def transformOptEmptySeqToNone(optSeq: Option[scala.collection.Set[String]]): Option[Set[String]] = {
    optSeq.flatMap {
      case q if q.nonEmpty => Some(q.toSet)
      case _ => None
    }
  }

  def findQIdEveryoneAnswerMultiRank(qs: Seq[Question]): Future[Map[String, Float]] = {
    Future.sequence(qs.flatMap { q =>
      q.id.map { id =>
        getInsightCodeCountById(q.mappings.head.insightDefinitionId).map { insightCodeCount =>
          val everyoneAns = insightCodeCount.map(kv => kv._1.toInt * kv._2).sum.toFloat / insightCodeCount.values.sum
          val safeEveryAns = if (everyoneAns.isNaN) { 0 } else { everyoneAns }
          (id, safeEveryAns)
        }
      }
    }).map(_.toMap)
  }

  def getInsightCodeCountById(insightId: String): Future[Map[String, Long]] = {
    components.analytics.countByGroupings(
      List(GroupingCriteria(EntityType.Insight, insightId)),
      List(),
      (Long.MinValue, Long.MaxValue)
    ).flatMap { res =>
      val insightCodeCount = res.map { groupedBySetCount =>
        groupedBySetCount.groupByCategorySet.head.value -> groupedBySetCount.count
      }.toMap
      components.insightDefinitions.fetchById(insightId).map { insightDef =>
        val insightDefCodeCount = insightDef.series.map { insightDefCode =>
          insightDefCode.insightValueCode -> insightCodeCount.getOrElse(insightDefCode.insightValueCode, 0L)
        }.toMap
        insightDefCodeCount
      }
    }
  }

  def findQIdEveryoneAnswerOpenEnded(qs: Seq[Question]): Future[Map[String, List[AnswerTimestamp]]] = {
    Future.sequence(
      qs.flatMap { q =>
        q.id.map { qid =>
          getOpenEndedResponsesByInsightId(q.mappings.head.insightDefinitionId).map { openended =>
            val answerTimestamp = openended.map(oe => AnswerTimestamp(oe.openText, oe.timeStamp))
            qid -> answerTimestamp
          }
        }
      }).map(_.toMap)
  }

  def getOpenEndedResponsesByInsightId(insightId: String): Future[List[OpenEndedResponse]] = {
    components.analytics.idsByGroupings(List(GroupingCriteria(
      EntityType.Insight, insightId)),
      List(),
      (Long.MinValue, Long.MaxValue)
    ).map { groupedBySetCountSeq =>
      groupedBySetCountSeq.headOption.map { grpHead =>
        grpHead.ids.map { id =>
          val userId = id._1
          val userData = id._2
          val respDate = new org.joda.time.DateTime(userData.ts)
          val dtFormat = org.joda.time.format.DateTimeFormat.forPattern("MM-dd-yyyy")
          OpenEndedResponse(userId, dtFormat.print(respDate), userData.openText)
        }.toList
      }.toList.flatten.take(SIMILAR_USERS_COUNT_THRESHOLD)
    }
  }

//  def getCompleteProductById(uid: String, pid: String): Route = {
//    // test url: http://local.dev.poshly.com:30030/api/v1/products/getCompleteProductById/P0000000000000000033
//    // the idea is to implement this in QMS, as close to the DB as possible
//
//    onComplete(components.products.fetchById(pid))(handleTry(_) { productQms =>
//      val multiRankSetQIds = productQms.multiRank.map(_.toSet).getOrElse(Set())
//      val dropdownSetQIds = productQms.dropdowns.map(_.toSet).getOrElse(Set())
//      val openendedSetQIds = productQms.openendeds.map(_.toSet).getOrElse(Set())
//
//      val mrF = productQms.multiRank.map(mrSet => components.questions.findByIds(mrSet.toSeq)).getOrElse(Future(Seq()))
//      val ddF = productQms.dropdowns.map(ddSet => components.questions.findByIds(ddSet.toSeq)).getOrElse(Future(Seq()))
//      val oeF = productQms.openendeds.map(oeSet => components.questions.findByIds(oeSet.toSeq)).getOrElse(Future(Seq()))
//      val mrMyAnswerF = findQIdMyAnswer(uid, multiRankSetQIds)
//      val ddMyAnswerF = findQIdMyAnswer(uid, dropdownSetQIds)
//      val oeMyAnswerF = findQIdMyAnswer(uid, openendedSetQIds)
//      val peopleLikeMeTupleF = getPeopleLikeMeTuple(
//        productQms.keywordsTags.map(_.toSet), optionOrNone(multiRankSetQIds), optionOrNone(openendedSetQIds), uid)
//      val getProductListByUserIdProductId = components.userService.findProductsListBy(uid, pid).map(_.items)
//      val findIdenticallyNamedProducts = components.products.findByTitleBrand(productQms.title, productQms.brand)
//
//      val completeProductEnvelope =
//        for {
//          productList <- getProductListByUserIdProductId
//          mrSeq <- mrF
//          ddSeq <- ddF
//          oeSeq <- oeF
//          mrMyAnswer <- mrMyAnswerF
//          ddMyAnswer <- ddMyAnswerF
//          oeMyAnswer <- oeMyAnswerF
//          peopleLikeMeTuple <- peopleLikeMeTupleF
//          identicallyNamedProducts <- findIdenticallyNamedProducts
//        } yield {
//          val mrEveryoneAnsF = findQIdEveryoneAnswerMultiRank(mrSeq)
//          val oeEveryoneAnsF = findQIdEveryoneAnswerOpenEnded(oeSeq)
//          for {
//            mrEveryoneAns <- mrEveryoneAnsF
//            oeEveryoneAns <- oeEveryoneAnsF
//          } yield {
//            // Combine multiRank data
//            val completeMRQuestionsOpt: Option[Seq[CompleteQuestionMultirank]] = optionOrNone(mrSeq.flatMap { q =>
//              q.id.map { qid =>
//                val myAns = mrMyAnswer.get(qid)
//                val everyoneAns = mrEveryoneAns.get(qid)
//                val peopleLikeMeAns = peopleLikeMeTuple.qidAverageList.find(_.qid == qid).map(_.average.toFloat)
//                CompleteQuestionMultirank(qid,
//                  q.text,
//                  q.underlyingResponses,
//                  myAns,
//                  everyoneAns,
//                  peopleLikeMeAns)
//              }
//            })
//            val matchScore = completeMRQuestionsOpt.map { completeMRQuestions =>
//              val sum = completeMRQuestions.flatMap(_.peopleLikeMeAns.map(a => a)).sum
//              Math.round(sum / completeMRQuestions.length * 10 + 50)
//            } match {
//              case Some(score: Int) => Option(score)
//              case _ => Option(50)
//            }
//            // Combine dropdown data
//            val completeDDQuestionsOpt: Option[Seq[CompleteQuestionDropdown]] = optionOrNone(ddSeq.flatMap { q =>
//              q.id.map { qid =>
//                val myAns = ddMyAnswer.get(qid)
//                CompleteQuestionDropdown(qid,
//                  q.text,
//                  q.underlyingResponses,
//                  myAns)
//              }
//            })
//            // Combine openended data
//
//            val completeOEQuestionsOpt: Option[Seq[CompleteQuestionOpenended]] = optionOrNone(oeSeq.flatMap { q =>
//              q.id.map { qid =>
//                val myAns = oeMyAnswer.get(qid)
//                val everyoneAns = oeEveryoneAns.get(qid)
//                val peopleLikeMeAns = optionOrNone(
//                  peopleLikeMeTuple.qidOpenendedList.filter(_.qid == qid).map(x => AnswerTimestamp(x.openended, "")))
//                CompleteQuestionOpenended(qid,
//                  q.text,
//                  q.underlyingResponses,
//                  myAns,
//                  everyoneAns,
//                  peopleLikeMeAns,
//                  myAns.nonEmpty)
//              }
//            })
//            val homonymousProducts = optionOrNone(identicallyNamedProducts.filter(_.productId.getOrElse(pid) != pid).
//              map(p => ProductQmsIdTitleBrand(p.productId, p.title, p.brand, p.imageUrls.map(_.head), p.size, p.color)))
//
//            CompleteProductEnvelope(
//              productQms.productId,
//              Option(productQms.title),
//              Option(productQms.brand),
//              productQms.imageUrls.map(_.head),
//              productQms.size,
//              productQms.color,
//              productQms.description,
//              productQms.affiliateLinks.map(_.head.url),
//              matchScore,
//              completeMRQuestionsOpt,
//              completeDDQuestionsOpt,
//              completeOEQuestionsOpt,
//              Option(productList),
//              homonymousProducts
//            )
//          }
//        }
//      complete(completeProductEnvelope)
//    })
//  }

  def optionOrNone[A, C <: Iterable[A]](iter: C with Iterable[A]): Option[C with Iterable[A]] = {
    if (iter.isEmpty) None else Some(iter)
  }

  def getProductListByUserIdProductId(userId: String, productId: String): Future[ProductList] = {
    components.userService.findProductsListBy(userId, productId)
  }

  //  def getInsightById(insightId: String): Route = {
  //    onComplete(components.analytics.countByGroupings(List(GroupingCriteria(EntityType.Insight, insightId)),
  //      List(), (Long.MinValue, Long.MaxValue)))(handleTry(_) { res =>
  //      val groupedList: List[GroupedBySetCount] = res.toList
  //      val insightCodeCount = groupedList.map { groupedBySetCount =>
  //        groupedBySetCount.groupByCategorySet.head.value -> groupedBySetCount.count
  //      }.toMap
  //      onComplete(components.insightDefinitions.fetchById(insightId))(handleTry(_) { insightDef =>
  //        val insightDefCodeCount = insightDef.series.map { insightDefCode =>
  //          insightDefCode.insightValueCode -> insightCodeCount.getOrElse(insightDefCode.insightValueCode, 0)
  //        }.toMap
  //        complete(insightDefCodeCount)
  //      })
  //    })
  //  }

  def getPeopleLikeMeTuple(keywords: Option[Set[String]],
                           multirankQIDs: Option[Set[String]],
                           openendedQIDs: Option[Set[String]],
                           userId: String
                          ): Future[PeopleLikeMeTuple] = {
    val bagRelevantQuestions = if (components.config.getString("env") == "dev") {
      val bagQuestionsDev = Map(
        "makeup" -> Set(
          "Q00000000000000000A5",
          "Q0000000000000000001",
          "Q0000000000000000002",
          "Q000000000000000019E",
          "Q00000000000000000D0"
        ),
        "fragrance" -> Set(
          "Q000000000000000002D",
          "Q0000000000000000306",
          "Q0000000000000000555",
          "Q00000000000000000EB",
          "Q00000000000000000D0"
        ),
        "skin" -> Set(
          "Q0000000000000000001",
          "Q0000000000000000002",
          "Q000000000000000019E",
          "Q00000000000000000D0"
        ),
        "nail" -> Set(
          "Q0000000000000000235",
          "Q00000000000000000D0",
          "Q000000000000000003E",
          "Q00000000000000001DA",
          "Q000000000000000008E",
          "Q000000000000000008C"
        ),
        "hair" -> Set(
          "Q0000000000000000066",
          "Q0000000000000000012",
          "Q000000000000000019E",
          "Q00000000000000000D0"
        ),
        "default" -> Set(
          "Q0000000000000000235",
          "Q000000000000000019E",
          "Q00000000000000000D0"
        )
      )
      bagQuestionsDev
    } else {
      val bagQuestionsProd = Map(
        "makeup" -> Set(
          "Q0000000000000000862",
          "Q0000000000000001265",
          "Q000000000000000074A",
          "Q000000000000000017B",
          "Q0000000000000000FFD",
          "Q00000000000000006B2",
          "Q00000000000000006B3",
          "Q0000000000000000001",
          "Q0000000000000000002",
          "Q0000000000000000235",
          "Q0000000000000000AA0"
        ),
        "fragrance" -> Set(
          "Q00000000000000003F9",
          "Q00000000000000003FD",
          "Q0000000000000001142",
          "Q000000000000000002D",
          "Q0000000000000000306",
          "Q0000000000000000555",
          "Q0000000000000001142",
          "Q0000000000000000235",
          "Q00000000000000000D0",
          "Q0000000000000000AA2"
        ),
        "skin" -> Set(
          "Q0000000000000001265",
          "Q000000000000000017B",
          "Q0000000000000000FFD",
          "Q0000000000000000001",
          "Q0000000000000000002",
          "Q0000000000000001139",
          "Q0000000000000000235",
          "Q00000000000000000D0",
          "Q000000000000000019E",
          "Q0000000000000000AA1"
        ),
        "nail" -> Set(
          "Q0000000000000001265",
          "Q000000000000000017B",
          "Q0000000000000000FFD",
          "Q0000000000000000235",
          "Q00000000000000000D0",
          "Q000000000000000008E",
          "Q00000000000000001D0",
          "Q00000000000000001CA",
          "Q00000000000000009C5"
        ),
        "hair" -> Set(
          "Q0000000000000001265",
          "Q000000000000000017B",
          "Q0000000000000000FFD",
          "Q0000000000000000235",
          "Q00000000000000000D0",
          "Q000000000000000019E",
          "Q0000000000000000591",
          "Q0000000000000000592"
        ),
        "default" -> Set(
          "Q0000000000000001265",
          "Q000000000000000017B",
          "Q0000000000000000235",
          "Q00000000000000000D0"
        )
      )
      bagQuestionsProd
    }

    val matchedQIDs = keywords.map { setOfKeywords =>
      val filteredQuestions = bagRelevantQuestions.filterKeys(setOfKeywords).valuesIterator
        .foldLeft(Set[String]())(_ ++ _)
      if (filteredQuestions.isEmpty) {
        bagRelevantQuestions("default")
      } else {
        filteredQuestions
      }
    }.getOrElse(Set())

    components.analytics.findSimilarUsersData(
      userId,
      matchedQIDs,
      multirankQIDs,
      openendedQIDs
    )
  }

  def getPeopleLikeMeTupleProduct(product: ProductQms, userId: String): Future[PeopleLikeMeTuple] = {
    val keywords = product.keywordsTags.map(x => x.toSet)
    val multirankQIDs: Option[scala.collection.immutable.Set[String]] = product.multiRank.map(x => x.toSet)
    val openendedQIDs: Option[scala.collection.immutable.Set[String]] = product.openendeds.map(x => x.toSet)
    getPeopleLikeMeTuple(keywords, multirankQIDs, openendedQIDs, userId)
  }

  def getPeopleLikeMeData(productId: String, userIdOpt: Option[String]): Route = {
    if (userIdOpt.isEmpty) {
      complete(ErrorResponse(ValidationError(Seq(
        ValidationMessage("User id not defined.", code = Some("USER_ID_NOT_DEFINED"))))))
    }
    onComplete(components.products.fetchById(productId))(handleTry(_) { product =>
      complete(userIdOpt.map(userId => getPeopleLikeMeTupleProduct(product, userId)))
    })
  }

  def getProductIdsByUserIdListName(userId: String, listName: String): StandardRoute = {
    complete(components.userService.findProductsBy(userId, listName).map(_.map(_.productId)))
  }

  def getProductsByUserIdListName(userId: String,
                                  listName: String,
                                  skip: Option[Int] = None,
                                  limit: Option[Int] = None): Route = {
    onComplete(components.userService.findProductsBy(userId,
      listName,
      Option(SearchOptions(skip, limit))))(handleTry(_) { productList =>
      onComplete(components.products.fetchByIds(productList.map(_.productId)))(handleTry(_) { products =>
        complete(products.map(ProductQmsIdTitleBrandJValueMapper.unapply))
      })
    })
  }

  private def unwrapQIDAndRestriction(questionJObj: JsonAST.JObject): (String, Option[String]) = {
    val paramMapOpt = questionJObj match {
      case JObject(fields) =>
        Option(fields.flatMap {
          case JField(com.poshly.qms.TQuestion.IdField.name, JString(id)) =>
            Option("id" -> id)
          case JField(com.poshly.core.data.TOntology.RestrictionField.name, JString(r)) =>
            Option("restriction" -> r)
          case JField(com.poshly.core.data.TOntology.RestrictionField.name, JInt(r)) =>
            Option("restriction" -> r.toString())
          case _ => None
        }.toMap)
      case _ => None
    }
    val paramMap = paramMapOpt.getOrElse(Map[String, String]())
    val qid = paramMap.getOrElse(com.poshly.qms.TQuestion.IdField.name, "")
    val restriction = paramMap.get(com.poshly.core.data.TOntology.RestrictionField.name)
    (qid, restriction)
  }


  private def unwrapQIDAndMultipleRestriction(questionJObj: JsonAST.JObject): (String, Option[Seq[String]]) = {
    val paramMapOpt = questionJObj match {
      case JObject(fields) =>
        Option(fields.flatMap {
          case JField(com.poshly.qms.TQuestion.IdField.name, JString(id)) =>
            Option("id" -> Seq(id))
          case JField(com.poshly.core.data.TOntology.RestrictionField.name, JArray(r)) =>
            Option("restriction" -> r.flatMap {
              case JString(rest) => Some(rest)
              case JInt(rest) => Some(rest.toString())
              case _ => None
            })
          case _ => None
        }.toMap)
      case _ => None
    }
    val paramMap = paramMapOpt.getOrElse(Map[String, Seq[String]]())
    val qid = paramMap.getOrElse(com.poshly.qms.TQuestion.IdField.name, Seq("")).head
    val restriction = paramMap.get(com.poshly.core.data.TOntology.RestrictionField.name)
    (qid, restriction)
  }

  def findPersonalizePoshlyQuestions(userId: String, skip: Option[Int] = None, limitOpt: Option[Int] = None): Route = {
    val safeLimit = limitOpt.map(limit => if (limit > 20) { 20 } else { limit })
    onComplete(components.questions.findPersonalizePoshlyQuestions(userId, Option(SearchOptions(skip, safeLimit)))
    )(handleTry(_) { questions =>
      if (logger.isDebugEnabled) {
        logger.debug("all the questions")
        questions.foreach(q => logger.debug(q.id))
        logger.debug("questions.size")
        logger.debug(questions.size.toString)
      }
      complete(questions.map(QuestionJValueMapper.unapply))
    })
  }

  def findMyAnswersCount(userId: String): Route = {
    onComplete(components.userService.fetchById(userId))(handleTry(_) { user =>
      complete(user.questionCount.toString())
    })
  }

  def saveMultipleSelectAnswer(userId: String, questionJObj: JsonAST.JObject): Route = {
    val (qid, restrictionSeq) = unwrapQIDAndMultipleRestriction(questionJObj)
    if (qid.isEmpty || restrictionSeq.isEmpty) {
      completeErrorMissingOrNotValidParams
    } else {
      onComplete(components.questions.fetchById(qid))(handleTry(_) { question =>
        val errorMessages = restrictionSeq.map(_.map { restriction =>
          saveOneAnswer(userId, Option(restriction), question)
        }.foldLeft(Seq[ValidationMessage]())(_ ++ _)).getOrElse(Seq())

        if (errorMessages.isEmpty) {
          complete("OK")
        } else {
          complete(ErrorResponse(ValidationError(errorMessages)))
        }
      })
    }
  }

  def saveAnswer(userId: String, questionJObj: JsonAST.JObject): Route = {
    val (qid, restriction) = unwrapQIDAndRestriction(questionJObj)
    if (qid.isEmpty || restriction.isEmpty) {
      completeErrorMissingOrNotValidParams
    } else {
      onComplete(components.questions.fetchById(qid))(handleTry(_) { question =>
        val errorMessages = saveOneAnswer(userId, restriction, question)
        if (errorMessages.isEmpty) {
          complete("OK")
        } else {
          complete(ErrorResponse(ValidationError(errorMessages)))
        }
      })
    }
  }

  private def saveOneAnswer(userId: String, restriction: Option[String], question: Question): Seq[ValidationMessage] = {
    val errorMessages: Seq[ValidationMessage] = Seq()
    val now = org.joda.time.DateTime.now()
    val answer = Answer.empty.
      applyAccountId(Some("A0000000000000000001")).
      applyUserId(userId).
      applyQuestionId(question.id.get).
      applyDomain(question.ontology.get.domain).
      applyClass(question.ontology.get.clazz).
      applyAttribute(question.ontology.get.attribute).
      applyRestriction(restriction).
      applyDuration(None).
      applyStringValue(Option(question.responseType)).
      applyCreatedOn(Some(now))
    // logger.debug("answer")
    // logger.debug(answer) //ex: TAnswer(USR0000000000021E943,Q00000000000000008B3,Some
    // (1471280261388),Some(A0000000000000000001),None,Product,EaudeParfum,None,Some(test open ended 1),
    // None,None,Some(open-ended-text),None,None,None,Set())
    val respAnswer = components.answers.answer(answer)

    val userInsight = UserInsight.empty.
      applyUserid(userId).
      applyQuestion(question.id).
      applyDomain(question.ontology.get.domain).
      applyClazz(question.ontology.get.clazz).
      applyAttribute(question.ontology.get.attribute).
      applyRestriction(restriction).
      applyCreatedOn(now).
      applyUpdatedOn(now)
    // logger.debug("userInsight")
    // logger.debug(userInsight) //ex: TUserInsight(USR0000000000021E943,Some
    // (Q00000000000000008B3),Product,EaudeParfum,None,Some(test open ended 1),20160815185741+02:00,
    // 20160815185741+02:00)
    val respUserInsight = components.userService.addInsight(userId, userInsight)

    respAnswer.onFailure {
      case fail =>
        logger.error("Error adding answer to CassandraDB", fail)
        errorMessages :+ ValidationMessage("Error adding answer to CassandraDB")
    }
    respUserInsight.onFailure {
      case fail =>
        logger.error("Error adding userInsight to MongoDB", fail)
        errorMessages :+ ValidationMessage("Error adding userInsight to MongoDB")
    }
    errorMessages
  }

  def upsertUserProductList(userId: String, productListJObj: JsonAST.JObject): Route = {
    val productList: ProductList = ProductListJValueMapper.apply(productListJObj)
    val productId = productList.productId
    val items = productList.items
    onComplete(components.userService.upsertUserProductList(userId, productId, items))(handleTry(_) { result =>
      complete(ProductListJValueMapper.unapply(result))
    })
  }

  def deleteProductFromList(userId: String, prodAndListJObj: JsonAST.JObject): Route = {
    val prodAndList = prodAndListJObj match {
      case JObject(fields) =>
        Option(fields.flatMap {
          case JField("productId", JString(productId)) =>
            Option("productId" -> productId)
          case JField("listName", JString(listName)) =>
            Option("listName" -> listName)
          case _ => None
        }.toMap)
      case _ => None
    }
    val productId = prodAndList.flatMap(a => a.get("productId")).getOrElse("")
    val listName = prodAndList.flatMap(a => a.get("listName")).getOrElse("")
    onComplete(components.userService.deleteProductFromList(userId, productId, listName))(handleTry(_) { _ =>
      complete("OK")
    })
  }

  def getUserEligibleOffers(userId: String): Route = {
    val sorting: Seq[SortOptions] = Seq(SortOptions(OfferQms.startOnField, SortDirection.Descending))
    onComplete(components.offerService.fetchOffersByUserEligibility(userId,
                Option(SearchOptions(sorting = sorting ))))(handleTry(_) { offers =>

      val limitedOfferIds: Seq[String] = offers.results.filter(offer => offer.offerCount.isDefined).map(_.offerId).flatten
      onComplete(components.userService.findConfirmedOffersStatusForUser(limitedOfferIds, userId))(handleTry(_) { confirmedOffers =>
        val offersUI = offers.results.map { offer =>
          var errorStatus = ""
          val confirmedOfferOption: Option[ConfirmedOfferUserStatus] = offer.offerCount.map { _ =>
            val confirmedOfferSeq = confirmedOffers.filter(_.offerId == offer.offerId.getOrElse(""))
            if (confirmedOfferSeq.isEmpty) {
              errorStatus = OfferUI.confirmedReadErrorStatus
              ConfirmedOfferUserStatus.empty()
            }
            else {
              confirmedOfferSeq.head
            }
          }

          OfferUI(offer.offerId.getOrElse(""),
            offer.name,
            offer.brand,
            offer.description.getOrElse(""),
            offer.startUnderlying,
            offer.endUnderlying,
            offer.imageUrl.getOrElse(""),
            offer.offerType,
            offer.digitalUrl,
            offer.physicalDeliveryLocationOption,
            confirmedOfferOption.map(_.availableCount),
            if (errorStatus.isEmpty) getOfferStatus(offer, confirmedOfferOption) else errorStatus
          )
        }

        complete(offersUI.filterNot(of => of.status.equals(OfferUI.expiredStatus))
          ++ offersUI.filter(of => of.status.equals(OfferUI.expiredStatus)))
      })
    })

  }

  def getUserFollowUpOffers(userId: String): Route = {
    onComplete(components.offerService.fetchFollowUpOffersByUserEligibility(userId))(handleTry(_) { offers =>
      val limitedOfferIds: Seq[String] = offers.results.filter(offer => offer.offerCount.isDefined).map(_.offerId).flatten
      val unlimitedOfferIds: Seq[String] = offers.results.filter(offer => offer.offerCount.isEmpty).map(_.offerId).flatten
      val limitedConfirmedOffersFut = components.userService.findConfirmedOffersStatusForUser(limitedOfferIds, userId)
      val unlimitedConfirmedOffersFut = components.userService.findUnlimitedConfirmedOffersForUser(unlimitedOfferIds, userId)

      val followUpOffersFut = for {
        limitedConfirmedOffers <- limitedConfirmedOffersFut
        unlimitedConfirmedOffers <- unlimitedConfirmedOffersFut
      }
        yield {
          val limitedConfirmedOffersMap = limitedConfirmedOffers.map(offer=> offer.offerId->offer).toMap
          val followUpOffers = offers.results.filter(offer => ((limitedConfirmedOffersMap.contains(offer.offerId.getOrElse("")) && limitedConfirmedOffersMap(offer.offerId.getOrElse("")).isConfirmed)
            || (!unlimitedConfirmedOffers.filter(unLimited => unLimited.offerId ==offer.offerId.getOrElse("")).isEmpty)))

          followUpOffers.map{offer =>
            
            FollowUpOfferUI(offer.offerId.getOrElse(""),
            offer.name,
            offer.offerType,
            offer.brand,
            offer.imageUrl.getOrElse(""),
            limited = offer.offerCount.isDefined,
            offer.callToActionAttributes.callToActionList.map(cta =>
              OfferCallToActionUI(cta.ctaType,cta.text.getOrElse(""),cta.url.getOrElse(""))),
            status = OfferUI.followUpStatus
          )}
        }
      complete(followUpOffersFut)
    })
  }

  def confirmOffer(offerId : String, userId: String): Route = {
    //first get offer to check latest eligibility for date and active
    onComplete(components.offerService.fetchById(offerId))(handleTry(_) { offerQms =>
      val currentOfferStatus = getOfferStatus(offerQms)
      if(!(currentOfferStatus.equals( OfferUI.newStatus) || currentOfferStatus.equals(OfferUI.onGoingStatus))){
        complete(OfferUIDynamicData(offerId,status=currentOfferStatus))
      } else {
        onComplete(components.userService.confirmOffer(offerId, userId))(handleTry(_) { result =>
          complete(OfferUIDynamicData(offerId,Some(result.availableCount),getOfferStatus(offerQms, Some(result))))
        })
      }
    })
  }

  def confirmOfferUnlimited(offerId : String, userId: String): Route = {
    // first get offer to check latest eligibility for date and active
    onComplete(components.offerService.fetchById(offerId))(handleTry(_) { offerQms =>
      val currentOfferStatus = getOfferStatus(offerQms)
      if(!(currentOfferStatus.equals( OfferUI.newStatus) || currentOfferStatus.equals(OfferUI.onGoingStatus))){
        complete(OfferUIDynamicData(offerId,status=currentOfferStatus))
      } else {
        onComplete(components.userService.confirmOfferUnlimited(offerId, userId))(handleTry(_) { _ =>
          complete(OfferUIDynamicData(offerId, status = getOfferStatus(offerQms)))
        })
      }
    })
  }

  def getOfferDynamicStatus(offerId : String, userId: String) : Route = {
    onComplete(components.offerService.fetchById(offerId))(handleTry(_) { offerQms =>
      if (offerQms.offerCount.isDefined ){
        onComplete(components.userService.findConfirmedOfferStatusForUser(offerId, userId))(handleTry(_) { confirmedStatus =>
          complete(OfferUIDynamicData(offerId,Some(confirmedStatus.availableCount), getOfferStatus(offerQms,Some(confirmedStatus) )))
        })
      } else{
        complete(OfferUIDynamicData(offerId,status=getOfferStatus(offerQms)))
      }
    })
  }

  def getUserAddress(userId: String) : Route = {
    onComplete(components.userService.fetchById(userId))(handleTry(_) { user =>

      val address : Option[UserAddressUI]= user.locations.find(loc => loc.code == "Home" || loc.description == "Home") match {
        case Some(loc) =>  loc.address.map ( addr => UserAddressUI(addr.addressLine1,
                                                              addr.addressLine2.getOrElse(""),
                                                              addr.addressLine3.getOrElse(""),
                                                              addr.city,
                                                              addr.state,
                                                              addr.country,
                                                              addr.postal,
                                                              addr.region.getOrElse(""))
                                                              )
        case None =>  None
      }
      val userUI = UserUI(user.firstName.getOrElse(""),
        user.lastName.getOrElse(""),
        user.mobilePhone.getOrElse(""),
        address)
      complete(userUI)
    })
  }

  private def getOfferStatus(offer:OfferQms,confirmedOffer : Option[ConfirmedOfferUserStatus] = None ):String={

    var offerStatus = OfferUI.unknownStatus
    val now = DateTime.now(DateTimeZone.UTC)
    val newDate = offer.startOn.plusDays(3).toDateTime(DateTimeZone.UTC)

    if(now.isAfter(offer.startOn) && now.isBefore(newDate) ){
      offerStatus = OfferUI.newStatus
    }
    if(now.isAfter(newDate) && now.isBefore(offer.endOn) ){
      offerStatus = OfferUI.onGoingStatus
    }
    if(!offer.active ){
      offerStatus = OfferUI.inactiveStatus
    }
    confirmedOffer.map { co =>
      if (co.availableCount <= 0) {
        offerStatus = OfferUI.outOfStockStatus
      }
    }
    if(DateTime.now(DateTimeZone.UTC).isAfter(offer.endOn) ){
      offerStatus = OfferUI.expiredStatus
    }
    confirmedOffer.map { co =>

      if (co.isConfirmed ) {
        offerStatus = OfferUI.confirmedStatus
      }
    }
    if(now.isAfter(offer.callToActionAttributes.startOn) &&
      now.isBefore(offer.callToActionAttributes.endOn)  ){
      offerStatus = OfferUI.followUpStatus
    }

    offerStatus
  }

}
