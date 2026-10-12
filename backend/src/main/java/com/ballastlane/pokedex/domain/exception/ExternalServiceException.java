package com.ballastlane.pokedex.domain.exception;

/**
 * The upstream catalog (PokeAPI) is unavailable or answered unexpectedly.
 */
public class ExternalServiceException extends DomainException {
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
