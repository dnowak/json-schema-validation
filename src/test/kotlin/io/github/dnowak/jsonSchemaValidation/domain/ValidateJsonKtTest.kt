package io.github.dnowak.jsonSchemaValidation.domain

import arrow.core.partially1
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class ValidateJsonKtTest : FreeSpec({
    "validateJson" - {
        val objectMapper = ObjectMapper()
        val validateJson: ValidateJson = ::validateJson.partially1(objectMapper)
        "on valid JSON returns success" - {
            listOf("4", "{}", "[]", """ { "id": 121, "name": "John" } """).forEach { json ->
                " - $json" {
                    validateJson(Json(json)).toEither().shouldBeRight()
                }
            }
        }
        "on invalid JSON returns error" - {
            listOf("\"", "{", "[", """ { "id: 121, "name: "John" } """).forEach { json ->
                " - $json" {
                    validateJson(Json(json)).toEither().shouldBeLeft()
                }
            }
        }
    }
})
