package com.pleasure

import zio.UIO

trait ProductRepository:
  def findById(id: ProductId): UIO[Option[Product]]

  def findAll(): UIO[List[Product]]

  def save(product: Product): UIO[Product]

  def delete(id: ProductId): UIO[Boolean]
