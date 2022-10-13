package com.dkovalenko

import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import zio.Scope
import zio.Task
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import zio.interop.catz._

object Main extends ZIOAppDefault {

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {

    val port = sys.env.get("http.port").map(_.toInt).getOrElse(8080)

    for {
      executor  <- ZIO.executor
      endpoints <- http.ServerAPI.getAllEndpoints()
      routes = ZHttp4sServerInterpreter().from(endpoints).toRoutes
      _ <- BlazeServerBuilder[Task]
        .withExecutionContext(executor.asExecutionContext)
        .bindHttp(port, "localhost")
        .withHttpApp(Router("/" -> (routes)).orNotFound)
        .resource
        .evalTap { server =>
          ZIO.succeedBlocking {
            println(s"Server started. Go to http://localhost:${server.address.getPort}/docs to open SwaggerUI.")
          }
        }
        .useForever
        .unit
    } yield ()
  }

}
