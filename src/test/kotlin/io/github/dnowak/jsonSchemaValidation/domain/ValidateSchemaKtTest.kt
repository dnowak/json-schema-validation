package io.github.dnowak.jsonSchemaValidation.domain

import arrow.core.partially1
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class ValidateSchemaKtTest : FreeSpec({

    val schema = """
        {
          "${'$'}schema": "http://json-schema.org/draft-04/schema#",
          "type": "object",
          "properties": {
            "source": {
              "type": "string"
            },
            "destination": {
              "type": "string"
            },
            "timeout": {
              "type": "integer",
              "minimum": 0,
              "maximum": 32767
            },
            "chunks": {
              "type": "object",
              "properties": {
                "size": {
                  "type": "integer"
                },
                "number": {
                  "type": "integer"
                }
              },
              "required": ["size"]
            }
          },
          "required": ["source", "destination"]
        }
    """.trimIndent()

    val document = """
        {
          "source": "/home/alice/image.iso",
          "destination": "/mnt/storage",
          "timeout": null,
          "chunks": {
            "size": 1024,
            "number": null
          }
        }
    """.trimIndent()

    "validateSchema" - {
        val objectMapper = ObjectMapper().also {
            it.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
        val stringToJsonNode = ::stringToJsonNode.partially1(objectMapper)
        val validateSchema: ValidateSchema = ::validateSchema.partially1(stringToJsonNode)
        "validates correct document" {
            val result = validateSchema(SchemaSource(schema), DocumentSource(document))
            result.shouldBeRight()
        }
    }
})
