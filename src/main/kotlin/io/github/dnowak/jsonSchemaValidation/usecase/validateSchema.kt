package io.github.dnowak.jsonSchemaValidation.usecase

import arrow.core.Either
import arrow.core.continuations.either
import io.github.dnowak.jsonSchemaValidation.domain.Document
import io.github.dnowak.jsonSchemaValidation.domain.SchemaId
import io.github.dnowak.jsonSchemaValidation.domain.ValidateJson
import io.github.dnowak.jsonSchemaValidation.port.repository.GetSchema
import io.github.dnowak.jsonSchemaValidation.domain.ValidateSchema as ValidateDocument

sealed interface ValidateSchemaError {

}

typealias ValidateSchema = (SchemaId, Document) -> Either<ValidateSchemaError, Unit>

suspend fun validateSchema(
    validateJson: ValidateJson,
    validateDocument: ValidateDocument,
    getSchema: GetSchema,
    schemaId: SchemaId,
    document: Document,
): Either<ValidateSchemaError, Unit> = either {
   TODO("implement")
}