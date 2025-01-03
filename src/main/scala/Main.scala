import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.{IpAddress, Port, ipv4, port}
import org.http4s.HttpRoutes
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.server.middleware.ErrorAction
import view.{AdminLoginPage, HomePage, LoginPage, RegisterPage}
import org.http4s.server.Router
import doobie.Transactor
import database.*
import scalatags.Text.all.*
import scalatags.Text.all.Frag
import view.*
import database.simple.*
import routes.*

import scalasql.dialects.PostgresDialect.*
import scalasql.*

val host = sys.env.get("HOST")
  .flatMap(IpAddress.fromString)
  .getOrElse(ipv4"0.0.0.0")
val port = sys.env.get("PORT")
  .flatMap(_.toIntOption)
  .flatMap(Port.fromInt)
  .getOrElse(port"8080")

val db = Transactor.fromDriverManager[IO](
  driver = "org.postgresql.Driver",
  url = sys.env.getOrElse("DATABASE_URL", "jdbc:postgresql:todos"),
  user = sys.env.getOrElse("DATABASE_USER", "postgres"),
  password = sys.env.getOrElse("DATABASE_PASSWORD", "password"),
  logHandler = None
)
object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    val resource = for 
      client <- EmberClientBuilder.default[IO].build
      app = ErrorAction.httpRoutes[IO](
        Router(
          "/" -> HomePage.routes(db, client),
          "/login" -> LoginPage.routes(db),
          "/register" -> RegisterPage.routes(db),
          "/admin-login" -> AdminLoginPage.routes(db)
        ),
        (req, thr) => IO.println("Oops: " ++ thr.toString)
      ).orNotFound
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(host)
        .withPort(port)
        .withHttpApp(app)
        .build
    yield ()
    resource.useForever.as(ExitCode.Success)



object MinimalRoutesMain extends cask.Main{
  printTodos()
  val allRoutes = Seq(AdminLoginRoutes(), LoginRoutes(), RegisterRoutes(), HomeRoutes())
}