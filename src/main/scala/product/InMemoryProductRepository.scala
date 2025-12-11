package com.pleasure.product

import zio.{Ref, UIO, ULayer, ZLayer}
import com.pleasure.product.ProductId
import com.pleasure.product.Product
import com.pleasure.product.ProductRepository

final case class InMemoryProductRepository(ref: Ref[Map[ProductId, Product]]) extends ProductRepository:
  override def findById(id: ProductId): UIO[Option[Product]] =
    ref.get.map(_.get(id))

  override def findAll(): UIO[List[Product]] =
    ref.get.map(_.values.toList)

  override def save(product: Product): UIO[Product] =
    ref.update(_ + (product.id -> product)).as(product)

  override def delete(id: ProductId): UIO[Boolean] =
    ref.modify: map =>
      if map.contains(id) then (true, map - id)
      else (false, map)

object InMemoryProductRepository:
  val layer: ULayer[ProductRepository] =
    ZLayer:
      Ref.make(Map.empty[ProductId, Product]).map(InMemoryProductRepository(_))