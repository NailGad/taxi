package com.taxi.user.config;

import com.taxi.user.security.JwtAuthenticationFilter;
import com.taxi.user.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenService jwtTokenService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenService, Arrays.asList(publicApiMatchers()));
    }

    /**
     * Paths callable without Bearer (inter-service + login/register). Uses {@link AntPathRequestMatcher}
     * so authorization works with multiple servlets (e.g. H2 console + DispatcherServlet).
     */
    private static RequestMatcher[] publicApiMatchers() {
        return new RequestMatcher[]{
                AntPathRequestMatcher.antMatcher("/auth/**"),
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/passengers"),
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/drivers"),
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/drivers/claim-available"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/passengers/*/exists"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/drivers/*/exists"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/drivers/available"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/drivers/available/list"),
                AntPathRequestMatcher.antMatcher(HttpMethod.PATCH, "/drivers/*/status")
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicApiMatchers()).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
