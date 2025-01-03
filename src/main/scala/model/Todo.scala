package model

import scalasql.PostgresDialect.*

case class Todos[T[_]]
(
  id: T[Int],
  title: T[String],
  complete: T[Boolean],
  ownerId: T[Int]
)

object Todos extends scalasql.Table[Todos]():
  def insertTodo(title: String, complete: Boolean, ownerId: Int) =
    db.run(Todos.insert.columns(_.title := title, _.complete := complete, _.ownerId := ownerId))

  def findAll() =
    db.run(Todos.select.sortBy(_.id))

  def updateTodo(todo: Todo) =
    db.run(Todos.update(_.id === todo.id).set(_.title := todo.title, _.complete := todo.complete, _.ownerId := todo.ownerId))

  def find(id: Int) =
    db.run(Todos.select.filter(_.id === id)).headOption


type Todo = Todos[scalasql.Sc]

extension (todo: Todo)
  def toggle = todo.copy(complete = !todo.complete)

