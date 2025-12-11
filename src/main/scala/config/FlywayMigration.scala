package com.pleasure.config
import org.flywaydb.core.Flyway
import zio.*

object FlywayMigration:
  def migrate(config: DatabaseConfig): Task[Unit] =
    ZIO.attempt {
      Flyway
        .configure()
        .dataSource(config.url, config.username, config.password)
        .load()
        .migrate()
    }.unit
