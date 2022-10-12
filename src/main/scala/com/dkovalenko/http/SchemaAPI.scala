package com.dkovalenko.http

import sttp.tapir._

import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.ztapir.ZServerEndpoint
import zio._
import sttp.model.StatusCode
import ApiResultError._
import com.dkovalenko.schema.SchemaService

trait SchemaAPI {

  def getSchemaAPIEndpoints(): List[ZServerEndpoint[Any, Any]]

  def getSchemaServerEndpoint(): ZServerEndpoint[Any, Any]

  def updateSchemaServerEndpoint(): ZServerEndpoint[Any, Any]

  def validateDocServerEndpoint(): ZServerEndpoint[Any, Any]

}

object SchemaAPI {

  case class SchemaID(id: String) extends AnyVal

  val GET_SCHEMA = "getSchema"
  val UPLOAD_SCHEMA = "uploadSchema"
  val VALIDATE_DOC = "validateDocument"

  def getSchemaAPIEndpoints(): ZIO[SchemaAPI, Nothing, List[ZServerEndpoint[Any, Any]]] =
    ZIO.serviceWith[SchemaAPI](_.getSchemaAPIEndpoints())

  def getSchemaServerEndpoint(): ZIO[SchemaAPI,Nothing,ZServerEndpoint[Any,Any]] = 
    ZIO.serviceWith[SchemaAPI](_.getSchemaServerEndpoint())

  def updateSchemaServerEndpoint(): ZIO[SchemaAPI,Nothing,ZServerEndpoint[Any,Any]] = 
    ZIO.serviceWith[SchemaAPI](_.updateSchemaServerEndpoint())

  def validateDocServerEndpoint(): ZIO[SchemaAPI,Nothing,ZServerEndpoint[Any,Any]] = 
    ZIO.serviceWith[SchemaAPI](_.validateDocServerEndpoint())
}

case class SchemaAPITapir(service: SchemaService) extends SchemaAPI {
  import SchemaAPI._

  val baseEndpoint = endpoint.errorOut(
    oneOf[ApiResultError](
      oneOfVariant(
        statusCode(StatusCode.NotFound).and(jsonBody[ApiResultError.NotFound].description("not found"))
      ),
      oneOfVariant(
        statusCode(StatusCode.BadRequest).and(jsonBody[ApiResultError.Error].description("bad request"))
      ),
      oneOfVariant(
        statusCode(StatusCode.InternalServerError)
          .and(jsonBody[ApiResultError.ServerError].description("internal server error"))
      ),
      oneOfDefaultVariant(jsonBody[ApiResultError.Error].description("generic error"))
    )
  )

  // ====== get Schema
  val getSchema: PublicEndpoint[SchemaID, ApiResultError, String, Any] = baseEndpoint.get
    .in("schema")
    .in(path[SchemaID]("id"))
    .out(stringBody)

  def getSchemaServerEndpoint(): ZServerEndpoint[Any, Any] = getSchema.serverLogic { schemaId =>
    val result: Task[Either[ApiResultError, String]] = service
      .getSchema(schemaId.id)
      .mapError(error => ApiResultError.fromApplicationError(error, GET_SCHEMA, schemaId.id))
      .either
    result
  }

  // ====== update Schema
  val updateSchema: PublicEndpoint[(SchemaID, String), ApiResultError, ApiResultSuccess, Any] = baseEndpoint.post
    .in("schema")
    .in(path[SchemaID]("id").and(stringJsonBody))
    .out(
      oneOf[ApiResultSuccess](
        oneOfVariant(
          statusCode(StatusCode.Created).and(jsonBody[ApiResultSuccess.Created].description("created"))
        ),
        oneOfDefaultVariant(jsonBody[ApiResultSuccess].description("default"))
      )
    )

  def updateSchemaServerEndpoint(): ZServerEndpoint[Any, Any] = updateSchema
    .serverLogic {
      case (schemaId, schema) =>
        val result: Task[Either[ApiResultError, ApiResultSuccess]] = service
          .updateSchema(schemaId.id, schema)
          .mapError(error => ApiResultError.fromThrowable(error, UPLOAD_SCHEMA, schemaId.id))
          .map(success => ApiResultSuccess.Created(UPLOAD_SCHEMA, schemaId.id))
          .either

        result
    }

  // ====== validate Schema
  val validateDoc: PublicEndpoint[(SchemaID, String), ApiResultError, ApiResultSuccess, Any] = baseEndpoint.post
    .in("validate")
    .in(path[SchemaID]("id").and(stringJsonBody))
    .out(jsonBody[ApiResultSuccess])

  def validateDocServerEndpoint(): ZServerEndpoint[Any, Any] = validateDoc
    .serverLogic {
      case (schemaId, jsonDoc) =>
        val result = service
          .validateDocument(schemaId.id, jsonDoc)
          .mapError(error => ApiResultError.fromThrowable(error, VALIDATE_DOC, schemaId.id))
          .map(success => ApiResultSuccess.Success(VALIDATE_DOC, schemaId.id))
          .either
        result
    }

  def getSchemaAPIEndpoints(): List[ZServerEndpoint[Any, Any]] =
    List(getSchemaServerEndpoint(), updateSchemaServerEndpoint(), validateDocServerEndpoint())

}

object SchemaAPITapir {

  val layer: ZLayer[SchemaService, Nothing, SchemaAPI] =
    ZLayer.fromFunction(SchemaAPITapir(_))

}
