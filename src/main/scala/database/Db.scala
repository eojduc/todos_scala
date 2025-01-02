package database

import cats.data.ReaderT
import cats.effect.IO
import doobie.{ConnectionIO, Transactor}
import doobie.implicits.*

// Connection[A] is a value that can give you an A, but it needs a Db to do so. 
// by calling .run(db) on a Connection[A] you get an IO[A]

// useful for dependency injection
type Db = Transactor[IO]

type Connection[A] = ReaderT[IO, Db, A]

extension [A](io: IO[A])
  def toConnection: Connection[A] = ReaderT.liftF(io)

extension [A](connIO: ConnectionIO[A])
  def toConnection: Connection[A] =
    for db <- Connection.getDb
        conn <- ReaderT.liftF(connIO.transact(db))
    yield conn

object Connection:

  def getDb: Connection[Db] =
    ReaderT.ask[IO, Db]


