package com.poshly.products

import org.scalatest._
import org.scalatest.mockito.MockitoSugar

abstract class UnitTest extends FlatSpec with Matchers with OptionValues with Inside with Inspectors with MockitoSugar
