package database.simple

import scalasql.*
import scalasql.PostgresDialect.*
import org.postgresql.ds.PGSimpleDataSource

val envVars = Map(
  "DATABASE_URL" -> "jdbc:postgresql:todos",
  "DATABASE_USER" -> "postgres",
  "DATABASE_PASSWORD" -> "password",
  "PORT" -> "3001",
  "HOST" -> "0.0.0.0"
)

case class Todo[T[_]](
  id: T[Int],
  title: T[String],
  complete: T[Boolean],
  ownerId: T[Int]
)

object Todo extends Table[Todo]()

sealed trait User[T[_]]

case class NormalUser[T[_]](
  id: T[Int],
  username: T[String],
  password: T[String]
) extends User[T]

case class AdminUser[T[_]](
  code: T[String]
) extends User[T]

// Example of usage:
object NormalUser extends Table[NormalUser]()
object AdminUser extends Table[AdminUser]()



def insertUser(username: String, password: String) =
  dbClient.transaction(_.run(NormalUser.insert.columns(_.username := username, _.password := password)))
  
def insertTodo(title: String, complete: Boolean, ownerId: Int) =
  dbClient.transaction(_.run(Todo.insert.columns(_.title := title, _.complete := complete, _.ownerId := ownerId)))
  
def allTodos() =
  dbClient.transaction(_.run(Todo.select))
  
def getTodoById(id: Int) =
  dbClient.transaction(_.run(Todo.select.filter(_.id === id)).headOption)
  
def updateTodo(todo: Todo[Sc]) =
  dbClient.transaction(_.run(Todo.update(_.id === todo.id)
    .set(_.title := todo.title, _.complete := todo.complete, _.ownerId := todo.ownerId)))

lazy val dbClient = new DbClient.Connection(
  java.sql.DriverManager.getConnection(
    "jdbc:postgresql:todos",
    "postgres",
    "password"
  ),
  new Config {
    override def tableNameMapper(v: String): String = v + "s"
    override def columnNameMapper(v: String): String = v
  }
)


def getDb =
  lazy val dbClient = new DbClient.Connection(
    java.sql.DriverManager.getConnection(
      "jdbc:postgresql:todos",
      "postgres",
      "password"
    ),
    new Config {
      override def tableNameMapper(v: String): String = v + "s"
      override def columnNameMapper(v: String): String = v
    }
  )
  dbClient.getAutoCommitClientConnection


def printTodos() =
  val x = dbClient.transaction(_.run(Todo.select))
  println(x)
def camelToSnakeCase(name: String): String = {
  name
    .replaceAll("([a-z])([A-Z])", "$1_$2") // Add underscores between lowercase and uppercase letters
    .replaceAll("([A-Z])([A-Z][a-z])", "$1_$2") // Handle acronyms
    .toLowerCase // Convert everything to lowercase
}