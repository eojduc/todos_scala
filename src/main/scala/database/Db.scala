package database

import cats.effect.IO
import doobie.Transactor
import doobie.ConnectionIO
import doobie.implicits.{toConnectionIOOps}

type Connection[A] = ConnectionIO[A]

type Db = Transactor[IO]
object Db:
  val db = Transactor.fromDriverManager[IO](
    driver = "org.postgresql.Driver",
    url = sys.env.getOrElse("DATABASE_URL", "jdbc:postgresql:todos"),
    user = sys.env.getOrElse("DATABASE_USER", "postgres"),
    password = sys.env.getOrElse("DATABASE_PASSWORD", "password"),
    logHandler = None
  )
  extension [A](connection: Connection[A])
    def use(db: Db): IO[A] = connection.transact(db)
  extension [A](ioConnection: IO[Connection[A]])
    def use(db: Db): IO[A] = ioConnection.flatMap(_.use(db))