package com.ballastlane.pokedex.infrastructure.web.dto;

import com.ballastlane.pokedex.domain.model.Customization;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Body to replicate a catalog Pokemon locally, optionally with proprietary fields.
 */
public record ImportPokemonRequest(
        @NotBlank @Size(max = 50) String idOrName,
        @Size(max = 60) String localizedName,
        @Size(max = 20) String region,
        @Size(max = 10) Set<String> tags,
        @Size(max = 500) String notes
) {
    public Customization customization() {
        return new Customization(localizedName, region, tags, notes);
    }
}
