package database

import cats.effect.IO
import doobie.Transactor
import doobie.ConnectionIO

type Connection[A] = ConnectionIO[A]

type Db = Transactor[IO]