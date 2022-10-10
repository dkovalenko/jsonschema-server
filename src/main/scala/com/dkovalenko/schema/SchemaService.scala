package com.dkovalenko.schema

import com.dkovalenko.common.ApplicationError
import zio._

trait SchemaService {
  def getSchema(key: String): ZIO[Any, ApplicationError, String]

  def updateSchema(key: String, value: String): ZIO[Any, ApplicationError, Boolean]
}

class SchemaServiceLive(repo: SchemaRepository) extends SchemaService {
  def getSchema(key: String): ZIO[Any, ApplicationError, String] = {
    repo
      .getByIdAsString(key)
      .catchAll(failure => ZIO.fail(ApplicationError.GeneralThrowable(failure)))
      .flatMap { schemaOpt => schemaOpt match {
          case Some(schema) => ZIO.succeed(schema)
          case None => ZIO.fail(ApplicationError.DBEntityNotFound)
        }
      }
  }

  def updateSchema(key: String, value: String): ZIO[Any, ApplicationError, Boolean] = {
    repo
      .setById(key, value)
      .catchAll(failure => ZIO.fail(ApplicationError.GeneralThrowable(failure)))
  }
}
