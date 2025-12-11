package com.pleasure.product

import zio.{IO, UIO, URLayer, ZIO, ZLayer}
import com.pleasure.product.ProductId
import com.pleasure.product.{Category, OfferType, Product}
import com.pleasure.product.ProductRepository
import com.pleasure.shared.AppError

trait ProductService:
  def getById(id: ProductId): IO[AppError, Product]

  def getAll: UIO[List[Product]]

  def create(name: String, category: Category, offerType: OfferType): IO[AppError, Product]

  def delete(id: ProductId): UIO[Boolean]

  def update(id: ProductId, name: String, category: Category, offerType: OfferType): IO[AppError, Product]

final case class ProductServiceLive(repo: ProductRepository) extends ProductService:
  override def getById(id: ProductId): IO[AppError, Product] =
    repo.findById(id).flatMap:
      case Some(product) => ZIO.succeed(product)
      case None => ZIO.fail(AppError.NotFound("Product", id.asString))

  override def getAll: UIO[List[Product]] =
    repo.findAll()

  override def create(name: String, category: Category, offerType: OfferType): IO[AppError, Product] =
    ZIO.fromEither(Product.create(name, category, offerType))
      .mapError(err => AppError.ValidationError(err))
      .flatMap(repo.save)

  override def delete(id: ProductId): UIO[Boolean] =
    repo.delete(id)

  override def update(id: ProductId, name: String, category: Category, offerType: OfferType): IO[AppError, Product] =
    for
      _ <- getById(id)
      updated = Product(id, name, category, offerType)
      saved <- repo.save(updated)
    yield saved

object ProductServiceLive:
  val layer: URLayer[ProductRepository, ProductService] =
    ZLayer.fromFunction(ProductServiceLive(_))
