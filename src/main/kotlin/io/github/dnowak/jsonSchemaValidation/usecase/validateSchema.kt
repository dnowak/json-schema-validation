package io.github.dnowak.jsonSchemaValidation.usecase

import arrow.core.Either
import arrow.core.continuations.either
import io.github.dnowak.jsonSchemaValidation.domain.*
import io.github.dnowak.jsonSchemaValidation.port.repository.GetSchema
import io.github.dnowak.jsonSchemaValidation.port.repository.GetSchemaError
import io.github.dnowak.jsonSchemaValidation.domain.ValidateSchema as CheckSchema

sealed interface ValidateSchemaError {
    data class Unknown(val message: String) : ValidateSchemaError
}

typealias ValidateSchema = (SchemaId, Document) -> Either<ValidateSchemaError, Unit>

suspend fun validateSchema(
    validateJson: ValidateJson,
    checkSchema: CheckSchema,
    getSchema: GetSchema,
    schemaId: SchemaId,
    document: Document,
): Either<ValidateSchemaError, Unit> = either {
    val schema = getSchema(schemaId).toEither().mapLeft(::mapError).bind()
    checkSchema(schema, document).mapLeft(::mapError).bind()
}

private fun mapError(error: GetSchemaError): ValidateSchemaError = ValidateSchemaError.Unknown(error.toString())

private fun mapError(error: SchemaValidationError): ValidateSchemaError  = ValidateSchemaError.Unknown(error.toString())