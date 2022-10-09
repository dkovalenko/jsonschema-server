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
  def spec = suite("Schema API should")(
    test("return HTTP \"Not Found\" for non-existent schema") {
      // given
      val backendStub = TapirStubInterpreter(SttpBackendStub(new RIOMonadError[Any]))
        .whenServerEndpoint(getSchemaServerEndpoint)
        .thenRunLogic()
        .backend()

      // when
      val response = basicRequest
        .get(uri"http://test.com/schema/non-existent-schema-id")
        .send(backendStub)

      // then
      assertZIO(response.map(_.code.code))(equalTo(404))
    },
    test("upload valid schema") {
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
    test("return HTTP \"Bad Request\" for invalid schema") {
      // given
      val backendStub = TapirStubInterpreter(SttpBackendStub(new RIOMonadError[Any]))
        .whenServerEndpoint(updateSchemaServerEndpoint)
        .thenRunLogic()
        .backend()

      val schemaId = "test-schema-id"

      // when
      val response = basicRequest
        .post(uri"http://test.com/schema/${schemaId}")
        .body("invalid json")
        .response(asJson[ApiResult])
        .send(backendStub)

      // then
      assertZIO(response.map(_.code.code))(equalTo(400)) //Bad request
    },
    test("return existing schema") {
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
    test("verify valid document by schema") {
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
    // test("verify invalid document by schema") {
  )
}
