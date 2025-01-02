package model

// enum is a sealed trait with a fixed number of subclasses, like a sealed class in java
enum User(val id: Int):
  case Normal(username: String, password: String, override val id: Int) extends User(id)
  case Admin(code: String) extends User(0)
  
  
  

extension (user: User)
  def title = user match
    case User.Normal(name, _, _) => name
    case User.Admin(_) => "admin"
extension (users: List[User])
  def findAdmin(code: String): Option[User.Admin] =
    users
      .collect({ case u: User.Admin => u })
      .find(u => u.code == code)
  def findUser(username: String, password: String): Option[User.Normal] =
    users
      .collect({ case u: User.Normal => u })
      .find(u => u.username == username && u.password == password)