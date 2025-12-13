package com.pleasure.product

import com.pleasure.*
import com.pleasure.config.DatabaseConfig
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

import java.util.UUID
import javax.sql.DataSource

final case class ProductPostgressRepository(quill: Quill.Postgres[SnakeCase])
    extends ProductRepository:
  import quill.*

  inline def products = querySchema[ProductRow]("products")

  override def findById(id: ProductId): UIO[Option[Product]] =
    run(products.filter(_.id == lift(ProductId.unwrap(id))))
      .map(_.headOption.map(_.toDomain))
      .orDie

  override def findAll(): UIO[List[Product]] =
    run(products)
      .map(_.map(_.toDomain))
      .orDie

  override def save(product: Product): UIO[Product] =
    val row = ProductRow.fromDomain(product)
    run(products.insertValue(lift(ProductRow.fromDomain(product))))
      .as(product)
      .orDie

  override def delete(id: ProductId): UIO[Boolean] =
    run(products.filter(_.id == lift(ProductId.unwrap(id))).delete)
      .map(_ > 0)
      .orDie

private case class ProductRow(
    id: UUID,
    name: String,
    category: String,
    offerType: String,
    price: Option[BigDecimal],
    dailyRate: Option[BigDecimal],
    deposit: Option[BigDecimal]
):
  def toDomain: Product =
    val offer = offerType match
      case "ForSale"   => OfferType.ForSale(price.get)
      case "ForRental" => OfferType.ForRental(dailyRate.get, deposit.get)
      case "ForBoth" => OfferType.ForBoth(price.get, dailyRate.get, deposit.get)
    Product(
      id = ProductId(id),
      name = name,
      category = Category.valueOf(category),
      offerType = offer
    )

private object ProductRow:
  def fromDomain(p: Product): ProductRow =
    val (offerType, price, dailyRate, deposit) = p.offerType match
      case OfferType.ForSale(pr)        => ("ForSale", Some(pr), None, None)
      case OfferType.ForRental(dr, dep) =>
        ("ForRental", None, Some(dr), Some(dep))
      case OfferType.ForBoth(dr, pr, dep) =>
        ("ForBoth", Some(pr), Some(dr), Some(dep))
    ProductRow(
      id = ProductId.unwrap(p.id),
      name = p.name,
      category = p.category.toString,
      offerType = offerType,
      price = price,
      dailyRate = dailyRate,
      deposit = deposit
    )

object PostgresProductRepository:
  val layer: URLayer[Quill.Postgres[SnakeCase], ProductRepository] =
    ZLayer.fromFunction(ProductPostgressRepository(_))
