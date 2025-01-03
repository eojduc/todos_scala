package view

import model.*
import scalatags.Text.all.*

def homePage(user: User, todos: Seq[Todo], quote: Quote)  =
  layout(
    header(user),
    div(
      `class` := "flex flex-col items-center w-3/5 gap-8 my-4",
      motivationalQuote(quote),
      title,
      user match
        case _: AdminUser => todoList(todos)
        case normal: NormalUser =>
          val myTodos = todos.filter(_.ownerId == normal.id)
          Seq(todoList(myTodos), newTodoForm(normal))
    )
  )


def todoCard(todo: Todo): Frag =
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

def todoList(todos: Seq[Todo]): Frag =
  div(
    `class` := "contents",
    id := "todos",
    for todo <- todos yield todoCard(todo)
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
private def newTodoForm(user: NormalUser): Frag =
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
        attr("hx-on::after-request") := "this.reset()",
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