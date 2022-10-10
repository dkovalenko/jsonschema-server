package com.dkovalenko.http

import sttp.tapir._

import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.ztapir.ZServerEndpoint
import zio._
import ApiResult._
import com.dkovalenko.schema.SchemaRepositoryRedis
import com.dkovalenko.schema.SchemaServiceLive

object SchemaAPI {
  case class SchemaID(id: String) extends AnyVal

  val repo = new SchemaRepositoryRedis("localhost", 6379)
  val service = new SchemaServiceLive(repo)
  
  //====== get Schema
  val getSchema: PublicEndpoint[SchemaID, ApiResult, String, Any] = endpoint.get
    .in("schema")
    .in(path[SchemaID]("id"))
    .out(stringBody)
    .errorOut(jsonBody[ApiResult])

  val getSchemaServerEndpoint: ZServerEndpoint[Any, Any] = getSchema.serverLogic { schemaId => 
    
    val result: Task[Either[ApiResult, String]] = service.getSchema(schemaId.id)
      .mapError(error => ApiResult.fromThrowable(error, "getSchema", schemaId.id))
      .either
    result
  }

  //====== update Schema
  val updateSchema: PublicEndpoint[(SchemaID, String), ApiResult, ApiResult, Any] = endpoint.post
    .in("schema")
    .in(path[SchemaID]("id").and(stringJsonBody))
    .out(jsonBody[ApiResult])
    .errorOut(jsonBody[ApiResult])

  val updateSchemaServerEndpoint: ZServerEndpoint[Any, Any] = updateSchema
    .serverLogic { case (schemaId, schema) => 
      val result: Task[Either[ApiResult, ApiResult]] = service
        .updateSchema(schemaId.id, schema)
        .mapError(error => ApiResult.fromThrowable(error, "uploadSchema", schemaId.id))
        .map(success => ApiResult.Success("uploadSchema", schemaId.id))
        .either
      
      result
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
