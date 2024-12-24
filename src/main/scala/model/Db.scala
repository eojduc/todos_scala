package model

import cats.effect.IO
import doobie.Transactor

val url = sys.env.getOrElse("DATABASE_URL", "jdbc:postgresql:todos")
val user = sys.env.getOrElse("DATABASE_USER", "postgres")
val password = sys.env.getOrElse("DATABASE_PASSWORD", "password")

object Db:
  val transactor = Transactor.fromDriverManager[IO](
    driver = "org.postgresql.Driver",
    url = url,
    user = user,
    password = password
  )