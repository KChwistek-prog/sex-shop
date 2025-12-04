package com.pleasure
package product

import Category.{BDSM, Dildos, Furniture, Lubricants}
import OfferType.{ForRental, ForSale}

import zio.test.{ZIOSpecDefault, assertTrue}

object ProductSpec extends ZIOSpecDefault:
  def spec = suite("Product")(
    test("create should create product for valid name"):
      val result = Product.create(
        name = "Magic wand",
        category = Dildos,
        offerType = ForSale(BigDecimal(299))
      )
      assertTrue(result.isRight),
    test("create should reject empty name"):
      val result = Product.create(
        name = "  ",
        category = BDSM,
        offerType = ForRental(BigDecimal(2), BigDecimal(20))
      )
      assertTrue(result == Left("Name cannot be empty")),
    test("ForSale product can be sold but not rented"):
      val Right(product) = Product.create(
        name = "Bondage Bench",
        category = Furniture,
        offerType = ForSale(BigDecimal(20))
      ): @unchecked
      assertTrue(product.canBeSold, !product.canBeRented),
    test("ForRental product can be rented but not sold"):
      val Right(product) = Product.create(
        name = "Lubricant",
        category = Lubricants,
        offerType = ForRental(20, BigDecimal(299))
      ): @unchecked
      assertTrue(!product.canBeSold, product.canBeRented)
  )
