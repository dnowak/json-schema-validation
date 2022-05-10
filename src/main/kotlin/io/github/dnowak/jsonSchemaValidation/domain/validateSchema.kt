package io.github.dnowak.jsonSchemaValidation.domain

import arrow.core.Either
import arrow.core.continuations.Effect
import arrow.core.continuations.either
import arrow.core.left
import arrow.core.nonFatalOrThrow
import arrow.core.right
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.JsonSchemaFactory

sealed interface SchemaValidationError {
    data class Invalid(val errors: List<String>): SchemaValidationError
    data class Unknown(val message: String): SchemaValidationError
}

typealias ValidateSchema = (SchemaSource, DocumentSource) -> Effect<SchemaValidationError, Unit>

data class JsonParseError(val message: String)

typealias StringToJsonNode = (String) -> Either<JsonParseError, JsonNode>

fun stringToJsonNode(
    objectMapper: ObjectMapper,
    document: String,
): Either<JsonParseError, JsonNode> = Either.catch {
    objectMapper.readTree(document)
}.mapLeft { throwable ->
    JsonParseError(throwable.nonFatalOrThrow().message ?: "UNKNOWN")
}

suspend fun validateSchema(
    stringToJsonNode: StringToJsonNode,
    schemaSource: SchemaSource,
    documentSource: DocumentSource
): Either<SchemaValidationError, Unit> = either {
    val factory = JsonSchemaFactory.byDefault();
    val schemaJson = stringToJsonNode(schemaSource.value)
        .mapLeft(::mapError)
        .bind()
    val documentJson = stringToJsonNode(documentSource.value)
        .mapLeft(::mapError)
        .bind()
    val schema = factory.getJsonSchema(schemaJson)
    val report = schema.validate(documentJson)
    verify(report).bind()
}

private fun verify(report: ProcessingReport): Either<SchemaValidationError, Unit> =
    if (report.isSuccess) {
        Unit.right()
    } else {
        val errors = report
            .map { message ->  message.message }
            .toList()
        SchemaValidationError.Invalid(errors).left()
    }

fun mapError(error: JsonParseError): SchemaValidationError = SchemaValidationError.Unknown(error.toString())

