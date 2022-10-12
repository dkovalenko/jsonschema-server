package com.dkovalenko

import com.dkovalenko.http.SchemaAPI._
import com.dkovalenko.http.ApiResultSuccess
import com.dkovalenko.http.ApiResultError
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{UriContext, basicRequest}
import sttp.tapir.server.stub.TapirStubInterpreter
import zio.test.Assertion._
import zio.test.{ZIOSpecDefault, assertZIO}

import io.circe.generic.auto._
import sttp.client3.circe._
import sttp.tapir.ztapir.RIOMonadError
import com.dkovalenko.http.SchemaAPI
import com.dkovalenko.http.SchemaAPITapir
import com.dkovalenko.schema.SchemaServiceLive
import com.dkovalenko.schema.SchemaRepositoryRedis
import com.dkovalenko.config.RedisConfig
import zio.ZLayer

object SchemaAPISpec extends ZIOSpecDefault {

  object Layers {
    val sharedLayer = ZLayer.make[SchemaAPI](
      SchemaAPITapir.layer,
      SchemaServiceLive.layer,
      SchemaRepositoryRedis.layer,
      RedisConfig.test
    )
  }

  def spec = suite("Schema API should")(
    test("return HTTP \"Not Found\" for non-existent schema") {
      for {
        endpoint <- SchemaAPI.getSchemaServerEndpoint()
        backendStub = TapirStubInterpreter(SttpBackendStub(new RIOMonadError[Any]))
          .whenServerEndpointRunLogic(endpoint)
          // .thenRunLogic()
          .backend()

        response = basicRequest
          .get(uri"http://test.com/schema/non-existent-schema-id")
          .send(backendStub)
      } yield assertZIO(response.map(_.code.code))(equalTo(404))

    },
    test("upload valid schema") {
      // given
      val backendStub = TapirStubInterpreter(SttpBackendStub(new RIOMonadError[Any]))
        .whenServerEndpoint(updateSchemaServerEndpoint)
        .thenRunLogic()
        .backend()

      // when
      val response = basicRequest
        .post(uri"http://test.com/schema/${Fixtures.schemaId}")
        .body(Fixtures.validJsonSchema)
        .response(asJson[ApiResultSuccess])
        .send(backendStub)

      val result = ApiResultSuccess.Created(
        action = "uploadSchema",
        id     = Fixtures.schemaId
      )
      // then
      assertZIO(response.map(_.body))(isRight(equalTo(result)))
      assertZIO(response.map(_.code.code))(equalTo(201))
    },
    test("return HTTP \"Bad Request\" for invalid schema") {
      // given
      val backendStub = TapirStubInterpreter(SttpBackendStub(new RIOMonadError[Any]))
        .whenServerEndpoint(updateSchemaServerEndpoint)
        .thenRunLogic()
        .backend()

      // when
      val response = basicRequest
        .post(uri"http://test.com/schema/${Fixtures.schemaId}")
        .body("invalid json")
        .response(asJson[ApiResultError])
        .send(backendStub)

      // then
      assertZIO(response.map(_.code.code))(equalTo(400)) // Bad request
    },
    test("return existing schema") {
      // given
      val backendStub = TapirStubInterpreter(SttpBackendStub(new RIOMonadError[Any]))
        .whenServerEndpoint(getSchemaServerEndpoint)
        .thenRunLogic()
        .backend()

      // when
      val response = basicRequest
        .get(uri"http://test.com/schema/${Fixtures.schemaId}")
        .send(backendStub)

      // then
      assertZIO(response.map(_.body))(isRight(equalTo(Fixtures.validJsonSchema)))
    },
    test("verify valid document by schema") {
      // given
      val backendStub = TapirStubInterpreter(SttpBackendStub(new RIOMonadError[Any]))
        .whenServerEndpoint(validateDocServerEndpoint)
        .thenRunLogic()
        .backend()

      // when
      val response = basicRequest
        .post(uri"http://test.com/validate/${Fixtures.schemaId}")
        .body(Fixtures.validJsonWithNulls)
        .response(asJson[ApiResultSuccess])
        .send(backendStub)

      val result = ApiResultSuccess.Success(
        action = "validateDocument",
        id     = Fixtures.schemaId
      )
      // then
      assertZIO(response.map(_.body))(isRight(equalTo(result)))
    },
    test("verify invalid document by schema") {
      // given
      val backendStub = TapirStubInterpreter(SttpBackendStub(new RIOMonadError[Any]))
        .whenServerEndpoint(validateDocServerEndpoint)
        .thenRunLogic()
        .backend()

      // when
      val response = basicRequest
        .post(uri"http://test.com/validate/${Fixtures.schemaId}")
        .body(Fixtures.invalidJsonDocument)
        .response(asJson[ApiResultError])
        .send(backendStub)

      // then
      assertZIO(response.map(_.code.code))(equalTo(400)) // Bad request
    }
  ).provideShared(Layers.sharedLayer)

}

object Fixtures {
  val schemaId = "test-schema-id"

  val validJsonSchema = """|{
                           |  "$schema": "http://json-schema.org/draft-04/schema#",
                           |  "type": "object",
                           |  "properties": {
                           |    "source": {
                           |      "type": "string"
                           |    },
                           |    "destination": {
                           |      "type": "string"
                           |    },
                           |    "timeout": {
                           |      "type": "integer",
                           |      "minimum": 0,
                           |      "maximum": 32767
                           |    },
                           |    "chunks": {
                           |      "type": "object",
                           |      "properties": {
                           |        "size": {
                           |          "type": "integer"
                           |        },
                           |        "number": {
                           |          "type": "integer"
                           |        }
                           |      },
                           |      "required": ["size"]
                           |    }
                           |  },
                           |  "required": ["source", "destination"]
                           |}
                           |""".stripMargin

  val validJsonWithNulls = """|{
                              |  "source": "/home/alice/image.iso",
                              |  "destination": "/mnt/storage",
                              |  "timeout": null,
                              |  "chunks": {
                              |    "size": 1024,
                              |    "number": null
                              |  }
                              |}
                              |""".stripMargin

  val invalidJsonDocument = "[]"

}
