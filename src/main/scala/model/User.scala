package model
import scalasql.PostgresDialect.*

case class NormalUsers[T[_]]
(
  id: T[Int],
  username: T[String],
  password: T[String]
)

case class AdminUsers[T[_]]
(
  code: T[String]
)

object NormalUsers extends scalasql.Table[NormalUsers]()

object AdminUsers extends scalasql.Table[AdminUsers]()


object Users:
  def insertUser(username: String, password: String) =
    db.run(NormalUsers.insert.columns(_.username := username, _.password := password))

  def findUser(username: String, password: String) =
    db.run(NormalUsers.select.filter(u => u.username === username && u.password === password)).headOption

  def findUserByUsername(username: String) =
    db.run(NormalUsers.select.filter(_.username === username)).headOption

  def getAdminByCode(code: String) =
    db.run(AdminUsers.select.filter(_.code === code)).headOption

type AdminUser = AdminUsers[scalasql.Sc]
type NormalUser = NormalUsers[scalasql.Sc]

type User = AdminUser | NormalUser
extension (user: User)
  def title: String = user match
    case AdminUser(_) => "admin"
    case NormalUser(_, username, _) => username
