package com.pleasure

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

enum Category:
  case Dildos, Lingerie, UsedDildos, UsedLingerie, BDSM, Furniture, Accessories, UsedAccessories, Lubricants

enum OfferType:
  case ForSale(price: BigDecimal)
  case ForRental(dailyRate: BigDecimal, deposit: BigDecimal)
  case ForBoth(dailyRate: BigDecimal, price: BigDecimal, deposit: BigDecimal)

  def canBeSold: Boolean = this match
    case ForSale(_) => true
    case ForRental(_, _) => false
    case ForBoth(_, _, _) => true

  def canBeRented: Boolean = this match
    case ForSale(_) => false
    case ForRental(_, _) => true
    case ForBoth(_, _, _) => true

  def salePrice: Option[BigDecimal] = this match
    case ForSale(price) => Some(price)
    case ForRental(_, _) => None
    case ForBoth(_, salePrice, _) => Some(salePrice)

  def rentalInfo: Option[(BigDecimal, BigDecimal)] = this match
    case ForSale(price) => None
    case ForRental(dailyRate, deposit) => Some((dailyRate, deposit))
    case ForBoth(dailyRate, price, deposit) => Some((dailyRate, deposit))


final case class Product(
                          id: ProductId,
                          name: String,
                          category: Category,
                          offerType: OfferType
                        ):
  def canBeSold: Boolean = offerType.canBeSold

  def canBeRented: Boolean = offerType.canBeRented

  def salePrice: Option[BigDecimal] = offerType.salePrice

  def rentalInfo: Option[(BigDecimal, BigDecimal)] = offerType.rentalInfo

object Product:
  def create(name: String,
             category: Category,
             offerType: OfferType
            ): Either[String, Product] =
              for 
                _ <- Either.cond(name.trim.nonEmpty, (), "Name cannot be empty")
                _ <- validateOfferType(offerType)
              yield new Product(ProductId.generate, name.trim, category, offerType)

  def validateOfferType(offerType: OfferType): Either[String, Unit] =
    offerType match
      case OfferType.ForSale(price) =>
         Either.cond(price > 0, (), "Sale price must be greater than zero")
      case OfferType.ForRental(dailyRate, deposit) =>
        for
          _ <- Either.cond(dailyRate > 0, (), "Daily rate must be greater than zero")
          _ <- Either.cond(deposit > 0, (), "Deposint must be non-negative")
          _ <- Either.cond(deposit >= dailyRate, (), "Deposit must be at least equal to daily rate")
        yield ()
      case OfferType.ForBoth(dailyRate, price, deposit) =>
        for
          _ <- Either.cond(price > 0, (), "Sale price must be greater than zero")
          _ <- Either.cond(dailyRate > 0, (), "Daily rate must be greater than zero")
          _ <- Either.cond(deposit > 0, (), "Deposint must be non-negative")
          _ <- Either.cond(deposit >= dailyRate, (), "Deposit must be at least equal to daily rate")
        yield ()

  given JsonEncoder[Product] = DeriveJsonEncoder.gen[Product]
  given JsonDecoder[Product] = DeriveJsonDecoder.gen[Product]

object Category:
  given JsonEncoder[Category] = JsonEncoder[String].contramap(_.toString)
  given JsonDecoder[Category] = JsonDecoder[String].mapOrFail: s =>
    Category.values.find(_.toString == s).toRight(s"Unknowns category: &s")

object OfferType:
  given JsonEncoder[OfferType] = DeriveJsonEncoder.gen[OfferType]
  given JsonDecoder[OfferType] = DeriveJsonDecoder.gen[OfferType]
