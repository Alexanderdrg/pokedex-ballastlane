package com.ballastlane.pokedex.infrastructure.web;

import com.ballastlane.pokedex.domain.exception.ValidationException;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Translates between the entity version and HTTP validators: {@code ETag: "3"} out, {@code If-Match: "3"} in.
 */
public final class ETags {

    public static final String IF_MATCH = "If-Match";
    private static final Pattern STRONG_ETAG = Pattern.compile("^\"(\\d{1,18})\"$");

    private ETags() {
    }

    public static String of(long version) {
        return "\"" + version + "\"";
    }

    public static long parseIfMatch(String header) {
        Matcher matcher = STRONG_ETAG.matcher(header == null ? "" : header.trim());
        if (!matcher.matches()) {
            throw new ValidationException("Invalid If-Match header",
                    Map.of(IF_MATCH, "must be the ETag returned by the API, e.g. \"3\""));
        }
        return Long.parseLong(matcher.group(1));
    }
}
