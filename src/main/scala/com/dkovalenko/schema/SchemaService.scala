package com.dkovalenko.schema

import com.dkovalenko.common.ApplicationError
import zio._
import com.dkovalenko.schema.validator.Validator
import io.circe.parser._

trait SchemaService {
  def getSchema(key: String): ZIO[Any, ApplicationError, String]

  def updateSchema(key: String, value: String): ZIO[Any, ApplicationError, Boolean]

}

class SchemaServiceLive(repo: SchemaRepository) extends SchemaService {
  def getSchema(key: String): ZIO[Any, ApplicationError, String] =
    repo
      .getByIdAsString(key)
      .catchAll(failure => ZIO.fail(ApplicationError.DBCommunicationError(failure.getMessage())))
      .flatMap { schemaOpt =>
        schemaOpt match {
          case Some(schema) => ZIO.succeed(schema)
          case None         => ZIO.fail(ApplicationError.DBEntityNotFound)
        }
      }

  def updateSchema(key: String, value: String): ZIO[Any, ApplicationError, Boolean] =
    (for {
      isValid <- ZIO.fromTry(Validator.tryParseSchema(value))
        .catchAll(failure => ZIO.fail(ApplicationError.GeneralThrowable(failure)))
      result  <- repo.setById(key, value)
        .catchAll(failure => ZIO.fail(ApplicationError.DBCommunicationError(failure.getMessage())))
    } yield result)

  def validateDocument(schemaKey: String, json: String): ZIO[Any, ApplicationError, Boolean] =
    (for {
      schema        <- getSchema(schemaKey)
      sanitizedJson <- ZIO.fromEither(parse(json).map(_.deepDropNullValues))
      isValid <- ZIO.fromTry(Validator.validate(sanitizedJson.noSpaces, schema))
    } yield isValid)
      .catchAll(failure => ZIO.fail(ApplicationError.GeneralThrowable(failure)))

}
