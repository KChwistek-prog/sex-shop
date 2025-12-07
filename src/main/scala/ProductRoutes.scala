package com.pleasure

import shared.AppError

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
  private def toResponse(error: AppError): Response = error match
    case AppError.NotFound(_, _) => Response.text(error.msg).status(NotFound)
    case AppError.ValidationError(_) => Response.text(error.msg).status(BadRequest)
    case AppError.InputError(_) => Response.text(error.msg).status(BadRequest)

  val routes: Routes[ProductService, Nothing] = Routes(
    Method.GET / "products" -> handler:
      ZIO.serviceWithZIO[ProductService](_.getAll)
        .map(products => Response.json(products.toJson))
    ,
    Method.GET / "products" / string("id") -> handler { (id: String, _: Request) =>
      ProductId.fromString(id) match
        case Left(err) => ZIO.succeed(Response.text(err).status(BadRequest))
        case Right(productId) =>
          ZIO.serviceWithZIO[ProductService](_.getById(productId))
            .map(product => Response.json(product.toJson))
            .catchAll(err => ZIO.succeed(toResponse(err)))
    }
    ,
    Method.POST / "products" -> handler { (req: Request) =>
      (for
        body <- req.body.asString.orDie
        parsed <- ZIO.fromEither(body.fromJson[CreateProductRequest])
          .mapError(err => AppError.InputError(err))
        product <- ZIO.serviceWithZIO[ProductService](_.create(parsed.name, parsed.category, parsed.offerType))
      yield Response.json(product.toJson).status(Status.Created))
        .catchAll(err => ZIO.succeed(toResponse(err)))
    }
    ,
    Method.DELETE / "products" / string("id") -> handler { (id: String, _: Request) =>
      ProductId.fromString(id) match
        case Left(err) => ZIO.succeed(Response.text(err).status(BadRequest))
        case Right(productId) =>
          ZIO.serviceWithZIO[ProductService](_.delete(productId))
            .map:
              case true => Response.status(NoContent)
              case false => Response.text("Product not found").status(NotFound)
    }
    ,
    Method.PUT / "products" / string("id") -> handler { (id: String, req: Request) =>
      ProductId.fromString(id) match
        case Left(err) => ZIO.succeed(Response.text(err).status(BadRequest))
        case Right(productId) =>
          (for
            body <- req.body.asString.orDie
            parsed <- ZIO.fromEither(body.fromJson[CreateProductRequest])
              .mapError(err => AppError.InputError(err))
            product <- ZIO.serviceWithZIO[ProductService](_.update(productId, parsed.name, parsed.category, parsed.offerType))
          yield Response.json(product.toJson))
            .catchAll(err => ZIO.succeed(toResponse(err)))
    }
  )