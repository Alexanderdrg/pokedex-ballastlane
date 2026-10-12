package com.ballastlane.pokedex.infrastructure.pokeapi;

import com.ballastlane.pokedex.domain.exception.ExternalServiceException;
import com.ballastlane.pokedex.infrastructure.pokeapi.dto.EvolutionChainResponse;
import com.ballastlane.pokedex.infrastructure.pokeapi.dto.PokemonListResponse;
import com.ballastlane.pokedex.infrastructure.pokeapi.dto.PokemonResponse;
import com.ballastlane.pokedex.infrastructure.pokeapi.dto.SpeciesResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Thin HTTP client for PokeAPI. Every call is cached (Caffeine) because PokeAPI data is
 * practically static and the service asks consumers to cache responses.
 * Caching lives here (not in the adapter) so the Spring proxy is hit on every call.
 */
public class PokeApiClient {

    private final RestClient restClient;

    public PokeApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Cacheable(cacheNames = "pokeapi-list", key = "#offset + ':' + #limit")
    public PokemonListResponse listPokemon(int offset, int limit) {
        return get("/pokemon?offset={offset}&limit={limit}", PokemonListResponse.class, offset, limit);
    }

    @Cacheable(cacheNames = "pokeapi-pokemon")
    public PokemonResponse getPokemon(String idOrName) {
        return get("/pokemon/{id}", PokemonResponse.class, idOrName);
    }

    @Cacheable(cacheNames = "pokeapi-species")
    public SpeciesResponse getSpecies(int speciesId) {
        return get("/pokemon-species/{id}", SpeciesResponse.class, speciesId);
    }

    @Cacheable(cacheNames = "pokeapi-evolution")
    public EvolutionChainResponse getEvolutionChain(int chainId) {
        return get("/evolution-chain/{id}", EvolutionChainResponse.class, chainId);
    }

    private <T> T get(String path, Class<T> type, Object... uriVariables) {
        try {
            T body = restClient.get()
                    .uri(path, uriVariables)
                    .retrieve()
                    .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(), (req, res) -> {
                        throw new PokeApiNotFoundException(req.getURI().getPath());
                    })
                    .body(type);
            if (body == null) {
                throw new ExternalServiceException("Empty response from PokeAPI " + path, null);
            }
            return body;
        } catch (PokeApiNotFoundException | ExternalServiceException e) {
            throw e;
        } catch (RestClientException e) {
            throw new ExternalServiceException("PokeAPI is unavailable, please try again later", e);
        }
    }
}
