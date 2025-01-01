package database

import doobie.implicits.toSqlInterpolator
import model.Todo
import util.*
object Todos:

  def findAll: Connection[List[Todo]] =
    sql"select title, complete, ownerId, id from todos order by id"
      .query[Todo]
      .to[List]
      |> Connection.fromConnectionIO
    
  def findByOwner(ownerId: Int): Connection[List[Todo]] =
    sql"select title, complete, ownerId, id from todos where ownerId = $ownerId order by id"
      .query[Todo]
      .to[List]
      |> Connection.fromConnectionIO


  def delete(id: Int): Connection[Unit] =
    sql"delete from todos where id = $id"
    .update
    .run
    .map(_ => ())
    |> Connection.fromConnectionIO

  def find(id: Int): Connection[Option[Todo]] =
    sql"select title, complete, ownerId, id from todos where id = $id"
      .query[Todo]
      .option
      |> Connection.fromConnectionIO

  def create(title: String, complete: Boolean, ownerId: Int): Connection[Todo] =
    sql"insert into todos (title, complete, ownerId) values ($title, $complete, $ownerId) returning id"
      .query[Int]
      .unique
      .map(id => Todo(title, complete, ownerId, id))
      |> Connection.fromConnectionIO


  def update(todo: Todo): Connection[Unit] =
    val Todo(title, complete, ownerId, id) = todo
    sql"update todos set title = $title, complete = $complete, ownerId = $ownerId where id = $id"
      .update
      .run
      .map(_ => ())
      |> Connection.fromConnectionIO

