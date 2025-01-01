package database

import cats.effect.IO
import doobie.Transactor
import cats.data.ReaderT
import doobie.ConnectionIO
import doobie.implicits.*
import util.*

// Connection[A] is a value that can give you an A, but it needs a Db to do so. 
// by calling .run(db) on a Connection[A] you get an IO[A]

// useful for dependency injection
type Db = Transactor[IO]

type Connection[A] = ReaderT[IO, Db, A]
object Connection:

  def pure[A](a: A): Connection[A] = ReaderT.pure(a)

  def getDb: Connection[Db] =
    ReaderT.ask[IO, Db]
    
  def fromIO[A](io: IO[A]): Connection[A] = ReaderT.liftF(io)

  
  def fromConnectionIO[A](connIO: ConnectionIO[A]): Connection[A] =
    for db <- getDb
      conn <- connIO.transact(db) |> ReaderT.liftF
    yield conn

