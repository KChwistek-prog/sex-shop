package com.pleasure

import com.pleasure.config.*
import com.pleasure.product.*
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.*
import zio.http.*

object Main extends ZIOAppDefault:

  val app: Routes[ProductService, Nothing] = ProductRoutes.routes

  def run =
    (for
      config <- ZIO.service[AppConfig]
      _      <- FlywayMigration.migrate(config.database)
      _      <- ZIO.logInfo("Migrations completed")
      _      <- Server.serve(app)
    yield ())
      .provide(
        Server.default,
        AppConfig.layer,
        ProductServiceLive.layer,
        PostgresProductRepository.layer,
        Quill.Postgres.fromNamingStrategy(SnakeCase),
        Quill.DataSource.fromPrefix("database")
      )