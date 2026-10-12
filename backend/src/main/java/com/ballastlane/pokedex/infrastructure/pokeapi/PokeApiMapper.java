package com.ballastlane.pokedex.infrastructure.pokeapi;

import com.ballastlane.pokedex.domain.model.EvolutionNode;
import com.ballastlane.pokedex.domain.model.PokemonDetail;
import com.ballastlane.pokedex.domain.model.PokemonStat;
import com.ballastlane.pokedex.domain.model.PokemonSummary;
import com.ballastlane.pokedex.infrastructure.pokeapi.dto.EvolutionChainResponse;
import com.ballastlane.pokedex.infrastructure.pokeapi.dto.EvolutionChainResponse.ChainLink;
import com.ballastlane.pokedex.infrastructure.pokeapi.dto.EvolutionChainResponse.EvolutionDetail;
import com.ballastlane.pokedex.infrastructure.pokeapi.dto.NamedResource;
import com.ballastlane.pokedex.infrastructure.pokeapi.dto.PokemonResponse;
import com.ballastlane.pokedex.infrastructure.pokeapi.dto.SpeciesResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Translates PokeAPI payloads into domain models.
 */
final class PokeApiMapper {

    static final String ARTWORK_URL =
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/%d.png";
    private static final String ENGLISH = "en";

    private PokeApiMapper() {
    }

    static PokemonSummary toSummary(PokemonResponse pokemon, SpeciesResponse species) {
        return new PokemonSummary(
                pokemon.id(),
                pokemon.name(),
                spriteUrl(pokemon),
                genus(species),
                types(pokemon),
                pokemon.weight() / 10.0,
                abilities(pokemon));
    }

    static PokemonDetail toDetail(PokemonResponse pokemon, SpeciesResponse species, EvolutionChainResponse chain) {
        List<PokemonStat> stats = nullSafe(pokemon.stats()).stream()
                .map(s -> new PokemonStat(s.stat().name(), s.baseStat()))
                .toList();
        return new PokemonDetail(
                pokemon.id(),
                pokemon.name(),
                imageUrl(pokemon),
                spriteUrl(pokemon),
                genus(species),
                description(species),
                types(pokemon),
                abilities(pokemon),
                pokemon.height() / 10.0,
                pokemon.weight() / 10.0,
                stats,
                chain == null ? null : toEvolutionNode(chain.chain()));
    }

    static int idFromUrl(String url) {
        if (url == null) {
            throw new IllegalArgumentException("Resource URL is missing");
        }
        String trimmed = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        return Integer.parseInt(trimmed.substring(trimmed.lastIndexOf('/') + 1));
    }

    private static EvolutionNode toEvolutionNode(ChainLink link) {
        int speciesId = idFromUrl(link.species().url());
        List<EvolutionNode> children = nullSafe(link.evolvesTo()).stream()
                .map(PokeApiMapper::toEvolutionNode)
                .toList();
        return new EvolutionNode(
                speciesId,
                link.species().name(),
                ARTWORK_URL.formatted(speciesId),
                describeTrigger(nullSafe(link.evolutionDetails())),
                children);
    }

    /** Builds a short human readable description of how the evolution happens. */
    static String describeTrigger(List<EvolutionDetail> details) {
        if (details.isEmpty()) {
            return null;
        }
        EvolutionDetail d = details.get(0);
        String trigger = d.trigger() == null ? "" : d.trigger().name();
        List<String> parts = new ArrayList<>();
        switch (trigger) {
            case "level-up" -> {
                if (d.minLevel() != null) {
                    parts.add("Level " + d.minLevel());
                }
                if (d.minHappiness() != null) {
                    parts.add("High friendship");
                }
                if (d.knownMove() != null) {
                    parts.add("Knows " + humanize(d.knownMove().name()));
                }
                if (d.knownMoveType() != null) {
                    parts.add("Knows a " + humanize(d.knownMoveType().name()) + " move");
                }
                if (d.location() != null) {
                    parts.add("At " + humanize(d.location().name()));
                }
                if (parts.isEmpty()) {
                    parts.add("Level up");
                }
            }
            case "use-item" -> parts.add(d.item() == null ? "Use item" : "Use " + humanize(d.item().name()));
            case "trade" -> parts.add(d.heldItem() == null ? "Trade" : "Trade holding " + humanize(d.heldItem().name()));
            default -> parts.add(trigger.isEmpty() ? "Special" : humanize(trigger));
        }
        if (d.timeOfDay() != null && !d.timeOfDay().isBlank()) {
            parts.add("(" + d.timeOfDay() + ")");
        }
        return String.join(" ", parts);
    }

    private static String humanize(String slug) {
        String text = slug.replace('-', ' ');
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private static String spriteUrl(PokemonResponse pokemon) {
        return pokemon.sprites() == null ? null : pokemon.sprites().frontDefault();
    }

    private static String imageUrl(PokemonResponse pokemon) {
        if (pokemon.sprites() != null && pokemon.sprites().other() != null
                && pokemon.sprites().other().officialArtwork() != null
                && pokemon.sprites().other().officialArtwork().frontDefault() != null) {
            return pokemon.sprites().other().officialArtwork().frontDefault();
        }
        return spriteUrl(pokemon);
    }

    private static List<String> types(PokemonResponse pokemon) {
        return nullSafe(pokemon.types()).stream()
                .sorted(Comparator.comparingInt(PokemonResponse.TypeSlot::slot))
                .map(t -> t.type().name())
                .toList();
    }

    private static List<String> abilities(PokemonResponse pokemon) {
        return nullSafe(pokemon.abilities()).stream()
                .sorted(Comparator.comparingInt(PokemonResponse.AbilitySlot::slot))
                .map(a -> a.ability().name())
                .toList();
    }

    private static String genus(SpeciesResponse species) {
        if (species == null) {
            return null;
        }
        return nullSafe(species.genera()).stream()
                .filter(g -> isEnglish(g.language()))
                .map(SpeciesResponse.Genus::genus)
                .findFirst()
                .orElse(null);
    }

    private static String description(SpeciesResponse species) {
        if (species == null) {
            return null;
        }
        return nullSafe(species.flavorTextEntries()).stream()
                .filter(f -> isEnglish(f.language()))
                .map(SpeciesResponse.FlavorText::flavorText)
                .filter(Objects::nonNull)
                .findFirst()
                // PokeAPI flavor texts contain form feeds, line breaks and soft hyphens from the game cartridges
                .map(text -> text.replaceAll("[\\f\\n\\r\\u00AD]+", " ").replaceAll("\\s{2,}", " ").trim())
                .orElse(null);
    }

    private static boolean isEnglish(NamedResource language) {
        return language != null && ENGLISH.equals(language.name());
    }

    private static <T> List<T> nullSafe(List<T> list) {
        return list == null ? List.of() : list;
    }
}
