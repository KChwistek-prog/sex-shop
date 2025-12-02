package com.pleasure
package product

import com.pleasure.Category.Dildos
import com.pleasure.OfferType.ForSale
import zio.test.assertTrue
import zio.test.ZIOSpecDefault

object ProductSpec extends ZIOSpecDefault:
  def spec = suite("Product")(
    test("create should create product for valid name"):
      val result = Product.create(
        name = "Magic wand",
        category = Dildos,
        offerType = ForSale(BigDecimal(299))
      )
      assertTrue(result.isRight)
  )
