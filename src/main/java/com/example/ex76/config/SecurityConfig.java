package com.example.ex76.config;

import com.example.ex76.security.handler.CustomLoginSuccessHandler;
import com.example.ex76.security.handler.CustomAccessDeniedHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  private static final String[] PUBLIC_URLS = {
      "/css/**", "/js/**", "/images/**", "/assets/**",
      "/", "/auth/login", "/auth/register", "/auth/accessDenied",
      "/auth/authenticationFailure"
  };

  @Bean
  protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers(PUBLIC_URLS).permitAll()
        // 수업 때 사용한 예전 기능은 코드만 남기고 PLAN QUEST에서는 접근하지 못하게 막는다.
        .requestMatchers("/member/**", "/movie/**", "/reviews/**",
            "/uploadAjax", "/display", "/removeFile").denyAll()
        .anyRequest().authenticated());

    http.formLogin(form -> form
        .loginPage("/auth/login")
        .loginProcessingUrl("/login")
        .failureUrl("/auth/login?error")
        .successHandler(authenticationSuccessHandler())
        .permitAll());

    http.logout(logout -> logout
        .logoutUrl("/logout")
        .deleteCookies("JSESSIONID")
        .invalidateHttpSession(true)
        .clearAuthentication(true)
        .logoutSuccessUrl("/")
        .permitAll());

    http.rememberMe(remember -> remember.tokenValiditySeconds(60 * 60 * 24 * 7));
    http.exceptionHandling(exception ->
        exception.accessDeniedHandler(new CustomAccessDeniedHandler()));
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationSuccessHandler authenticationSuccessHandler() {
    return new CustomLoginSuccessHandler();
  }
}
