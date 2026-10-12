package com.ballastlane.pokedex;

import com.ballastlane.pokedex.domain.model.EvolutionNode;
import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.PokemonDetail;
import com.ballastlane.pokedex.domain.model.PokemonStat;
import com.ballastlane.pokedex.domain.model.PokemonSummary;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public final class TestData {

    public static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private TestData() {
    }

    public static PokemonDetail pikachuDetail() {
        EvolutionNode raichu = new EvolutionNode(26, "raichu", "raichu.png", "Use Thunder stone", List.of());
        EvolutionNode pikachu = new EvolutionNode(25, "pikachu", "pikachu.png", null, List.of(raichu));
        EvolutionNode pichu = new EvolutionNode(172, "pichu", "pichu.png", null, List.of(pikachu));
        return new PokemonDetail(25, "pikachu", "pikachu-art.png", "pikachu-sprite.png", "Mouse Pokémon",
                "It keeps its tail raised to monitor its surroundings.", List.of("electric"),
                List.of("static", "lightning-rod"), 0.4, 6.0,
                List.of(new PokemonStat("hp", 35), new PokemonStat("speed", 90)), pichu);
    }

    public static PokemonSummary pikachuSummary() {
        return new PokemonSummary(25, "pikachu", "pikachu-sprite.png", "Mouse Pokémon", List.of("electric"), 6.0,
                List.of("static", "lightning-rod"));
    }

    public static LocalPokemon storedPikachu(long id) {
        return new LocalPokemon(id, 25, "pikachu", "pikachu-art.png", "pikachu-sprite.png", "Mouse Pokémon",
                List.of("electric"), List.of("static"), 0.4, 6.0, "Pikachu (JP)", null, Set.of("mascot"), null,
                NOW, NOW, 3);
    }
}
