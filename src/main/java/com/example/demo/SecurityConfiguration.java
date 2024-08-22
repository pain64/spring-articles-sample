package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@EnableMethodSecurity @Configuration public class SecurityConfiguration {
    @Bean UserDetailsManager udm() {
        // static users configuration example
        // passwords must be stored in env / properties
        // Another way it to store users in DB with JdbcUserDetailsManager
        return new InMemoryUserDetailsManager(
            User.withUsername("publisher")
                .authorities("ROLE_PUBLISHER")
                .password("{noop}123")
                .build(),
            User.withUsername("admin")
                .authorities("ROLE_ADMIN")
                .password("{noop}321")
                .build()
        );
    }

    @Bean SecurityFilterChain configure(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(c -> c
                .requestMatchers("swagger-ui/**", "v3/api-docs/**").permitAll()
                .requestMatchers("/**").authenticated()
            )
            .formLogin(Customizer.withDefaults())
            .csrf(c -> c.ignoringRequestMatchers("/api/**"))
            .build();
    }
}
