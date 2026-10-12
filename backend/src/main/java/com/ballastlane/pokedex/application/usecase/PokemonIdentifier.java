package com.ballastlane.pokedex.application.usecase;

import com.ballastlane.pokedex.domain.exception.ValidationException;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Normalises and validates the "id or name" used to look up a Pokemon in the catalog.
 * Prevents path traversal / garbage being forwarded to the external API.
 */
final class PokemonIdentifier {

    private static final Pattern VALID = Pattern.compile("^(\\d{1,5}|(?=[a-z0-9-]*[a-z])[a-z0-9][a-z0-9-]{0,49})$");

    private PokemonIdentifier() {
    }

    static String normalize(String idOrName) {
        String value = idOrName == null ? "" : idOrName.trim().toLowerCase(Locale.ROOT);
        if (!VALID.matcher(value).matches() || value.matches("^0+$")) {
            throw new ValidationException("Invalid Pokemon identifier",
                    Map.of("idOrName", "must be a positive number or a name made of letters, digits and dashes"));
        }
        return value.matches("^\\d+$") ? String.valueOf(Integer.parseInt(value)) : value;
    }
}
