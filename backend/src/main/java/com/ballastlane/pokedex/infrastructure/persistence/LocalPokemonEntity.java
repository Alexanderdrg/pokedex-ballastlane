package com.ballastlane.pokedex.infrastructure.persistence;

import com.ballastlane.pokedex.domain.model.Region;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "local_pokemon")
public class LocalPokemonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pokeapi_id", nullable = false, unique = true)
    private int pokeApiId;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(name = "image_url", length = 300)
    private String imageUrl;

    @Column(name = "sprite_url", length = 300)
    private String spriteUrl;

    @Column(length = 60)
    private String category;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "local_pokemon_type", joinColumns = @JoinColumn(name = "pokemon_id"))
    @OrderColumn(name = "position")
    @Column(name = "type", nullable = false, length = 30)
    private List<String> types = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "local_pokemon_ability", joinColumns = @JoinColumn(name = "pokemon_id"))
    @OrderColumn(name = "position")
    @Column(name = "ability", nullable = false, length = 60)
    private List<String> abilities = new ArrayList<>();

    @Column(name = "height_m", nullable = false)
    private double heightM;

    @Column(name = "weight_kg", nullable = false)
    private double weightKg;

    @Column(name = "localized_name", length = 60)
    private String localizedName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Region region;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "local_pokemon_tag", joinColumns = @JoinColumn(name = "pokemon_id"))
    @Column(name = "tag", nullable = false, length = 20)
    private Set<String> tags = new HashSet<>();

    @Column(length = 500)
    private String notes;

    @Column(name = "synced_at", nullable = false)
    private Instant syncedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected LocalPokemonEntity() {
    }

    public Long getId() { return id; }
    public int getPokeApiId() { return pokeApiId; }
    public void setPokeApiId(int pokeApiId) { this.pokeApiId = pokeApiId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getSpriteUrl() { return spriteUrl; }
    public void setSpriteUrl(String spriteUrl) { this.spriteUrl = spriteUrl; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public List<String> getTypes() { return types; }
    public void setTypes(List<String> types) { this.types = types; }
    public List<String> getAbilities() { return abilities; }
    public void setAbilities(List<String> abilities) { this.abilities = abilities; }
    public double getHeightM() { return heightM; }
    public void setHeightM(double heightM) { this.heightM = heightM; }
    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }
    public String getLocalizedName() { return localizedName; }
    public void setLocalizedName(String localizedName) { this.localizedName = localizedName; }
    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }
    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getSyncedAt() { return syncedAt; }
    public void setSyncedAt(Instant syncedAt) { this.syncedAt = syncedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }

    static LocalPokemonEntity empty() {
        return new LocalPokemonEntity();
    }
}
