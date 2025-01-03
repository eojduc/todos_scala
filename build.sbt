scalaVersion := "3.6.2"

val http4sVersion = "0.23.30"
lazy val doobieVersion = "1.0.0-RC4"
val htmxVersion = "0.a2.1"

envVars := Map(
    "DATABASE_URL" -> "jdbc:postgresql:todos",
    "DATABASE_USER" -> "postgres",
    "DATABASE_PASSWORD" -> "password",
    "PORT" -> "3001",
    "HOST" -> "0.0.0.0"
)
Compile / run / fork := true
libraryDependencies ++= Seq(
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-ember-server" % http4sVersion,
    "org.http4s" %% "http4s-ember-client" % http4sVersion,
    "org.http4s" %% "http4s-circe" % http4sVersion,
    "com.softwaremill.sttp.client3" %% "core" % "3.10.2",
    "com.softwaremill.sttp.client3" %% "circe" % "3.10.2",

    "org.tpolecat" %% "doobie-core"     % doobieVersion,
    "org.tpolecat" %% "doobie-postgres" % doobieVersion,


    "io.circe" %% "circe-generic" % "0.14.8",
    "com.lihaoyi" %% "cask" % "0.9.5",

    "com.lihaoyi" %% "scalatags" % "0.13.1",
    "com.lihaoyi" %% "scalasql" % "0.1.14"
)
