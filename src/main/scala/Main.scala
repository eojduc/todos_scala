import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.{Port, ipv4, port, IpAddress, Host}
import org.http4s.{HttpRoutes, UrlForm}
import org.http4s.dsl.io.*
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.client.Client
import org.http4s.server.middleware.ErrorAction
import view.{AdminLoginPage, HomePage, LoginPage, RegisterPage}
import database.{Todos, Users}
import database.{Db, Connection}
import database.Db.*
import model.{Response, Request}

import doobie.implicits.{toConnectionIOOps, toSqlInterpolator}
import cats.syntax.applicative.*
// move routing to view classes
def routes(db: Db, client: Client[IO]) = HttpRoutes.of[IO]:
  case req @ GET -> Root => HomePage.get(req, client).use(db)
  case GET -> Root / "logout" => HomePage.logout.pure[IO]
  case req @ POST -> Root / "todo" => HomePage.post(req).use(db)
  case POST -> Root / "todo" / IntVar(id) / "toggle" => HomePage.toggle(id).use(db)
  case req @ GET -> Root / "login" => LoginPage.get(req).use(db)
  case req @ POST -> Root / "login" => LoginPage.post(req).use(db)
  case req @ GET -> Root / "register" => RegisterPage.get(req).pure[IO]
  case req @ POST -> Root / "register" => RegisterPage.post(req).use(db)
  case req @ GET -> Root / "admin-login" => AdminLoginPage.get(req).pure[IO]
  case req @ POST -> Root / "admin-login" => AdminLoginPage.post(req).use(db)

val host = sys.env.get("HOST")
  .flatMap(IpAddress.fromString)
  .getOrElse(ipv4"0.0.0.0")
val port = sys.env.get("PORT")
  .flatMap(_.toIntOption)
  .flatMap(Port.fromInt)
  .getOrElse(port"8080")
object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] = (for
    client <- EmberClientBuilder.default[IO].build
    app = ErrorAction.httpApp[IO](
      routes(Db.db, client).orNotFound,
      (req, thr) => IO.println("Oops: " ++ thr.toString)
    )
    _ <- EmberServerBuilder
      .default[IO]
      .withHost(host)
      .withPort(port)
      .withHttpApp(app)
      .build
  yield ()).useForever
