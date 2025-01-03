object MinimalRoutesMain extends cask.Main:
  val allRoutes = Seq(controller.AdminLoginRoutes, controller.LoginRoutes, controller.RegisterRoutes, controller.HomeRoutes)

  override def port: Int = sys.env.get("PORT").fold(3000)(_.toInt)

  override def host: String = sys.env.getOrElse("HOST", "localhost")