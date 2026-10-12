package com.ballastlane.pokedex.infrastructure.web.controller;

import com.ballastlane.pokedex.application.usecase.ManageLocalPokemonUseCase;
import com.ballastlane.pokedex.application.usecase.SyncPokemonUseCase;
import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.infrastructure.web.ETags;
import com.ballastlane.pokedex.infrastructure.web.dto.CustomizationRequest;
import com.ballastlane.pokedex.infrastructure.web.dto.ImportPokemonRequest;
import com.ballastlane.pokedex.infrastructure.web.dto.LocalPokemonResponse;
import com.ballastlane.pokedex.infrastructure.web.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * CRUD over the local replica (User Stories 03 and 04). Requires authentication.
 */
@RestController
@RequestMapping("/api/local-pokemon")
@Tag(name = "Local Pokemon", description = "Local replica with proprietary fields")
@SecurityRequirement(name = "bearer")
public class LocalPokemonController {

    private final SyncPokemonUseCase sync;
    private final ManageLocalPokemonUseCase manage;

    public LocalPokemonController(SyncPokemonUseCase sync, ManageLocalPokemonUseCase manage) {
        this.sync = sync;
        this.manage = manage;
    }

    @GetMapping
    @Operation(summary = "List locally stored Pokemon (optional search by name / localized name)")
    public PageResponse<LocalPokemonResponse> list(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @RequestParam(required = false) String search) {
        return PageResponse.from(manage.list(page, size, search), LocalPokemonResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a locally stored Pokemon (the ETag header is its current version)")
    public ResponseEntity<LocalPokemonResponse> get(@PathVariable long id) {
        return withETag(ResponseEntity.ok(), manage.get(id));
    }

    @PostMapping
    @Operation(summary = "US03 - Import a Pokemon from PokeAPI into the local database")
    public ResponseEntity<LocalPokemonResponse> importPokemon(@Valid @RequestBody ImportPokemonRequest request) {
        LocalPokemon saved = sync.importPokemon(request.idOrName(), request.customization());
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId())
                .toUri();
        return withETag(ResponseEntity.created(location), saved);
    }

    @PostMapping("/{id}/sync")
    @Operation(summary = "US03 - Refresh catalog data from PokeAPI keeping proprietary fields")
    public ResponseEntity<LocalPokemonResponse> refresh(@PathVariable long id) {
        return withETag(ResponseEntity.ok(), sync.refresh(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "US04 - Update proprietary fields",
            description = "Requires If-Match with the ETag you read: 428 if missing, 412 if someone saved a newer "
                    + "version, 404 if the Pokemon does not exist, 400 if the body is invalid")
    public ResponseEntity<LocalPokemonResponse> update(@PathVariable long id,
                                                       @RequestHeader(ETags.IF_MATCH) String ifMatch,
                                                       @Valid @RequestBody CustomizationRequest request) {
        LocalPokemon saved = manage.update(id, ETags.parseIfMatch(ifMatch), request.toDomain());
        return withETag(ResponseEntity.ok(), saved);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a locally stored Pokemon (ADMIN only)")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        manage.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static ResponseEntity<LocalPokemonResponse> withETag(ResponseEntity.BodyBuilder builder, LocalPokemon p) {
        return builder.eTag(ETags.of(p.getVersion())).body(LocalPokemonResponse.from(p));
    }
}
