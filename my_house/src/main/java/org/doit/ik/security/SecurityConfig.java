package org.doit.ik.security;

import org.doit.ik.security.oauth.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
        .authorizeHttpRequests(auth -> auth
               .requestMatchers(
                   // public pages
                   new AntPathRequestMatcher("/"),
                   new AntPathRequestMatcher("/main"),
                   new AntPathRequestMatcher("/error"),

                   // auth pages
                   new AntPathRequestMatcher("/user/login"),
                   new AntPathRequestMatcher("/user/signup"),
                   new AntPathRequestMatcher("/user/withdraw"),

                   // ✅ 아이디/비번 찾기(정적 화면들) 전부 공개
                   new AntPathRequestMatcher("/user/find-**"),

                   // (선택) 비밀번호찾기도 같은 규칙이면 같이
                   // new AntPathRequestMatcher("/user/pw-**"),

                   // static resources
                   new AntPathRequestMatcher("/css/**"),
                   new AntPathRequestMatcher("/js/**"),
                   new AntPathRequestMatcher("/image/**"),
                   new AntPathRequestMatcher("/images/**"),

                   // oauth endpoints
                   new AntPathRequestMatcher("/oauth2/**"),
                   new AntPathRequestMatcher("/login/**"),

                   // form login processing endpoint
                   new AntPathRequestMatcher("/login"),

                   // dev
                   new AntPathRequestMatcher("/h2-console/**")
               ).permitAll()
               .anyRequest().authenticated()
           )

           .formLogin(form -> form
               .loginPage("/user/login")
               .loginProcessingUrl("/login")
               .usernameParameter("email")
               .passwordParameter("password")
               .failureUrl("/user/login?error")

               // ✅ 로그인 성공 후 메인으로
               .defaultSuccessUrl("/main", false)
               .permitAll()
           )

           .oauth2Login(oauth2 -> oauth2
               .loginPage("/user/login")

               // ✅ 소셜 로그인 성공 후 메인으로
               .defaultSuccessUrl("/main", false)

               .userInfoEndpoint(userInfo -> userInfo
                   .userService(customOAuth2UserService)
               )
           )

           .logout(logout -> logout
               .logoutUrl("/user/logout")
               .logoutSuccessUrl("/main")
               .invalidateHttpSession(true)
               .deleteCookies("JSESSIONID")
           );

        return http.build();
    }
}