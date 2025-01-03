scalaVersion := "3.6.2"

envVars := Map(
    "DATABASE_URL" -> "jdbc:postgresql:todos",
    "DATABASE_USER" -> "postgres",
    "DATABASE_PASSWORD" -> "password",
    "PORT" -> "3001",
    "HOST" -> "0.0.0.0"
)
Compile / run / fork := true
libraryDependencies ++= Seq(
    "org.postgresql" % "postgresql" % "42.6.0",
    "com.lihaoyi" %% "upickle" % "4.0.2",
    "com.lihaoyi" %% "requests" % "0.9.0",
    "com.lihaoyi" %% "cask" % "0.9.5",
    "com.lihaoyi" %% "scalatags" % "0.13.1",
    "com.lihaoyi" %% "scalasql" % "0.1.14"
)
