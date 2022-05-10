package io.github.dnowak.jsonSchemaValidation.port.repository

import arrow.core.Either
import arrow.core.continuations.Effect
import io.github.dnowak.jsonSchemaValidation.domain.SchemaId
import io.github.dnowak.jsonSchemaValidation.domain.SchemaSource

sealed interface SaveSchemaError {
    data class DuplicatedId(val id: SchemaId): SaveSchemaError
    data class Unknown(val message: String): SaveSchemaError
}

typealias SaveSchema = (SchemaId, SchemaSource) -> Either<SaveSchemaError, Unit>

sealed interface GetSchemaError {
     data class NotFound(val id: SchemaId): GetSchemaError
    data class Unknown(val message: String): GetSchemaError
}

typealias GetSchema = (SchemaId) -> Effect<GetSchemaError, SchemaSource>