package com.ballastlane.pokedex.domain.model;

import java.util.List;

/**
 * A node of an evolution tree. Branching evolutions (e.g. Eevee) have several children.
 *
 * @param trigger human readable condition to reach this stage ("Level 16", "Use thunder-stone"), null for the base form
 */
public record EvolutionNode(
        int speciesId,
        String name,
        String imageUrl,
        String trigger,
        List<EvolutionNode> evolvesTo
) {
    public EvolutionNode {
        evolvesTo = List.copyOf(evolvesTo);
    }
}
