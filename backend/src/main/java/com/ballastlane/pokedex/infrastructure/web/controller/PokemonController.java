package com.ballastlane.pokedex.infrastructure.web.controller;

import com.ballastlane.pokedex.application.usecase.BrowsePokemonUseCase;
import com.ballastlane.pokedex.application.usecase.GetPokemonDetailUseCase;
import com.ballastlane.pokedex.infrastructure.web.dto.PageResponse;
import com.ballastlane.pokedex.infrastructure.web.dto.PokemonDetailResponse;
import com.ballastlane.pokedex.infrastructure.web.dto.PokemonSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public catalog backed by PokeAPI (User Stories 01 and 02).
 */
@RestController
@RequestMapping("/api/pokemon")
@Tag(name = "Catalog", description = "Pokemon catalog backed by PokeAPI")
public class PokemonController {

    private final BrowsePokemonUseCase browse;
    private final GetPokemonDetailUseCase detail;

    public PokemonController(BrowsePokemonUseCase browse, GetPokemonDetailUseCase detail) {
        this.browse = browse;
        this.detail = detail;
    }

    @GetMapping
    @Operation(summary = "US01 - Paginated list with sprite, category, weight and abilities")
    public PageResponse<PokemonSummaryResponse> list(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(browse.browse(page, size), PokemonSummaryResponse::from);
    }

    @GetMapping("/{idOrName}")
    @Operation(summary = "US02 - Detail with image, stats, description and evolution chain")
    public PokemonDetailResponse get(@PathVariable String idOrName) {
        return PokemonDetailResponse.from(detail.get(idOrName));
    }
}
