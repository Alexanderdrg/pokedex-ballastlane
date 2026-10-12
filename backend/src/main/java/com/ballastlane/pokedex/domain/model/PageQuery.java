package com.ballastlane.pokedex.domain.model;

import com.ballastlane.pokedex.domain.exception.ValidationException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pagination request. Page is zero based.
 */
public record PageQuery(int page, int size) {

    public static final int MAX_SIZE = 100;

    public PageQuery {
        Map<String, String> errors = new LinkedHashMap<>();
        if (page < 0) {
            errors.put("page", "must be greater than or equal to 0");
        }
        if (size < 1 || size > MAX_SIZE) {
            errors.put("size", "must be between 1 and " + MAX_SIZE);
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Invalid pagination parameters", errors);
        }
    }

    public int offset() {
        return page * size;
    }
}
