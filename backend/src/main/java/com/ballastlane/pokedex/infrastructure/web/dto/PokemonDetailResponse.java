package com.ballastlane.pokedex.infrastructure.web.dto;

import com.ballastlane.pokedex.domain.model.EvolutionNode;
import com.ballastlane.pokedex.domain.model.PokemonDetail;

import java.util.List;

public record PokemonDetailResponse(int id, String name, String imageUrl, String spriteUrl, String category,
                                    String description, List<String> types, List<String> abilities, double heightM,
                                    double weightKg, List<Stat> stats, Evolution evolutionChain) {

    public record Stat(String name, int baseValue) {
    }

    public record Evolution(int speciesId, String name, String imageUrl, String trigger, List<Evolution> evolvesTo) {
        static Evolution from(EvolutionNode node) {
            return node == null ? null : new Evolution(node.speciesId(), node.name(), node.imageUrl(), node.trigger(),
                    node.evolvesTo().stream().map(Evolution::from).toList());
        }
    }

    public static PokemonDetailResponse from(PokemonDetail p) {
        return new PokemonDetailResponse(p.id(), p.name(), p.imageUrl(), p.spriteUrl(), p.category(),
                p.description(), p.types(), p.abilities(), p.heightM(), p.weightKg(),
                p.stats().stream().map(s -> new Stat(s.name(), s.baseValue())).toList(),
                Evolution.from(p.evolutionChain()));
    }
}
