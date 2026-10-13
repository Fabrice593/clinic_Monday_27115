package kigali.clinic.clinicmanagement.utils;

import kigali.clinic.clinicmanagement.utils.ApiResponses.Failure;
import kigali.clinic.clinicmanagement.utils.ApiResponses.Message;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final String UNREADABLE_BODY = "Request body is not valid JSON or has a value of the wrong type";

    @ExceptionHandler(Failure.class)
    public ResponseEntity<Message> handleFailure(Failure failure) {
        return ResponseEntity.status(failure.status()).body(new Message(failure.getMessage()));
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<Message> handleUnparseableDate(DateTimeParseException exception) {
        return badRequest("Dates must use the format yyyy-MM-dd");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Message> handleMismatchedArgument(MethodArgumentTypeMismatchException exception) {
        return badRequest(Optional.ofNullable(exception.getRequiredType())
                .filter(Class::isEnum)
                .map(enumType -> exception.getName() + " must be one of " + constantNamesOf(enumType))
                .orElseGet(() -> "Invalid value for " + exception.getName()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Message> handleUnreadableBody(HttpMessageNotReadableException exception) {
        Throwable rootCause = NestedExceptionUtils.getMostSpecificCause(exception);

        return badRequest(rootCause instanceof IllegalArgumentException selfExplainingRejection
                ? selfExplainingRejection.getMessage()
                : UNREADABLE_BODY);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<Message> handleUnknownSortProperty(PropertyReferenceException exception) {
        return badRequest("Unknown sort property " + exception.getPropertyName());
    }

    private static ResponseEntity<Message> badRequest(String message) {
        return ResponseEntity.badRequest().body(new Message(message));
    }

    private static String constantNamesOf(Class<?> enumType) {
        return Arrays.stream(enumType.getEnumConstants())
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
    }
}
