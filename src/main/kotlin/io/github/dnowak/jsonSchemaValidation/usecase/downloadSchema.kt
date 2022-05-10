package io.github.dnowak.jsonSchemaValidation.usecase

import arrow.core.Either
import io.github.dnowak.jsonSchemaValidation.domain.SchemaId
import io.github.dnowak.jsonSchemaValidation.domain.SchemaSource
import io.github.dnowak.jsonSchemaValidation.port.repository.GetSchema
import io.github.dnowak.jsonSchemaValidation.port.repository.GetSchemaError

sealed interface DownloadSchemaError {
    data class NotFound(val id: SchemaId): DownloadSchemaError
    data class Unknown(val message: String): DownloadSchemaError
}

typealias DownloadSchema = (SchemaId) -> Either<DownloadSchemaError, SchemaSource>

suspend fun downloadSchema(
    getSchema: GetSchema,
    schemaId: SchemaId
): Either<DownloadSchemaError, SchemaSource> =
    getSchema(schemaId)
        .toEither()
        .mapLeft(::mapError)

private fun mapError(error: GetSchemaError): DownloadSchemaError = when(error) {
    is GetSchemaError.NotFound -> DownloadSchemaError.NotFound(error.id)
    is GetSchemaError.Unknown -> DownloadSchemaError.Unknown(error.message)
}