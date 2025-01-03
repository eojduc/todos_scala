package view

import cats.effect.IO
import cats.syntax.applicative.*
import database.*
import io.circe.generic.auto.deriveDecoder
import model.*
import org.http4s.circe.jsonOf
import org.http4s.dsl.io.*
import org.http4s.implicits.uri
import org.http4s.{EntityDecoder, HttpRoutes, Status}
import scalatags.Text.all.*



object HomePage:
  def routes(db: Db, client: Client): HttpRoutes[IO] = HttpRoutes.of[IO]:
    case req @ GET -> Root => get(req, client).run(db)
    case GET -> Root / "logout" => logout.pure[IO]
    case req @ POST -> Root / "todo" => post(req).run(db)
    case POST -> Root / "todo" / IntVar(id) / "toggle" => toggle(id).run(db)

  private def logout: Response =
    Response.redirect(uri"/login")
      .removeCookies("userType", "username", "password", "code")

  given EntityDecoder[IO, List[Quote]] = jsonOf[IO, List[Quote]]

  private def get(request: Request, client: Client): Connection[Response] =
    for quotes <- client.expect[List[Quote]](uri"https://zenquotes.io/api/random").toConnection
        users <- Users.findAll
        todos <- Todos.findAll
    yield request.getUser(users) match
      case None => Response.redirect(uri"/login")
        .removeCookies("userType", "username", "password", "code")
      case Some(user) =>
        val quote = Quote.firstOrDefault(quotes)
        page(user, todos, quote).toResponse


  private def toggle(id: Int): Connection[Response] =
    for todoOption <- Todos.find(id)
        response <- todoOption match
          case None => Response(Status.NotFound).pure[Connection]
          case Some(todo) =>
            for _ <- Todos.update(todo.toggle)
              yield todoCard(todo.toggle).toResponse
    yield response


  def post(request: Request): Connection[Response] =
    for form <- request.getForm.toConnection
        users <- Users.findAll
        response <- request.getUser(users) match
          case None => Response
            .redirect(uri"/login".withError("Log in to create a todo."))
            .removeCookies("userType", "username", "password")
            .pure[Connection]
          case Some(user) => form.getFirst("title") match
            case None => Response.badRequest("user not found").pure[Connection]
            case Some(title) =>
              for _ <- Todos.create(title, false, user.id)
                  todos <- Todos.findByOwner(user.id)
              yield
                todoList(todos).toResponse
    yield response


  private def page(user: User, todos: List[Todo], quote: Quote): Frag =
    View.layout(
      header(user),
      div(
        `class` := "flex flex-col items-center w-3/5 gap-8 my-4",
        motivationalQuote(quote),
        title,
        user match
          case _: User.Admin => todoList(todos)
          case normal: User.Normal =>
            val myTodos = todos.filter(_.ownerId == normal.id)
              List(todoList(myTodos), newTodoForm(normal))
      )
    )

  private def motivationalQuote(quote: Quote): Frag =
    div(
      `class` := "card bg-base-200 card-normal w-3/5 my-2",
      div(
        `class` := "card-body",
        h2(`class` := "text-lg", quote.q),
        h3("-" ++ quote.a)
      )
    )

  private def title: Frag = h1(`class` := "text-3xl font-bold", "Todo List")

  private def todoCard(todo: Todo): Frag =
    div(
      `class` := "bg-base-200 card card-normal w-full",
      id := s"todo-${todo.id}",
      div(
        `class` := "card-body",
        h5(`class` := "card-title", todo.title),
        input(
          `class` := "checkbox",
          `type` := "checkbox",
          attr("hx-post") := s"/todo/${todo.id}/toggle",
          attr("hx-trigger") := "change",
          attr("hx-target") := s"#todo-${todo.id}",
          attr("hx-swap") := "outerHTML",
          if todo.complete then checked else ()
        ),
        p(if todo.complete then "Completed" else "Not completed")
      )
    )

  private def todoList(todos: List[Todo]): Frag =
    div(
      `class` := "contents",
      id := "todos",
      for todo <- todos yield todoCard(todo)
    )

  private def newTodoForm(user: User.Normal): Frag =
    div(
      `class` := "card card-normal bg-base-200 w-3/5",
      div(
        `class` := "card-body",
        h2(`class` := "card-title", "Add a new todo"),
        form(
          `class` := "contents",
          attr("hx-post") := s"/todo",
          attr("hx-trigger") := "submit",
          attr("hx-target") := "#todos",
          attr("hx-swap") := "outerHTML",
          input(
            `class` := "input",
            `type` := "text",
            name := "title",
            placeholder := "title"
          ),
          div(
            `class` := "flex justify-end w-full",
            input(
              `type` := "submit",
              `class` := "btn btn-primary",
              value := "Add"
            )
          )
        )
      )

    )

  private def header(user: User): Frag =
    div(
      `class` := "navbar",
      div(
        `class` := "navbar-start",
        h3(`class` := "text-lg", s"Welcome, ${user.title}"),
      ),
      div(
        `class` := "navbar-end",
        a(
          href := "/logout",
          `class` := "p-2 btn btn-secondary",
          "Logout"
        )
      )
    )
