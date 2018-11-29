//package com.poshly.products.services
//
//import com.poshly.core.data.ComparisonOperator
//import com.poshly.core.finagle.TwitterFutureSupport
//import com.poshly.core.logging.Loggable
//import com.poshly.products.{InsightDefinitionConfiguration, AnalyticsConfiguration, UnitTest}
//import com.poshly.products.data.PoshlySprayJsonSupport
//import com.poshly.pie.{DemographicType, EntityType}
//import com.poshly.pie.client.data.{SubFilteringCriteria, FilteringCriteria}
//
//class FilterExtractionSpec extends UnitTest with TwitterFutureSupport with PoshlySprayJsonSupport with Loggable {
//
//  val services = mock[AnalyticsConfiguration with InsightDefinitionConfiguration]
//  val insightsAnalyticsService = new InsightsAnalyticsService(services, null)
//
//  val testFilterRequest1 = "ageGroup:equal:18to24"
//  val expectedFilter1 = FilteringCriteria.empty.applyFilters(Seq(
//    SubFilteringCriteria.empty.
//      applyGroupingEntityType(EntityType.Demographic).
//      applyDemographic(Some(DemographicType.AgeGroup)).
//      applyComparisonOperator(ComparisonOperator.Equal).
//      applyInsightValueCode("ageGroup18to24")))
//  val resultFilter1 = insightsAnalyticsService.extractFilters(Some(testFilterRequest1))
//  resultFilter1 shouldEqual expectedFilter1
//
//
//
//  val testFilterRequest2 = "ageGroup:equal:18to24,ID000000000000000A73:equal:hairDyedClassSemi"
//  val expectedFilter2 = FilteringCriteria.empty.applyFilters(Seq(
//    SubFilteringCriteria.empty.
//      applyGroupingEntityType(EntityType.Demographic).
//      applyDemographic(Some(DemographicType.AgeGroup)).
//      applyComparisonOperator(ComparisonOperator.Equal).
//      applyInsightValueCode("ageGroup18to24"),
//
//    SubFilteringCriteria.empty.
//      applyGroupingEntityType(EntityType.Insight).
//      applyDemographic(None).
//      applyEntityId(Some("ID000000000000000A73")).
//      applyComparisonOperator(ComparisonOperator.Equal).
//      applyInsightValueCode("hairDyedClassSemi")
//  ))
//
//  val resultFilter2 = insightsAnalyticsService.extractFilters(Some(testFilterRequest2))
//  resultFilter2 shouldEqual expectedFilter2
//
//
//}
