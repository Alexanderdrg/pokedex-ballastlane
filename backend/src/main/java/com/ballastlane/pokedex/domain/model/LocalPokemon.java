package com.ballastlane.pokedex.domain.model;

import com.ballastlane.pokedex.domain.exception.StaleVersionException;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Local replica of a PokeAPI Pokemon enriched with proprietary fields.
 */
public final class LocalPokemon {

    private final Long id;
    private final int pokeApiId;
    private String name;
    private String imageUrl;
    private String spriteUrl;
    private String category;
    private List<String> types;
    private List<String> abilities;
    private double heightM;
    private double weightKg;

    private String localizedName;
    private Region region;
    private Set<String> tags;
    private String notes;

    private Instant syncedAt;
    private Instant updatedAt;
    // Optimistic concurrency token: the version the caller read. Never changed by the domain itself.
    private final long version;

    @SuppressWarnings("java:S107")
    public LocalPokemon(Long id, int pokeApiId, String name, String imageUrl, String spriteUrl, String category,
                        List<String> types, List<String> abilities, double heightM, double weightKg,
                        String localizedName, Region region, Set<String> tags, String notes,
                        Instant syncedAt, Instant updatedAt, long version) {
        this.id = id;
        this.pokeApiId = pokeApiId;
        this.name = Objects.requireNonNull(name, "name");
        this.imageUrl = imageUrl;
        this.spriteUrl = spriteUrl;
        this.category = category;
        this.types = List.copyOf(types);
        this.abilities = List.copyOf(abilities);
        this.heightM = heightM;
        this.weightKg = weightKg;
        this.localizedName = localizedName;
        this.region = region;
        this.tags = new TreeSet<>(tags == null ? Set.of() : tags);
        this.notes = notes;
        this.syncedAt = syncedAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    /** Creates a brand new (not yet persisted) local copy from catalog data. */
    public static LocalPokemon fromCatalog(PokemonDetail detail, Instant now) {
        return new LocalPokemon(null, detail.id(), detail.name(), detail.imageUrl(), detail.spriteUrl(),
                detail.category(), detail.types(), detail.abilities(), detail.heightM(), detail.weightKg(),
                null, null, Set.of(), null, now, now, 0);
    }

    /** Re-applies catalog data while preserving the proprietary fields. */
    public void refreshFrom(PokemonDetail detail, Instant now) {
        if (detail.id() != pokeApiId) {
            throw new IllegalArgumentException("Catalog data belongs to a different Pokemon");
        }
        this.name = detail.name();
        this.imageUrl = detail.imageUrl();
        this.spriteUrl = detail.spriteUrl();
        this.category = detail.category();
        this.types = List.copyOf(detail.types());
        this.abilities = List.copyOf(detail.abilities());
        this.heightM = detail.heightM();
        this.weightKg = detail.weightKg();
        this.syncedAt = now;
        this.updatedAt = now;
    }

    /**
     * Guards against lost updates: the caller must prove it edited the latest version.
     */
    public void ensureVersion(long expectedVersion) {
        if (expectedVersion != version) {
            throw new StaleVersionException(id, expectedVersion, version);
        }
    }

    /** Applies already validated proprietary fields. */
    public void customize(String localizedName, Region region, Set<String> tags, String notes, Instant now) {
        this.localizedName = localizedName;
        this.region = region;
        this.tags = new TreeSet<>(tags);
        this.notes = notes;
        this.updatedAt = now;
    }

    public Long getId() { return id; }
    public int getPokeApiId() { return pokeApiId; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public String getSpriteUrl() { return spriteUrl; }
    public String getCategory() { return category; }
    public List<String> getTypes() { return types; }
    public List<String> getAbilities() { return abilities; }
    public double getHeightM() { return heightM; }
    public double getWeightKg() { return weightKg; }
    public String getLocalizedName() { return localizedName; }
    public Region getRegion() { return region; }
    public Set<String> getTags() { return Set.copyOf(tags); }
    public String getNotes() { return notes; }
    public Instant getSyncedAt() { return syncedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
}
