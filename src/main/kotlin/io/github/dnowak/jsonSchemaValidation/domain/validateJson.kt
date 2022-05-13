package io.github.dnowak.jsonSchemaValidation.domain

import arrow.core.continuations.Effect
import arrow.core.continuations.effect
import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.databind.ObjectMapper

@JvmInline
value class Json(val value: String)

sealed interface JsonValidationError {
    data class Invalid(val message: String) : JsonValidationError
}

typealias ValidateJson = (Json) -> Effect<JsonValidationError, Unit>

fun validateJson(objectMapper: ObjectMapper, json: Json): Effect<JsonValidationError, Unit> = effect {
    try {
        objectMapper.readTree(json.value)
    } catch (e: JacksonException) {
        shift(JsonValidationError.Invalid(e.message ?: "UNKNOWN"))
    }
}