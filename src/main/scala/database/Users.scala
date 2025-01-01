package database

import doobie.implicits.toSqlInterpolator
import model.User
import util.*

object Users:


  def findAll: Connection[List[User]] =
    (for
      normal <- sql"select username, password, id from normal_users".query[User.Normal].to[List]
      admin <- sql"select code from admin_users".query[User.Admin].to[List]
    yield normal ++ admin)
    |> Connection.fromConnectionIO

  def find(id: Int): Connection[Option[User.Normal]] =
    sql"select username, password, id from normal_users where id = $id"
      .query[User.Normal]
      .option
      |> Connection.fromConnectionIO

  enum Insert:
    case Normal(username: String, password: String)
    case Admin(code: String)


  def insert(user: Insert): Connection[Unit] =
    (user match
      case Insert.Normal(username, password) =>
        sql"insert into normal_users (username, password) values ($username, $password)"
      case Insert.Admin(code) =>
        sql"insert into admin_users (code) values ($code)"
      ).update
      .run
      .map(_ => ())
      |> Connection.fromConnectionIO


  def delete(id: Int): Connection[Unit] =
    sql"delete from normal_users where id = $id".update.run.map(_ => ())
      |> Connection.fromConnectionIO

  def update: User => Connection[Unit] =
    case User.Normal(username, password, id) =>
      sql"update normal_users set username = $username, password = $password where id = $id"
        .update
        .run
        .map(_ => ())
        |> Connection.fromConnectionIO
    case User.Admin(code) =>
      sql"update admin_users set code = $code".update.run.map(_ => ())
        |> Connection.fromConnectionIO



