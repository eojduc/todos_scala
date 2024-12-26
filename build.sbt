scalaVersion := "3.3.4"

val http4sVersion = "0.23.30"
lazy val doobieVersion = "1.0.0-RC4"
val htmxVersion = "0.a2.1"

envVars := Map(
    "DATABASE_URL" -> "jdbc:postgresql:todos",
    "DATABASE_USER" -> "postgres",
    "DATABASE_PASSWORD" -> "password",
    "PORT" -> "3000",
    "HOST" -> "0.0.0.0"
)
Compile / run / fork := true
libraryDependencies ++= Seq(
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-ember-server" % http4sVersion,
    "org.http4s" %% "http4s-ember-client" % http4sVersion,
    "org.http4s" %% "http4s-circe" % http4sVersion,

    "org.tpolecat" %% "doobie-core"     % doobieVersion,
    "org.tpolecat" %% "doobie-postgres" % doobieVersion,


    "io.circe" %% "circe-generic" % "0.14.8",

    "com.lihaoyi" %% "scalatags" % "0.13.1",
)
