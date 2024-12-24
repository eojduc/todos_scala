package model


import cats.effect.IO
import doobie.implicits.{toConnectionIOOps, toSqlInterpolator}
import scalatags.Text.all.*

// enum is a sealed trait with a fixed number of subclasses, like a sealed class in java
enum User(password: String):
  case Normal(username: String, password: String, id: Int) extends User(password)
  case Admin(code: String) extends User(code)



extension (user: User)
  def title = user match
    case User.Normal(name, _, _) => name
    case _ => "admin"

// columns in db MUST be in SAME ORDER as in case class!!! tricky bug
object User:


  def findAll: IO[List[User]] =
    val list =
      for normal <- sql"select username, password, id from normal_users".query[User.Normal].to[List]
        admin <- sql"select code from admin_users".query[User.Admin].to[List]
      yield normal ++ admin
    list.transact(Db.transactor)
    
  def find(id: Int): IO[Option[User]] =
    sql"select username, password, id from normal_users where id = $id".query[User.Normal]
      .option
      .transact(Db.transactor)

  enum Insert:
    case Normal(username: String, password: String)
    case Admin(code: String)
  def insert(user: Insert): IO[Unit] = user match
    case Insert.Normal(username, password) =>
      sql"insert into normal_users (username, password) values ($username, $password)"
        .update
        .run
        .transact(Db.transactor)
        .void
    case Insert.Admin(code) =>
      sql"insert into admin_users (code) values ($code)"
        .update
        .run
        .transact(Db.transactor)
        .void
      
      
    def delete(id: Int): IO[Unit] =
      sql"delete from normal_users where id = $id".update.run.transact(Db.transactor).void
      
    def update(user: User): IO[Unit] = user match
      case User.Normal(username, password, id) =>
        sql"update normal_users set username = $username, password = $password where id = $id"
          .update
          .run
          .transact(Db.transactor)
          .void
      case User.Admin(code) => sql"update admin_users set code = $code".update.run.transact(Db.transactor).void
