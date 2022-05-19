package io.github.dnowak.jsonSchemaValidation.usecase

import arrow.core.Either
import arrow.core.continuations.either
import io.github.dnowak.jsonSchemaValidation.domain.*
import io.github.dnowak.jsonSchemaValidation.port.repository.SaveSchema
import io.github.dnowak.jsonSchemaValidation.port.repository.SaveSchemaError

sealed interface SchemaUploadError {
    data class SchemaAlreadyExists(val id: SchemaId) : SchemaUploadError
    data class InvalidJson(val message: String) : SchemaUploadError
    data class Unknown(val message: String) : SchemaUploadError
}

typealias UploadSchema = (Schema) -> Either<SchemaUploadError, Unit>

suspend fun uploadSchema(
    validateJson: ValidateJson,
    saveSchema: SaveSchema,
    schemaId: SchemaId,
    schema: Schema
): Either<SchemaUploadError, Unit> = either {
    validateJson(Json(schema.value))
        .toEither()
        .mapLeft(::mapError)
        .bind()
    saveSchema(schemaId, schema)
        .mapLeft(::mapError)
        .bind()
}

private fun mapError(error: JsonValidationError): SchemaUploadError = SchemaUploadError.InvalidJson(error.toString())

private fun mapError(error: SaveSchemaError): SchemaUploadError = when(error) {
    is SaveSchemaError.DuplicatedId -> SchemaUploadError.SchemaAlreadyExists(error.id)
    is SaveSchemaError.Unknown -> SchemaUploadError.Unknown(error.message)
}

