package com.dkovalenko.schema.validator

import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.json.JSONTokener
import scala.util.Try
import org.everit.json.schema.Schema

object Validator {
  
  def validate(json: String, schema: String): Try[Boolean] = {
    for {
      schemaLoaded <- tryParseSchema(schema)
      _ <- Try(schemaLoaded.validate(new JSONObject(json)))
    } yield true
  }

  def tryParseSchema(schema: String): Try[Schema] = {
    Try {
      val rawSchema    = new JSONObject(new JSONTokener(schema))
      SchemaLoader.load(rawSchema)
    }
  }
}
