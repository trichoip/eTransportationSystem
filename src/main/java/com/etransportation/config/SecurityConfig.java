package com.etransportation.config;

import com.etransportation.security.jwt.JwtAccessDeniedHandler;
import com.etransportation.security.jwt.JwtAuthEntryPoint;
import com.etransportation.security.jwt.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
            .anyRequest()
            .authenticated()
            .and()
            .build();
    }
}
