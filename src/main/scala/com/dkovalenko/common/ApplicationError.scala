package com.dkovalenko.common

abstract class ApplicationError(
    message: String,
    val httpCode: Int,
    val isExpected: Boolean
) extends Throwable(message)

object ApplicationError {

  abstract class RepositoryError(message: String, httpCode: Int = 500, isExpected: Boolean = true)
      extends ApplicationError(message, httpCode, isExpected)

  case object DBEntityNotFound
      extends RepositoryError(
        message  = s"DB: Entity not found",
        httpCode = 404
      )

  case object CirceCodecError extends ApplicationError(message = "Circe: Error during encoding/decoding",  httpCode = 400, isExpected = false)

  case class GeneralThrowable(message: String) extends ApplicationError(message, httpCode = 400, isExpected = true)

  object GeneralThrowable {
    def apply(e: Throwable) = new GeneralThrowable(e.getMessage())
  }

}