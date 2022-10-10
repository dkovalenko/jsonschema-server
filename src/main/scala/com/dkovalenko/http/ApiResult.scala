package com.dkovalenko.http

import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import cats.syntax.functor._

sealed trait ApiResult {
  val action: String
  val id: String
  val status: String
}

object ApiResult {
  case class Success(action: String, id: String, val status: String = "success") extends ApiResult

  case class Error(action: String, id: String, message: String, val status: String = "error") extends ApiResult

  def fromThrowable(ex: Throwable, action: String, id: String): ApiResult.Error = {
    Error(action, id, ex.getMessage())
  }
  
  implicit val successCodec: Codec[Success] = deriveCodec
  implicit val errorCodec: Codec[Error] = deriveCodec
  implicit val encodeApiResult: Encoder[ApiResult] = Encoder.instance {
    case s @ Success(_, _, _) => s.asJson
    case e @ Error(_, _, _, _) => e.asJson
  }
  implicit val decodeApiResult: Decoder[ApiResult] = 
    List[Decoder[ApiResult]](
      Decoder[Success].widen,
      Decoder[Error].widen,
    ).reduceLeft(_ or _)
}