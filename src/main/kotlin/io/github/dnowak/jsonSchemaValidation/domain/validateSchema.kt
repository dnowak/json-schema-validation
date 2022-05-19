package io.github.dnowak.jsonSchemaValidation.domain

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import arrow.core.nonFatalOrThrow
import arrow.core.right
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.JsonSchemaFactory

sealed interface SchemaValidationError {
    data class Invalid(val errors: List<String>) : SchemaValidationError
    data class Unknown(val message: String) : SchemaValidationError
}

typealias ValidateSchema = suspend (Schema, Document) -> Either<SchemaValidationError, Unit>

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
    schema: Schema,
    document: Document
): Either<SchemaValidationError, Unit> = either {
    val factory = JsonSchemaFactory.byDefault();
    val schemaJson = stringToJsonNode(schema.value)
        .mapLeft(::mapError)
        .bind()
    val documentJson = stringToJsonNode(document.value)
        .mapLeft(::mapError)
        .bind()
    cleanJsonNode(documentJson)
    val schema = factory.getJsonSchema(schemaJson)
    val report = schema.validate(documentJson)
    verify(report).bind()
}

private fun verify(report: ProcessingReport): Either<SchemaValidationError, Unit> =
    if (report.isSuccess) {
        Unit.right()
    } else {
        val errors = report
            .map { message -> message.asJson().toString() }
            .toList()
        SchemaValidationError.Invalid(errors).left()
    }

private fun cleanJsonNode(jsonNode: JsonNode) {
    if (jsonNode.isObject) {
        val objectNode = jsonNode as ObjectNode
        val names = objectNode.fieldNames().asSequence().toList()
        names.forEach { name ->
            val field = objectNode.get(name)
            if (field.isNull) {
                objectNode.remove(name)
            } else {
                cleanJsonNode(field)
            }
        }
    }
    if (jsonNode.isArray) {
        val arrayNode = jsonNode as ArrayNode
        arrayNode.forEach(::cleanJsonNode)
    }
}

fun mapError(error: JsonParseError): SchemaValidationError = SchemaValidationError.Unknown(error.toString())

