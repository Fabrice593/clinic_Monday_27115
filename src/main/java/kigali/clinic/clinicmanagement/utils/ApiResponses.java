package kigali.clinic.clinicmanagement.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Map;

public final class ApiResponses {

    private ApiResponses() {
    }

    public static ResponseEntity<?> fromMessage(String message, HttpStatus successStatus) {
        return new ResponseEntity<>(Map.of("message", message), statusOf(message, successStatus));
    }

    public static ResponseEntity<?> fromData(Object data, String notFoundMessage) {

        if (data == null) {
            return new ResponseEntity<>(Map.of("message", notFoundMessage), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    // services answer with a message, and its wording decides the status code
    private static HttpStatus statusOf(String message, HttpStatus successStatus) {

        String text = message.toLowerCase();

        if (containsAny(text, "success", "added to", "removed from")) {
            return successStatus;
        } else if (containsAny(text, "not found", "does not exist")) {
            return HttpStatus.NOT_FOUND;
        } else if (containsAny(text, "already", "taken by", "does not have", "has prescriptions")) {
            return HttpStatus.CONFLICT;
        } else {
            return HttpStatus.BAD_REQUEST;
        }
    }

    private static boolean containsAny(String text, String... words) {
        return Arrays.stream(words).anyMatch(text::contains);
    }
}
