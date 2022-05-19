package io.github.dnowak.jsonSchemaValidation.domain

import arrow.core.partially1
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.types.shouldBeInstanceOf

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

    val documentWithoutNulls = """
        {
          "source": "/home/alice/image.iso",
          "destination": "/mnt/storage",
          "chunks": {
            "size": 1024
          }
        }
    """.trimIndent()

    val documentWithNulls = """
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

    val invalidDocumentWithoutSize = """
        {
          "source": "/home/alice/image.iso",
          "destination": "/mnt/storage",
          "timeout": null,
          "chunks": {
            "number": null
          }
        }
    """.trimIndent()

    val invalidDocumentWithoutSourceAndDestination = """
        {
          "destination": null,
          "timeout": null,
          "chunks": {
            "size": 2048,
            "number": null
          }
        }
    """.trimIndent()

    "validateSchema" - {
        val objectMapper = ObjectMapper().also {
            it.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
        val stringToJsonNode = ::stringToJsonNode.partially1(objectMapper)
        val validateSchema: ValidateSchema = ::validateSchema.partially1(stringToJsonNode)
        "validates correct document without nulls" {
            val result = validateSchema(Schema(schema), Document(documentWithoutNulls))
            result.shouldBeRight()
        }
        "validates correct document with nulls" {
            val result = validateSchema(Schema(schema), Document(documentWithNulls))
            result.shouldBeRight()
        }
        "validates invalid document without size" {
            val result = validateSchema(Schema(schema), Document(invalidDocumentWithoutSize))
            val error = result.shouldBeLeft()
            val invalidError = error.shouldBeInstanceOf<SchemaValidationError.Invalid>()
            invalidError.errors.shouldExist { message -> message.contains("\"size\"") }
        }
        "validates invalid document without source and destination" {
            val result = validateSchema(Schema(schema), Document(invalidDocumentWithoutSourceAndDestination))
            val error = result.shouldBeLeft()
            val invalidError = error.shouldBeInstanceOf<SchemaValidationError.Invalid>()
            invalidError.errors.shouldExist { message -> message.contains("\"source\"") }
            invalidError.errors.shouldExist { message -> message.contains("\"destination\"") }
        }
    }
})
