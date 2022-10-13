package com.dkovalenko

import com.dkovalenko.config.RedisConfig
import com.dkovalenko.http.ApiResultError
import com.dkovalenko.http.ApiResultSuccess
import com.dkovalenko.http.SchemaAPI
import com.dkovalenko.http.SchemaAPITapir
import com.dkovalenko.schema.SchemaRepositoryRedis
import com.dkovalenko.schema.SchemaServiceLive
import sttp.client3.UriContext
import sttp.client3.basicRequest
import sttp.client3.circe._
import sttp.client3.testing.SttpBackendStub
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import zio.ZLayer
import zio.test.Assertion._
import zio.test.ZIOSpecDefault
import zio.test.assertZIO

object SchemaAPISpec extends ZIOSpecDefault {

  object Layers {

    val testLayer = ZLayer.make[SchemaAPI](
      SchemaAPITapir.layer,
      SchemaServiceLive.layer,
      SchemaRepositoryRedis.layer,
      RedisConfig.test // @TODO: use testcontainer for the best DX
    )

  }

  def spec = suite("Schema API should")(
    test("return HTTP \"Not Found\" for non-existent schema") {
      val result = for {
        endpoint <- SchemaAPI.getSchemaServerEndpoint()
        backendStub = TapirStubInterpreter(SttpBackendStub(new RIOMonadError[Any]))
          .whenServerEndpointRunLogic(endpoint)
          .backend()

        response <- basicRequest
          .get(uri"http://test.com/schema/non-existent-schema-id")
          .send(backendStub)
      } yield response
      assertZIO(result.map(_.code.code))(equalTo(404))
    },
    test("upload valid schema") {
      val result = for {
        endpoint <- SchemaAPI.updateSchemaServerEndpoint()
        backendStub = TapirStubInterpreter(SttpBackendStub(new RIOMonadError[Any]))
          .whenServerEndpointRunLogic(endpoint)
          .backend()

        response <- basicRequest
          .post(uri"http://test.com/schema/${Fixtures.schemaId}")
          .body(Fixtures.validJsonSchema)
          .response(asJson[ApiResultSuccess])
          .send(backendStub)
      } yield response

      val expectedResult = ApiResultSuccess.Created(
        action = "uploadSchema",
        id     = Fixtures.schemaId
      )
      assertZIO(result.map(_.body))(isRight(equalTo(expectedResult))) *>
        assertZIO(result.map(_.code.code))(equalTo(201))
    },
    test("return HTTP \"Bad Request\" for invalid schema") {
      val result = for {
        endpoint <- SchemaAPI.updateSchemaServerEndpoint()
        backendStub = TapirStubInterpreter(SttpBackendStub(new RIOMonadError[Any]))
          .whenServerEndpointRunLogic(endpoint)
          .backend()

        response <- basicRequest
          .post(uri"http://test.com/schema/${Fixtures.schemaId}")
          .body("invalid json")
          .response(asJson[ApiResultError])
          .send(backendStub)
      } yield response

      assertZIO(result.map(_.code.code))(equalTo(400)) // Bad request
    },
    test("return existing schema") {
      val result = for {
        endpoint <- SchemaAPI.getSchemaServerEndpoint()
        backendStub = TapirStubInterpreter(SttpBackendStub(new RIOMonadError[Any]))
          .whenServerEndpointRunLogic(endpoint)
          .backend()

        response <- basicRequest
          .get(uri"http://test.com/schema/${Fixtures.schemaId}")
          .send(backendStub)
      } yield response

      assertZIO(result.map(_.body))(isRight(equalTo(Fixtures.validJsonSchema)))
    },
    test("verify valid document by schema") {
      val result = for {
        endpoint <- SchemaAPI.validateDocServerEndpoint()
        backendStub = TapirStubInterpreter(SttpBackendStub(new RIOMonadError[Any]))
          .whenServerEndpointRunLogic(endpoint)
          .backend()

        response <- basicRequest
          .post(uri"http://test.com/validate/${Fixtures.schemaId}")
          .body(Fixtures.validJsonWithNulls)
          .response(asJson[ApiResultSuccess])
          .send(backendStub)
      } yield response

      val expectedResult = ApiResultSuccess.Success(
        action = "validateDocument",
        id     = Fixtures.schemaId
      )

      assertZIO(result.map(_.body))(isRight(equalTo(expectedResult)))
    },
    test("verify invalid document by schema") {
      val result = for {
        endpoint <- SchemaAPI.validateDocServerEndpoint()
        backendStub = TapirStubInterpreter(SttpBackendStub(new RIOMonadError[Any]))
          .whenServerEndpointRunLogic(endpoint)
          .backend()

        response <- basicRequest
          .post(uri"http://test.com/validate/${Fixtures.schemaId}")
          .body(Fixtures.invalidJsonDocument)
          .response(asJson[ApiResultError])
          .send(backendStub)
      } yield response

      assertZIO(result.map(_.code.code))(equalTo(400)) // Bad request
    }
  ).provideShared(Layers.testLayer)

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
