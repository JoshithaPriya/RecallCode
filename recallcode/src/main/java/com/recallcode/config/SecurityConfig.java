
package com.recallcode.config;

import com.recallcode.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Wires the application's UserDetailsService and BCrypt encoder into
     * Spring Security's authentication provider.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
            DaoAuthenticationProvider provider =
            new DaoAuthenticationProvider(userDetailsService);

           provider.setPasswordEncoder(passwordEncoder());

    return provider;
}

    /**
     * Exposes the AuthenticationManager as a bean so it can be injected
     * into UserController for the custom JSON login endpoint.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Main security filter chain.
     *
     * CSRF strategy:
     * - CookieCsrfTokenRepository writes the token into a cookie named XSRF-TOKEN
     *   with HttpOnly=false so JavaScript can read it.
     * - JavaScript reads the cookie and sends it as the X-XSRF-TOKEN request header.
     * - Spring Security 7 uses deferred CSRF tokens; the EagerCsrfTokenFilter below
     *   forces the token to be materialised on every request so the cookie is always
     *   present for JS before it makes its first POST.
     * - CsrfTokenRequestAttributeHandler (not XorCsrfTokenRequestAttributeHandler)
     *   is used so the raw token value is compared directly with the header value
     *   sent by JavaScript, without XOR masking. CookieCsrfTokenRepository handles
     *   its own masking internally in Spring Security 7.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // Use the plain attribute handler so JS can send the raw cookie value back.
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();

        http
            .authenticationProvider(authenticationProvider())

            // ── CSRF ──────────────────────────────────────────────────────────
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(requestHandler)
            )

            // ── Eager CSRF token filter ────────────────────────────────────────
            // Forces Spring Security to write the XSRF-TOKEN cookie on every
            // request, not just on requests that go through a CSRF-aware path.
            .addFilterAfter(new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        FilterChain filterChain) throws ServletException, IOException {

                    // Accessing the token causes the deferred token to be resolved
                    // and the cookie to be written to the response.
                    CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                    if (token != null) {
                        token.getToken(); // triggers cookie write
                    }
                    filterChain.doFilter(request, response);
                }
            }, BasicAuthenticationFilter.class)

            // ── Route security ─────────────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // Static resources
                .requestMatchers("/css/**", "/js/**", "/favicon.ico").permitAll()
                // Auth pages and registration API
                .requestMatchers("/login", "/register").permitAll()
                .requestMatchers("/api/auth/register").permitAll()
                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            // ── Form login (Spring Security processes POST /login) ─────────────
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // ── Logout ────────────────────────────────────────────────────────
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                .permitAll()
            )

            // ── Session management ─────────────────────────────────────────────
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            );

        return http.build();
    }
}
