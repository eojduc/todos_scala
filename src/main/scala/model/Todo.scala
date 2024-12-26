package model
case class Todo(title: String, complete: Boolean, ownerId: Int, id: Int)

object Todo:
  extension (todo: Todo)
    def toggle: Todo = todo.copy(complete = !todo.complete)


