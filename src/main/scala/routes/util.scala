package routes

import database.simple.*
import scalasql.Sc
import scalasql.dialects.PostgresDialect.TableOpsConv


def getUser(request: cask.Request): Option[User[Sc]] =
  request.cookies.get("userType").map(_.value) match
    case Some("normal") =>
      for
        username <- request.cookies.get("username").map(_.value)
        password <- request.cookies.get("password").map(_.value)
        user <- dbClient.transaction(_.run(NormalUser.select))
          .find(_.username == username)
          .filter(_.password == password)
      yield user
    case Some("admin") =>
      for
        code <- request.cookies.get("code").map(_.value)
        user <- dbClient.transaction(_.run(AdminUser.select))
          .find(_.code == code)
      yield user
    case _ => None

 
