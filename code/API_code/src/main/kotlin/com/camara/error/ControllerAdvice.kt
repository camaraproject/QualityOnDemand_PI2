/*
* Software Name : camara-qod-api
* Version: 0.1.0
* SPDX-FileCopyrightText: Copyright (c) 2022 Orange
* SPDX-License-Identifier: Apache-2.0
*
* This software is distributed under the Apache-2.0,
* the text of which is available at https://www.apache.org/licenses/LICENSE-2.0
* or see the "LICENCE" file for more details.
*
* Author: patrice.conil@orange.com
*/
package com.camara.error

import com.camara.model.ErrorInfo
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.databind.exc.ValueInstantiationException
import jakarta.validation.ConstraintViolationException
import jakarta.ws.rs.NotAllowedException
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.NotSupportedException
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestResponse
import org.jboss.resteasy.reactive.server.ServerExceptionMapper
import java.io.UnsupportedEncodingException
import java.util.concurrent.TimeoutException
import java.util.logging.Logger
import javax.management.InvalidAttributeValueException

@Suppress("unused", "TooManyFunctions")
class ControllerAdvice {

    val logger: Logger = Logger.getLogger(this.javaClass.simpleName)

    private fun mapExceptionIntoError(
        status: Response.Status,
        code: String,
        message: String?,
    ): RestResponse<ErrorInfo> {
        return RestResponse.status(
            status,
            ErrorInfo().status(status.statusCode).code(code).message(message)
        )
    }

    @ServerExceptionMapper
    fun durationOutOfBoundException(e: DurationOutOfBoundException): RestResponse<ErrorInfo> {
        return mapExceptionIntoError(Response.Status.BAD_REQUEST, "INVALID_ARGUMENT", e.message)
    }

    @ServerExceptionMapper
    fun notSupportedException(e: NotSupportedException): RestResponse<ErrorInfo> {
        return mapExceptionIntoError(Response.Status.BAD_REQUEST, "INVALID_ARGUMENT", e.message)
    }

    @ServerExceptionMapper
    fun constraintViolationException(e: ConstraintViolationException) =
        mapExceptionIntoError(
            Response.Status.BAD_REQUEST,
            "CONSTRAINT_VIOLATION",
            e.constraintViolations.joinToString(", ", "[", "]") { v -> "${v.propertyPath}: ${v.message}" }
        )

    @ServerExceptionMapper
    fun valueInstantiationException(e: ValueInstantiationException) =
        mapExceptionIntoError(Response.Status.BAD_REQUEST, "BAD_REQUEST", e.originalMessage)

    @ServerExceptionMapper
    fun jsonParseException(e: JsonParseException) =
        mapExceptionIntoError(Response.Status.BAD_REQUEST, "INVALID_JSON", e.message)

    @ServerExceptionMapper
    fun internalServerError(e: InternalServerError) =
        mapExceptionIntoError(Response.Status.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", e.message)

    @ServerExceptionMapper
    fun notAllowedException(e: NotAllowedException): RestResponse<ErrorInfo> {
        logger.severe("Unsupported method called: ${e.message}")
        return mapExceptionIntoError(Response.Status.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "unsupported")
    }

    @ServerExceptionMapper
    fun unsupportedEncodingException(e: UnsupportedEncodingException) =
        mapExceptionIntoError(Response.Status.BAD_REQUEST, "UNSUPPORTED_ENCODING", e.message)

    @ServerExceptionMapper
    fun mismatchedInputException(e: MismatchedInputException) =
        mapExceptionIntoError(Response.Status.BAD_REQUEST, "MISMATCHED_INPUT", e.message)

    @ServerExceptionMapper
    fun unknownProfileException(e: UnknownProfileException) =
        mapExceptionIntoError(Response.Status.BAD_REQUEST, "UNSUPPORTED_QOS_PROFILE", e.message)

    @ServerExceptionMapper
    fun noSuchElementException(e: NoSuchElementException) =
        mapExceptionIntoError(Response.Status.NOT_FOUND, "NOT_FOUND", e.message)

    @Suppress("unused")
    @ServerExceptionMapper
    fun mapException(e: WebApplicationException): RestResponse<ErrorInfo> {
        return when (val original = e.cause ?: e) {
            is ConstraintViolationException -> constraintViolationException(original)

            is ValueInstantiationException -> valueInstantiationException(original)

            is JsonParseException -> jsonParseException(original)

            is InternalServerError -> internalServerError(original)

            is MismatchedInputException -> mismatchedInputException(original)

            is NotAllowedException -> notAllowedException(original)

            is UnsupportedEncodingException -> unsupportedEncodingException(original)

            is TimeoutException -> mapExceptionIntoError(
                Response.Status.SERVICE_UNAVAILABLE,
                "UNAVAILABLE",
                original.message
            )

            is InvalidAttributeValueException -> mapExceptionIntoError(
                Response.Status.BAD_REQUEST,
                "INVALID_ATTRIBUTE_VALUE",
                original.message
            )

            is NotFoundException -> mapExceptionIntoError(
                Response.Status.NOT_FOUND,
                Response.Status.NOT_FOUND.statusCode.toString(),
                original.message
            )

            is IllegalArgumentException -> mapExceptionIntoError(
                Response.Status.BAD_REQUEST,
                "INVALID_ARGUMENT",
                original.message
            )

            else -> {
                logger.info("Unmapped Exception occurred: $original")
                mapExceptionIntoError(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "INTERNAL_SERVER_ERROR",
                    original.message
                )
            }
        }
    }
}
