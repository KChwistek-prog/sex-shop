package com.pleasure
package product

import Category.{Dildos, UsedDildos}
import OfferType.ForSale

import zio.test.{ZIOSpecDefault, assertTrue}
import zio.ULayer
import zio.ZIO

object ProductServiceSpec extends ZIOSpecDefault:
  val testLayer: ULayer[ProductService] =
    InMemoryProductRepository.layer >>> ProductServiceLive.layer

  def spec = suite("Product Service")(
    test("create and getById should work"):
      for
        created <- ZIO.serviceWithZIO[ProductService](
          _.create("Magic wand", Dildos, ForSale(BigDecimal(299)))
        )
        found <- ZIO.serviceWithZIO[ProductService](_.getById(created.id)
        )
      yield assertTrue(found.name == "Magic wand"),
    test("getById should fail for non-existent product"):
      for
        result <- ZIO.serviceWithZIO[ProductService](_.getById(ProductId.generate)).either
      yield assertTrue(result.isLeft),
    test("getAll should return all products"):
      for
        _ <- ZIO.serviceWithZIO[ProductService](_.create("Dildo 1", UsedDildos, ForSale(BigDecimal(29))))
        _ <- ZIO.serviceWithZIO[ProductService](_.create("Dildo 2", UsedDildos, ForSale(BigDecimal(29))))
        products <- ZIO.serviceWithZIO[ProductService](_.getAll)
      yield assertTrue(products.length == 2),
    test("should remove product"):
      for
        created <- ZIO.serviceWithZIO[ProductService](_.create("Used wand", UsedDildos, ForSale(BigDecimal(30))))
        deleted <- ZIO.serviceWithZIO[ProductService](_.delete(created.id))
        result <- ZIO.serviceWithZIO[ProductService](_.getById(created.id)).either
      yield assertTrue(deleted, result.isLeft)
  ).provide(testLayer)
