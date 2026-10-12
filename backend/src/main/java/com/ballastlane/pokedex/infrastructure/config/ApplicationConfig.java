package com.ballastlane.pokedex.infrastructure.config;

import com.ballastlane.pokedex.application.port.LocalPokemonRepository;
import com.ballastlane.pokedex.application.port.PasswordHasher;
import com.ballastlane.pokedex.application.port.PokemonCatalog;
import com.ballastlane.pokedex.application.port.TokenIssuer;
import com.ballastlane.pokedex.application.port.UserRepository;
import com.ballastlane.pokedex.application.usecase.AuthUseCase;
import com.ballastlane.pokedex.application.usecase.BrowsePokemonUseCase;
import com.ballastlane.pokedex.application.usecase.GetPokemonDetailUseCase;
import com.ballastlane.pokedex.application.usecase.ManageLocalPokemonUseCase;
import com.ballastlane.pokedex.application.usecase.SyncPokemonUseCase;
import com.ballastlane.pokedex.domain.policy.CredentialsPolicy;
import com.ballastlane.pokedex.domain.policy.CustomizationPolicy;
import com.ballastlane.pokedex.infrastructure.pokeapi.PokeApiCatalogAdapter;
import com.ballastlane.pokedex.infrastructure.pokeapi.PokeApiClient;
import com.ballastlane.pokedex.infrastructure.pokeapi.PokeApiProperties;
import com.ballastlane.pokedex.infrastructure.security.BCryptPasswordHasher;
import com.ballastlane.pokedex.infrastructure.security.JwtProperties;
import com.ballastlane.pokedex.infrastructure.security.JwtTokenService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Clock;

/**
 * Composition root: wires framework-free use cases with their infrastructure adapters.
 * Use cases are plain Java classes, so they are declared here instead of being annotated.
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties({PokeApiProperties.class, JwtProperties.class})
public class ApplicationConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    // ---- domain policies
    @Bean
    CustomizationPolicy customizationPolicy() {
        return new CustomizationPolicy();
    }

    @Bean
    CredentialsPolicy credentialsPolicy() {
        return new CredentialsPolicy();
    }

    // ---- PokeAPI adapter
    @Bean
    PokeApiClient pokeApiClient(PokeApiProperties props, RestClient.Builder builder) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(props.connectTimeout()).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(props.readTimeout());
        return new PokeApiClient(builder.baseUrl(props.baseUrl()).requestFactory(factory).build());
    }

    @Bean
    ThreadPoolTaskExecutor pokeApiExecutor(PokeApiProperties props) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(props.parallelism());
        executor.setMaxPoolSize(props.parallelism());
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("pokeapi-");
        executor.initialize();
        return executor;
    }

    @Bean
    PokemonCatalog pokemonCatalog(PokeApiClient client, ThreadPoolTaskExecutor pokeApiExecutor) {
        return new PokeApiCatalogAdapter(client, pokeApiExecutor);
    }

    // ---- security adapters
    @Bean
    JwtTokenService jwtTokenService(JwtProperties props, Clock clock) {
        return new JwtTokenService(props, clock);
    }

    @Bean
    PasswordHasher passwordHasher() {
        return new BCryptPasswordHasher();
    }

    // ---- use cases
    @Bean
    BrowsePokemonUseCase browsePokemonUseCase(PokemonCatalog catalog) {
        return new BrowsePokemonUseCase(catalog);
    }

    @Bean
    GetPokemonDetailUseCase getPokemonDetailUseCase(PokemonCatalog catalog) {
        return new GetPokemonDetailUseCase(catalog);
    }

    @Bean
    SyncPokemonUseCase syncPokemonUseCase(PokemonCatalog catalog, LocalPokemonRepository repository,
                                          CustomizationPolicy policy, Clock clock) {
        return new SyncPokemonUseCase(catalog, repository, policy, clock);
    }

    @Bean
    ManageLocalPokemonUseCase manageLocalPokemonUseCase(LocalPokemonRepository repository,
                                                        CustomizationPolicy policy, Clock clock) {
        return new ManageLocalPokemonUseCase(repository, policy, clock);
    }

    @Bean
    AuthUseCase authUseCase(UserRepository users, PasswordHasher hasher, TokenIssuer tokens,
                            CredentialsPolicy policy, Clock clock) {
        return new AuthUseCase(users, hasher, tokens, policy, clock);
    }
}
