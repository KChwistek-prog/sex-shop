package com.pleasure

import zio.*
import zio.http.*
import zio.http.Status.{BadRequest, NoContent, NotFound}
import zio.json.*

final case class CreateProductRequest(
                                       name: String,
                                       category: Category,
                                       offerType: OfferType
                                     )

object CreateProductRequest:
  given JsonDecoder[CreateProductRequest] = DeriveJsonDecoder.gen[CreateProductRequest]

object ProductRoutes:
  val routes: Routes[ProductService, Nothing] = Routes(
    Method.GET / "products" -> handler:
      ZIO.serviceWithZIO[ProductService](_.getAll)
        .map(products => Response.json(products.toJson)),
    Method.GET / "products" / string("id") -> handler { (id: String, _: Request) =>
      ZIO.serviceWithZIO[ProductService](_.getById(ProductId.fromString(id).toOption.get))
        .map(product => Response.json(product.toJson))
        .catchAll(err => ZIO.succeed(Response.text(err).status(Status.NotFound)))
    },
    Method.POST / "products" -> handler { (req: Request) =>
      for
        body <- req.body.asString.orDie
        parsed <- ZIO.fromEither(body.fromJson[CreateProductRequest])
          .mapError(err => Response.text(err).status(Status.BadRequest))
        product <- ZIO.serviceWithZIO[ProductService](_.create(parsed.name, parsed.category, parsed.offerType))
          .mapError(err => Response.text(err).status(Status.BadRequest))
      yield Response.json(product.toJson).status(Status.Created)
    }.catchAll(err => handler(err)),
    Method.DELETE / "products" / string("id") -> handler { (id: String, _: Request) =>
      ProductId.fromString(id) match
        case Left(err) => ZIO.succeed(Response.text(err).status(BadRequest))
        case Right(productId) =>
          ZIO.serviceWithZIO[ProductService](_.delete(productId))
            .map:
              case true => Response.status(NoContent)
              case false => Response.text("Product not found").status(NotFound)
    },
    Method.PUT / "products" / string("id") -> handler { (id: String, req: Request) =>
      ProductId.fromString(id) match {
        case Left(err) => ZIO.succeed(Response.text(err).status(BadRequest))
        case Right(productId) =>
          (for
            body <- req.body.asString.orDie
            parsed <- ZIO.fromEither(body.fromJson[CreateProductRequest])
              .mapError(err => Response.text(err).status(Status.BadRequest))
            product <- ZIO.serviceWithZIO[ProductService](_.update(productId, parsed.name, parsed.category, parsed.offerType))
              .mapError(err => Response.text(err).status(NotFound))
          yield Response.json(product.toJson)).catchAll(err => ZIO.succeed(err))
      }
    }
  )