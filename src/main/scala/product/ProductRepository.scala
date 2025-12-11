package com.pleasure.product

import zio.UIO
import com.pleasure.product.ProductId
import com.pleasure.product.Product

trait ProductRepository:
  def findById(id: ProductId): UIO[Option[Product]]

  def findAll(): UIO[List[Product]]

  def save(product: Product): UIO[Product]

  def delete(id: ProductId): UIO[Boolean]


