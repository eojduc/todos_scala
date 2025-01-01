package model
case class Todo(title: String, complete: Boolean, ownerId: Int, id: Int):
  def toggle: Todo = this.copy(complete = !this.complete)


