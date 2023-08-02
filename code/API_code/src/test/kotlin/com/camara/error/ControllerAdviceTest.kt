package com.camara.error

import com.camara.model.ErrorInfo
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.databind.exc.ValueInstantiationException
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.validation.ConstraintViolationException
import jakarta.ws.rs.NotAllowedException
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.io.UnsupportedEncodingException
import java.util.concurrent.TimeoutException
import javax.management.InvalidAttributeValueException

@QuarkusTest
class ControllerAdviceTest {

    val controllerAdvice = ControllerAdvice()

    @Inject
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `should return UNAVAILABLE when timeout occurs`() {
        val exception = WebApplicationException(TimeoutException("Timeout exception"))
        val response = controllerAdvice.mapException(exception)

        val expected = ErrorInfo()
            .status(Response.Status.SERVICE_UNAVAILABLE.statusCode)
            .code("UNAVAILABLE")
            .message("Timeout exception")
        assertThat(response.status).isEqualTo(Response.Status.SERVICE_UNAVAILABLE.statusCode)
        assertThat(response.entity).isEqualTo(expected)
    }

    @Test
    fun `should return BAD_REQUEST when InvalidAttributeValueException occurs`() {
        val exception = WebApplicationException(InvalidAttributeValueException("Invalid attribute value"))
        val response = controllerAdvice.mapException(exception)

        val expected = ErrorInfo()
            .status(Response.Status.BAD_REQUEST.statusCode)
            .code("INVALID_ATTRIBUTE_VALUE")
            .message("Invalid attribute value")

        assertThat(response.status).isEqualTo(Response.Status.BAD_REQUEST.statusCode)
        assertThat(response.entity).isEqualTo(expected)
    }

    @Test
    fun `should return BAD_REQUEST when IllegalArgumentException occurs`() {
        val exception = WebApplicationException(IllegalArgumentException("Illegal argument"))
        val response = controllerAdvice.mapException(exception)

        val expected = ErrorInfo()
            .status(Response.Status.BAD_REQUEST.statusCode)
            .code("INVALID_ARGUMENT")
            .message("Illegal argument")

        assertThat(response.status).isEqualTo(Response.Status.BAD_REQUEST.statusCode)
        assertThat(response.entity).isEqualTo(expected)
    }

    @Test
    fun `should return BAD_REQUEST when ConstraintViolationException occurs`() {
        val exception = WebApplicationException(
            ConstraintViolationException(
                emptySet()
            )
        )
        val response = controllerAdvice.mapException(exception)

        val expected = ErrorInfo()
            .status(Response.Status.BAD_REQUEST.statusCode)
            .code("CONSTRAINT_VIOLATION")
            .message("[]")

        assertThat(response.status).isEqualTo(Response.Status.BAD_REQUEST.statusCode)
        assertThat(response.entity).isEqualTo(expected)
    }

    @Test
    fun `should return BAD_REQUEST when ValueInstantiationException occurs`() {
        val mock: ValueInstantiationException = mock()
        Mockito.`when`(mock.originalMessage).thenReturn("Value is invalid")

        val exception = WebApplicationException(mock)
        val response = controllerAdvice.mapException(exception)

        val expected = ErrorInfo()
            .status(Response.Status.BAD_REQUEST.statusCode)
            .code("BAD_REQUEST")
            .message("Value is invalid")

        assertThat(response.status).isEqualTo(Response.Status.BAD_REQUEST.statusCode)
        assertThat(response.entity).isEqualTo(expected)
    }

    @Test
    fun `should return INTERNAL_SERVER_ERROR when INTERNAL_SERVER_ERROR occurs`() {
        val exception = WebApplicationException(InternalServerError("500", "Unsupported error"))
        val response = controllerAdvice.mapException(exception)

        val expected = ErrorInfo()
            .status(Response.Status.INTERNAL_SERVER_ERROR.statusCode)
            .code("INTERNAL_SERVER_ERROR")
            .message("Unsupported error")

        assertThat(response.status).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.statusCode)
        assertThat(response.entity).isEqualTo(expected)
    }

    @Test
    fun `should return BAD_REQUEST when JsonParseException occurs`() {
        lateinit var ex: JsonParseException
        try {
            objectMapper.readValue("test", String::class.java)
        } catch (e: JsonParseException) {
            ex = e
        }

        val exception = WebApplicationException(ex)
        val response = controllerAdvice.mapException(exception)

        val expected = ErrorInfo()
            .status(Response.Status.BAD_REQUEST.statusCode)
            .code("INVALID_JSON")
            .message(
                "Unrecognized token 'test': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')\n" +
                    " at [Source: (String)\"test\"; line: 1, column: 5]"
            )

        assertThat(response.status).isEqualTo(Response.Status.BAD_REQUEST.statusCode)
        assertThat(response.entity).isEqualTo(expected)
    }

    @Test
    fun `should return BAD_REQUEST when MismatchedInputException occurs`() {
        lateinit var ex: MismatchedInputException
        try {
            objectMapper.readValue("{\"toto\":\"is a funny girl\"}", List::class.java)
        } catch (e: MismatchedInputException) {
            ex = e
        }

        val exception = WebApplicationException(ex)
        val response = controllerAdvice.mapException(exception)

        val expected = ErrorInfo()
            .status(Response.Status.BAD_REQUEST.statusCode)
            .code("MISMATCHED_INPUT")
            .message(
                "Cannot deserialize value of type `java.util.ArrayList<java.lang.Object>` from Object value (token `JsonToken.START_OBJECT`)\n" +
                    " at [Source: (String)\"{\"toto\":\"is a funny girl\"}\"; line: 1, column: 1]"
            )

        assertThat(response.status).isEqualTo(Response.Status.BAD_REQUEST.statusCode)
        assertThat(response.entity).isEqualTo(expected)
    }

    @Test
    fun `should return METHOD_NOT_ALLOWED when NotAllowedException occurs`() {
        val exception = WebApplicationException(NotAllowedException("unsupported"))
        val response = controllerAdvice.mapException(exception)

        val expected = ErrorInfo()
            .status(Response.Status.METHOD_NOT_ALLOWED.statusCode)
            .code("METHOD_NOT_ALLOWED")
            .message("unsupported")

        assertThat(response.status).isEqualTo(Response.Status.METHOD_NOT_ALLOWED.statusCode)
        assertThat(response.entity).isEqualTo(expected)
    }

    @Test
    fun `should return BAD_REQUEST when UnsupportedEncodingException occurs`() {
        val exception = WebApplicationException(UnsupportedEncodingException("unsupported"))
        val response = controllerAdvice.mapException(exception)

        val expected = ErrorInfo()
            .status(Response.Status.BAD_REQUEST.statusCode)
            .code("UNSUPPORTED_ENCODING")
            .message("unsupported")

        assertThat(response.status).isEqualTo(Response.Status.BAD_REQUEST.statusCode)
        assertThat(response.entity).isEqualTo(expected)
    }

    @Test
    fun `should return NOT_FOUD when NoSuchElementException occurs`() {
        val exception = NoSuchElementException("Unknown profile")
        val response = controllerAdvice.noSuchElementException(exception)

        val expected = ErrorInfo()
            .status(Response.Status.NOT_FOUND.statusCode)
            .code("NOT_FOUND")
            .message("Unknown profile")

        assertThat(response.status).isEqualTo(Response.Status.NOT_FOUND.statusCode)
        assertThat(response.entity).isEqualTo(expected)
    }

    @Test
    fun `should return BAD_REQUEST when UnknownProfileException occurs`() {
        val exception = UnknownProfileException("UNKNOWN_PROFILE", "Unknown profile")
        val response = controllerAdvice.unknownProfileException(exception)

        val expected = ErrorInfo()
            .status(Response.Status.BAD_REQUEST.statusCode)
            .code("UNSUPPORTED_QOS_PROFILE")
            .message("Unknown profile")

        assertThat(response.status).isEqualTo(Response.Status.BAD_REQUEST.statusCode)
        assertThat(response.entity).isEqualTo(expected)
    }

    @Test
    fun `should return INTERNAL_SERVER_ERROR when anything else occurs`() {
        val exception = WebApplicationException(NullPointerException("NPE"))
        val response = controllerAdvice.mapException(exception)

        val expected = ErrorInfo()
            .status(Response.Status.INTERNAL_SERVER_ERROR.statusCode)
            .code("INTERNAL_SERVER_ERROR")
            .message("NPE")

        assertThat(response.status).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.statusCode)
        assertThat(response.entity).isEqualTo(expected)
    }
}
