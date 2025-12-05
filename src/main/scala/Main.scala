package com.pleasure

import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}
import zio.http.{Routes, Server}

object Main extends ZIOAppDefault:

  val app: Routes[ProductService, Nothing] = ProductRoutes.routes

  def run =
    Server
      .serve(app)
      .provide(
        Server.default,
        ProductServiceLive.layer,
        InMemoryProductRepository.layer
      )