//package com.poshly.products.services
//
//import com.poshly.core.logging.Loggable
//import com.poshly.core.zk.ZooKeeperClientConfiguration
//import com.poshly.products.UnitTest
//import com.poshly.products.util.ZooKeeperClientMockComponent
//import com.poshly.products.data.{InsightDefinitionSearchResult => LocalInsightDefinitionSearchResult}
//import com.poshly.pie.client.InsightDefinitionService
//import com.poshly.pie.client.data.{InsightDefinitionSeries, InsightDefinition, InsightDefinitionSearchCriteria, InsightDefinitionSearchResult}
//import org.apache.lucene.search.{BooleanClause, BooleanQuery}
//import org.mockito.Mockito._
//
//import scala.concurrent.Await
//import scala.concurrent.duration._
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.concurrent.Future
//
//class InsightDefinitionCacheSpec extends UnitTest with Loggable {
//
//  val def1 = InsightDefinition.empty.
//    applyId(Some("idValue1")).
//    applyTitle("titleValue1").
//    applyShortTitle(Some("shortTitleValue1")).
//    applyDescription("description1").
//    applyKeywords(Set("titlevalue1", "shorttitlevalue1", "cat1", "cat2")).
//    applyCategories(Set("cat1", "cat2"))
//  val def2 = InsightDefinition.empty.
//    applyId(Some("idValue2")).
//    applyTitle("titleValue2").
//    applyShortTitle(Some("shortTitleValue2")).
//    applyKeywords(Set("titlevalue2", "shorttitlevalue2", "cat1", "cat2")).
//    applyDescription("description2")
//  val def3 = InsightDefinition.empty.
//    applyId(Some("ID0000000000000000A68")).
//    applyTitle("phrase query title").
//    applyShortTitle(Some("short title for phrase query")).
//    applyKeywords(Set("phrase", "query", "title", "short", "for", "short")).
//    applyDescription("description2").
//    applySeries(Seq(
//      InsightDefinitionSeries.empty.applyTitle("Insight Title 1").applyInsightValueCode("insightValue1"),
//      InsightDefinitionSeries.empty.applyTitle("Insight Title 2").applyInsightValueCode("insightValue2"))
//    )
//
//  type AllConfigurationTypes = ZooKeeperClientConfiguration
//
//  def defaultComponents: AllConfigurationTypes = new ZooKeeperClientMockComponent {
//  }
//
//  "The Insight Definition Cache  " should
//  "findBy correctly" in {
//    val insightDefinitions = mock[InsightDefinitionService]
//
//    when(insightDefinitions.findBy(InsightDefinitionSearchCriteria.empty)).thenReturn(Future(
//      InsightDefinitionSearchResult.empty
//        .applyCount(2)
//        .applyResults(Seq(def1, def2))))
//
//    val cache = new InsightDefinitionSearchIndex(insightDefinitions, defaultComponents)
//    Await.result(cache.build(), 10 second)
//
//    val definitions = cache.findBy(new InsightSearchCriteria()).data
//    definitions.size shouldEqual 2
//    definitions(0) shouldEqual new LocalInsightDefinitionSearchResult("idValue1", "titleValue1", "shortTitleValue1",Set("cat1", "cat2"))
//    definitions(1) shouldEqual new LocalInsightDefinitionSearchResult("idValue2", "titleValue2", "shortTitleValue2", Set())
//  }
//
//  "The Insight Definition Cache  " should
//    "findBy correctly when empty search criteria" in {
//      val insightDefinitions = mock[InsightDefinitionService]
//
//      when(insightDefinitions.findBy(InsightDefinitionSearchCriteria.empty)).thenReturn(Future(
//        InsightDefinitionSearchResult.empty
//          .applyCount(3)
//          .applyResults(Seq(def1, def2, def3))))
//
//    val cache = new InsightDefinitionSearchIndex(insightDefinitions, defaultComponents)
//    Await.result(cache.build(), 10 second)
//
//      val definitions = cache.findBy(InsightSearchCriteria())
//      definitions.data.length shouldEqual 3
//      definitions.data(0) shouldEqual LocalInsightDefinitionSearchResult("idValue1", "titleValue1", "shortTitleValue1", Set("cat1", "cat2"))
//      definitions.data(1) shouldEqual LocalInsightDefinitionSearchResult("idValue2", "titleValue2", "shortTitleValue2", Set())
//  }
//
//  "The Insight Definition cache  " should
//    "performSearch in isolation correctly by keyword search" in {
//    val insightDefinitions = mock[InsightDefinitionService]
//    when(insightDefinitions.findBy(InsightDefinitionSearchCriteria.empty)).thenReturn(Future(
//      InsightDefinitionSearchResult.empty
//        .applyCount(3)
//        .applyResults(Seq(def1, def2, def3))))
//    val cache = new InsightDefinitionSearchIndex(insightDefinitions, defaultComponents)
//    Await.result(cache.build(), 10 second)
//
//    cache.search(cache.addQuery(new BooleanQuery(), cache.keyQueries(InsightSearchCriteria(q = Some("titleValue1"))),
//      BooleanClause.Occur.SHOULD), 10).documents.seq.size shouldEqual 1
//    cache.search(cache.addQuery(new BooleanQuery(), cache.keyQueries(InsightSearchCriteria(q = Some("titleValue2"))),
//      BooleanClause.Occur.SHOULD), 10).documents.seq.size shouldEqual 1
//    cache.search(cache.addQuery(new BooleanQuery(), cache.keyQueries(InsightSearchCriteria(q = Some("titleValue*"))),
//      BooleanClause.Occur.SHOULD), 10).documents.seq.size shouldEqual 2
//
//    cache.search(cache.addQuery(new BooleanQuery(), cache.keyQueries(InsightSearchCriteria(q = Some("shortTitleValue1"))),
//      BooleanClause.Occur.SHOULD), 10).documents.seq.size shouldEqual 1
//    cache.search(cache.addQuery(new BooleanQuery(), cache.keyQueries(InsightSearchCriteria(q = Some("shortTitleValue2"))),
//      BooleanClause.Occur.SHOULD), 10).documents.seq.size shouldEqual 1
//
//    cache.search(cache.addQuery(new BooleanQuery(), cache.keyQueries(InsightSearchCriteria(q = Some("cat1"))),
//      BooleanClause.Occur.SHOULD), 10).documents.seq.size shouldEqual 2
//    cache.search(cache.addQuery(new BooleanQuery(), cache.keyQueries(InsightSearchCriteria(q = Some("cat2"))),
//      BooleanClause.Occur.SHOULD), 10).documents.seq.size shouldEqual 2
//
//    cache.search(cache.addQuery(new BooleanQuery(), cache.keyQueries(InsightSearchCriteria(q = Some("short"))),
//      BooleanClause.Occur.SHOULD), 10).documents.seq.size shouldEqual 1
//    cache.search(cache.addQuery(new BooleanQuery(), cache.keyQueries(InsightSearchCriteria(q = Some("phrase"))),
//      BooleanClause.Occur.SHOULD), 10).documents.seq.size shouldEqual 1
//  }
//
//  "The Insight Definition Cache  " should
//    "findBy correctly keyword search" in {
//    val insightDefinitions = mock[InsightDefinitionService]
//    when(insightDefinitions.findBy(InsightDefinitionSearchCriteria.empty)).thenReturn(Future(
//      InsightDefinitionSearchResult.empty
//        .applyCount(3)
//        .applyResults(Seq(def1, def2, def3))))
//
//    val cache = new InsightDefinitionSearchIndex(insightDefinitions, defaultComponents)
//    Await.result(cache.build(), 10 second)
//
//    cache.findBy(InsightSearchCriteria(q = Some("titleValue1"))).data.size shouldEqual 1
//    cache.findBy(InsightSearchCriteria(q = Some("titleValue2"))).data.size shouldEqual 1
//
//    cache.findBy(InsightSearchCriteria(q = Some("shortTitleValue1"))).data.size shouldEqual 1
//    cache.findBy(InsightSearchCriteria(q = Some("shortTitleValue2"))).data.size shouldEqual 1
//
//    cache.findBy(InsightSearchCriteria(q = Some("cat1"))).data.size shouldEqual 2
//    cache.findBy(InsightSearchCriteria(q = Some("cat2"))).data.size shouldEqual 2
//  }
//
//  "The Insight Definition Cache  " should
//    "performSearch in isolation correctly by term search" in {
//    val insightDefinitions = mock[InsightDefinitionService]
//    when(insightDefinitions.findBy(InsightDefinitionSearchCriteria.empty)).thenReturn(Future(
//      InsightDefinitionSearchResult.empty
//        .applyCount(3)
//        .applyResults(Seq(def1, def2, def3))))
//
//    val cache = new InsightDefinitionSearchIndex(insightDefinitions, defaultComponents)
//    Await.result(cache.build(), 10 second)
//
//    cache.search(cache.addQuery(new BooleanQuery(), cache.termQueries(InsightSearchCriteria(q = Some("title:titleValue1"))),
//      BooleanClause.Occur.MUST), 10).documents.seq.size shouldEqual 1
//    cache.search(cache.addQuery(new BooleanQuery(), cache.termQueries(InsightSearchCriteria(q = Some("title:titleValue2"))),
//      BooleanClause.Occur.MUST), 10).documents.seq.size shouldEqual 1
//    cache.search(cache.addQuery(new BooleanQuery(), cache.termQueries(InsightSearchCriteria(q = Some("title:title"))),
//      BooleanClause.Occur.MUST), 10).documents.seq.size shouldEqual 1
//    cache.search(cache.addQuery(new BooleanQuery(), cache.termQueries(InsightSearchCriteria(q = Some("title:query"))),
//      BooleanClause.Occur.MUST), 10).documents.seq.size shouldEqual 1
//
//    cache.search(cache.addQuery(new BooleanQuery(), cache.termQueries(InsightSearchCriteria(q = Some("short-title:shortTitleValue1"))),
//      BooleanClause.Occur.MUST), 10).documents.seq.size shouldEqual 1
//    cache.search(cache.addQuery(new BooleanQuery(), cache.termQueries(InsightSearchCriteria(q = Some("short-title:shortTitleValue2"))),
//      BooleanClause.Occur.MUST), 10).documents.seq.size shouldEqual 1
//    cache.search(cache.addQuery(new BooleanQuery(), cache.termQueries(InsightSearchCriteria(q = Some("short-title:title"))),
//      BooleanClause.Occur.MUST), 10).documents.seq.size shouldEqual 1
//    cache.search(cache.addQuery(new BooleanQuery(), cache.termQueries(InsightSearchCriteria(q = Some("short-title:query"))),
//      BooleanClause.Occur.MUST), 10).documents.seq.size shouldEqual 1
//  }
//
//
//  "The Insight Definition Cache  " should
//    "findBy correctly term search" in {
//    val insightDefinitions = mock[InsightDefinitionService]
//    when(insightDefinitions.findBy(InsightDefinitionSearchCriteria.empty)).thenReturn(Future(
//      InsightDefinitionSearchResult.empty
//        .applyCount(3)
//        .applyResults(Seq(def1, def2, def3))))
//    val cache = new InsightDefinitionSearchIndex(insightDefinitions, defaultComponents)
//    Await.result(cache.build(), 10 seconds)
//
//    cache.findBy(InsightSearchCriteria(q = Some("title:titleValue1"))).data.size shouldEqual 1
//    cache.findBy(InsightSearchCriteria(q = Some("title:titleValue2"))).data.size shouldEqual 1
//
//    cache.findBy(InsightSearchCriteria(q = Some("short-title:shortTitleValue1"))).data.size shouldEqual 1
//    cache.findBy(InsightSearchCriteria(q = Some("short-title:shortTitleValue2"))).data.size shouldEqual 1
//  }
//
//  "The Insight Definition Cache  " should
//    "performSearch in isolation correctly by phrase search" in {
//    val insightDefinitions = mock[InsightDefinitionService]
//    when(insightDefinitions.findBy(InsightDefinitionSearchCriteria.empty)).thenReturn(Future(
//      InsightDefinitionSearchResult.empty
//        .applyCount(3)
//        .applyResults(Seq(def1, def2, def3))))
//
//    val cache = new InsightDefinitionSearchIndex(insightDefinitions, defaultComponents)
//    Await.result(cache.build(), 10 seconds)
//
//    cache.search(cache.addQuery(new BooleanQuery(), cache.phraseQueries(InsightSearchCriteria(q = Some("phrase query"))),
//      BooleanClause.Occur.SHOULD), 10).documents.seq.size shouldEqual 1
//    cache.search(cache.addQuery(new BooleanQuery(), cache.phraseQueries(InsightSearchCriteria(q = Some("query title"))),
//      BooleanClause.Occur.SHOULD), 10).documents.seq.size shouldEqual 1
//    cache.search(cache.addQuery(new BooleanQuery(), cache.phraseQueries(InsightSearchCriteria(q = Some("phrase query title"))),
//      BooleanClause.Occur.SHOULD), 10).documents.seq.size shouldEqual 1
//
//    cache.search(cache.addQuery(new BooleanQuery(), cache.phraseQueries(InsightSearchCriteria(q = Some("short title"))),
//      BooleanClause.Occur.SHOULD), 10).documents.seq.size shouldEqual 1
//    cache.search(cache.addQuery(new BooleanQuery(), cache.phraseQueries(InsightSearchCriteria(q = Some("short title for"))),
//      BooleanClause.Occur.SHOULD), 10).documents.seq.size shouldEqual 1
//    cache.search(cache.addQuery(new BooleanQuery(), cache.phraseQueries(InsightSearchCriteria(q = Some("title for phrase"))),
//      BooleanClause.Occur.SHOULD), 10).documents.seq.size shouldEqual 1
//    cache.search(cache.addQuery(new BooleanQuery(), cache.phraseQueries(InsightSearchCriteria(q = Some("title for phrase query"))),
//      BooleanClause.Occur.SHOULD), 10).documents.seq.size shouldEqual 1
//    cache.search(cache.addQuery(new BooleanQuery(), cache.phraseQueries(InsightSearchCriteria(q = Some("short title for phrase query"))),
//      BooleanClause.Occur.SHOULD), 10).documents.seq.size shouldEqual 1
//  }
//
//  "The Insight Definition Cache  " should
//    "findBy correctly phrase search" in {
//    val insightDefinitions = mock[InsightDefinitionService]
//    when(insightDefinitions.findBy(InsightDefinitionSearchCriteria.empty)).thenReturn(Future(
//      InsightDefinitionSearchResult.empty
//        .applyCount(3)
//        .applyResults(Seq(def1, def2, def3))))
//
//    val cache = new InsightDefinitionSearchIndex(insightDefinitions, defaultComponents)
//    Await.result(cache.build(), 10 seconds)
//
//    cache.findBy(InsightSearchCriteria(q = Some("titleValue1"))).data.size shouldEqual 1
//    cache.findBy(InsightSearchCriteria(q = Some("titleValue2"))).data.size shouldEqual 1
//
//    cache.findBy(InsightSearchCriteria(q = Some("shortTitleValue1"))).data.size shouldEqual 1
//    cache.findBy(InsightSearchCriteria(q = Some("shortTitleValue2"))).data.size shouldEqual 1
//
//    cache.findBy(InsightSearchCriteria(q = Some("cat1"))).data.size shouldEqual 2
//    cache.findBy(InsightSearchCriteria(q = Some("cat2"))).data.size shouldEqual 2
//  }
//
//  "The Insight Definition Cache  " should
//    "findById correctly " in {
//    val insightDefinitions = mock[InsightDefinitionService]
//    when(insightDefinitions.findBy(InsightDefinitionSearchCriteria.empty)).thenReturn(Future(
//      InsightDefinitionSearchResult.empty
//        .applyCount(3)
//        .applyResults(Seq(def1, def2, def3))))
//
//    val cache = new InsightDefinitionSearchIndex(insightDefinitions, defaultComponents)
//    Await.result(cache.build(), 10 seconds)
//
//    val results = cache.findById("ID0000000000000000A68").data
//    val definition = results.get
//    results.size shouldEqual 1
//    definition.series.size shouldEqual 2
//    definition.series.head.insight_value_code shouldEqual "insightValue1"
//    definition.series.head.title shouldEqual "Insight Title 1"
//    definition.series.last.insight_value_code shouldEqual "insightValue2"
//    definition.series.last.title shouldEqual "Insight Title 2"
//  }
//
//  "The Insight Definition Cache  " should
//  "update correctly" in {
//    val insightDefinitions = mock[InsightDefinitionService]
//
//    when(insightDefinitions.findBy(InsightDefinitionSearchCriteria.empty)).thenReturn(Future(
//      InsightDefinitionSearchResult.empty
//        .applyCount(2)
//        .applyResults(Seq(def1, InsightDefinition.empty.
//        applyId(Some("idValue2")).
//        applyTitle("titleValue2-Updated").
//        applyShortTitle(Some("shortTitleValue2")).
//        applyActive(true).
//        applyDescription("description2")))))
//    val cache = new InsightDefinitionSearchIndex(insightDefinitions, defaultComponents)
//    Await.result(cache.build(), 10 seconds)
//
//    cache.onUpdate("idValue2")
//
//    val definitions = cache.findBy(new InsightSearchCriteria()).data
//    definitions.size shouldEqual 2
//    definitions(0) shouldEqual new LocalInsightDefinitionSearchResult("idValue1", "titleValue1", "shortTitleValue1",Set("cat1", "cat2"))
//    definitions(1) shouldEqual new LocalInsightDefinitionSearchResult("idValue2", "titleValue2-Updated", "shortTitleValue2", Set())
//  }
//
//}
