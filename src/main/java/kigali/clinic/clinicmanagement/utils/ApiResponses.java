package kigali.clinic.clinicmanagement.utils;

import org.springframework.http.HttpStatus;

public final class ApiResponses {

    private ApiResponses() {
    }

    public record Message(String message) {
    }

    public static final class Failure extends RuntimeException {

        private final HttpStatus status;

        private Failure(HttpStatus status, String message) {
            super(message);
            this.status = status;
        }

        public HttpStatus status() {
            return status;
        }
    }

    public static Failure notFound(String message) {
        return new Failure(HttpStatus.NOT_FOUND, message);
    }

    public static Failure badRequest(String message) {
        return new Failure(HttpStatus.BAD_REQUEST, message);
    }

    public static Failure conflict(String message) {
        return new Failure(HttpStatus.CONFLICT, message);
    }
}
