package com.dkovalenko

import com.dkovalenko.http.SchemaAPI._
import com.dkovalenko.http.ApiResult
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{UriContext, basicRequest}
import sttp.tapir.server.stub.TapirStubInterpreter
import zio.test.Assertion._
import zio.test.{ZIOSpecDefault, assertZIO}

import io.circe.generic.auto._
import sttp.client3.circe._
import sttp.tapir.ztapir.RIOMonadError

object SchemaAPISpec extends ZIOSpecDefault {
  def spec = suite("Schema API spec")(
    test("return schema") {
      // given
      val backendStub = TapirStubInterpreter(SttpBackendStub(new RIOMonadError[Any]))
        .whenServerEndpoint(getSchemaServerEndpoint)
        .thenRunLogic()
        .backend()

      // when
      val response = basicRequest
        .get(uri"http://test.com/schema/test-schema-id")
        .send(backendStub)

      // then
      assertZIO(response.map(_.body))(isRight(equalTo("GET getSchema test-schema-id")))
    },
    test("update schema") {
      // given
      val backendStub = TapirStubInterpreter(SttpBackendStub(new RIOMonadError[Any]))
        .whenServerEndpoint(updateSchemaServerEndpoint)
        .thenRunLogic()
        .backend()

      val schemaId = "test-schema-id"

      // when
      val response = basicRequest
        .post(uri"http://test.com/schema/${schemaId}")
        .body("{}")
        .response(asJson[ApiResult])
        .send(backendStub)

      val result = ApiResult.Success(
          action = "uploadSchema",
          id = schemaId
        )
      // then
      assertZIO(response.map(_.body))(isRight(equalTo(result)))
    },
    test("validate doc by schema") {
      // given
      val backendStub = TapirStubInterpreter(SttpBackendStub(new RIOMonadError[Any]))
        .whenServerEndpoint(validateDocServerEndpoint)
        .thenRunLogic()
        .backend()

      val schemaId = "test-schema-id"

      // when
      val response = basicRequest
        .post(uri"http://test.com/validate/${schemaId}")
        .body("{}")
        .response(asJson[ApiResult])
        .send(backendStub)

      val result = ApiResult.Success(
          action = "validateDocument",
          id = schemaId
        )
      // then
      assertZIO(response.map(_.body))(isRight(equalTo(result)))
    }
  )
}
