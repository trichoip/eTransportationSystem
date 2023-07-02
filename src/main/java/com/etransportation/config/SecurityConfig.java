package com.etransportation.config;

import com.etransportation.security.jwt.JwtAccessDeniedHandler;
import com.etransportation.security.jwt.JwtAuthEntryPoint;
import com.etransportation.security.jwt.JwtFilter;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final JwtAuthEntryPoint unauthorizedHandler;
    private final JwtAccessDeniedHandler accessDeniedHandler;
    private final CorsConfig corsConfig;

    private static final String[] AUTH_WHITELIST = {
        "/swagger-resources/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v2/api-docs",
        "/webjars/**",
        "/api/auth/**",
        "/oauth2/**",
        "/**/*swagger*/**",
        "/info",
        "/api/faker/**",
        "/",
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .cors()
            .and()
            .csrf()
            .disable()
            .addFilter(corsConfig.corsFilter())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .exceptionHandling()
            .authenticationEntryPoint(unauthorizedHandler)
            .and()
            .exceptionHandling()
            .accessDeniedHandler(accessDeniedHandler)
            .and()
            .authorizeHttpRequests()
            .antMatchers(AUTH_WHITELIST)
            .permitAll()
            .antMatchers("/api/account/**")
            .permitAll()
            .antMatchers("/api/car/**")
            .permitAll()
            .antMatchers("/api/city/**")
            .permitAll()
            .antMatchers("/api/book/**")
            .permitAll()
            .antMatchers("/api/check/**")
            .permitAll()
            .antMatchers("/api/admin/**")
            .permitAll()
            .antMatchers("/api/oauth2/**")
            .permitAll()
            .antMatchers("/api/voucher/**")
            .permitAll()
            .antMatchers("/api/feature/**")
            .permitAll()
            .antMatchers("/api/like/**")
            .permitAll()
            .antMatchers("/api/management/**")
            .hasAuthority("MANAGER")
            .antMatchers("/api/employee/**")
            .hasAnyAuthority("MANAGER", "ADMIN")
            .antMatchers("/api/timekeeping/**")
            .hasAnyAuthority("MANAGER", "ADMIN")
            .anyRequest()
            .authenticated()
            .and()
            .build();
    }

    // @Bean
    public SecurityFilterChain securityFilterChain2(HttpSecurity http) throws Exception {
        return http
            .cors(cors ->
                cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(Collections.singletonList("*"));
                    config.setAllowedMethods(Collections.singletonList("*"));
                    config.setAllowedHeaders(Collections.singletonList("*"));
                    config.setAllowCredentials(true);
                    config.addAllowedOriginPattern("*");
                    return config;
                })
            )
            .csrf(AbstractHttpConfigurer::disable)
            // .addFilter(corsConfig.corsFilter())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(handling -> {
                handling.authenticationEntryPoint(unauthorizedHandler);
                handling.accessDeniedHandler(accessDeniedHandler);
                // ================================================================
                handling.authenticationEntryPoint(unauthorizedHandler).accessDeniedHandler(accessDeniedHandler);
            })
            .exceptionHandling(handling -> handling.authenticationEntryPoint(unauthorizedHandler).accessDeniedHandler(accessDeniedHandler))
            .authorizeHttpRequests(auth -> {
                auth.antMatchers(AUTH_WHITELIST).permitAll();
                auth.antMatchers("/api/account/**").permitAll();
                auth.antMatchers("/api/car/**").permitAll();
                auth.antMatchers("/api/city/**").permitAll();
                auth.antMatchers("/api/book/**").permitAll();
                auth.antMatchers("/api/check/**").permitAll();
                auth.antMatchers("/api/admin/**").permitAll();
                auth.antMatchers("/api/oauth2/**").permitAll();
                auth.antMatchers("/api/voucher/**").permitAll();
                auth.antMatchers("/api/feature/**").permitAll();
                auth.antMatchers("/api/like/**").permitAll();
                auth.antMatchers("/api/management/**").hasAuthority("MANAGER");
                auth.antMatchers("/api/employee/**").hasAnyAuthority("MANAGER", "ADMIN");
                auth.antMatchers("/api/timekeeping/**").hasAnyAuthority("MANAGER", "ADMIN");
                auth.anyRequest().authenticated();

                // ================================================================
                auth
                    .antMatchers(AUTH_WHITELIST)
                    .permitAll()
                    .antMatchers("/api/account/**")
                    .permitAll()
                    .antMatchers("/api/car/**")
                    .permitAll()
                    .antMatchers("/api/city/**")
                    .permitAll()
                    .antMatchers("/api/book/**")
                    .permitAll()
                    .antMatchers("/api/check/**")
                    .permitAll()
                    .antMatchers("/api/admin/**")
                    .permitAll()
                    .antMatchers("/api/oauth2/**")
                    .permitAll()
                    .antMatchers("/api/voucher/**")
                    .permitAll()
                    .antMatchers("/api/feature/**")
                    .permitAll()
                    .antMatchers("/api/like/**")
                    .permitAll()
                    .antMatchers("/api/management/**")
                    .hasAuthority("MANAGER")
                    .antMatchers("/api/employee/**")
                    .hasAnyAuthority("MANAGER", "ADMIN")
                    .antMatchers("/api/timekeeping/**")
                    .hasAnyAuthority("MANAGER", "ADMIN")
                    .anyRequest()
                    .authenticated();
            })
            .authorizeHttpRequests(auth ->
                auth
                    .antMatchers(AUTH_WHITELIST)
                    .permitAll()
                    .antMatchers("/api/account/**")
                    .permitAll()
                    .antMatchers("/api/car/**")
                    .permitAll()
                    .antMatchers("/api/city/**")
                    .permitAll()
                    .antMatchers("/api/book/**")
                    .permitAll()
                    .antMatchers("/api/check/**")
                    .permitAll()
                    .antMatchers("/api/admin/**")
                    .permitAll()
                    .antMatchers("/api/oauth2/**")
                    .permitAll()
                    .antMatchers("/api/voucher/**")
                    .permitAll()
                    .antMatchers("/api/feature/**")
                    .permitAll()
                    .antMatchers("/api/like/**")
                    .permitAll()
                    .antMatchers("/api/management/**")
                    .hasAuthority("MANAGER")
                    .antMatchers("/api/employee/**")
                    .hasAnyAuthority("MANAGER", "ADMIN")
                    .antMatchers("/api/timekeeping/**")
                    .hasAnyAuthority("MANAGER", "ADMIN")
                    .anyRequest()
                    .authenticated()
            )
            .build();
    }
}
