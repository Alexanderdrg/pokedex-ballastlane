package com.ballastlane.pokedex.domain.policy;

import com.ballastlane.pokedex.domain.exception.ValidationException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Rules for user registration credentials.
 */
public final class CredentialsPolicy {

    private static final Pattern USERNAME = Pattern.compile("^[a-zA-Z0-9._-]{3,30}$");
    private static final int MIN_PASSWORD = 8;
    // BCrypt ignores everything after 72 bytes
    private static final int MAX_PASSWORD = 72;

    public void validate(String username, String password) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (username == null || !USERNAME.matcher(username).matches()) {
            errors.put("username", "must be 3-30 characters: letters, digits, '.', '_' or '-'");
        }
        if (password == null || password.length() < MIN_PASSWORD || password.length() > MAX_PASSWORD) {
            errors.put("password", "must be between " + MIN_PASSWORD + " and " + MAX_PASSWORD + " characters");
        } else if (!password.chars().anyMatch(Character::isLetter) || !password.chars().anyMatch(Character::isDigit)) {
            errors.put("password", "must contain at least one letter and one digit");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Invalid registration data", errors);
        }
    }
}
