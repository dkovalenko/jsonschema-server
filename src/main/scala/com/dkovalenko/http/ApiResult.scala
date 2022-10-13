package com.dkovalenko.http

import cats.syntax.functor._
import com.dkovalenko.common.ApplicationError
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

sealed trait ApiResultSuccess {
  val action: String
  val id: String
  val status: String
}

sealed trait ApiResultError {
  val action: String
  val id: String
  val status: String
  val message: String
}

object ApiResultSuccess {
  private val SUCCESS = "success"
  case class Success(action: String, id: String, val status: String = SUCCESS) extends ApiResultSuccess

  case class Created(action: String, id: String, val status: String = SUCCESS) extends ApiResultSuccess

  implicit val successCodec: Codec[Success] = deriveCodec
  implicit val createdCodec: Codec[Created] = deriveCodec

  implicit val encodeApiResult: Encoder[ApiResultSuccess] = Encoder.instance {
    case a @ Success(_, _, _) => a.asJson
    case a @ Created(_, _, _) => a.asJson
  }

  implicit val decodeApiResult: Decoder[ApiResultSuccess] =
    List[Decoder[ApiResultSuccess]](
      Decoder[Success].widen,
      Decoder[Created].widen
    ).reduceLeft(_ or _)

}

object ApiResultError {
  private val ERROR = "error"
  case class Error(action: String, id: String, message: String, val status: String = ERROR) extends ApiResultError

  case class ServerError(action: String, id: String, message: String, val status: String = ERROR)
      extends ApiResultError

  case class NotFound(action: String, id: String, message: String, val status: String = ERROR)
      extends ApiResultError

  def fromThrowable(ex: Throwable, action: String, id: String): ApiResultError = {
    Error(action, id, ex.getMessage())
  }

  def fromApplicationError(ex: ApplicationError, action: String, id: String): ApiResultError = {
    ex match {
      case ApplicationError.DBEntityNotFound              => NotFound(action, id, ex.getMessage())
      case ApplicationError.DBCommunicationError(message) => ServerError(action, id, message)
      case e: ApplicationError.GeneralThrowable           => fromThrowable(e, action, id)
    }
  }

  implicit val errorCodec: Codec[Error]             = deriveCodec
  implicit val servererrorCodec: Codec[ServerError] = deriveCodec
  implicit val notFoundCodec: Codec[NotFound]       = deriveCodec

  implicit val encodeApiResult: Encoder[ApiResultError] = Encoder.instance {
    case a @ Error(_, _, _, _)       => a.asJson
    case a @ ServerError(_, _, _, _) => a.asJson
    case a @ NotFound(_, _, _, _)    => a.asJson
  }

  implicit val decodeApiResult: Decoder[ApiResultError] =
    List[Decoder[ApiResultError]](
      Decoder[NotFound].widen,
      Decoder[ServerError].widen,
      Decoder[Error].widen
    ).reduceLeft(_ or _)

}
