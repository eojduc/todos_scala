package db

import scalasql.PostgresDialect.*

def run[Q, R](a: Q)(using scalasql.Queryable[Q, R]): R = dbClient.transaction(_.run(a))

private lazy val dbClient = new scalasql.DbClient.Connection(
  java.sql.DriverManager.getConnection(
    sys.env.getOrElse("DATABASE_URL", "jdbc:postgresql:todos"),
    sys.env.getOrElse("DATABASE_USER", "postgres"),
    sys.env.getOrElse("DATABASE_PASSWORD", "password")
  )
)
