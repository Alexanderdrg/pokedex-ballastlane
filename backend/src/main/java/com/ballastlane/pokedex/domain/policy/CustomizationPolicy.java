package com.ballastlane.pokedex.domain.policy;

import com.ballastlane.pokedex.domain.exception.ValidationException;
import com.ballastlane.pokedex.domain.model.Customization;
import com.ballastlane.pokedex.domain.model.Region;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Business rules for the proprietary fields of a local Pokemon.
 * Normalises input (trims, lower-cases tags, blank to null) and rejects invalid data.
 */
public final class CustomizationPolicy {

    public static final int MAX_LOCALIZED_NAME = 60;
    public static final int MAX_NOTES = 500;
    public static final int MAX_TAGS = 10;
    public static final int MAX_TAG_LENGTH = 20;
    private static final Pattern TAG_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9-]*$");

    public Validated validate(Customization input) {
        Map<String, String> errors = new LinkedHashMap<>();

        String localizedName = blankToNull(input.localizedName());
        if (localizedName != null && localizedName.length() > MAX_LOCALIZED_NAME) {
            errors.put("localizedName", "must be at most " + MAX_LOCALIZED_NAME + " characters");
        }

        Region region = null;
        String rawRegion = blankToNull(input.region());
        if (rawRegion != null) {
            region = Region.parse(rawRegion).orElse(null);
            if (region == null) {
                errors.put("region", "must be one of " + Arrays.toString(Region.values()));
            }
        }

        Set<String> tags = input.tags().stream()
                .map(CustomizationPolicy::blankToNull)
                .filter(t -> t != null)
                .map(t -> t.toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(TreeSet::new));
        if (tags.size() > MAX_TAGS) {
            errors.put("tags", "must contain at most " + MAX_TAGS + " tags");
        }
        tags.stream()
                .filter(t -> t.length() > MAX_TAG_LENGTH || !TAG_PATTERN.matcher(t).matches())
                .findFirst()
                .ifPresent(t -> errors.put("tags", "invalid tag '" + t + "': use lowercase letters, digits and dashes, max "
                        + MAX_TAG_LENGTH + " characters"));

        String notes = blankToNull(input.notes());
        if (notes != null && notes.length() > MAX_NOTES) {
            errors.put("notes", "must be at most " + MAX_NOTES + " characters");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Invalid Pokemon customization", errors);
        }
        return new Validated(localizedName, region, Set.copyOf(tags), notes);
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record Validated(String localizedName, Region region, Set<String> tags, String notes) {
    }
}
