package com.pleasure

import zio.{Ref, UIO}

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

