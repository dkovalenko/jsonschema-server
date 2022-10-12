package com.dkovalenko.common

abstract class ApplicationError(
    message: String,
    val httpCode: Int,
    val isExpected: Boolean,
  ) extends Throwable(message)

object ApplicationError {
  case object DBEntityNotFound
      extends ApplicationError(
        message = s"DB: Entity not found",
        httpCode = 404,
        isExpected = true,
      )

  case class DBCommunicationError(message: String)
      extends ApplicationError(
        message = s"DB: Problem with communicating with Redis: $message",
        httpCode = 500,
        isExpected = false,
      )

  case class GeneralThrowable(message: String) extends ApplicationError(message, httpCode = 400, isExpected = true)

  object GeneralThrowable {
    def apply(e: Throwable) = new GeneralThrowable(e.getMessage())
  }

}
