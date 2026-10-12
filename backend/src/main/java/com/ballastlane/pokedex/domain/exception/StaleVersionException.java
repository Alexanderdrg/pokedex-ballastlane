package com.ballastlane.pokedex.domain.exception;

/**
 * The caller edited an outdated copy: someone else saved a newer version in the meantime.
 */
public class StaleVersionException extends DomainException {

    public StaleVersionException(Long id, long expectedVersion, long currentVersion) {
        super("Local Pokemon " + id + " was modified by someone else (you edited version " + expectedVersion
                + ", current is " + currentVersion + "). Reload it and apply your changes again.");
    }
}
