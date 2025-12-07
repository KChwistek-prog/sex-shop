package com.pleasure

import zio.{IO, UIO, URLayer, ZIO, ZLayer}

trait ProductService:
  def getById(id: ProductId): IO[String, Product]

  def getAll: UIO[List[Product]]

  def create(name: String, category: Category, offerType: OfferType): IO[String, Product]

  def delete(id: ProductId): UIO[Boolean]

  def update(id: ProductId, name: String, category: Category, offerType: OfferType): IO[String, Product]

final case class ProductServiceLive(repo: ProductRepository) extends ProductService:
  override def getById(id: ProductId): IO[String, Product] =
    repo.findById(id).flatMap:
      case Some(product) => ZIO.succeed(product)
      case None => ZIO.fail(s"Product not found: ${id.asString}")

  override def getAll: UIO[List[Product]] =
    repo.findAll()

  override def create(name: String, category: Category, offerType: OfferType): IO[String, Product] =
    ZIO.fromEither(Product.create(name, category, offerType))
      .flatMap(repo.save)

  override def delete(id: ProductId): UIO[Boolean] =
    repo.delete(id)

  override def update(id: ProductId, name: String, category: Category, offerType: OfferType): IO[String, Product] =
    for
      _ <- getById(id)
      updated = Product(id, name, category, offerType)
      saved <- repo.save(updated)
    yield saved

object ProductServiceLive:
  val layer: URLayer[ProductRepository, ProductService] =
    ZLayer.fromFunction(ProductServiceLive(_))
