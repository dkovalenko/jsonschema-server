package com.dkovalenko.schema.validator

import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.json.JSONTokener
import scala.util.Try

object Validator {
  
  def validate(json: String, schema: String): Try[Boolean] =
    Try {
      val rawSchema    = new JSONObject(new JSONTokener(schema))
      val schemaLoaded = SchemaLoader.load(rawSchema)
      schemaLoaded.validate(new JSONObject(json))
    }.map(_ => true)

}
