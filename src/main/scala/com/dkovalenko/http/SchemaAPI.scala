package com.dkovalenko.http

import sttp.tapir._

import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.ztapir.ZServerEndpoint
import zio.ZIO
import ApiResult._

object SchemaAPI {
  case class SchemaID(id: String) extends AnyVal
  
  //====== get Schema
  val getSchema: PublicEndpoint[SchemaID, Unit, String, Any] = endpoint.get
    .in("schema")
    .in(path[SchemaID]("id"))
    .out(stringBody)

  val getSchemaServerEndpoint: ZServerEndpoint[Any, Any] = getSchema.serverLogicSuccess(schemaId => ZIO.succeed(s"GET getSchema ${schemaId.id}"))

  //====== update Schema
  val updateSchema: PublicEndpoint[(SchemaID, String), Unit, ApiResult, Any] = endpoint.post
    .in("schema")
    .in(path[SchemaID]("id").and(stringJsonBody))
    .out(jsonBody[ApiResult])

  val updateSchemaServerEndpoint: ZServerEndpoint[Any, Any] = updateSchema
    .serverLogicSuccess { case (schemaId, schema) => 
      ZIO.succeed(
        ApiResult.Success(
          action = "uploadSchema",
          id = schemaId.id
        )
      )
    }

  //====== validate Schema
  val validateDoc: PublicEndpoint[(SchemaID, String), Unit, ApiResult, Any] = endpoint.post
    .in("validate")
    .in(path[SchemaID]("id").and(stringJsonBody))
    .out(jsonBody[ApiResult])

  val validateDocServerEndpoint: ZServerEndpoint[Any, Any] = validateDoc
  .serverLogicSuccess { case (schemaId, jsonDoc) => 
    ZIO.succeed(
      ApiResult.Success(
          action = "validateDocument",
          id = schemaId.id
        )
    ) 
  }


  val schemaApiEndpoints: List[ZServerEndpoint[Any, Any]] = List(getSchemaServerEndpoint, updateSchemaServerEndpoint, validateDocServerEndpoint)
  
}
