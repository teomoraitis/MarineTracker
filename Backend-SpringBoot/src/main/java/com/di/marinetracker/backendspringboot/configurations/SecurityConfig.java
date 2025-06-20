package com.di.marinetracker.backendspringboot.configurations;

import com.di.marinetracker.backendspringboot.services.UserDetailsServiceImpl;
import com.di.marinetracker.backendspringboot.utils.AuthEntryPointJwt;
import com.di.marinetracker.backendspringboot.utils.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// Enables Spring Security's web security support
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    // Injects the custom UserDetailsService implementation
    @Autowired
    UserDetailsServiceImpl userDetailsService;

    // Injects the custom authentication entry point for handling unauthorized access
    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    // Defines a bean for the JWT authentication filter
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    // Configures the authentication provider with user details service and password encoder
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    // Exposes the authentication manager bean
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Defines the password encoder bean using BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configures CORS (Cross-Origin Resource Sharing) settings for the application
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Create a new CORS configuration object:
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow requests from the specified origin pattern (e.g., React frontend running on localhost:3000):
        configuration.setAllowedOriginPatterns(List.of("https://localhost:3000"));
        // Allow only specific HTTP methods for cross-origin requests:
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Allow all headers in cross-origin requests:
        configuration.setAllowedHeaders(List.of("*"));
        // Allow credentials (such as cookies or authorization headers) to be included in cross-origin requests:
        configuration.setAllowCredentials(true);
        // Set the maximum age (in seconds) for which the CORS response should be cached by the browser:
        configuration.setMaxAge(3600L);

        // Create a source object that maps URL patterns to CORS configurations:
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Register the above configuration for all endpoints (/**):
        source.registerCorsConfiguration("/**", configuration);
        // Return the configured source to be used by Spring Security:
        return source;
    }

    // Configures the security filter chain, including CORS, CSRF, session management, and endpoint authorization
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
       http
               // Enables CORS with the defined configuration:
               .cors(cors -> cors.configurationSource(corsConfigurationSource()))
               // Disables CSRF protection (not needed for stateless APIs):
               .csrf(csrf -> csrf.disable())
               // Sets the entry point for unauthorized requests:
               .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
               // Configures session management to be stateless (no HTTP session):
               .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
               // Allows unauthenticated access to authentication and websocket endpoints:
               .authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/**").permitAll())
               .authorizeHttpRequests(auth -> auth.requestMatchers("/ws/**").permitAll())
               // Requires authentication for all other endpoints:
               .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
               // Adds the JWT authentication filter before the username/password filter:
               .addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class)
               // Sets the custom authentication provider:
               .authenticationProvider(authenticationProvider());

       return http.build();
    }
}
