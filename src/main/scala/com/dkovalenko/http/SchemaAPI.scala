package com.dkovalenko.http

import sttp.tapir._

// import io.circe.generic.auto._
// import sttp.tapir.generic.auto._
// import sttp.tapir.json.circe._
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.ZServerEndpoint
import zio.Task
import zio.ZIO

object SchemaAPI {
  case class SchemaID(id: String) extends AnyVal
  
  val getSchema: PublicEndpoint[SchemaID, Unit, String, Any] = endpoint.get
    .in("schema")
    .in(path[SchemaID]("id"))
    .out(stringBody)
  val getSchemaServerEndpoint: ZServerEndpoint[Any, Any] = getSchema.serverLogicSuccess(schemaId => ZIO.succeed(s"GET getSchema ${schemaId}"))

  val updateSchema: PublicEndpoint[SchemaID, Unit, String, Any] = endpoint.post
    .in("schema")
    .in(path[SchemaID]("id"))
    .out(stringBody)
  val updateSchemaServerEndpoint: ZServerEndpoint[Any, Any] = updateSchema.serverLogicSuccess(schemaId => ZIO.succeed(s"POST uploadSchema ${schemaId}"))

  val validateSchema: PublicEndpoint[SchemaID, Unit, String, Any] = endpoint.post
    .in("validate")
    .in(path[SchemaID]("id"))
    .out(stringBody)
  val validateSchemaServerEndpoint: ZServerEndpoint[Any, Any] = validateSchema.serverLogicSuccess(schemaId => ZIO.succeed(s"POST validateDocument ${schemaId}"))


  val apiEndpoints: List[ZServerEndpoint[Any, Any]] = List(getSchemaServerEndpoint, updateSchemaServerEndpoint, validateSchemaServerEndpoint)

  val docEndpoints: List[ZServerEndpoint[Any, Any]] = SwaggerInterpreter()
    .fromServerEndpoints[Task](apiEndpoints, "jsonschema-server", "1.0.0")

  val endpoints: List[ZServerEndpoint[Any, Any]] = apiEndpoints ++ docEndpoints
}
