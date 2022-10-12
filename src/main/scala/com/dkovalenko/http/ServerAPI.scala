package com.dkovalenko.http

import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.ZServerEndpoint
import com.dkovalenko.http.SchemaAPI
import zio._
import com.dkovalenko.schema.SchemaServiceLive
import com.dkovalenko.schema.SchemaRepositoryRedis
import com.dkovalenko.config.RedisConfig

//Wire all dependencies and do DI
object ServerAPI {

  def getDocEndpoints(endpoints: List[ZServerEndpoint[Any, Any]]): List[ZServerEndpoint[Any, Any]] = SwaggerInterpreter()
    .fromServerEndpoints[Task](endpoints, "jsonschema-server", "1.0.0")

  def getAllEndpoints() = {
    (for {
      schemaApiEndpoints <- SchemaAPI.getSchemaAPIEndpoints()
      docEndpoints = getDocEndpoints(schemaApiEndpoints)
    } yield schemaApiEndpoints ++ docEndpoints)
    .provide(
      SchemaAPITapir.layer,
      SchemaServiceLive.layer,
      SchemaRepositoryRedis.layer,
      RedisConfig.localhost
    )
  }
}