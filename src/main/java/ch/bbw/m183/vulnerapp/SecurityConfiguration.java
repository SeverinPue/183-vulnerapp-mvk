package ch.bbw.m183.vulnerapp;

import ch.bbw.m183.vulnerapp.service.RestfulFormService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories
                .createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, RestfulFormService restfulFormService) {
        return http.formLogin(restfulFormService.restfulFormLogin())
                .exceptionHandling(restfulFormService.unauthorizedPerDefault())
                .csrf(x -> x.spa().ignoringRequestMatchers("/login"))
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers(HttpMethod.POST, "/api/blog")
                                .hasAnyRole("EDITOR", "ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/blog")
                                .hasRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/user/whoami")
                                .authenticated()
                                .anyRequest()
                                .permitAll())
                .build();
    }
}
