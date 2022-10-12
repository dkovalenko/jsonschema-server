package com.dkovalenko.http

import sttp.tapir._

import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.ztapir.ZServerEndpoint
import zio._
import com.dkovalenko.schema.SchemaRepositoryRedis
import com.dkovalenko.schema.SchemaServiceLive
import sttp.model.StatusCode
// import ApiResultSuccess._
import ApiResultError._

object SchemaAPI {
  case class SchemaID(id: String) extends AnyVal

  val repo    = new SchemaRepositoryRedis("localhost", 6379)
  val service = new SchemaServiceLive(repo)

  val baseEndpoint = endpoint.errorOut(
    oneOf[ApiResultError](
      oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[ApiResultError.NotFound].description("not found"))),
      oneOfVariant(statusCode(StatusCode.BadRequest).and(jsonBody[ApiResultError.Error].description("bad request"))),
      oneOfVariant(statusCode(StatusCode.InternalServerError).and(jsonBody[ApiResultError.ServerError].description("internal server error"))),
      oneOfDefaultVariant(jsonBody[ApiResultError.Error].description("generic error"))
    )
  )

  // ====== get Schema
  val getSchema: PublicEndpoint[SchemaID, ApiResultError, String, Any] = baseEndpoint
    .get
    .in("schema")
    .in(path[SchemaID]("id"))
    .out(stringBody)

  val getSchemaServerEndpoint: ZServerEndpoint[Any, Any] = getSchema.serverLogic { schemaId =>

    val result: Task[Either[ApiResultError, String]] = service
      .getSchema(schemaId.id)
      .mapError(error => ApiResultError.fromApplicationError(error, "getSchema", schemaId.id))
      .either
    result
  }

  // ====== update Schema
  val updateSchema: PublicEndpoint[(SchemaID, String), ApiResultError, ApiResultSuccess, Any] = baseEndpoint
    .post
    .in("schema")
    .in(path[SchemaID]("id").and(stringJsonBody))
    .out(
      oneOf[ApiResultSuccess](
        oneOfVariant(statusCode(StatusCode.Created).and(jsonBody[ApiResultSuccess.Created].description("created"))),
        oneOfDefaultVariant(jsonBody[ApiResultSuccess].description("default"))
      )
    )

  val updateSchemaServerEndpoint: ZServerEndpoint[Any, Any] = updateSchema
    .serverLogic {
      case (schemaId, schema) =>
        val result: Task[Either[ApiResultError, ApiResultSuccess]] = service
          .updateSchema(schemaId.id, schema)
          .mapError(error => ApiResultError.fromThrowable(error, "uploadSchema", schemaId.id))
          .map(success => ApiResultSuccess.Created("uploadSchema", schemaId.id))
          .either

        result
    }

  // ====== validate Schema
  val validateDoc: PublicEndpoint[(SchemaID, String), ApiResultError, ApiResultSuccess, Any] = baseEndpoint
    .post
    .in("validate")
    .in(path[SchemaID]("id").and(stringJsonBody))
    .out(jsonBody[ApiResultSuccess])

  val validateDocServerEndpoint: ZServerEndpoint[Any, Any] = validateDoc
      .serverLogic { case (schemaId, jsonDoc) =>
        val result = service
          .validateDocument(schemaId.id, jsonDoc)
          .mapError(error => ApiResultError.fromThrowable(error, "validateDocument", schemaId.id))
          .map(success => ApiResultSuccess.Success("validateDocument", schemaId.id))
          .either
        result
      }

  val schemaApiEndpoints: List[ZServerEndpoint[Any, Any]] =
    List(getSchemaServerEndpoint, updateSchemaServerEndpoint, validateDocServerEndpoint)

}
