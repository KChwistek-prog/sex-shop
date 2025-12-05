package com.pleasure

import zio.json.{JsonDecoder, JsonEncoder}
import zio.prelude.Newtype

import java.util.UUID

object ProductId extends Newtype[UUID]:
  def generate: ProductId = wrap(UUID.randomUUID())

  def fromString(s: String): Either[String, ProductId] =
    try Right(ProductId(UUID.fromString(s)))
    catch case _: IllegalArgumentException => Left(s"Invalid ProductId: $s")

  given JsonEncoder[ProductId] = JsonEncoder[String].contramap(_.asString)
  given JsonDecoder[ProductId] = JsonDecoder[String].mapOrFail(ProductId.fromString)

  extension (id: ProductId)
    def asString: String = ProductId.unwrap(id).toString

type ProductId = ProductId.Type