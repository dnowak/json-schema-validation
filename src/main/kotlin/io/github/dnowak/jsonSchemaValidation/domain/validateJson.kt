package io.github.dnowak.jsonSchemaValidation.domain

import arrow.core.continuations.Effect

@JvmInline
value class Json(val value: String)

sealed interface JsonValidationError {
    data class Invalid(val message: String): JsonValidationError
}

typealias ValidateJson = (Json) -> Effect<JsonValidationError, Unit>