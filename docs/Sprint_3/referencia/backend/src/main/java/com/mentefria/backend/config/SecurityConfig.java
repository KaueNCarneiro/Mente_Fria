package com.mentefria.backend.config;

import com.mentefria.backend.auth.JwtAuthFilter;
import com.mentefria.backend.auth.JwtService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
    private static final String ADMIN = "ADMINISTRADOR";
    private static final String JSON = "application/json;charset=UTF-8";

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") List<String> origens) {
        var cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(origens);
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cfg.setMaxAge(3600L);
        var fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/api/**", cfg);
        return fonte;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtService jwt, UrlBasedCorsConfigurationSource cors, Environment env) throws Exception {
        boolean dev = env.acceptsProfiles(Profiles.of("dev"));
        http.cors(c -> c.configurationSource(cors))
            .csrf(csrf -> csrf.disable())                         // sem cookie/sessão: não há CSRF
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) -> { res.setStatus(401); res.setContentType(JSON);
                    res.getWriter().write("{\"mensagem\":\"Sua sessão expirou ou você não está autenticado.\"}"); })
                .accessDeniedHandler((req, res, ex) -> { res.setStatus(403); res.setContentType(JSON);
                    res.getWriter().write("{\"mensagem\":\"Você não tem permissão para esta ação.\"}"); }))
            .authorizeHttpRequests(a -> {
                a.requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll();
                a.requestMatchers(HttpMethod.GET, "/api/saude").permitAll();
                a.requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/registro").permitAll();
                if (dev) a.requestMatchers("/api/teste/**").permitAll();       // F4: só no perfil dev
                a.requestMatchers("/api/usuarios/**", "/api/unidades-medida/**", "/api/producao/**",
                                  "/api/reposicao/**", "/api/marketing/**", "/api/dashboard/**").hasRole(ADMIN);
                a.requestMatchers(HttpMethod.POST, "/api/ingredientes/**", "/api/produtos/**").hasRole(ADMIN);
                a.requestMatchers(HttpMethod.PUT, "/api/ingredientes/**", "/api/produtos/**").hasRole(ADMIN);
                a.requestMatchers(HttpMethod.PATCH, "/api/ingredientes/**", "/api/produtos/**").hasRole(ADMIN);
                a.anyRequest().authenticated();
            })
            .addFilterBefore(new JwtAuthFilter(jwt), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
