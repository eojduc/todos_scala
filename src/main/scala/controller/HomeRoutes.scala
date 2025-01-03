package controller

import model.*

case object HomeRoutes extends cask.Routes:
  @cask.get("/")
  def home(request: cask.Request) =
    request.getUser match
      case None => cask.Response.seeOther("/login")
      case Some(user) =>
        val todos = Todos.findAll()
        val quote = getQuote()
        view.homePage(user, todos, quote).toResponse


  @cask.get("/logout")
  def logout() =
    cask.Response.seeOther("/login")
      .removeCookies("userType", "username", "password")

  @cask.postForm("/todo")
  def addTodo(title: cask.FormValue, request: cask.Request) =
    request.getUser match
      case Some(user: NormalUser) =>
        Todos.insertTodo(title.value, false, user.id)
        val myTodos = Todos.findAll().filter(_.ownerId == user.id)
        view.todoList(myTodos).toResponse
      case _ => cask.Response("Not found", statusCode = 404)

  @cask.post("/todo/:id/toggle")
  def toggleTodo(id: Int) =
    Todos.find(id) match
      case None => cask.Response("Not found", statusCode = 404)
      case Some(todo) =>
        val toggled = todo.toggle
        Todos.updateTodo(toggled)
        view.todoCard(toggled).toResponse
  initialize()
