import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.{Port, ipv4, port, Ipv4Address, IpAddress, Host}
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.headers.`Content-Type`
import org.http4s.server.middleware.ErrorAction
import org.typelevel.log4cats.slf4j.Slf4jLogger
import view.{AdminLoginPage, HomePage, LoginPage, RegisterPage}


type Request = org.http4s.Request[IO]
type Response = org.http4s.Response[IO]

/*
each route returns a
 */
val routes = HttpRoutes.of[IO]:
  case req @ GET -> Root => HomePage.get(req)
  case GET -> Root / "logout" => HomePage.logout
  case req @ POST -> Root / "todo" => HomePage.post(req)
  case POST -> Root / "todo" / IntVar(id) / "toggle" => HomePage.toggle(id)
  case req @ GET -> Root / "login" => LoginPage.get(req)
  case req @ POST -> Root / "login"  => LoginPage.post(req)
  case req @ GET -> Root / "register" => RegisterPage.get(req)
  case req @ POST -> Root / "register" => RegisterPage.post(req)
  case req @ GET -> Root / "admin-login" => AdminLoginPage.get(req)
  case req @ POST -> Root / "admin-login" => AdminLoginPage.post(req)


val app = ErrorAction.httpApp[IO](
  routes.orNotFound,
  (req, thr) => IO.println("Oops: " ++ thr.toString ++ " " ++
    thr.getMessage)
)
val host: Host = sys.env.get("HOST")
  .flatMap(IpAddress.fromString)
  .getOrElse(ipv4"0.0.0.0")
val port: Port = sys.env.get("PORT")
  .flatMap(_.toIntOption)
  .flatMap(Port.fromInt)
  .getOrElse(port"8080")
object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    for logger <- Slf4jLogger.create[IO]
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(host)
        .withPort(port)
        .withHttpApp(app)
        .withLogger(logger)
        .build
        .useForever
    yield ExitCode.Success
