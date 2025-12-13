package com.pleasure.config

import zio.*
import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.TypesafeConfigProvider

final case class DatabaseConfig(
    jdbcUrl: String,
    username: String,
    password: String
)

final case class AppConfig(
    database: DatabaseConfig
)

object AppConfig:
  val layer: ZLayer[Any, Config.Error, AppConfig] =
    ZLayer.fromZIO(
      TypesafeConfigProvider.fromResourcePath().load(deriveConfig[AppConfig])
    )
