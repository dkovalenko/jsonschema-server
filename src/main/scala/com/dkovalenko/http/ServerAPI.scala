package com.dkovalenko.http

import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.ZServerEndpoint
import zio.Task

object ServerAPI {
  val docEndpoints: List[ZServerEndpoint[Any, Any]] = SwaggerInterpreter()
    .fromServerEndpoints[Task](SchemaAPI.schemaApiEndpoints, "jsonschema-server", "1.0.0")

  val allEndpoints: List[ZServerEndpoint[Any, Any]] = SchemaAPI.schemaApiEndpoints ++ docEndpoints
}