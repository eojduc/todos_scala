package model
import cats.effect.IO
import doobie.implicits.{toConnectionIOOps, toSqlInterpolator}
import org.http4s.UrlForm
import scalatags.Text.all.*
// case classes are like records in java, come with a bunch of useful methods like equals, hashcode, toString
case class Todo(title: String, complete: Boolean, ownerId: Int, id: Int)
extension (todo: Todo)
  def toggle: Todo = todo.copy(complete = !todo.complete)


/*
given is sort of like dependency injection in java, it provides an instance of a
given type to the whole project. In this case, it provides an instance of EntityDecoder and EntityEncoder
for the model.Todo class, which is used by http4s to encode and decode json
we use circe to give us the instances of EntityDecoder and EntityEncoder.
type classes are similar to interfaces in java, but they are more powerful because they can be
added to existing classes without modifying them, and they can be implemented for multiple classes
*/

// db operations use doobie and postgres, uses IO for async operations, similar to Promise in js / Future in java
// IO is technically a monad, which is just a monoid in the category of endofunctors
object Todo:
  case class Create(title: String, complete: Boolean, ownerId: Int)

  def findAll : IO[List[Todo]] = sql"select title, complete, ownerId, id from todos order by id"
    .query[Todo]
    .to[List]
    .transact(Db.transactor)

  def delete (id: Int) : IO[Unit] = sql"delete from todos where id = $id"
    .update
    .run
    .transact(Db.transactor)
    .void
  def find (id: Int) : IO[Option[Todo]] = sql"select title, complete, ownerId, id from todos where id = $id"
    .query[Todo]
    .option
    .transact(Db.transactor)

  def create(title: String, complete: Boolean, ownerId: Int): IO[Todo] =
    sql"insert into todos (title, complete, ownerId) values ($title, $complete, $ownerId) returning id"
    .query[Int]
    .unique
    .transact(Db.transactor)
    .map(id => Todo(title, complete, ownerId, id))

  def update(todo: Todo): IO[Unit] =
    val Todo(title, complete, ownerId, id) = todo
    sql"update todos set title = $title, complete = $complete, ownerId = $ownerId where id = $id"
    .update
    .run
    .transact(Db.transactor)
    .void
