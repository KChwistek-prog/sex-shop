
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.7.4"

val zioVersion = "2.1.23"

lazy val root = (project in file("."))
  .settings(
    name := "sex-shop",
    idePackagePrefix := Some("com.pleasure"),
    libraryDependencies ++= Seq(
      // ZIO core
      "dev.zio" %% "zio"         % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,

      // HTTP
      "dev.zio" %% "zio-http" % "3.7.0",

      // JSON
      "dev.zio" %% "zio-json" % "0.7.45",

      // Prelude (Newtype, Validation)
      "dev.zio" %% "zio-prelude" % "1.0.0-RC43",

      // Config
      "dev.zio" %% "zio-config"          % "4.0.6",
      "dev.zio" %% "zio-config-typesafe" % "4.0.6",
      "dev.zio" %% "zio-config-magnolia" % "4.0.6",

      // Database (Quill + PostgreSQL)
      "io.getquill"    %% "quill-jdbc-zio" % "4.8.6",
      "org.postgresql"  % "postgresql"     % "42.7.8",

      // Migrations
      "org.flywaydb" % "flyway-core"                % "11.18.0",
      "org.flywaydb" % "flyway-database-postgresql" % "11.18.0",

      // Logging
      "dev.zio"       %% "zio-logging"       % "2.5.2",
      "dev.zio"       %% "zio-logging-slf4j" % "2.5.2",
      "ch.qos.logback" % "logback-classic"   % "1.5.21",

      // Testing
      "dev.zio"      %% "zio-test"                        % zioVersion            % Test,
      "dev.zio"      %% "zio-test-sbt"                    % zioVersion            % Test,
      "dev.zio"      %% "zio-test-magnolia"               % zioVersion            % Test,
      "com.dimafeng" %% "testcontainers-scala-postgresql" % "0.44.0"              % Test,

    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
